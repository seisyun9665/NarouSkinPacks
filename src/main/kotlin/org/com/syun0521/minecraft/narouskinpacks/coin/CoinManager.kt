package org.com.syun0521.minecraft.narouskinpacks.coin

import org.com.syun0521.minecraft.narouskinpacks.CustomConfig

/**
 * プレイヤーのコイン管理を行うクラス
 * コインの追加、取得、設定などの操作を提供します
 */
class CoinManager(private val config: CustomConfig) {

    /**
     * プレイヤーの現在のコイン数を取得します
     *
     * @param playerName プレイヤー名
     * @return プレイヤーのコイン数、存在しない場合は0
     */
    fun getCoins(playerName: String): Int {
        return config.getInt("players.$playerName.coins", 0)
    }

    /**
     * プレイヤーにコインを付与します
     *
     * @param playerName プレイヤー名
     * @param amount 付与するコイン数
     * @return 操作の成否
     */
    fun addCoins(playerName: String, amount: Int): Boolean {
        if (amount <= 0) return false

        val currentCoins = getCoins(playerName)
        val newCoins = currentCoins + amount

        return setCoins(playerName, newCoins)
    }

    /**
     * プレイヤーからコインを消費します
     *
     * @param playerName プレイヤー名
     * @param amount 消費するコイン数
     * @return 操作の成否
     */
    fun removeCoins(playerName: String, amount: Int): Boolean {
        if (amount <= 0) return false

        val currentCoins = getCoins(playerName)
        if (currentCoins < amount) return false

        val newCoins = currentCoins - amount
        return setCoins(playerName, newCoins)
    }

    /**
     * プレイヤーのコイン数を設定します
     *
     * @param playerName プレイヤー名
     * @param amount 設定するコイン数
     * @return 操作の成否
     */
    fun setCoins(playerName: String, amount: Int): Boolean {
        if (amount < 0) return false

        config.set("players.$playerName.coins", amount)
        config.saveConfig()
        return true
    }

    /**
     * 全プレイヤーのコイン情報をリロードします
     */
    fun reloadCoins() {
        config.reloadConfig()
    }
}
