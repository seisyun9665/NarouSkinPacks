package org.com.syun0521.minecraft.narouskinpacks.api

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.embeddedServer
import io.ktor.server.engine.sslConnector
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receiveText
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import org.bukkit.Bukkit
import org.com.syun0521.minecraft.narouskinpacks.NarouSkinPacks
import org.com.syun0521.minecraft.narouskinpacks.coin.CoinManager
import java.io.File
import java.security.KeyStore

class KtorApiServer(
    private val plugin: NarouSkinPacks,
    private val coinManager: CoinManager,
    private val port: Int,
    private val apiKey: String,
) {
    private var server: ApplicationEngine? = null

    fun start() {
        plugin.logger.info("===== KtorApiServerの初期化を開始します =====")
        plugin.logger.info("ポート: $port, APIキー: $apiKey")

        try {
            // キーストアのロード
            val keyStoreFile = File(plugin.dataFolder, "keystore.p12")
            val keyStorePassword = "password".toCharArray()
            val keyAlias = "myalias"
            val keyStore =
                KeyStore.getInstance("PKCS12").apply {
                    keyStoreFile.inputStream().use { load(it, keyStorePassword) }
                }

            server =
                embeddedServer(
                    Netty,
                    environment =
                    applicationEngineEnvironment {
                        // SSLコネクタの設定
                        sslConnector(
                            keyStore = keyStore,
                            keyAlias = keyAlias,
                            keyStorePassword = { keyStorePassword },
                            privateKeyPassword = { keyStorePassword },
                        ) {
                            port = this@KtorApiServer.port
                            host = "0.0.0.0"
                        }

                        // モジュール設定
                        module {
                            plugin.logger.info("HTTPSサーバーの設定を開始します")

                            install(ContentNegotiation) {
                                json(Json { prettyPrint = true })
                            }

                            plugin.logger.info(
                                "ContentNegotiationをインストールしました",
                            )

                            routing {
                                plugin.logger.info("ルーティングを設定中...")

                                // サーバー状態確認用のシンプルなGETエンドポイント
                                get("/") {
                                    call.respondText("Hello World")
                                    println("Connection OK")
                                }

                                post("/api/purchase/notify") {
                                    plugin.logger.info(
                                        "APIエンドポイントにリクエストを受信しました: /api/purchase/notify",
                                    )

                                    // APIキー認証
                                    plugin.logger.info("APIキー認証を実行中...")
                                    val requestApiKey =
                                        call.request.headers[
                                            "X-API-Key",
                                        ]
                                    plugin.logger.info(
                                        "リクエストAPIキー: $requestApiKey, 設定APIキー: $apiKey",
                                    )

                                    if (apiKey != requestApiKey) {
                                        plugin.logger.warning(
                                            "APIキー認証に失敗しました",
                                        )
                                        call.respondText(
                                            """{"success":false,"message":"Unauthorized"}""",
                                            status =
                                            HttpStatusCode
                                                .Unauthorized,
                                        )
                                        return@post
                                    }
                                    plugin.logger.info("APIキー認証に成功しました")

                                    try {
                                        // JSONリクエストの解析
                                        plugin.logger.info(
                                            "JSONリクエストの解析を開始します",
                                        )
                                        val jsonText = call.receiveText()
                                        plugin.logger.info(
                                            "受信したJSONテキスト: $jsonText",
                                        )

                                        // シンプルな方法でJSONを解析
                                        val regex =
                                            """playerName"\s*:\s*"([^"]+)"|"coin"\s*:\s*(\d+)|"transactionId"\s*:\s*"([^"]+)""".toRegex()

                                        val matches =
                                            regex.findAll(jsonText)

                                        var playerName: String? = null
                                        var coin: Int? = null
                                        var transactionId: String? = null

                                        matches.forEach { matchResult ->
                                            val value =
                                                matchResult.groupValues[
                                                    1,
                                                ]
                                            val numValue =
                                                matchResult.groupValues[
                                                    2,
                                                ]
                                            val idValue =
                                                matchResult.groupValues[
                                                    3,
                                                ]

                                            if (value.isNotEmpty()) {
                                                playerName = value
                                            } else if (numValue.isNotEmpty()
                                            ) {
                                                coin =
                                                    numValue.toIntOrNull()
                                            } else if (idValue.isNotEmpty()
                                            ) {
                                                transactionId = idValue
                                            }
                                        }

                                        plugin.logger.info(
                                            "解析結果 - プレイヤー名: $playerName, コイン: $coin, 取引ID: $transactionId",
                                        )

                                        // 必須パラメータの検証
                                        plugin.logger.info("必須パラメータの検証中...")
                                        if (playerName == null ||
                                            coin == null ||
                                            transactionId ==
                                            null
                                        ) {
                                            plugin.logger.warning(
                                                "必須パラメータが不足しています: player=$playerName, coin=$coin, id=$transactionId",
                                            )
                                            call.respondText(
                                                """{"success":false,"message":"Bad Request: Missing required parameters"}""",
                                                status =
                                                HttpStatusCode
                                                    .BadRequest,
                                            )
                                            return@post
                                        }
                                        plugin.logger.info(
                                            "必須パラメータの検証に成功しました",
                                        )

                                        // null検証後、変数を明示的に非Nullableに変換する
                                        val safePlayerName: String =
                                            playerName!! // null検証済みなので強制アンラップ安全
                                        val safeCoin: Int =
                                            coin!! // null検証済みなので強制アンラップ安全
                                        val safeTransactionId: String =
                                            transactionId!! // null検証済みなので強制アンラップ安全

                                        plugin.logger.info(
                                            "プレイヤー名: $safePlayerName",
                                        )
                                        plugin.logger.info(
                                            "コイン数: $safeCoin",
                                        )
                                        plugin.logger.info(
                                            "取引ID: $safeTransactionId",
                                        )

                                        // メインスレッドでコイン付与処理
                                        plugin.logger.info(
                                            "メインスレッドでのコイン付与処理を開始します",
                                        )
                                        try {
                                            Bukkit.getScheduler().runTask(
                                                plugin,
                                            ) {
                                                try {
                                                    plugin.logger.info(
                                                        "コイン付与処理を実行中...",
                                                    )
                                                    val success =
                                                        coinManager
                                                            .addCoins(
                                                                safePlayerName,
                                                                safeCoin,
                                                            )
                                                    if (success) {
                                                        val player =
                                                            Bukkit.getPlayerExact(
                                                                safePlayerName,
                                                            )
                                                        player?.sendMessage(
                                                            "§a${safeCoin}コインを受け取りました！(取引ID: $safeTransactionId)",
                                                        )
                                                        plugin.logger.info(
                                                            "$safePlayerName に ${safeCoin}コインを付与しました (取引ID: $safeTransactionId)",
                                                        )
                                                    } else {
                                                        plugin.logger
                                                            .warning(
                                                                "$safePlayerName へのコイン付与に失敗しました (取引ID: $safeTransactionId)",
                                                            )
                                                    }
                                                } catch (e: Exception) {
                                                    plugin.logger.severe(
                                                        "コイン付与処理中に例外が発生しました: ${e.message}",
                                                    )
                                                    e.printStackTrace()
                                                }
                                            }
                                            plugin.logger.info(
                                                "Bukkit.getScheduler().runTask()の呼び出しが成功しました",
                                            )

                                            // 成功レスポンス
                                            plugin.logger.info(
                                                "クライアントに成功レスポンスを返します",
                                            )
                                            call.respondText(
                                                """{"success":true,"message":"OK"}""",
                                                contentType =
                                                ContentType
                                                    .Application
                                                    .Json,
                                            )
                                            plugin.logger.info(
                                                "レスポンスの送信が完了しました",
                                            )
                                        } catch (e: Exception) {
                                            plugin.logger.severe(
                                                "Bukkit.getScheduler().runTask()の呼び出しに失敗しました: ${e.message}",
                                            )
                                            e.printStackTrace()
                                        }
                                    } catch (e: Exception) {
                                        plugin.logger.severe(
                                            "JSONの解析中に例外が発生しました: ${e.message}",
                                        )
                                        e.printStackTrace()
                                        call.respondText(
                                            """{"success":false,"message":"Bad Request: Invalid JSON format"}""",
                                            status =
                                            HttpStatusCode
                                                .BadRequest,
                                        )
                                    }
                                }
                                plugin.logger.info("ルーティング設定が完了しました")
                            }
                        }
                    },
                )
                    .start(wait = false)
            plugin.logger.info("HTTPSサーバーの起動が成功しました（ポート: $port）")
        } catch (e: Exception) {
            plugin.logger.severe("HTTPSサーバーの起動中に例外が発生しました: ${e.message}")
            e.printStackTrace()
        }
    }

    fun stop() {
        plugin.logger.info("KtorApiServerの停止を開始します")
        // Add log to check server state
        plugin.logger.info("serverインスタンス: $server")
        try {
            server?.stop(1000, 2000)
            plugin.logger.info("サーバーは正常に停止しました")
        } catch (e: Exception) {
            plugin.logger.severe("サーバー停止中に例外が発生しました: ${e.message}")
            e.printStackTrace()
        }
    }
}
