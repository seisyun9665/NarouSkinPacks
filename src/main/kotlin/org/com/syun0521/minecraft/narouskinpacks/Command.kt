package org.com.syun0521.minecraft.narouskinpacks

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class Command(private val plugin: NarouSkinPacks, private val config: CustomConfig) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (args.isEmpty()) {
            sender.sendMessage("Usage: /nsp <sub command>")
            return true
        }

        when (args[0]) {
            "reload" -> {
                plugin.reloadAllConfig()
                sender.sendMessage("Config reloaded!")
                return true
            }
            "setparticle" -> {
                if (args.size == 2) {
                    val particleName = args[1]
                    config.set("default.particle.type", particleName)
                    config.saveConfig()
                    plugin.reloadAllConfig()
                    sender.sendMessage("Particle type set to $particleName!")
                    return true
                } else {
                    sender.sendMessage("Usage: /nsp setparticle <ParticleName>")
                    return true
                }
            }
            "setx" -> {
                if (args.size == 2) {
                    val x = args[1].toDoubleOrNull()
                    if (x != null) {
                        config.set("default.particle.x", x)
                        config.saveConfig()
                        plugin.reloadAllConfig()
                        sender.sendMessage("Particle x offset set to $x!")
                    } else {
                        sender.sendMessage("Invalid x offset: ${args[1]}")
                    }
                    return true
                } else {
                    sender.sendMessage("Usage: /nsp setx <X>")
                    return true
                }
            }
            "sety" -> {
                if (args.size == 2) {
                    val y = args[1].toDoubleOrNull()
                    if (y != null) {
                        config.set("default.particle.y", y)
                        config.saveConfig()
                        plugin.reloadAllConfig()
                        sender.sendMessage("Particle y offset set to $y!")
                    } else {
                        sender.sendMessage("Invalid y offset: ${args[1]}")
                    }
                    return true
                } else {
                    sender.sendMessage("Usage: /nsp sety <Y>")
                    return true
                }
            }
            "setz" -> {
                if (args.size == 2) {
                    val z = args[1].toDoubleOrNull()
                    if (z != null) {
                        config.set("default.particle.z", z)
                        config.saveConfig()
                        plugin.reloadAllConfig()
                        sender.sendMessage("Particle z offset set to $z!")
                    } else {
                        sender.sendMessage("Invalid z offset: ${args[1]}")
                    }
                    return true
                } else {
                    sender.sendMessage("Usage: /nsp setz <Z>")
                    return true
                }
            }
            "setspeed" -> {
                if (args.size == 2) {
                    val speed = args[1].toDoubleOrNull()
                    if (speed != null) {
                        config.set("default.particle.speed", speed)
                        config.saveConfig()
                        plugin.reloadAllConfig()
                        sender.sendMessage("Particle speed set to $speed!")
                    } else {
                        sender.sendMessage("Invalid speed: ${args[1]}")
                    }
                    return true
                } else {
                    sender.sendMessage("Usage: /nsp setspeed <Speed>")
                    return true
                }
            }
            "setoffset" -> {
                if (args.size == 2) {
                    val offset = args[1].toDoubleOrNull()
                    if (offset != null) {
                        config.set("default.particle.forwardOffset", offset)
                        config.saveConfig()
                        plugin.reloadAllConfig()
                        sender.sendMessage("Forward offset set to $offset!")
                    } else {
                        sender.sendMessage("Invalid offset: ${args[1]}")
                    }
                    return true
                } else {
                    sender.sendMessage("Usage: /nsp setoffset <Offset>")
                    return true
                }
            }
            else -> {
                sender.sendMessage("Unknown sub command: ${args[0]}")
                return true
            }
        }
    }
}
