package org.com.syun0521.minecraft.narouskinpacks

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class NSPCommandTabCompleter : TabCompleter {
    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String>? {
        if (args.size == 1) {
            return listOf("reload", "setparticle", "setamount", "setx", "sety", "setz", "setspeed", "setoffset").filter { it.startsWith(args[0], ignoreCase = true) }
        }
        return null
    }
}
