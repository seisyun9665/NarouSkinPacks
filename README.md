# NarouSkinPacks

Minecraft サーバー用プラグインで、プレイヤーが歩くたびにパーティクルエフェクト（スキン）を表示します。プレイヤーごとに異なるスキンを設定できます。

## 機能

- プレイヤーごとに異なるパーティクルエフェクトを設定可能
- 豊富なパーティクル設定オプション（パーティクルの種類、量、速度など）
- コマンドを使用した簡単な設定
- コイン管理システム
- HTTP ベースの API 機能（オプション）

## 要件

- Minecraft 1.16+
- Bukkit/Spigot/Paper サーバー

## インストール方法

1. [リリースページ](https://github.com/yourusername/NarouSkinPacks/releases)から最新の JAR ファイルをダウンロード
2. サーバーの`plugins`フォルダに配置
3. サーバーを再起動またはリロード

## 使用方法

### コマンド

- `/nsp reload` - プラグインの設定をリロードします
- `/nsp setuseskin <player> [<skinname>]` - 指定したプレイヤーにスキンを設定します。skinname を省略すると、スキン設定を解除します
- `/nsp coins check <player>` - プレイヤーのコイン残高を確認します
- `/nsp coins add <player> <amount>` - プレイヤーにコインを追加します
- `/nsp coins remove <player> <amount>` - プレイヤーからコインを削除します
- `/nsp coins set <player> <amount>` - プレイヤーのコイン残高を設定します

### 権限

- `nsp.nsp` - 基本コマンド実行権限
- `nsp.admin` - 管理者向けコマンド実行権限

## 設定

### config.yml

```yaml
# プレイヤーごとの設定されているスキン情報
players:
  playername:
    currentSkin: "skinname"

# API設定
api:
  enabled: false
  port: 8080
  apiKey: "your-secret-api-key"

# コイン設定
coins:
  enabled: true
```

### skins.yml

```yaml
skins:
  skinname:
    particle: "END_ROD" # パーティクルの種類（Minecraft のパーティクル名）
    type: "currentSkin" # 発動タイプ（現在は currentSkin のみ対応）
    amount: 20 # パーティクルの量
    x: 0.5 # X方向の広がり
    y: 0.0 # Y方向の広がり
    z: 0.5 # Z方向の広がり
    speed: 0.1 # パーティクルの速度
    forwardOffset: 5.0 # 前方オフセット
```

## 利用可能なスキン

デフォルトで以下のスキンが含まれています:

- `shining` - END_ROD パーティクルを使用した輝くエフェクト
- `redstone` - REDSTONE パーティクルを使用した赤いエフェクト
- `magic` - SPELL_WITCH パーティクルを使用した魔法のエフェクト
- `heart` - HEART パーティクルを使用したハートエフェクト

## カスタムスキンの作成

`skins.yml`ファイルに新しいスキン設定を追加することで、カスタムスキンを作成できます。パーティクルの種類は Minecraft の[パーティクル名](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Particle.html)を参照してください。

## ライセンス

このプロジェクトは[MIT ライセンス](LICENSE)のもとで公開されています。

## 開発者向け情報

このプロジェクトは Kotlin で開発されており、Gradle を使用してビルドします。

```bash
# ビルド方法
./gradlew clean shadowJar
```

### 開発スクリプト

本プロジェクトには開発、デプロイ、テストを効率化するための様々なスクリプトが含まれています。詳細は[scripts/README.md](scripts/README.md)を参照してください。

**主なスクリプト：**

- `scripts/deploy-and-verify.sh` - ビルド、デプロイ、検証を一括で行う
- `scripts/deploy-narou.sh` - ビルドしたプラグインをリモートサーバーにデプロイ
- `scripts/mc-console.sh` - リモートの Minecraft サーバーコンソールに接続
- `scripts/start-mc-server.sh` - リモートサーバーを起動
- `scripts/verify-plugin.sh` - プラグインの動作検証を支援

### プラグインのデプロイ

開発環境からリモートの Minecraft サーバーにプラグインを自動デプロイするためのスクリプトが用意されています。

```bash
# デプロイスクリプトの実行
./scripts/deploy-narou.sh
```

デプロイスクリプトは以下の処理を行います：

- ローカルのビルド成果物をリモートサーバーにコピー
- デプロイ前に自動的にバックアップを作成
- サーバー状態の検出と適切なフィードバックの提供

※スクリプトを使用する前に、SSH 接続設定と対象サーバーのパスを適切に設定してください。

## リモート接続方法

リモート Minecraft サーバーに接続・デプロイするためのガイドラインです。

### SSH 接続設定

1. SSH 鍵の設定

   ```bash
   # SSH鍵の生成（未作成の場合）
   ssh-keygen -t rsa -b 4096 -C "your_email@example.com"

   # SSH鍵をリモートサーバーに登録
   ssh-copy-id -i ~/.ssh/id_rsa.pub username@remote_host
   ```

2. SSH 設定ファイルの作成（~/.ssh/config）
   ```
   Host minecraft-server
     HostName xxx.xxx.xxx.xxx
     User username
     Port 22
     IdentityFile ~/.ssh/id_rsa
   ```

### デプロイスクリプトの設定

`deploy-narou.sh`スクリプトを使用する前に、以下の変数を環境に合わせて修正してください：

```bash
# 設定
REMOTE_USER="username"      # リモートサーバーのユーザー名
REMOTE_HOST="xxx.xxx.xxx.xxx"  # リモートサーバーのIPアドレス
SSH_KEY="~/.ssh/id_rsa"     # SSH鍵のパス
LOCAL_JAR="build/libs/NarouSkinPacks-1.0.jar"  # ローカルのJARパス
REMOTE_PATH="/path/to/server/plugins/NarouSkinPacks-1.0.jar"  # リモート配置先
BACKUP_DIR="/path/to/backups/plugins"  # バックアップディレクトリ
```

### 簡単アクセス用スクリプト

本プロジェクトでは、開発効率を向上させるための以下のスクリプトを提供しています：

```bash
# リモートサーバーへの接続とプラグイン検証を一括で行う
./scripts/deploy-and-verify.sh

# サーバーコンソールに直接接続
./scripts/mc-console.sh

# リモートサーバーの起動
./scripts/start-mc-server.sh [JARファイル名] [メモリ量]

# プラグインの動作検証のみを行う
./scripts/verify-plugin.sh
```

### CI/CD 自動化

本プロジェクトは GitHub Actions による自動ビルド・デプロイに対応しています。設定には以下のシークレットが必要です：

- `SSH_PRIVATE_KEY`: SSH プライベートキー
- `KNOWN_HOSTS`: サーバーの known_hosts 情報
- `REMOTE_USER`: リモートユーザー名
- `REMOTE_HOST`: リモートホスト名
- `REMOTE_PATH`: リモートプラグインのパス
- `BACKUP_DIR`: バックアップディレクトリのパス

コミットを push するか GitHub 上で手動トリガーすることで、自動的にビルドとデプロイが実行されます。

### サーバー管理コマンド

リモートサーバーに接続後、以下のコマンドで Minecraft サーバーを管理できます：

```bash
# サーバーの起動
./start-minecraft.sh

# 実行中のサーバーセッション一覧
screen -list

# サーバーコンソールに接続
screen -r <セッション名>

# サーバーコンソールから切断（サーバーは実行したまま）
# Ctrl+A, D キーを順に押す
```

## プロジェクト構造

プロジェクトの主要な構造と重要なファイルは以下の通りです：

```
NarouSkinPacks/
├── src/main/
│   ├── kotlin/org/com/syun0521/minecraft/narouskinpacks/
│   │   ├── NarouSkinPacks.kt       # メインプラグインクラス
│   │   ├── Command.kt              # コマンド処理
│   │   ├── CustomConfig.kt         # 設定管理
│   │   ├── NSPCommandTabCompleter.kt # タブ補完
│   │   ├── api/                    # HTTP API関連
│   │   ├── coin/                   # コイン管理システム
│   │   ├── events/                 # イベント処理
│   │   └── skin/                   # スキン関連
│   └── resources/
│       ├── config.yml              # 基本設定
│       ├── skins.yml               # スキン定義
│       └── plugin.yml              # プラグイン定義
├── scripts/                        # 開発・デプロイスクリプト
└── .cursor/rules/                  # Cursor AIの理解を助けるルール
```

## 開発方針

- コードは Kotlin のベストプラクティスに従って記述する
- 機能ごとに適切なパッケージに分離する
- テスト容易性を考慮した設計にする
- スクリプトを活用して開発効率を向上させる

## 問題報告

バグや機能リクエストは[Issue](https://github.com/yourusername/NarouSkinPacks/issues)でお知らせください。
