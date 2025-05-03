#!/bin/bash

# NarouSkinPacksプラグインのデプロイと検証スクリプト
# Deploy and verify NarouSkinPacks plugin

# 設定（環境変数があれば利用、なければデフォルト値）
REMOTE_USER=${REMOTE_USER:-"opc"}
REMOTE_HOST=${REMOTE_HOST:-"152.69.192.181"}
SSH_KEY=${SSH_KEY:-"~/.ssh/ssh-key-2021-11-26.key"}
LOCAL_JAR=${LOCAL_JAR:-"build/libs/NarouSkinPacks.jar"}
SERVERS_BASE_DIR="/home/opc/servers"
BACKUP_DIR=${BACKUP_DIR:-"/home/opc/backups/plugins"}

# タイムスタンプ
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

# 色付きメッセージ用の設定
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# SSH接続方法の選択（CI環境と通常環境の両方に対応）
if [ -n "$CI" ]; then
    # CI環境ではssh直接実行
    SSH_CMD="ssh $REMOTE_USER@$REMOTE_HOST"
    SCP_CMD="scp"
else
    # 通常環境ではSSHキーを使用
    SSH_CMD="ssh -i $SSH_KEY $REMOTE_USER@$REMOTE_HOST"
    SCP_CMD="scp -i $SSH_KEY"
fi

# ユーティリティ関数
function step() {
  echo -e "${BLUE}=== $1 ===${NC}"
}

function error() {
  echo -e "${RED}エラー: $1${NC}"
  exit 1
}

function success() {
  echo -e "${GREEN}$1${NC}"
}

function warning() {
  echo -e "${YELLOW}$1${NC}"
}

step "NarouSkinPacks更新処理を開始します"

# 1. プラグインのビルド
step "プラグインをビルドしています"
./gradlew build

if [ $? -ne 0 ]; then
  error "ビルドに失敗しました"
fi

# JARファイルの存在確認
if [ ! -f "$LOCAL_JAR" ]; then
  error "ビルド成果物($LOCAL_JAR)が見つかりません"
fi

success "ビルドが完了しました"

# プラグインのデプロイ確認
read -p "ビルドしたプラグインをリモートサーバーにデプロイしますか？ (y/n): " DEPLOY_CONFIRM

if [ "$DEPLOY_CONFIRM" = "y" ] || [ "$DEPLOY_CONFIRM" = "Y" ]; then
  # 利用可能なサーバーディレクトリを検索
  step "利用可能なMinecraftサーバーを検索しています"
  SERVER_DIRS=()
  i=0

  # サーバーディレクトリのみを取得 (spigot.jarまたはpaper.jarがあるディレクトリを検索)
  while read -r line; do
    SERVER_DIRS[$i]="$line"
    i=$((i+1))
  done < <($SSH_CMD "find $SERVERS_BASE_DIR -maxdepth 2 -name 'spigot*.jar' -o -name 'paper*.jar' | grep -v '/scripts/' | grep -v '/backup/' | sort | xargs -I{} dirname {} | xargs -I{} basename {}")

  # サーバーが見つからない場合
  if [ ${#SERVER_DIRS[@]} -eq 0 ]; then
    error "Minecraftサーバーが見つかりませんでした"
  fi

  # サーバーリストを表示
  echo -e "${YELLOW}利用可能なサーバー:${NC}"
  for i in "${!SERVER_DIRS[@]}"; do
    echo -e "$((i+1)). ${SERVER_DIRS[$i]}"
  done

  # サーバー選択
  echo -e "${BLUE}デプロイ先サーバーを選択してください (1-${#SERVER_DIRS[@]}):${NC}"
  read -p "> " SERVER_CHOICE

  # 選択の検証
  if ! [[ "$SERVER_CHOICE" =~ ^[0-9]+$ ]] || [ "$SERVER_CHOICE" -lt 1 ] || [ "$SERVER_CHOICE" -gt ${#SERVER_DIRS[@]} ]; then
    error "無効な選択です"
  fi

  # 選択されたサーバー
  SELECTED_SERVER=${SERVER_DIRS[$SERVER_CHOICE-1]}
  REMOTE_SERVER_DIR="$SERVERS_BASE_DIR/$SELECTED_SERVER"
  REMOTE_PLUGINS_DIR="$REMOTE_SERVER_DIR/plugins"
  REMOTE_PATH="$REMOTE_PLUGINS_DIR/NarouSkinPacks.jar"

  echo -e "${YELLOW}選択されたサーバー: $SELECTED_SERVER${NC}"
  echo -e "${YELLOW}リモートパス: $REMOTE_PATH${NC}"
  
  # 既存のプラグインの確認とバックアップ
  step "既存のプラグインを確認しています"
  PLUGIN_EXISTS=$($SSH_CMD "[ -f $REMOTE_PATH ] && echo 'true' || echo 'false'")

  if [ "$PLUGIN_EXISTS" = "true" ]; then
    step "既存のプラグインをバックアップしています"
    $SSH_CMD "mkdir -p $BACKUP_DIR && cp $REMOTE_PATH $BACKUP_DIR/NarouSkinPacks-$TIMESTAMP.jar"
    if [ $? -eq 0 ]; then
      success "プラグインのバックアップが完了しました"
    else
      warning "プラグインのバックアップに失敗しました"
    fi
  else
    warning "既存のプラグインが見つかりません。新規インストールを行います。"
  fi
  
  # JARファイルのアップロード
  step "プラグインをアップロードしています"
  $SSH_CMD "mkdir -p $REMOTE_PLUGINS_DIR"
  $SCP_CMD "$LOCAL_JAR" "$REMOTE_USER@$REMOTE_HOST:$REMOTE_PATH"
  
  if [ $? -ne 0 ]; then
    error "プラグインのアップロードに失敗しました"
  fi
  
  success "プラグインのデプロイが完了しました"
else
  warning "デプロイをスキップしました"
  exit 0
fi

# サーバー起動確認
step "Minecraftサーバーの状態を確認しています"
# サーバー実行状態を確認する方法を改善
SERVER_RUNNING=$($SSH_CMD "cd $REMOTE_SERVER_DIR && (screen -list | grep -q \"\.$SELECTED_SERVER\" && echo \"1\" || echo \"0\")")

if [ "$SERVER_RUNNING" -eq 0 ]; then
  SERVER_RUNNING=$($SSH_CMD "ps aux | grep -v grep | grep -i \"java.*server.*$SELECTED_SERVER\" | wc -l")
fi

if [ "$SERVER_RUNNING" -eq 0 ]; then
  warning "Minecraftサーバーが実行されていません"
  read -p "サーバーを起動しますか? (y/n): " START_SERVER
  
  if [ "$START_SERVER" = "y" ] || [ "$START_SERVER" = "Y" ]; then
    echo -e "${BLUE}サーバーを起動しています...${NC}"
    $SSH_CMD "cd $REMOTE_SERVER_DIR && /home/opc/scripts/start-minecraft.sh"
    echo -e "${YELLOW}サーバーの起動を待っています...（15秒）${NC}"
    sleep 15
  else
    warning "サーバー起動をスキップしました。プラグインの検証はできません。"
    exit 0
  fi
fi

success "Minecraftサーバーが実行中です"

# プラグインのテスト手順の表示
step "プラグインのテスト手順"
echo -e "${YELLOW}1. サーバーにログインする${NC}"
echo -e "${YELLOW}2. 以下のコマンドを実行して機能を確認:${NC}"
echo "   - /nsp reload - プラグインの再読み込み"
echo "   - /nsp setuseskin <プレイヤー名> <スキン名> - プレイヤーのスキンを設定"
echo "   - /nsp coins check <プレイヤー名> - プレイヤーのコイン残高を確認"
echo "   - HTTP APIテスト用に以下のコマンドを実行:"
echo "     curl -X POST -H \"Content-Type: application/json\" -H \"X-API-Key: YOUR_API_KEY\" \\"
echo "       -d '{\"playerName\":\"seisyun\",\"coin\":100,\"transactionId\":\"$(date +%s)\"}' \\"
echo "       http://localhost:8080/api/purchase/notify"
echo ""

# Minecraftコンソールへの接続
read -p "サーバーコンソールに接続しますか? (y/n): " CONNECT_CONSOLE

if [ "$CONNECT_CONSOLE" = "y" ] || [ "$CONNECT_CONSOLE" = "Y" ]; then
  step "サーバーコンソールに接続しています"
  echo -e "${YELLOW}コンソールから抜けるには Ctrl+A, D を押してください${NC}"
  
  # 実行中のセッション名を取得
  SESSION_NAME=$($SSH_CMD "screen -list | grep -o '[0-9]*\.$SELECTED_SERVER' | head -1")
  
  if [ -z "$SESSION_NAME" ]; then
    warning "セッション名が見つかりません。すべてのセッションを確認します..."
    SESSION_NAME=$($SSH_CMD "screen -list | grep -o '[0-9]*\.[a-zA-Z0-9]*' | grep -v '^[0-9]*\.$' | head -1")
    
    if [ -z "$SESSION_NAME" ]; then
      error "Minecraftセッションが見つかりません"
    fi
  fi
  
  # コンソールに接続
  $SSH_CMD -t "screen -r $SESSION_NAME"
  
  success "コンソール接続を終了しました"
else
  warning "コンソール接続をスキップしました"
fi

success "デプロイと検証プロセスが完了しました" 