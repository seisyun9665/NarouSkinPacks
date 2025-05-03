package org.com.syun0521.minecraft.narouskinpacks

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class NSPCommandTabCompleter(private val plugin: NarouSkinPacks) : TabCompleter {
    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<String>): List<String>? {
        try {
            if (command.name.equals("nsp", ignoreCase = true)) {
                // 最後に入力された引数を取得（フィルタリングに使用）
                val lastArg = if (args.isNotEmpty()) args[args.size - 1].lowercase() else ""

                when (args.size) {
                    1 -> {
                        // サブコマンドリスト
                        val subCommands = listOf("reload", "setuseskin", "coins", "shop")
                        // 入力された接頭辞でフィルタリング
                        return subCommands.filter { it.startsWith(lastArg) }
                    }
                    2 -> {
                        if (args[0].equals("setuseskin", ignoreCase = true)) {
                            // プレイヤー名を入力された接頭辞でフィルタリング
                            return Bukkit.getOnlinePlayers().map { it.name }
                                .filter { it.lowercase().startsWith(lastArg) }
                        } else if (args[0].equals("coins", ignoreCase = true)) {
                            // coinsサブコマンドを入力された接頭辞でフィルタリング
                            val coinSubCommands = listOf("get", "add", "set", "remove")
                            return coinSubCommands.filter { it.startsWith(lastArg) }
                        }
                    }
                    3 -> {
                        if (args[0].equals("setuseskin", ignoreCase = true)) {
                            try {
                                // スキン名を入力された接頭辞でフィルタリング
                                return plugin.getSkinNames()
                                    .filter { it.lowercase().startsWith(lastArg) }
                            } catch (e: Exception) {
                                plugin.logger.warning("スキン名の取得中にエラーが発生しました: ${e.message}")
                                return emptyList()
                            }
                        } else if (args[0].equals("coins", ignoreCase = true)) {
                            // プレイヤー名を入力された接頭辞でフィルタリング
                            return Bukkit.getOnlinePlayers().map { it.name }
                                .filter { it.lowercase().startsWith(lastArg) }
                        }
                    }
                    4 -> {
                        if (args[0].equals("coins", ignoreCase = true)) {
                            when (args[1]) {
                                "add", "set", "remove" -> {
                                    // 金額候補を入力された接頭辞でフィルタリング
                                    val amounts = listOf("10", "50", "100", "500", "1000")
                                    return amounts.filter { it.startsWith(lastArg) }
                                }
                            }
                        }
                    }
                }
            }
            return null
        } catch (e: Exception) {
            plugin.logger.warning("タブ補完中にエラーが発生しました: ${e.message}")
            e.printStackTrace()
            return emptyList() // エラー時は空のリストを返す
        }
    }
}
