#!/bin/bash

# NarouSkinPacks更新スクリプト
# 1. ローカルの成果物をリモートサーバーにコピー
# 2. サーバーにデプロイ

# 設定
REMOTE_USER="opc"
REMOTE_HOST="152.69.192.181"
SSH_KEY="~/.ssh/ssh-key-2021-11-26.key"
LOCAL_JAR="build/libs/NarouSkinPacks-1.0.jar"
REMOTE_PATH="/home/opc/servers/ros/plugins/NarouSkinPacks-1.0.jar"
BACKUP_DIR="/home/opc/backups/plugins"

# タイムスタンプ
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

# 色付きメッセージ
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}=== NarouSkinPacks更新処理を開始します ===${NC}"

# ローカルJARファイルの存在チェック
if [ ! -f "$LOCAL_JAR" ]; then
    echo -e "${RED}エラー: ローカルJARファイル($LOCAL_JAR)が見つかりません。${NC}"
    echo "ビルドを実行してください。"
    exit 1
fi

# バックアップディレクトリの作成
echo -e "${YELLOW}リモートプラグインのバックアップを作成します...${NC}"
ssh -i "$SSH_KEY" "$REMOTE_USER@$REMOTE_HOST" "mkdir -p $BACKUP_DIR && cp $REMOTE_PATH $BACKUP_DIR/NarouSkinPacks-$TIMESTAMP.jar"

# JARファイルのコピー
echo -e "${YELLOW}新しいプラグインをアップロードしています...${NC}"
scp -i "$SSH_KEY" "$LOCAL_JAR" "$REMOTE_USER@$REMOTE_HOST:$REMOTE_PATH"

if [ $? -eq 0 ]; then
    echo -e "${GREEN}プラグインのアップロードが完了しました。${NC}"
else
    echo -e "${RED}プラグインのアップロードに失敗しました。${NC}"
    exit 1
fi

# サーバーステータスのチェック
SERVER_RUNNING=$(ssh -i "$SSH_KEY" "$REMOTE_USER@$REMOTE_HOST" "ps aux | grep -i 'java.*spigot' | grep -v grep | wc -l")

if [ "$SERVER_RUNNING" -gt 0 ]; then
    echo -e "${YELLOW}サーバーは現在実行中です。リロードが必要かもしれません。${NC}"
    echo -e "${YELLOW}サーバーコンソールで '/reload' コマンドを実行してください。${NC}"
else
    echo -e "${YELLOW}サーバーは現在停止しています。${NC}"
    echo -e "${YELLOW}次回サーバー起動時に新しいプラグインが読み込まれます。${NC}"
fi

echo -e "${GREEN}=== NarouSkinPacks更新処理が完了しました ===${NC}" 