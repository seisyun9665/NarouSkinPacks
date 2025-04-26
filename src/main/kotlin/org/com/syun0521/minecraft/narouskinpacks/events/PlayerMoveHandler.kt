package org.com.syun0521.minecraft.narouskinpacks.events

import org.bukkit.Particle
import org.bukkit.event.player.PlayerMoveEvent
import org.com.syun0521.minecraft.narouskinpacks.CustomConfig

class PlayerMoveHandler(private val config: CustomConfig) {

    fun handlePlayerMove(event: PlayerMoveEvent) {
        val player = event.player
        if (player.isOnGround) {
            val playerName = player.name
            if (config.getConfig()?.contains("players.$playerName.onstep") == true) {
                val from = event.from
                val to = event.to

                // Calculate the movement direction
                val direction = to.toVector().subtract(from.toVector())
                direction.setY(0)

                // Retrieve the skin name from the configuration for the player
                val skinName = config.getConfig()?.getString("players.$playerName.onstep") ?: "shining"
                val skin = config.getSkin(skinName)

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
    }
}
