package org.com.syun0521.minecraft.narouskinpacks

import org.bukkit.Particle
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.plugin.java.JavaPlugin

class NarouSkinPacks : JavaPlugin(), Listener {
    lateinit var config: CustomConfig
    private lateinit var particle: Particle
    private var amount: Int = 20
    private var x: Double = 0.5
    private var y: Double = 0.0
    private var z: Double = 0.5
    private var speed: Double = 0.1
    private var forwardOffset: Double = 5.0 // New variable for forward offset

    override fun onEnable() {
        saveDefaultConfig()
        config = CustomConfig(this)
        server.pluginManager.registerEvents(this, this)
        val commandExecutor = Command(this, config)
        getCommand("nsp")?.executor = commandExecutor
        getCommand("nsp")?.tabCompleter = NSPCommandTabCompleter()
        reloadAllConfig()
    }

    fun reloadAllConfig() {
        config.reloadConfig()
        val particleName = config.getString("default.particle.type", "END_ROD")
        particleName?.let { particle = Particle.valueOf(it) }
        amount = config.getInt("default.particle.amount", 20)
        x = config.getDouble("default.particle.x", 0.5)
        y = config.getDouble("default.particle.y", 0.0)
        z = config.getDouble("default.particle.z", 0.5)
        speed = config.getDouble("default.particle.speed", 0.1)
        forwardOffset = config.getDouble("default.particle.forwardOffset", 5.0) // Load from config
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

            val spawnLocation = player.location.add(direction.multiply(forwardOffset))
            player.world.spawnParticle(particle, spawnLocation, amount, x, y, z, speed)
        }
    }
}
