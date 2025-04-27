#!/bin/bash

# NarouSkinPacksプラグインの検証スクリプト
# プラグインを検証するためのスクリプト

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

# SSH接続コマンド
SSH_CMD="ssh -i $SSH_KEY $REMOTE_USER@$REMOTE_HOST"

echo -e "${BLUE}=== NarouSkinPacksプラグインの検証 ===${NC}"

# プラグインのJARファイルをチェック
JAR_FILE="build/libs/NarouSkinPacks-1.0.jar"
if [ ! -f "$JAR_FILE" ]; then
  echo -e "${RED}エラー: プラグインJARファイルが見つかりません。${NC}"
  echo -e "${YELLOW}先にビルドを実行してください: ./gradlew build${NC}"
  exit 1
fi

echo -e "${GREEN}ビルド済みプラグインを確認しました: $JAR_FILE${NC}"

# リモートサーバーへのデプロイを確認
echo -e "${YELLOW}リモートサーバーにプラグインをデプロイしますか？ (y/n)${NC}"
read -p "> " DEPLOY

if [ "$DEPLOY" = "y" ] || [ "$DEPLOY" = "Y" ]; then
  echo -e "${BLUE}プラグインをデプロイしています...${NC}"
  ./scripts/deploy-narou.sh
  echo -e "${GREEN}デプロイが完了しました${NC}"
else
  echo -e "${YELLOW}デプロイをスキップします${NC}"
fi

# リモートサーバーのMinecraftサーバーを起動
echo -e "${YELLOW}Minecraftサーバーを起動して検証しますか？ (y/n)${NC}"
read -p "> " START_SERVER

if [ "$START_SERVER" = "y" ] || [ "$START_SERVER" = "Y" ]; then
  echo -e "${BLUE}Minecraftサーバーを起動しています...${NC}"
  $SSH_CMD "cd /home/opc/servers/ros && /home/opc/scripts/start-minecraft.sh spigot-1.12.2.jar 4G"
  
  # サーバー起動を待つ
  echo -e "${YELLOW}サーバーの起動を待っています...（10秒）${NC}"
  sleep 10
  
  # サーバーが正常に起動したか確認
  SESSIONS=$($SSH_CMD "screen -list | grep -o '[0-9]*\.ros'")
  if [ -z "$SESSIONS" ]; then
    echo -e "${RED}エラー: Minecraftサーバーが正常に起動していません${NC}"
    exit 1
  fi
  
  echo -e "${GREEN}Minecraftサーバーが正常に起動しました${NC}"
  
  # サーバーコンソールに接続
  echo -e "${YELLOW}サーバーコンソールに接続しますか？ (y/n)${NC}"
  read -p "> " CONNECT
  
  if [ "$CONNECT" = "y" ] || [ "$CONNECT" = "Y" ]; then
    echo -e "${BLUE}サーバーコンソールに接続しています...${NC}"
    ./scripts/mc-console.sh
  fi
else
  echo -e "${YELLOW}サーバー起動をスキップします${NC}"
fi

echo -e "${GREEN}検証プロセスが完了しました${NC}" 