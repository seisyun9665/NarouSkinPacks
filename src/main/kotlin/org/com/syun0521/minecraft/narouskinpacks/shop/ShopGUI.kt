package org.com.syun0521.minecraft.narouskinpacks.shop

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.com.syun0521.minecraft.narouskinpacks.CustomConfig
import org.com.syun0521.minecraft.narouskinpacks.NarouSkinPacks

/**
 * スキンショップのGUIを管理するクラス
 */
class ShopGUI(private val plugin: NarouSkinPacks) : Listener {

    private val shopInventories = mutableMapOf<Player, Inventory>()
    private val skinConfig: CustomConfig = plugin.getSkinConfig()!!
    private val coinManager by lazy { plugin.getCoinManager() }

    init {
        // リスナーとして登録
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    /**
     * プレイヤーにスキンショップを表示する
     */
    fun openShop(player: Player) {
        val inventory = Bukkit.createInventory(player, 27, "${ChatColor.GOLD}スキンショップ")

        // プレイヤーのコイン残高表示
        val coins = coinManager.getCoins(player.name)
        val infoItem = createInfoItem(
            Material.GOLD_INGOT, "${ChatColor.YELLOW}残高: ${coins}コイン",
            listOf("${ChatColor.GRAY}スキンパックを購入するには", "${ChatColor.GRAY}アイテムをクリックしてください")
        )
        inventory.setItem(0, infoItem)

        // 利用可能なスキンを表示
        loadSkins(inventory, player)

        // インベントリを開く
        player.openInventory(inventory)
        shopInventories[player] = inventory
    }

    /**
     * 利用可能なスキンをショップに表示する
     */
    private fun loadSkins(inventory: Inventory, player: Player) {
        val skinsSection = skinConfig.getConfig()?.getConfigurationSection("skins")
        val currentSkin = plugin.config.getString("players.${player.name}.currentSkin")
        val ownedSkins = plugin.config.getStringList("players.${player.name}.ownedSkins")

        skinsSection?.getKeys(false)?.forEachIndexed { index, skinName ->
            val particle = skinConfig.getString("skins.$skinName.particle", "FLAME")
            val price = plugin.config.getInt("skinshop.skins.$skinName.price", 100)
            val displayItem = Material.valueOf(
                plugin.config.getString("skinshop.skins.$skinName.display_item", "ENDER_EYE").uppercase()
            )
            val description = plugin.config.getString("skinshop.skins.$skinName.description", "カスタムスキンエフェクト")

            val isOwned = ownedSkins.contains(skinName)
            val isSelected = skinName == currentSkin

            val item = if (isOwned) {
                if (isSelected) {
                    createSkinItem(
                        displayItem, "${ChatColor.GREEN}$skinName ${ChatColor.GOLD}[選択中]",
                        listOf(
                            "${ChatColor.GRAY}パーティクル: $particle",
                            "${ChatColor.GREEN}購入済み",
                            "${ChatColor.YELLOW}クリックして選択"
                        )
                    )
                } else {
                    createSkinItem(
                        displayItem, "${ChatColor.GREEN}$skinName",
                        listOf(
                            "${ChatColor.GRAY}パーティクル: $particle",
                            "${ChatColor.GREEN}購入済み",
                            "${ChatColor.YELLOW}クリックして選択"
                        )
                    )
                }
            } else {
                createSkinItem(
                    displayItem, "${ChatColor.WHITE}$skinName",
                    listOf(
                        "${ChatColor.GRAY}パーティクル: $particle",
                        "${ChatColor.GRAY}説明: $description",
                        "${ChatColor.GOLD}価格: ${price}コイン",
                        "${ChatColor.YELLOW}クリックして購入"
                    )
                )
            }

            // 10から始めて、各スキンを配置
            // 上部メニューバーと下部ボタンを避けるレイアウト
            val slot = if (index < 7) 10 + index else 19 + (index - 7)
            if (slot < inventory.size) {
                inventory.setItem(slot, item)
            }
        }
    }

    /**
     * アイテムを作成する
     */
    private fun createSkinItem(material: Material, name: String, lore: List<String>): ItemStack {
        val item = ItemStack(material)
        val meta = item.itemMeta

        meta?.setDisplayName(name)
        meta?.lore = lore

        item.itemMeta = meta
        return item
    }

    /**
     * 情報アイテムを作成する
     */
    private fun createInfoItem(material: Material, name: String, lore: List<String>): ItemStack {
        return createSkinItem(material, name, lore)
    }

    /**
     * クリックイベントを処理する
     */
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val inventory = event.inventory

        // プレイヤーのショップインベントリかチェック
        if (inventory != shopInventories[player]) return

        // クリックをキャンセル
        event.isCancelled = true

        val slot = event.slot
        if (slot > 0) { // 情報アイテム以外
            val clickedItem = event.currentItem ?: return

            // スキン名を取得
            val skinName = ChatColor.stripColor(clickedItem.itemMeta?.displayName?.split(" ")?.get(0) ?: return)
            val owned = clickedItem.itemMeta?.lore?.any { it.contains("購入済み") } == true
            val price = plugin.config.getInt("skinshop.skins.$skinName.price", 100)

            if (owned) {
                // 所有済みのスキンを選択
                plugin.config.set("players.${player.name}.currentSkin", skinName)
                plugin.saveConfig()
                player.sendMessage("${ChatColor.GREEN}スキン「$skinName」を選択しました。")

                // GUIを更新
                refreshShop(player)
            } else {
                // スキンを購入
                val coins = coinManager.getCoins(player.name)

                if (coins >= price) {
                    // 十分なコインがある場合
                    val success = coinManager.removeCoins(player.name, price)

                    if (success) {
                        // 購入したスキンを所有リストに追加
                        val ownedSkins = plugin.config.getStringList("players.${player.name}.ownedSkins").toMutableList()
                        ownedSkins.add(skinName)
                        plugin.config.set("players.${player.name}.ownedSkins", ownedSkins)

                        // 新しいスキンを選択状態にする
                        plugin.config.set("players.${player.name}.currentSkin", skinName)
                        plugin.saveConfig()

                        player.sendMessage("${ChatColor.GREEN}スキン「$skinName」を${price}コインで購入しました。")

                        // GUIを更新
                        refreshShop(player)
                    } else {
                        player.sendMessage("${ChatColor.RED}購入処理中にエラーが発生しました。")
                    }
                } else {
                    player.sendMessage("${ChatColor.RED}コインが不足しています。必要コイン: $price, 所持コイン: $coins")
                }
            }
        }
    }

    /**
     * ショップを更新する
     */
    private fun refreshShop(player: Player) {
        // インベントリを閉じて再度開く
        player.closeInventory()
        openShop(player)
    }

    /**
     * インベントリが閉じられたときの処理
     */
    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        val player = event.player as? Player ?: return
        shopInventories.remove(player)
    }
}
