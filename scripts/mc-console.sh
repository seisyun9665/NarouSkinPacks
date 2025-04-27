#!/bin/bash

# Minecraftサーバーコンソール接続スクリプト (Minecraft Server Console Connection Script)
# 実行中のマイクラサーバーのscreenセッションに接続します

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

# サーバー基本ディレクトリ
SERVERS_BASE_DIR="/home/opc/servers"

# SSH接続コマンド
SSH_CMD="ssh -i $SSH_KEY $REMOTE_USER@$REMOTE_HOST"

echo -e "${BLUE}=== Minecraftサーバーコンソールに接続 ===${NC}"

# セッション一覧を取得
SESSIONS=$($SSH_CMD "screen -list")
echo -e "${YELLOW}利用可能なセッション:${NC}"
echo "$SESSIONS"

# 利用可能なセッションを抽出（パターンを改善）
SESSION_LIST=$($SSH_CMD "screen -list | grep -o '[0-9]*\.[a-zA-Z0-9]*' | grep -v '^[0-9]*\.$'")

# セッションが存在するか確認
if [ -z "$SESSION_LIST" ]; then
  echo -e "${RED}エラー: 実行中のMinecraftセッションが見つかりません${NC}"
  
  # サーバー起動の確認
  read -p "サーバーを起動しますか？ (y/n): " START_SERVER
  
  if [ "$START_SERVER" = "y" ] || [ "$START_SERVER" = "Y" ]; then
    # 利用可能なサーバーディレクトリを検索
    echo -e "${BLUE}利用可能なMinecraftサーバーを検索しています...${NC}"
    SERVER_DIRS=()
    i=0
    
    while read -r line; do
      SERVER_DIRS[$i]="$line"
      i=$((i+1))
    done < <($SSH_CMD "find $SERVERS_BASE_DIR -maxdepth 1 -type d -not -path '$SERVERS_BASE_DIR' -exec basename {} \;")
    
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
    echo -e "${BLUE}起動するサーバーを選択してください (1-${#SERVER_DIRS[@]}):${NC}"
    read -p "> " SERVER_CHOICE
    
    # 選択の検証
    if ! [[ "$SERVER_CHOICE" =~ ^[0-9]+$ ]] || [ "$SERVER_CHOICE" -lt 1 ] || [ "$SERVER_CHOICE" -gt ${#SERVER_DIRS[@]} ]; then
      echo -e "${RED}エラー: 無効な選択です${NC}"
      exit 1
    fi
    
    # 選択されたサーバー
    SELECTED_SERVER=${SERVER_DIRS[$SERVER_CHOICE-1]}
    
    echo -e "${BLUE}サーバー $SELECTED_SERVER を起動しています...${NC}"
    $SSH_CMD "cd $SERVERS_BASE_DIR/$SELECTED_SERVER && /home/opc/scripts/start-minecraft.sh"
    echo -e "${YELLOW}サーバーの起動を待っています...（10秒）${NC}"
    sleep 10
    
    # 再度セッション一覧を取得
    SESSION_LIST=$($SSH_CMD "screen -list | grep -o '[0-9]*\.[a-zA-Z0-9]*' | grep -v '^[0-9]*\.$'")
    
    if [ -z "$SESSION_LIST" ]; then
      echo -e "${RED}エラー: サーバーの起動に失敗しました${NC}"
      exit 1
    fi
  else
    exit 0
  fi
fi

# セッションを配列に変換
SESSION_NAMES=()
i=0

while read -r line; do
  SESSION_NAMES[$i]="$line"
  i=$((i+1))
done < <(echo "$SESSION_LIST")

# 複数のセッションがある場合は選択を求める
if [ ${#SESSION_NAMES[@]} -gt 1 ]; then
  echo -e "${BLUE}接続するセッションを選択してください:${NC}"
  for i in "${!SESSION_NAMES[@]}"; do
    echo -e "$((i+1)). ${SESSION_NAMES[$i]}"
  done
  
  read -p "> " SESSION_CHOICE
  
  # 選択の検証
  if ! [[ "$SESSION_CHOICE" =~ ^[0-9]+$ ]] || [ "$SESSION_CHOICE" -lt 1 ] || [ "$SESSION_CHOICE" -gt ${#SESSION_NAMES[@]} ]; then
    echo -e "${RED}エラー: 無効な選択です${NC}"
    exit 1
  fi
  
  # 選択されたセッション
  SESSION_NAME="${SESSION_NAMES[$SESSION_CHOICE-1]}"
else
  # セッションが1つしかない場合
  SESSION_NAME="${SESSION_NAMES[0]}"
fi

echo -e "${GREEN}セッション '$SESSION_NAME' に接続します${NC}"
echo -e "${YELLOW}注意: サーバーコンソールから抜けるには Ctrl+A, D を押してください${NC}"
echo ""
echo "よく使うコマンド:"
echo "---------------------------------------------------------------"
echo "help                # ヘルプを表示"
echo "list                # オンラインプレイヤーの一覧表示"
echo "op <プレイヤー名>    # プレイヤーに管理者権限を付与"
echo "reload              # サーバーのリロード"
echo "nsp reload          # NarouSkinPacksプラグインのリロード"
echo "---------------------------------------------------------------"
echo ""

# セッションのステータスを確認
SESSION_STATUS=$($SSH_CMD "screen -list | grep '$SESSION_NAME' | grep -o '(Attached)' || echo '(Detached)'")

if [ "$SESSION_STATUS" = "(Attached)" ]; then
  echo -e "${YELLOW}セッションは既にアタッチされています。デタッチしてから接続しますか？ (y/n)${NC}"
  read -p "> " DETACH_CONFIRM
  
  if [ "$DETACH_CONFIRM" = "y" ] || [ "$DETACH_CONFIRM" = "Y" ]; then
    echo -e "${BLUE}セッションをデタッチしています...${NC}"
    $SSH_CMD "screen -d $SESSION_NAME"
    sleep 1
  else
    echo -e "${YELLOW}接続をキャンセルしました${NC}"
    exit 0
  fi
fi

# サーバーに接続
$SSH_CMD -t "screen -r $SESSION_NAME"

echo -e "${GREEN}コンソール接続を終了しました${NC}" 