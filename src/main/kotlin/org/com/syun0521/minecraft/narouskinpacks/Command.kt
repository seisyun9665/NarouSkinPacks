package org.com.syun0521.minecraft.narouskinpacks

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

// Commandクラスはプラグインコマンドの実行を処理します。
// プラグインの設定をリロードするためのサブコマンドを処理します。
class Command(private val plugin: NarouSkinPacks) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (args.isEmpty()) {
            sender.sendMessage("Usage: /nsp <sub command>")
            return true
        }

        when (args[0]) {
            // プラグインの設定をリロードします
            "reload" -> {
                plugin.reloadAllConfig()
                sender.sendMessage("Config reloaded!")
                return true
            }
            // skinNameを設定します
            "setUseSkin" -> {
                if (args.size < 2) {
                    sender.sendMessage("Usage: /nsp setUseSkin <skinName>")
                    return true
                }
                plugin.skinName = args[1]
                sender.sendMessage("Skin set to ${args[1]}")
                return true
            }
            else -> {
                sender.sendMessage("Unknown sub command: ${args[0]}")
                return true
            }
        }
    }
}
