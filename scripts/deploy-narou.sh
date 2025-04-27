#!/bin/bash

# NarouSkinPacks更新スクリプト
# 1. ローカルの成果物をリモートサーバーにコピー
# 2. サーバーにデプロイ

# 設定（環境変数があれば利用、なければデフォルト値）
REMOTE_USER=${REMOTE_USER:-"opc"}
REMOTE_HOST=${REMOTE_HOST:-"152.69.192.181"}
SSH_KEY=${SSH_KEY:-"~/.ssh/ssh-key-2021-11-26.key"}
LOCAL_JAR=${LOCAL_JAR:-"build/libs/NarouSkinPacks-1.0.jar"}
SERVERS_BASE_DIR="/home/opc/servers"
BACKUP_DIR=${BACKUP_DIR:-"/home/opc/backups/plugins"}

# タイムスタンプ
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

# 色付きメッセージ
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${GREEN}=== NarouSkinPacks更新処理を開始します ===${NC}"

# ローカルJARファイルの存在チェック
if [ ! -f "$LOCAL_JAR" ]; then
    echo -e "${RED}エラー: ローカルJARファイル($LOCAL_JAR)が見つかりません。${NC}"
    echo "ビルドを実行してください。"
    exit 1
fi

# SSH接続方法の選択（CI環境と通常環境の両方に対応）
if [ -n "$CI" ]; then
    # CI環境ではssh直接実行
    SSH_CMD="ssh $REMOTE_USER@$REMOTE_HOST"
else
    # 通常環境ではSSHキーを使用
    SSH_CMD="ssh -i $SSH_KEY $REMOTE_USER@$REMOTE_HOST"
fi

# 利用可能なサーバーディレクトリを検索
echo -e "${BLUE}利用可能なMinecraftサーバーを検索しています...${NC}"
SERVER_DIRS=()
i=0

# サーバーディレクトリのみを取得 (spigot.jarまたはpaper.jarがあるディレクトリを検索)
while read -r line; do
  SERVER_DIRS[$i]="$line"
  i=$((i+1))
done < <($SSH_CMD "find $SERVERS_BASE_DIR -maxdepth 2 -name 'spigot*.jar' -o -name 'paper*.jar' | grep -v '/scripts/' | grep -v '/backup/' | sort | xargs -I{} dirname {} | xargs -I{} basename {}")

# サーバーが見つからない場合
if [ ${#SERVER_DIRS[@]} -eq 0 ]; then
  echo -e "${RED}エラー: Minecraftサーバーが見つかりませんでした${NC}"
  exit 1
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
  echo -e "${RED}エラー: 無効な選択です${NC}"
  exit 1
fi

# 選択されたサーバー
SELECTED_SERVER=${SERVER_DIRS[$SERVER_CHOICE-1]}
REMOTE_PATH="$SERVERS_BASE_DIR/$SELECTED_SERVER/plugins/NarouSkinPacks-1.0.jar"

echo -e "${YELLOW}選択されたサーバー: $SELECTED_SERVER${NC}"
echo -e "${YELLOW}リモートパス: $REMOTE_PATH${NC}"

# SCP設定を更新
if [ -n "$CI" ]; then
    SCP_CMD="scp $LOCAL_JAR $REMOTE_USER@$REMOTE_HOST:$REMOTE_PATH"
else
    SCP_CMD="scp -i $SSH_KEY $LOCAL_JAR $REMOTE_USER@$REMOTE_HOST:$REMOTE_PATH"
fi

# バックアップディレクトリの作成
echo -e "${YELLOW}リモートプラグインのバックアップを作成します...${NC}"
$SSH_CMD "mkdir -p $BACKUP_DIR && cp $REMOTE_PATH $BACKUP_DIR/NarouSkinPacks-$TIMESTAMP.jar 2>/dev/null || echo '既存のプラグインがないため、バックアップはスキップします'"

# JARファイルのコピー
echo -e "${YELLOW}新しいプラグインをアップロードしています...${NC}"
$SSH_CMD "mkdir -p $SERVERS_BASE_DIR/$SELECTED_SERVER/plugins"
$SCP_CMD

if [ $? -eq 0 ]; then
    echo -e "${GREEN}プラグインのアップロードが完了しました。${NC}"
else
    echo -e "${RED}プラグインのアップロードに失敗しました。${NC}"
    exit 1
fi

# サーバーステータスのチェック
SERVER_RUNNING=$($SSH_CMD "ps aux | grep -i 'java.*spigot.*$SELECTED_SERVER' | grep -v grep | wc -l")

if [ "$SERVER_RUNNING" -gt 0 ]; then
    echo -e "${YELLOW}サーバー $SELECTED_SERVER は現在実行中です。リロードが必要かもしれません。${NC}"
    echo -e "${YELLOW}サーバーコンソールで '/reload' コマンドを実行してください。${NC}"
    
    # リロードするか確認
    read -p "サーバーコンソールに接続しますか？ (y/n): " CONNECT_CONSOLE
    if [ "$CONNECT_CONSOLE" = "y" ] || [ "$CONNECT_CONSOLE" = "Y" ]; then
        SESSION_NAME=$($SSH_CMD "screen -list | grep -o '[0-9]*\.$SELECTED_SERVER' | head -1")
        if [ -n "$SESSION_NAME" ]; then
            echo -e "${GREEN}セッション '$SESSION_NAME' に接続します${NC}"
            echo -e "${YELLOW}注意: サーバーコンソールから抜けるには Ctrl+A, D を押してください${NC}"
            $SSH_CMD -t "screen -r $SESSION_NAME"
        else
            echo -e "${RED}接続可能なセッションがありません${NC}"
        fi
    fi
else
    echo -e "${YELLOW}サーバー $SELECTED_SERVER は現在停止しています。${NC}"
    echo -e "${YELLOW}次回サーバー起動時に新しいプラグインが読み込まれます。${NC}"
fi

echo -e "${GREEN}=== NarouSkinPacks更新処理が完了しました ===${NC}" 