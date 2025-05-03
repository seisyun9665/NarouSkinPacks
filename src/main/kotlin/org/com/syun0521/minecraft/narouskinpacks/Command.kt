package org.com.syun0521.minecraft.narouskinpacks

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.com.syun0521.minecraft.narouskinpacks.shop.ShopGUI

class Command(private val plugin: NarouSkinPacks) : CommandExecutor {
    // ShopGUIインスタンスを遅延初期化
    private val shopGUI by lazy { ShopGUI(plugin) }

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
                    plugin.getPluginConfig().set("players.$playerName.currentSkin", null)
                    sender.sendMessage("Skin for player $playerName has been removed.")
                    return true
                }
                val skinName = args[2]
                val validSkins = plugin.getSkinNames()
                if (!validSkins.contains(skinName)) {
                    sender.sendMessage("Invalid skin name. Valid options are: ${validSkins.joinToString(", ")}")
                    return true
                }
                plugin.getPluginConfig().set("players.$playerName.currentSkin", skinName)
                sender.sendMessage("Skin for player $playerName set to $skinName")
                return true
            }
            "coins" -> {
                if (args.size < 2) {
                    sender.sendMessage("Usage: /nsp coins <get|add|set|remove> <player> [amount]")
                    return true
                }

                when (args[1]) {
                    "get" -> {
                        if (args.size < 3) {
                            sender.sendMessage("Usage: /nsp coins get <player>")
                            return true
                        }
                        val playerName = args[2]
                        val coins = plugin.getCoinManager().getCoins(playerName)
                        sender.sendMessage("$playerName のコイン数: $coins")
                        return true
                    }
                    "add" -> {
                        if (args.size < 4) {
                            sender.sendMessage("Usage: /nsp coins add <player> <amount>")
                            return true
                        }
                        val playerName = args[2]
                        val amount = args[3].toIntOrNull()
                        if (amount == null || amount <= 0) {
                            sender.sendMessage("金額は正の整数で指定してください")
                            return true
                        }

                        val success = plugin.getCoinManager().addCoins(playerName, amount)
                        if (success) {
                            val newCoins = plugin.getCoinManager().getCoins(playerName)
                            sender.sendMessage("$playerName に $amount コインを追加しました。現在のコイン数: $newCoins")

                            // オンラインプレイヤーに通知
                            val player = Bukkit.getPlayerExact(playerName)
                            player?.sendMessage("§a$amount コインを受け取りました！")
                        } else {
                            sender.sendMessage("コインの追加に失敗しました")
                        }
                        return true
                    }
                    "remove" -> {
                        if (args.size < 4) {
                            sender.sendMessage("Usage: /nsp coins remove <player> <amount>")
                            return true
                        }
                        val playerName = args[2]
                        val amount = args[3].toIntOrNull()
                        if (amount == null || amount <= 0) {
                            sender.sendMessage("金額は正の整数で指定してください")
                            return true
                        }

                        val success = plugin.getCoinManager().removeCoins(playerName, amount)
                        if (success) {
                            val newCoins = plugin.getCoinManager().getCoins(playerName)
                            sender.sendMessage("$playerName から $amount コインを消費しました。現在のコイン数: $newCoins")

                            // オンラインプレイヤーに通知
                            val player = Bukkit.getPlayerExact(playerName)
                            player?.sendMessage("§c$amount コインを消費しました")
                        } else {
                            sender.sendMessage("コインの消費に失敗しました（コインが不足している可能性があります）")
                        }
                        return true
                    }
                    "set" -> {
                        if (args.size < 4) {
                            sender.sendMessage("Usage: /nsp coins set <player> <amount>")
                            return true
                        }
                        val playerName = args[2]
                        val amount = args[3].toIntOrNull()
                        if (amount == null || amount < 0) {
                            sender.sendMessage("金額は0以上の整数で指定してください")
                            return true
                        }

                        val success = plugin.getCoinManager().setCoins(playerName, amount)
                        if (success) {
                            sender.sendMessage("$playerName のコイン数を $amount に設定しました")

                            // オンラインプレイヤーに通知
                            val player = Bukkit.getPlayerExact(playerName)
                            player?.sendMessage("§eあなたのコイン数が $amount に設定されました")
                        } else {
                            sender.sendMessage("コインの設定に失敗しました")
                        }
                        return true
                    }
                    else -> {
                        sender.sendMessage("Usage: /nsp coins <get|add|set|remove> <player> [amount]")
                        return true
                    }
                }
            }
            "shop" -> {
                // プレイヤーからの実行のみ許可
                if (sender !is Player) {
                    sender.sendMessage("このコマンドはゲーム内プレイヤーのみが実行できます")
                    return true
                }

                // ショップを開く
                shopGUI.openShop(sender)
                return true
            }
            else -> {
                sender.sendMessage("Unknown sub command: ${args[0]}")
                return true
            }
        }
    }
}
