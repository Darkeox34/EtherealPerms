package it.ethereallabs.etherealperms.data

import Configs
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.permissions.models.Group
import it.ethereallabs.etherealperms.permissions.models.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.yaml.snakeyaml.Yaml
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

class Storage(private val plugin: EtherealPerms) {

    private val configFile: Path by lazy { plugin.dataDirectory.resolve("config.yml") }
    private val yaml = Yaml()

    val storageScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @Volatile
    private var configs: Configs? = null

    lateinit var storageMethod: IStorageMethod

    init {
        if (!Files.exists(plugin.dataDirectory)) Files.createDirectories(plugin.dataDirectory)
        if (!Files.exists(configFile)) {
            javaClass.classLoader.getResourceAsStream("config.yml")?.use { input ->
                Files.copy(input, configFile)
            } ?: throw IllegalStateException("config.yml not found in resources")
        }
        reloadStorageMethod()
    }

    private fun reloadStorageMethod() {
        var config = loadRawConfig()
        var storageConfig = config["storage"] as? Map<String, Any>

        if (storageConfig == null) {
            plugin.logger.atWarning().log("Storage configuration not found. Creating backup and restoring from resources.")
            val backupFile = configFile.resolveSibling("config.yml.bck")
            try {
                Files.move(configFile, backupFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING)
            } catch (e: java.io.IOException) {
                throw IllegalStateException("Failed to back up config.yml", e)
            }

            javaClass.classLoader.getResourceAsStream("config.yml")?.use { input ->
                Files.copy(input, configFile)
            } ?: throw IllegalStateException("config.yml not found in resources")

            config = loadRawConfig()
            storageConfig = config["storage"] as? Map<String, Any>
                ?: throw IllegalStateException("Storage configuration is still missing after restoring from resources.")
        }

        val type = storageConfig["type"] as? String ?: "local"

        storageMethod = when (type.lowercase()) {
            "mongodb" -> {
                val databaseConfig = storageConfig["database"] as? Map<String, Any>
                    ?: throw IllegalStateException("Database configuration 'database' not found under 'storage' for mongodb")
                val dbConfig = databaseConfig["mongodb"] as? Map<String, Any>
                    ?: throw IllegalStateException("MongoDB configuration not found")
                MongoStorage(
                    dbConfig["connection-string"] as String,
                    dbConfig["database"] as String
                )
            }
            "mysql" -> {
                val databaseConfig = storageConfig["database"] as? Map<String, Any>
                    ?: throw IllegalStateException("Database configuration 'database' not found under 'storage' for mysql")
                val dbConfig = databaseConfig["mysql"] as? Map<String, Any>
                    ?: throw IllegalStateException("MySQL configuration not found")
                MySqlStorage(dbConfig)
            }
            else -> FileStorage(plugin)
        }
    }

    private fun loadRawConfig(): Map<String, Any> {
        return Files.newInputStream(configFile).use { yaml.load(it) }
    }

    suspend fun loadUser(uuid: UUID): User? = storageMethod.loadUser(uuid)
    suspend fun saveUser(user: User) = storageMethod.saveUser(user)
    suspend fun loadAllUsers(): List<User> = storageMethod.loadAllUsers()
    suspend fun loadGroups(): MutableMap<String, Group> = storageMethod.loadGroups()
    suspend fun saveGroups(groups: Map<String, Group>) = storageMethod.saveGroups(groups)
    suspend fun loadDefaultGroup(): Group = storageMethod.loadDefaultGroup()

    fun getConfigs(): Configs {
        return configs ?: loadConfigs()
    }

    fun reloadConfigs() {
        configs = loadConfigs()
        reloadStorageMethod()
    }

    fun loadConfigs(): Configs {
        val data = loadRawConfig()
        val chatSection = data["chat"] as? Map<*, *> ?: error("Missing 'chat' section in config.yml")
        val format = chatSection["format"] as? String ?: error("Missing chat.format in config.yml")
        val groupFormats = (chatSection["group-formats"] as? Map<*, *>)?.mapNotNull {
            val key = it.key as? String ?: return@mapNotNull null
            val value = it.value as? String ?: return@mapNotNull null
            key to value
        }?.toMap() ?: emptyMap()

        val config = Configs(format, groupFormats)
        configs = config
        return config
    }

    suspend fun sync() {
        if (storageMethod is FileStorage) {
            plugin.logger.atWarning().log("Sync is not available for local file storage.")
            return
        }
        plugin.logger.atInfo().log("Syncing data from database...")

        val fileStorage = FileStorage(plugin)
        val groups = storageMethod.loadGroups()
        val users = storageMethod.loadAllUsers()

        if (groups.isEmpty() && users.isEmpty()) {
            plugin.logger.atInfo().log("Database is empty, loading default data.")
            val defaultGroup = fileStorage.loadDefaultGroup()
            fileStorage.saveGroups(mapOf(defaultGroup.name to defaultGroup))
        } else {
            fileStorage.saveGroups(groups)
            users.forEach { fileStorage.saveUser(it) }
        }
        plugin.logger.atInfo().log("Sync complete.")
    }

    suspend fun syncUpload() {
        if (storageMethod is FileStorage) {
            plugin.logger.atWarning().log("Sync upload is not available for local file storage.")
            return
        }
        plugin.logger.atInfo().log("Uploading local data to the database...")
        val fileStorage = FileStorage(plugin)
        val groups = fileStorage.loadGroups()
        val users = fileStorage.loadAllUsers()

        storageMethod.saveGroups(groups)
        users.forEach { storageMethod.saveUser(it) }
        plugin.logger.atInfo().log("Upload complete.")
    }
}