package org.com.syun0521.minecraft.narouskinpacks.api

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import org.bukkit.Bukkit
import org.com.syun0521.minecraft.narouskinpacks.NarouSkinPacks
import org.com.syun0521.minecraft.narouskinpacks.coin.CoinManager
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets

/**
 * HTTP APIサーバーを提供するクラス
 * 外部からのリクエストを受け付け、プラグインの機能を呼び出します
 */
class HttpApiServer(
    private val plugin: NarouSkinPacks,
    private val coinManager: CoinManager,
    private val port: Int,
    private val apiKey: String
) {
    private var server: HttpServer? = null

    /**
     * HTTPサーバーを起動し、エンドポイントを登録します
     */
    fun start() {
        try {
            server = HttpServer.create(InetSocketAddress(port), 0)
            server?.createContext("/api/purchase/notify", this::handlePurchaseNotify)
            server?.start()
            plugin.logger.info("HTTPサーバー起動: http://localhost:$port/api/purchase/notify")
        } catch (e: IOException) {
            plugin.logger.severe("HTTPサーバーの起動に失敗しました: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * HTTPサーバーを停止します
     */
    fun stop() {
        server?.stop(0)
        plugin.logger.info("HTTPサーバーを停止しました")
    }

    /**
     * 購入通知エンドポイントのハンドラー
     */
    private fun handlePurchaseNotify(exchange: HttpExchange) {
        try {
            // POSTメソッド以外は拒否
            if ("POST" != exchange.requestMethod) {
                sendErrorResponse(exchange, 405, "Method Not Allowed")
                return
            }

            // APIキー認証
            val requestApiKey = exchange.requestHeaders.getFirst("X-API-Key")
            if (this.apiKey != requestApiKey) {
                sendErrorResponse(exchange, 401, "Unauthorized")
                return
            }

            // JSONリクエストの解析
            val requestBody = readRequestBody(exchange)
            val json = JSONObject(requestBody)

            // 必須パラメータの検証
            if (!json.has("playerName") || !json.has("coin") || !json.has("transactionId")) {
                sendErrorResponse(exchange, 400, "Bad Request: Missing required parameters")
                return
            }

            val playerName = json.getString("playerName")
            val coin = json.getInt("coin")
            val transactionId = json.getString("transactionId")

            // メインスレッドでコイン付与処理
            Bukkit.getScheduler().runTask(
                plugin,
                Runnable {
                    val success = coinManager.addCoins(playerName, coin)
                    if (success) {
                        val player = Bukkit.getPlayerExact(playerName)
                        player?.sendMessage("§a${coin}コインを受け取りました！(取引ID: $transactionId)")
                        plugin.logger.info("$playerName に ${coin}コインを付与しました (取引ID: $transactionId)")
                    } else {
                        plugin.logger.warning("$playerName へのコイン付与に失敗しました (取引ID: $transactionId)")
                    }
                }
            )

            // 成功レスポンス
            val response = JSONObject()
                .put("success", true)
                .put("message", "OK")
                .toString()

            sendResponse(exchange, 200, response)
        } catch (e: Exception) {
            plugin.logger.severe("リクエスト処理中にエラーが発生しました: ${e.message}")
            e.printStackTrace()
            sendErrorResponse(exchange, 500, "Internal Server Error")
        }
    }

    /**
     * リクエストボディを文字列として読み込む
     */
    private fun readRequestBody(exchange: HttpExchange): String {
        val inputStream = exchange.requestBody
        return BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8))
            .lines()
            .reduce("", { acc, line -> acc + line })
    }

    /**
     * エラーレスポンスを送信
     */
    private fun sendErrorResponse(exchange: HttpExchange, statusCode: Int, message: String) {
        val response = JSONObject()
            .put("success", false)
            .put("message", message)
            .toString()
        sendResponse(exchange, statusCode, response)
    }

    /**
     * JSON形式のレスポンスを送信
     */
    private fun sendResponse(exchange: HttpExchange, statusCode: Int, responseBody: String) {
        exchange.responseHeaders.set("Content-Type", "application/json")
        exchange.sendResponseHeaders(statusCode, responseBody.length.toLong())
        val outputStream: OutputStream = exchange.responseBody
        outputStream.write(responseBody.toByteArray(StandardCharsets.UTF_8))
        outputStream.close()
    }
}
