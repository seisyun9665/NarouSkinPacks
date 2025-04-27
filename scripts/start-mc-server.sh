#!/bin/bash

# Minecraftサーバー起動スクリプト
# リモートサーバーのMinecraftを起動します

# 色付きメッセージ用の設定
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# デフォルト値
DEFAULT_JAR="spigot-1.12.2.jar"
DEFAULT_MEMORY="4G"
REMOTE_USER="opc"
REMOTE_HOST="152.69.192.181"
SSH_KEY="~/.ssh/ssh-key-2021-11-26.key"

# SSH接続コマンド
SSH_CMD="ssh -i $SSH_KEY $REMOTE_USER@$REMOTE_HOST"

# 引数の処理
JAR_FILE=${1:-$DEFAULT_JAR}
MEMORY=${2:-$DEFAULT_MEMORY}

echo -e "${BLUE}=== Minecraftサーバーの起動 ===${NC}"
echo -e "${YELLOW}JARファイル: $JAR_FILE${NC}"
echo -e "${YELLOW}メモリ容量: $MEMORY${NC}"

# サーバーが既に起動しているか確認
SERVER_RUNNING=$($SSH_CMD "ps aux | grep -i 'java.*spigot' | grep -v grep | wc -l")

if [ "$SERVER_RUNNING" -gt 0 ]; then
  echo -e "${RED}エラー: Minecraftサーバーは既に実行中です${NC}"
  
  # セッション一覧の表示
  echo -e "${YELLOW}実行中のセッション:${NC}"
  $SSH_CMD "screen -list"
  
  # 接続の確認
  read -p "既存のセッションに接続しますか？ (y/n): " CONNECT
  
  if [ "$CONNECT" = "y" ] || [ "$CONNECT" = "Y" ]; then
    SESSION_NAME=$($SSH_CMD "screen -list | grep -o '[0-9]*\.ros' | head -1")
    if [ -n "$SESSION_NAME" ]; then
      echo -e "${GREEN}セッション '$SESSION_NAME' に接続します${NC}"
      $SSH_CMD -t "screen -r $SESSION_NAME"
    else
      echo -e "${RED}接続可能なセッションがありません${NC}"
    fi
  fi
  
  exit 0
fi

# サーバー起動
echo -e "${BLUE}サーバーを起動しています...${NC}"
$SSH_CMD "cd /home/opc/servers/ros && /home/opc/scripts/start-minecraft.sh $JAR_FILE $MEMORY"

# 起動確認と接続
sleep 5
SERVER_RUNNING=$($SSH_CMD "ps aux | grep -i 'java.*spigot' | grep -v grep | wc -l")

if [ "$SERVER_RUNNING" -gt 0 ]; then
  echo -e "${GREEN}サーバーが正常に起動しました！${NC}"
  
  # セッション名の取得
  SESSION_NAME=$($SSH_CMD "screen -list | grep -o '[0-9]*\.ros' | head -1")
  
  if [ -n "$SESSION_NAME" ]; then
    echo -e "${GREEN}セッション '$SESSION_NAME' に接続します${NC}"
    echo -e "${YELLOW}注意: サーバーコンソールから抜けるには Ctrl+A, D を押してください${NC}"
    
    # 接続の確認
    read -p "サーバーコンソールに接続しますか？ (y/n): " CONNECT
    
    if [ "$CONNECT" = "y" ] || [ "$CONNECT" = "Y" ]; then
      $SSH_CMD -t "screen -r $SESSION_NAME"
    fi
  else
    echo -e "${YELLOW}警告: サーバーは起動していますが、セッションが見つかりません${NC}"
  fi
else
  echo -e "${RED}エラー: サーバーの起動に失敗したようです${NC}"
fi 