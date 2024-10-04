package org.com.syun0521.minecraft.narouskinpacks

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class NSPCommandTabCompleter(private val plugin: NarouSkinPacks) : TabCompleter {
    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<String>): List<String>? {
        if (command.name.equals("nsp", ignoreCase = true)) {
            if (args.size == 1) {
                return listOf("reload", "setUseSkin")
            } else if (args.size == 2 && args[0].equals("setUseSkin", ignoreCase = true)) {
                return plugin.getSkinNames()
            }
        }
        return null
    }
}
