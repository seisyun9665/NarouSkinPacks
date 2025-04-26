package org.com.syun0521.minecraft.narouskinpacks

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class NSPCommandTabCompleter(private val plugin: NarouSkinPacks) : TabCompleter {
    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<String>): List<String>? {
        if (command.name.equals("nsp", ignoreCase = true)) {
            when (args.size) {
                1 -> return listOf("reload", "setuseskin")
                2 -> if (args[0].equals("setuseskin", ignoreCase = true)) {
                    return Bukkit.getOnlinePlayers().map { it.name }
                }
                3 -> if (args[0].equals("setuseskin", ignoreCase = true)) {
                    return plugin.getSkinNames()
                }
            }
        }
        return null
    }
}
