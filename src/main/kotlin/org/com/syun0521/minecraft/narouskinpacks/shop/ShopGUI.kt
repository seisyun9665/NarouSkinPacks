package org.com.syun0521.minecraft.narouskinpacks.shop

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
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
    private val pluginConfig by lazy { plugin.getPluginConfig() }
    init {
        // リスナーとして登録
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    /**
     * プレイヤーにスキンショップを表示する
     */
    fun openShop(player: Player) {
        val inventory = Bukkit.createInventory(player, 27, "${ChatColor.GOLD}スキンショップ")

        // コイン残高表示と利用可能なスキンを表示
        updateShopContents(inventory, player)

        // インベントリを開く
        player.openInventory(inventory)
        shopInventories[player] = inventory
    }

    /**
     * ショップインベントリのコンテンツを更新する
     */
    private fun updateShopContents(inventory: Inventory, player: Player) {
        // プレイヤーのコイン残高表示
        updateCoinDisplay(inventory, player)

        // 利用可能なスキンを表示
        loadSkins(inventory, player)
    }

    /**
     * ショップのコイン残高表示を更新する
     */
    private fun updateCoinDisplay(inventory: Inventory, player: Player) {
        // プレイヤーのコイン残高表示
        val coins = coinManager.getCoins(player.name)
        val infoItem = createInfoItem(
            Material.GOLD_INGOT,
            "${ChatColor.YELLOW}残高: ${coins}コイン",
            listOf("${ChatColor.GRAY}スキンパックを購入するには", "${ChatColor.GRAY}アイテムをクリックしてください"),
        )
        inventory.setItem(0, infoItem)
    }

    /**
     * 利用可能なスキンをショップに表示する
     */
    private fun loadSkins(inventory: Inventory, player: Player) {
        val skinsSection = skinConfig.getConfig()?.getConfigurationSection("skins")
        val currentSkin = pluginConfig.getString("players.${player.name}.currentSkin", "shining")
        val ownedSkins = pluginConfig.getStringList("players.${player.name}.ownedSkins")

        skinsSection?.getKeys(false)?.forEachIndexed { index, skinName ->
            val particle = skinConfig.getString("skins.$skinName.particle", "FLAME")
            val price = pluginConfig.getInt("skinshop.skins.$skinName.price", 100)
            val displayItem = Material.valueOf(
                pluginConfig.getString("skinshop.skins.$skinName.display_item", "ENDER_EYE").uppercase(),
            )
            val description = pluginConfig.getString("skinshop.skins.$skinName.description", "カスタムスキンエフェクト")

            val isOwned = ownedSkins.contains(skinName)
            val isSelected = skinName == currentSkin

            val item = if (isOwned) {
                if (isSelected) {
                    createSkinItem(
                        displayItem,
                        "${ChatColor.GREEN}$skinName ${ChatColor.GOLD}[選択中]",
                        listOf(
                            "${ChatColor.GRAY}パーティクル: $particle",
                            "${ChatColor.GREEN}購入済み",
                            "${ChatColor.YELLOW}クリックして選択",
                        ),
                    )
                } else {
                    createSkinItem(
                        displayItem,
                        "${ChatColor.GREEN}$skinName",
                        listOf(
                            "${ChatColor.GRAY}パーティクル: $particle",
                            "${ChatColor.GREEN}購入済み",
                            "${ChatColor.YELLOW}クリックして選択",
                        ),
                    )
                }
            } else {
                createSkinItem(
                    displayItem,
                    "${ChatColor.WHITE}$skinName",
                    listOf(
                        "${ChatColor.GRAY}パーティクル: $particle",
                        "${ChatColor.GRAY}説明: $description",
                        "${ChatColor.GOLD}価格: ${price}コイン",
                        "${ChatColor.YELLOW}クリックして購入",
                    ),
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

        // すべてのクリックをキャンセル（プレイヤーのインベントリを含む）
        event.isCancelled = true

        val clickedInventory = event.clickedInventory

        // クリックしたのがショップインベントリではない場合は無視
        if (clickedInventory != shopInventories[player]) return

        val clickedItem = event.currentItem ?: return

        // メタデータがない場合は処理しない
        val itemMeta = clickedItem.itemMeta ?: return
        val displayName = itemMeta.displayName ?: return

        // スキン名を取得
        val skinNameRaw = ChatColor.stripColor(displayName.split(" ").firstOrNull() ?: return)

        // 有効なスキン名かチェック
        val validSkinNames = plugin.getSkinNames()
        if (!validSkinNames.contains(skinNameRaw)) {
            return
        }

        val owned = itemMeta.lore?.any { it.contains("購入済み") } == true
        val price = pluginConfig.getInt("skinshop.skins.$skinNameRaw.price", 100)

        if (owned) {
            // 所有済みのスキンを選択
            pluginConfig.set("players.${player.name}.currentSkin", skinNameRaw)
            player.sendMessage("${ChatColor.GREEN}スキン「$skinNameRaw」を選択しました。")

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
                    val ownedSkins = pluginConfig.getStringList("players.${player.name}.ownedSkins").toMutableList()
                    ownedSkins.add(skinNameRaw)
                    pluginConfig.set("players.${player.name}.ownedSkins", ownedSkins)

                    // 新しいスキンを選択状態にする
                    pluginConfig.set("players.${player.name}.currentSkin", skinNameRaw)

                    player.sendMessage("${ChatColor.GREEN}スキン「$skinNameRaw」を${price}コインで購入しました。")

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

    /**
     * ショップを更新する
     */
    private fun refreshShop(player: Player) {
        // 現在開いているショップインベントリを取得
        val inventory = shopInventories[player] ?: return

        // インベントリの内容をクリア（コイン表示アイテム以外）
        for (i in 1 until inventory.size) {
            inventory.setItem(i, null)
        }

        // インベントリの内容を更新
        updateShopContents(inventory, player)

        // インベントリを強制的に更新
        player.updateInventory()
    }

    /**
     * インベントリが閉じられたときの処理
     */
    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        val player = event.player as? Player ?: return

        // プレイヤーのショップインベントリだった場合
        if (shopInventories.containsKey(player)) {
            // インベントリを強制的に更新して状態を同期
            player.updateInventory()

            // スケジューラで遅延実行して確実に反映
            plugin.server.scheduler.runTask(
                plugin,
                Runnable {
                    player.updateInventory()
                },
            )

            // ショップインベントリの参照を削除
            shopInventories.remove(player)
        }
    }

    /**
     * インベントリドラッグイベントを処理する
     */
    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST)
    fun onInventoryDrag(event: InventoryDragEvent) {
        val player = event.whoClicked as? Player ?: return

        // プレイヤーがショップを開いている場合はすべてのドラッグをキャンセル
        if (shopInventories.containsKey(player)) {
            event.isCancelled = true
        }
    }
}
