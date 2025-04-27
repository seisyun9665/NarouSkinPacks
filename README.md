# NarouSkinPacks

Minecraftサーバー用プラグインで、プレイヤーが歩くたびにパーティクルエフェクト（スキン）を表示します。プレイヤーごとに異なるスキンを設定できます。

## 機能

- プレイヤーごとに異なるパーティクルエフェクトを設定可能
- 豊富なパーティクル設定オプション（パーティクルの種類、量、速度など）
- コマンドを使用した簡単な設定

## 要件

- Minecraft 1.16+
- Bukkit/Spigot/Paper サーバー

## インストール方法

1. [リリースページ](https://github.com/yourusername/NarouSkinPacks/releases)から最新のJARファイルをダウンロード
2. サーバーの`plugins`フォルダに配置
3. サーバーを再起動またはリロード

## 使用方法

### コマンド

- `/nsp reload` - プラグインの設定をリロードします
- `/nsp setuseskin <player> [<skinname>]` - 指定したプレイヤーにスキンを設定します。skinname を省略すると、スキン設定を解除します

### 権限

- `nsp.nsp` - 基本コマンド実行権限

## 設定

### config.yml

```yaml
# プレイヤーごとの設定されているスキン情報
players:
  playername:
    onStep: "skinname"
```

### skins.yml

```yaml
skins:
  skinname:
    particle: "END_ROD"  # パーティクルの種類（Minecraft のパーティクル名）
    type: "onStep"       # 発動タイプ（現在は onStep のみ対応）
    amount: 20           # パーティクルの量
    x: 0.5               # X方向の広がり
    y: 0.0               # Y方向の広がり
    z: 0.5               # Z方向の広がり
    speed: 0.1           # パーティクルの速度
    forwardOffset: 5.0   # 前方オフセット
```

## 利用可能なスキン

デフォルトで以下のスキンが含まれています:

- `shining` - END_RODパーティクルを使用した輝くエフェクト
- `redstone` - REDSTONEパーティクルを使用した赤いエフェクト

## カスタムスキンの作成

`skins.yml`ファイルに新しいスキン設定を追加することで、カスタムスキンを作成できます。パーティクルの種類はMinecraftの[パーティクル名](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Particle.html)を参照してください。

## ライセンス

このプロジェクトは[MITライセンス](LICENSE)のもとで公開されています。

## 開発者向け情報

このプロジェクトはKotlinで開発されており、Gradleを使用してビルドします。

```bash
# ビルド方法
./gradlew build
```

### プラグインのデプロイ

開発環境からリモートのMinecraftサーバーにプラグインを自動デプロイするためのスクリプトが用意されています。

```bash
# デプロイスクリプトの実行
./deploy-narou.sh
```

デプロイスクリプトは以下の処理を行います：
- ローカルのビルド成果物をリモートサーバーにコピー
- デプロイ前に自動的にバックアップを作成
- サーバー状態の検出と適切なフィードバックの提供

※スクリプトを使用する前に、SSH接続設定と対象サーバーのパスを適切に設定してください。

## 問題報告

バグや機能リクエストは[Issue](https://github.com/yourusername/NarouSkinPacks/issues)でお知らせください。 