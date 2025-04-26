package org.com.syun0521.minecraft.narouskinpacks

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class Command(private val plugin: NarouSkinPacks) : CommandExecutor {
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
            "setuseskin" -> {
                if (args.size < 2) {
                    sender.sendMessage("Usage: /nsp setuseskin <player> [<skinname>]")
                    return true
                }
                val playerName = args[1]
                val player = Bukkit.getPlayerExact(playerName)
                if (player == null) {
                    sender.sendMessage("Player $playerName not found on the server.")
                    return true
                }
                if (args.size == 2) {
                    plugin.config.set("players.$playerName.onstep", null)
                    sender.sendMessage("Skin for player $playerName has been removed.")
                    return true
                }
                val skinName = args[2]
                val validSkins = plugin.getSkinNames()
                if (!validSkins.contains(skinName)) {
                    sender.sendMessage("Invalid skin name. Valid options are: ${validSkins.joinToString(", ")}")
                    return true
                }
                plugin.config.set("players.$playerName.onstep", skinName)
                sender.sendMessage("Skin for player $playerName set to $skinName")
                return true
            }
            else -> {
                sender.sendMessage("Unknown sub command: ${args[0]}")
                return true
            }
        }
    }
}
