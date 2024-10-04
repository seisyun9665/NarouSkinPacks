package org.com.syun0521.minecraft.narouskinpacks

import org.bukkit.Particle
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.plugin.java.JavaPlugin
import org.com.syun0521.minecraft.narouskinpacks.skin.Skin

class NarouSkinPacks : JavaPlugin(), Listener {
    lateinit var config: CustomConfig
    lateinit var skinConfig: CustomConfig
    var skinName: String = "shining"

    override fun onEnable() {
        saveDefaultConfig()
        config = CustomConfig(this)
        skinConfig = CustomConfig(this, fileName = "skins.yml", resource = "skins.yml")
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
        val player = event.player
        if (player.isOnGround) {
            val from = event.from
            val to = event.to

            // Calculate the movement direction
            val direction = to.toVector().subtract(from.toVector())
            direction.setY(0)

            // Retrieve the skin configuration for the player
            val skin = getSkinFromConfig(skinName)

            val spawnLocation = player.location.add(direction.multiply(skin.forwardOffset))
            player.world.spawnParticle(
                Particle.valueOf(skin.particle),
                spawnLocation,
                skin.amount,
                skin.x,
                skin.y,
                skin.z,
                skin.speed
            )
        }
    }

    fun getSkinFromConfig(skinName: String): Skin {
        val skinSection = skinConfig.getConfig()?.getConfigurationSection("skins.$skinName")
        val particle = skinSection?.getString("particle") ?: "END_ROD"
        val type = skinSection?.getString("type") ?: "onStep"
        val amount = skinSection?.getInt("amount") ?: 20
        val x = skinSection?.getDouble("x") ?: 0.5
        val y = skinSection?.getDouble("y") ?: 0.0
        val z = skinSection?.getDouble("z") ?: 0.5
        val speed = skinSection?.getDouble("speed") ?: 0.1
        val forwardOffset = skinSection?.getDouble("forwardOffset") ?: 5.0

        return Skin(skinName, particle, type, amount, x, y, z, speed, forwardOffset)
    }

    fun getSkinNames(): List<String> {
        val skinsSection = skinConfig.getConfig()?.getConfigurationSection("skins")
        return skinsSection?.getKeys(false)?.toList() ?: emptyList()
    }
}
