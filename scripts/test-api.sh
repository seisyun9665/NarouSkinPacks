#!/bin/bash

# NarouSkinPacks HTTP APIサーバー接続テストスクリプト

# カラー定義
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# リモートサーバー設定
REMOTE_USER="opc"
REMOTE_HOST="152.69.192.181"
SSH_KEY="~/.ssh/ssh-key-2021-11-26.key"

# デフォルト設定値
DEFAULT_PORT=8080
DEFAULT_API_KEY="test-api-key"
DEFAULT_PLAYER="testPlayer"
DEFAULT_COINS=100
DEFAULT_TRANSACTION_ID=$(date +%s)

# SSH接続コマンド
SSH_CMD="ssh -i $SSH_KEY $REMOTE_USER@$REMOTE_HOST"

# パラメータオーバーライド（ホストは常にリモートホストを使用）
PORT=${1:-$DEFAULT_PORT}
API_KEY=${2:-$DEFAULT_API_KEY}
PLAYER=${3:-$DEFAULT_PLAYER}
COINS=${4:-$DEFAULT_COINS}

# 使用方法の表示
usage() {
    echo "使用法: $0 [ポート] [APIキー] [プレイヤー名] [コイン数]"
    echo "例: $0 8080 test-api-key testPlayer 100"
    echo ""
    echo "デフォルト値:"
    echo "  リモートホスト: $REMOTE_HOST (固定)"
    echo "  ポート: $DEFAULT_PORT"
    echo "  APIキー: $DEFAULT_API_KEY (config.ymlのapi.key値に合わせてください)"
    echo "  プレイヤー名: $DEFAULT_PLAYER"
    echo "  コイン数: $DEFAULT_COINS"
}

# ヘルプ表示
if [ "$1" = "-h" ] || [ "$1" = "--help" ]; then
    usage
    exit 0
fi

echo -e "${BLUE}===== NarouSkinPacks HTTP API テスト =====${NC}"
echo -e "${YELLOW}接続先:${NC} http://${REMOTE_HOST}:${PORT}/api/purchase/notify"
echo -e "${YELLOW}APIキー:${NC} ${API_KEY}"
echo -e "${YELLOW}テストデータ:${NC} プレイヤー=${PLAYER}, コイン=${COINS}, 取引ID=${DEFAULT_TRANSACTION_ID}"
echo ""

# JSONペイロード作成
JSON_PAYLOAD="{\"playerName\":\"${PLAYER}\",\"coin\":${COINS},\"transactionId\":\"${DEFAULT_TRANSACTION_ID}\"}"

echo -e "${BLUE}リモートサーバーの状態を確認しています...${NC}"
SERVER_STATUS=$($SSH_CMD "ps aux | grep 'java' | grep -v 'grep' || echo 'サーバー停止中'")

if [[ "$SERVER_STATUS" == *"サーバー停止中"* ]]; then
    echo -e "${RED}Minecraftサーバーが実行されていません${NC}"
    echo -e "${YELLOW}サーバーを起動してから再試行してください:${NC}"
    echo "  ./scripts/mc-console.sh"
    exit 1
fi

echo -e "${GREEN}Minecraftサーバーが実行中です${NC}"
echo -e "${BLUE}リクエスト送信中...${NC}"
echo "curl -X POST -H \"Content-Type: application/json\" -H \"X-API-Key: ${API_KEY}\" -d '${JSON_PAYLOAD}' http://${REMOTE_HOST}:${PORT}/api/purchase/notify"
echo ""

# curlコマンド実行（リモートサーバーにSSH接続してcurlを実行）
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST \
  -H "Content-Type: application/json" \
  -H "X-API-Key: ${API_KEY}" \
  -d "${JSON_PAYLOAD}" \
  http://${REMOTE_HOST}:${PORT}/api/purchase/notify)

# ステータスコードと本文を分離
HTTP_BODY=$(echo "${RESPONSE}" | sed '$ d')
HTTP_STATUS=$(echo "${RESPONSE}" | tail -n1)

# 結果表示
echo -e "${YELLOW}HTTPステータス:${NC} ${HTTP_STATUS}"
echo -e "${YELLOW}レスポンス:${NC} ${HTTP_BODY}"
echo ""

# 成功/失敗の判定
if [ "${HTTP_STATUS}" -eq 200 ]; then
    echo -e "${GREEN}テスト成功！${NC} サーバーからの応答：${HTTP_BODY}"
    echo -e "${YELLOW}注意:${NC} サーバーログとゲーム内メッセージを確認してください。"
    echo -e "${BLUE}サーバーコンソールを確認するには:${NC} ./scripts/mc-console.sh"
else
    echo -e "${RED}テスト失敗${NC} HTTPステータス: ${HTTP_STATUS}"
    echo "考えられる問題:"
    echo "1. ポート番号が間違っている"
    echo "2. APIキーが無効"
    echo "3. JSONフォーマットが正しくない"
    echo "4. プラグインのAPIが有効になっていない"
    echo "5. サーバーのファイアウォールがHTTPポートをブロックしている"
    echo ""
    echo -e "${YELLOW}設定ファイルを確認してください:${NC}"
    echo "  $SSH_CMD \"cat /home/opc/servers/paper/plugins/NarouSkinPacks/config.yml | grep -A5 api:\""
fi 