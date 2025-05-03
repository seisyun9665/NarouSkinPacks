#!/bin/bash

# リモートサーバーの設定ファイルを更新するスクリプト
#
# 使用方法:
# ./scripts/update-config.sh
#
# 説明:
# このスクリプトは、ローカルのsrc/main/resourcesディレクトリ内のYMLファイルを
# リモートサーバーのNarouSkinPacksプラグインディレクトリにコピーします。

# 変数設定
REMOTE_USER="opc"
REMOTE_HOST="152.69.192.181"
REMOTE_KEY="$HOME/.ssh/ssh-key-2021-11-26.key"
REMOTE_PLUGIN_DIR="/home/opc/servers/narouTest/plugins/NarouSkinPacks"
LOCAL_CONFIG_DIR="src/main/resources"

# カラー表示用
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 実行前の確認
echo -e "${YELLOW}以下のYMLファイルをリモートサーバーに転送します:${NC}"
for config_file in "$LOCAL_CONFIG_DIR"/*.yml; do
  filename=$(basename "$config_file")
  echo "- $filename"
done

read -p "続行しますか？ (y/n): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
  echo -e "${RED}操作をキャンセルしました${NC}"
  exit 1
fi

# リモートディレクトリの存在確認
echo -e "${YELLOW}リモートディレクトリを確認しています...${NC}"
ssh -i "$REMOTE_KEY" "$REMOTE_USER@$REMOTE_HOST" "[ -d $REMOTE_PLUGIN_DIR ] || mkdir -p $REMOTE_PLUGIN_DIR"

if [ $? -ne 0 ]; then
  echo -e "${RED}リモートディレクトリの確認/作成に失敗しました${NC}"
  exit 1
fi

# 設定ファイルのバックアップ
echo -e "${YELLOW}リモートサーバーで設定ファイルのバックアップを作成しています...${NC}"
TIMESTAMP=$(date +"%Y%m%d%H%M%S")
ssh -i "$REMOTE_KEY" "$REMOTE_USER@$REMOTE_HOST" "mkdir -p $REMOTE_PLUGIN_DIR/backups/$TIMESTAMP && cp $REMOTE_PLUGIN_DIR/*.yml $REMOTE_PLUGIN_DIR/backups/$TIMESTAMP/ 2>/dev/null || true"

# YMLファイルの転送
echo -e "${YELLOW}設定ファイルを転送しています...${NC}"
for config_file in "$LOCAL_CONFIG_DIR"/*.yml; do
  filename=$(basename "$config_file")
  echo "転送中: $filename"
  scp -i "$REMOTE_KEY" "$config_file" "$REMOTE_USER@$REMOTE_HOST:$REMOTE_PLUGIN_DIR/"
  
  if [ $? -eq 0 ]; then
    echo -e "${GREEN}$filename の転送に成功しました${NC}"
  else
    echo -e "${RED}$filename の転送に失敗しました${NC}"
    exit 1
  fi
done

echo -e "${GREEN}すべての設定ファイルの転送が完了しました${NC}"
echo -e "${YELLOW}バックアップは $REMOTE_PLUGIN_DIR/backups/$TIMESTAMP/ に保存されています${NC}"

# オプション: サーバーにリロードコマンドを送信
echo -e "${YELLOW}サーバーに設定のリロードを要求しますか？ (y/n): ${NC}"
read -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
  echo -e "${YELLOW}サーバーに /nsp reload コマンドを送信しています...${NC}"
  ssh -i "$REMOTE_KEY" "$REMOTE_USER@$REMOTE_HOST" "cd /home/opc/servers/narouTest && echo 'nsp reload' > ./cache/console_input.txt"
  
  if [ $? -eq 0 ]; then
    echo -e "${GREEN}リロードコマンドの送信に成功しました${NC}"
  else
    echo -e "${RED}リロードコマンドの送信に失敗しました${NC}"
  fi
fi

echo -e "${GREEN}処理が完了しました${NC}"
exit 0 