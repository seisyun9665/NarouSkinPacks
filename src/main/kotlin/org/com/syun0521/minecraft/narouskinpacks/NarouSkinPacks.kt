package org.com.syun0521.minecraft.narouskinpacks

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.plugin.java.JavaPlugin
import org.com.syun0521.minecraft.narouskinpacks.api.HttpApiServer
import org.com.syun0521.minecraft.narouskinpacks.coin.CoinManager
import org.com.syun0521.minecraft.narouskinpacks.events.PlayerMoveHandler
import kotlin.text.contains

class NarouSkinPacks : JavaPlugin(), Listener {
    private var pluginConfig: CustomConfig? = null
    private var skinConfig: CustomConfig? = null
    private var playerMoveHandler: PlayerMoveHandler? = null
    private var coinManager: CoinManager? = null
    private var httpApiServer: HttpApiServer? = null

    override fun onEnable() {
        saveDefaultConfig()
        loadConfigs()

        // loadConfigs()の後はskinConfigはnullではなくなるため安全に非null型として扱う
        playerMoveHandler = PlayerMoveHandler(skinConfig!!)

        server.pluginManager.registerEvents(this, this)

        getCommand("nsp")?.executor = Command(this)
        getCommand("nsp")?.tabCompleter = NSPCommandTabCompleter(this)

        // コイン管理クラスの初期化
        coinManager = CoinManager(pluginConfig!!)

        // API設定の取得
        val apiPort = pluginConfig?.getInt("api.port", 8080) ?: 8080
        val apiKey = pluginConfig?.getString("api.apiKey", "your-api-key") ?: "your-api-key"
        val apiEnabled = pluginConfig?.getBoolean("api.enabled", false) ?: false

        // HTTPサーバーの初期化と起動
        if (apiEnabled) {
            try {
                logger.info("APIサーバーを初期化しています...")
                httpApiServer = HttpApiServer(this, coinManager!!, apiPort, apiKey)
                httpApiServer?.start()
                logger.info("HTTP API Server started on port $apiPort")
            } catch (e: Exception) {
                logger.severe("Failed to start HTTP API Server: ${e.message}")
                e.printStackTrace()
            }
        } else {
            logger.info("API設定が無効化されているため、APIサーバーは起動しません")
        }
    }

    override fun onDisable() {
        // HTTP APIサーバーの停止
        httpApiServer?.stop()
        logger.info("HTTP API Server stopped")
    }

    private fun loadConfigs() {
        // メイン設定ファイルの読み込み
        pluginConfig = CustomConfig(this)

        // スキン設定ファイルの読み込み
        skinConfig = CustomConfig(this, "skins.yml")
    }

    fun reloadAllConfig() {
        reloadConfig()
        pluginConfig?.reloadConfig()
        skinConfig?.reloadConfig()
    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        playerMoveHandler?.handlePlayerMove(event)
    }

    fun getSkinNames(): List<String> {
        val skinSection = skinConfig?.getConfig()?.getConfigurationSection("skins")
        return skinSection?.getKeys(false)?.toList() ?: listOf()
    }

    fun getSkinConfig(): CustomConfig? {
        return skinConfig
    }

    // コイン管理クラスのゲッター
    fun getCoinManager(): CoinManager {
        return coinManager ?: throw IllegalStateException("CoinManager is not initialized")
    }
}
