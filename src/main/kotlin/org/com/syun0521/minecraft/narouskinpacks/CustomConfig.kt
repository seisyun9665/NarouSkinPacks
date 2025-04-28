package org.com.syun0521.minecraft.narouskinpacks

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.com.syun0521.minecraft.narouskinpacks.skin.Skin
import java.io.File
import java.io.IOException

/**
 * YAMLファイルの読み込みと操作を行うクラス
 * コンストラクタ内で自動的に設定ファイルを読み込みます
 */
class CustomConfig {
    private var config: FileConfiguration? = null
    private val configFile: File
    private val file: String
    private val plugin: org.bukkit.plugin.Plugin

    constructor(plugin: org.bukkit.plugin.Plugin, fileName: String, directory: File) {
        this.plugin = plugin
        this.file = fileName
        configFile = File(directory, this.file)
        makeDirectory(directory)
        makeFile(configFile)
        reloadConfig() // インスタンス化時に設定を読み込む
    }

    @JvmOverloads
    constructor(plugin: org.bukkit.plugin.Plugin, fileName: String = "config.yml") {
        this.plugin = plugin
        this.file = fileName
        configFile = File(plugin.dataFolder, file)
        makeFile(configFile)
        reloadConfig() // インスタンス化時に設定を読み込む
    }

    constructor(plugin: org.bukkit.plugin.Plugin, configFile: File) {
        this.plugin = plugin
        this.file = configFile.name
        this.configFile = configFile
        makeFile(configFile)
        reloadConfig() // インスタンス化時に設定を読み込む
    }

    constructor(plugin: org.bukkit.plugin.Plugin, fileName: String, resource: String?) {
        this.plugin = plugin
        this.file = fileName

        configFile = File(plugin.dataFolder, this.file)

        if (configFile.exists()) {
            reloadConfig() // 既存ファイルがある場合は設定を読み込む
            return
        }

        try {
            plugin.getResource(resource)?.copyTo(configFile.outputStream())
        } catch (e: IOException) {
            plugin.logger.info("Can not make the default file $resource")
        }
        makeFile(configFile)
        reloadConfig() // インスタンス化時に設定を読み込む
    }

    constructor(plugin: org.bukkit.plugin.Plugin, fileName: String, directory: File, resource: String?) {
        this.plugin = plugin
        this.file = fileName
        configFile = File(directory, this.file)

        if (configFile.exists()) {
            reloadConfig() // 既存ファイルがある場合は設定を読み込む
            return
        }

        makeDirectory(directory)
        try {
            plugin.getResource(resource)?.copyTo(configFile.outputStream())
        } catch (e: IOException) {
            plugin.logger.info("Can not make the default file $resource")
        }
        makeFile(configFile)
        reloadConfig() // インスタンス化時に設定を読み込む
    }

    fun saveDefaultConfig() {
        if (!configFile.exists()) {
            plugin.saveResource(file, false)
        }
    }

    fun reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile)

        val defConfigStream = plugin.getResource(file)
        if (defConfigStream == null) {
            return
        }

        config?.setDefaults(
            YamlConfiguration.loadConfiguration(
                java.io.InputStreamReader(
                    defConfigStream,
                    java.nio.charset.StandardCharsets.UTF_8
                )
            )
        )
    }

    fun getConfig(): FileConfiguration? {
        if (config == null) {
            reloadConfig()
        }
        return config
    }

    fun saveConfig() {
        if (config == null) {
            return
        }
        try {
            config?.save(configFile)
        } catch (ex: IOException) {
            plugin.logger.log(java.util.logging.Level.SEVERE, "Could not save config to $configFile", ex)
        }
    }

    private fun makeDirectory(file: File) {
        if (!file.exists()) {
            if (file.mkdirs()) {
                plugin.logger.info("Created a \"${file.name}\" directory")
            } else {
                plugin.logger.info("Could not create \"${file.name}\" directory")
            }
        }
    }

    private fun makeFile(file: File) {
        try {
            if (!file.exists()) {
                if (file.createNewFile()) {
                    plugin.logger.info("Created a \"${file.name}\" file")
                } else {
                    plugin.logger.info("Could not create \"${file.name}\" file")
                }
            }
        } catch (e: IOException) {
            plugin.logger.info("directory path does not found")
        }
    }

    fun getFileName(): String {
        return file
    }

    fun setString(path: String?, string: String?) {
        config?.set(path, string)
        saveConfig()
        reloadConfig()
    }

    fun set(path: String?, value: Any?) {
        config?.set(path, value)
        saveConfig()
        reloadConfig()
    }

    fun setInt(path: String?, amount: Int) {
        config?.set(path, amount)
        saveConfig()
        reloadConfig()
    }

    fun setDouble(path: String?, amount: Double) {
        config?.set(path, amount)
        saveConfig()
        reloadConfig()
    }

    fun setBoolean(path: String?, amount: Boolean) {
        config?.set(path, amount)
        saveConfig()
        reloadConfig()
    }

    fun getString(path: String?, def: String?): String? {
        return config?.getString(path, def)
    }

    fun getInt(path: String?, def: Int): Int {
        return config?.getInt(path, def) ?: def
    }

    fun getDouble(path: String?, def: Double): Double {
        return config?.getDouble(path, def) ?: def
    }

    fun getBoolean(path: String?, def: Boolean): Boolean {
        return config?.getBoolean(path, def) ?: def
    }
    fun getSkin(skinName: String): Skin {
        val skinSection = this.getConfig()?.getConfigurationSection("skins.$skinName")
        val particle = skinSection?.getString("particle") ?: "END_ROD"
        val type = skinSection?.getString("type") ?: "onStep"
        val amount = skinSection?.getInt("amount") ?: 1
        val x = skinSection?.getDouble("x") ?: 0.0
        val y = skinSection?.getDouble("y") ?: 0.0
        val z = skinSection?.getDouble("z") ?: 0.0
        val speed = skinSection?.getDouble("speed") ?: 0.0
        val forwardOffset = skinSection?.getDouble("forwardOffset") ?: 5.0

        return Skin(skinName, particle, type, amount, x, y, z, speed, forwardOffset)
    }
}
