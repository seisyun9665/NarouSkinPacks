package org.com.syun0521.minecraft.narouskinpacks

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class NSPCommandTabCompleter(private val plugin: NarouSkinPacks) : TabCompleter {
    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<String>): List<String>? {
        if (command.name.equals("nsp", ignoreCase = true)) {
            when (args.size) {
                1 -> return listOf("reload", "setuseskin", "coins")
                2 -> {
                    if (args[0].equals("setuseskin", ignoreCase = true)) {
                        return Bukkit.getOnlinePlayers().map { it.name }
                    } else if (args[0].equals("coins", ignoreCase = true)) {
                        return listOf("get", "add", "set", "remove")
                    }
                }
                3 -> {
                    if (args[0].equals("setuseskin", ignoreCase = true)) {
                        return plugin.getSkinNames()
                    } else if (args[0].equals("coins", ignoreCase = true)) {
                        // すべてのcoinsサブコマンドで3番目はプレイヤー名
                        return Bukkit.getOnlinePlayers().map { it.name }
                    }
                }
                4 -> {
                    if (args[0].equals("coins", ignoreCase = true)) {
                        when (args[1]) {
                            "add", "set", "remove" -> {
                                // 金額のデフォルト候補を提供
                                return listOf("10", "50", "100", "500", "1000")
                            }
                        }
                    }
                }
            }
        }
        return null
    }
}
