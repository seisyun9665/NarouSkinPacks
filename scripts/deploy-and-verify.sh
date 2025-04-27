#!/bin/bash

# NarouSkinPacksプラグインのデプロイと検証スクリプト
# Deploy and verify NarouSkinPacks plugin

# 色付きメッセージ用の設定
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# SSH接続設定
REMOTE_USER="opc"
REMOTE_HOST="152.69.192.181"
SSH_KEY="~/.ssh/ssh-key-2021-11-26.key"

# エラー表示関数
function error() {
  echo -e "${RED}エラー: $1${NC}"
  exit 1
}

# 成功表示関数
function success() {
  echo -e "${GREEN}$1${NC}"
}

# 警告表示関数
function warning() {
  echo -e "${YELLOW}$1${NC}"
}

# 1. プラグインのビルド
step "プラグインをビルドしています"
./gradlew build

if [ $? -ne 0 ]; then
  error "ビルドに失敗しました"
fi

# JARファイルの存在確認
if [ ! -f "build/libs/NarouSkinPacks-1.0.jar" ]; then
  error "ビルド成果物が見つかりません"
fi

success "ビルドが完了しました"

if [ "$DEPLOY_CONFIRM" = "y" ] || [ "$DEPLOY_CONFIRM" = "Y" ]; then
  # バックアップ作成
  echo -e "${BLUE}既存のプラグインをバックアップしています...${NC}"
  TIMESTAMP=$(date +"%Y%m%d%H%M%S")
  $SSH_CMD "mkdir -p $REMOTE_PLUGINS_DIR/backups && \
    if [ -f $REMOTE_PLUGINS_DIR/NarouSkinPacks.jar ]; then \
      cp $REMOTE_PLUGINS_DIR/NarouSkinPacks.jar $REMOTE_PLUGINS_DIR/backups/NarouSkinPacks-$TIMESTAMP.jar; \
    fi"
  
  # JARファイルのアップロード
  echo -e "${BLUE}プラグインをアップロードしています...${NC}"
  scp -i "$SSH_KEY" "$PLUGIN_JAR_PATH" "$REMOTE_USER@$REMOTE_HOST:$REMOTE_PLUGINS_DIR/NarouSkinPacks.jar"
  
  if [ $? -ne 0 ]; then
    echo -e "${RED}エラー: プラグインのアップロードに失敗しました${NC}"
    exit 1
  fi
  
  echo -e "${GREEN}プラグインのデプロイが完了しました${NC}"
else
  echo -e "${YELLOW}デプロイをスキップしました${NC}"
fi

# サーバー起動確認
echo -e "${BLUE}Minecraftサーバーの状態を確認しています...${NC}"
SERVER_RUNNING=$($SSH_CMD "screen -list | grep -c minecraft")

if [ "$SERVER_RUNNING" -eq 0 ]; then
  echo -e "${YELLOW}Minecraftサーバーが実行されていません${NC}"
  read -p "サーバーを起動しますか? (y/n): " START_SERVER
  
  if [ "$START_SERVER" = "y" ] || [ "$START_SERVER" = "Y" ]; then
    echo -e "${BLUE}サーバーを起動しています...${NC}"
    $SSH_CMD "cd $REMOTE_SERVER_DIR && /home/opc/scripts/start-minecraft.sh"
    echo -e "${YELLOW}サーバーの起動を待っています...（15秒）${NC}"
    sleep 15
  else
    echo -e "${YELLOW}サーバー起動をスキップしました。プラグインの検証はできません。${NC}"
    exit 0
  fi
fi

echo -e "${GREEN}Minecraftサーバーが実行中です${NC}"

# プラグインのテスト手順の表示
echo -e "${BLUE}=== プラグインのテスト手順 ===${NC}"
echo -e "${YELLOW}1. サーバーにログインする${NC}"
echo -e "${YELLOW}2. 以下のコマンドを実行して機能を確認:${NC}"
echo "   - /nsp reload - プラグインの再読み込み"
echo "   - /nsp setuseskin <プレイヤー名> <スキン名> - プレイヤーのスキンを設定"
echo "   - /nsp coins check <プレイヤー名> - プレイヤーのコイン残高を確認"
echo ""

# Minecraftコンソールへの接続
read -p "サーバーコンソールに接続しますか? (y/n): " CONNECT_CONSOLE

if [ "$CONNECT_CONSOLE" = "y" ] || [ "$CONNECT_CONSOLE" = "Y" ]; then
  echo -e "${BLUE}サーバーコンソールに接続しています...${NC}"
  echo -e "${YELLOW}コンソールから抜けるには Ctrl+A, D を押してください${NC}"
  
  # 実行中のセッション名を取得
  SESSION_NAME=$($SSH_CMD "screen -list | grep minecraft | grep -o '[0-9]*\.minecraft'")
  
  if [ -z "$SESSION_NAME" ]; then
    echo -e "${RED}エラー: Minecraftセッションが見つかりません${NC}"
    exit 1
  fi
  
  # コンソールに接続
  $SSH_CMD -t "screen -r $SESSION_NAME"
  
  echo -e "${GREEN}コンソール接続を終了しました${NC}"
else
  echo -e "${YELLOW}コンソール接続をスキップしました${NC}"
fi

echo -e "${GREEN}デプロイと検証プロセスが完了しました${NC}" 