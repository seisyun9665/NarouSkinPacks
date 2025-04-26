package org.com.syun0521.minecraft.narouskinpacks

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.plugin.java.JavaPlugin
import org.com.syun0521.minecraft.narouskinpacks.events.PlayerMoveHandler
import kotlin.text.contains

class NarouSkinPacks : JavaPlugin(), Listener {
    lateinit var config: CustomConfig
    lateinit var skinConfig: CustomConfig
    private lateinit var playerMoveHandler: PlayerMoveHandler

    override fun onEnable() {
        saveDefaultConfig()
        config = CustomConfig(this)
        skinConfig = CustomConfig(this, fileName = "skins.yml", resource = "skins.yml")
        playerMoveHandler = PlayerMoveHandler(skinConfig)
        server.pluginManager.registerEvents(this, this)
        val commandExecutor = Command(this)
        getCommand("nsp")?.executor = commandExecutor
        getCommand("nsp")?.tabCompleter = NSPCommandTabCompleter(this)
        reloadAllConfig()
    }

    fun reloadAllConfig() {
        config.reloadConfig()
        skinConfig.reloadConfig()
    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        playerMoveHandler.handlePlayerMove(event)
    }

    fun getSkinNames(): List<String> {
        val skinsSection = skinConfig.getConfig()?.getConfigurationSection("skins")
        return skinsSection?.getKeys(false)?.toList() ?: emptyList()
    }
}
