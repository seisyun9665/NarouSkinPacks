#!/bin/bash

# NarouSkinPacks更新スクリプト
# 1. ローカルの成果物をリモートサーバーにコピー
# 2. サーバーにデプロイ

# 設定（環境変数があれば利用、なければデフォルト値）
REMOTE_USER=${REMOTE_USER:-"opc"}
REMOTE_HOST=${REMOTE_HOST:-"152.69.192.181"}
SSH_KEY=${SSH_KEY:-"~/.ssh/ssh-key-2021-11-26.key"}
LOCAL_JAR=${LOCAL_JAR:-"build/libs/NarouSkinPacks-1.0.jar"}
REMOTE_PATH=${REMOTE_PATH:-"/home/opc/servers/ros/plugins/NarouSkinPacks-1.0.jar"}
BACKUP_DIR=${BACKUP_DIR:-"/home/opc/backups/plugins"}

# タイムスタンプ
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

# 色付きメッセージ
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}=== NarouSkinPacks更新処理を開始します ===${NC}"
echo -e "${YELLOW}リモートユーザー: $REMOTE_USER${NC}"
echo -e "${YELLOW}リモートホスト: $REMOTE_HOST${NC}"
echo -e "${YELLOW}SSHキー: $SSH_KEY${NC}"
echo -e "${YELLOW}ローカルJAR: $LOCAL_JAR${NC}"
echo -e "${YELLOW}リモートパス: $REMOTE_PATH${NC}"
echo -e "${YELLOW}バックアップディレクトリ: $BACKUP_DIR${NC}"

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
    SCP_CMD="scp $LOCAL_JAR $REMOTE_USER@$REMOTE_HOST:$REMOTE_PATH"
else
    # 通常環境ではSSHキーを使用
    SSH_CMD="ssh -i $SSH_KEY $REMOTE_USER@$REMOTE_HOST"
    SCP_CMD="scp -i $SSH_KEY $LOCAL_JAR $REMOTE_USER@$REMOTE_HOST:$REMOTE_PATH"
fi

# バックアップディレクトリの作成
echo -e "${YELLOW}リモートプラグインのバックアップを作成します...${NC}"
$SSH_CMD "mkdir -p $BACKUP_DIR && cp $REMOTE_PATH $BACKUP_DIR/NarouSkinPacks-$TIMESTAMP.jar"

# JARファイルのコピー
echo -e "${YELLOW}新しいプラグインをアップロードしています...${NC}"
$SCP_CMD

if [ $? -eq 0 ]; then
    echo -e "${GREEN}プラグインのアップロードが完了しました。${NC}"
else
    echo -e "${RED}プラグインのアップロードに失敗しました。${NC}"
    exit 1
fi

# サーバーステータスのチェック
SERVER_RUNNING=$($SSH_CMD "ps aux | grep -i 'java.*spigot' | grep -v grep | wc -l")

if [ "$SERVER_RUNNING" -gt 0 ]; then
    echo -e "${YELLOW}サーバーは現在実行中です。リロードが必要かもしれません。${NC}"
    echo -e "${YELLOW}サーバーコンソールで '/reload' コマンドを実行してください。${NC}"
else
    echo -e "${YELLOW}サーバーは現在停止しています。${NC}"
    echo -e "${YELLOW}次回サーバー起動時に新しいプラグインが読み込まれます。${NC}"
fi

echo -e "${GREEN}=== NarouSkinPacks更新処理が完了しました ===${NC}" 