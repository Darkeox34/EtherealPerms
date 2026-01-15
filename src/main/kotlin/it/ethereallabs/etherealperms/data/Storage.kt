package it.ethereallabs.etherealperms.data

import Configs
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.permissions.models.Group
import it.ethereallabs.etherealperms.permissions.models.Node
import it.ethereallabs.etherealperms.permissions.models.User
import org.yaml.snakeyaml.Yaml
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

/**
 * Handles JSON file storage for users and groups.
 */
class Storage(plugin: EtherealPerms) {

    private val dataFolder: Path = plugin.dataDirectory
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val yaml = Yaml()

    private val usersDir: Path by lazy { dataFolder.resolve("users") }
    private val groupsFile: Path by lazy { dataFolder.resolve("groups.json") }
    private val configFile: Path by lazy { dataFolder.resolve("config.yml") }

    @Volatile
    private var configs: Configs? = null

    init {
        if (!Files.exists(dataFolder)) Files.createDirectories(dataFolder)
        if (!Files.exists(usersDir)) Files.createDirectories(usersDir)
        if (!Files.exists(configFile)) {
            javaClass.classLoader.getResourceAsStream("config.yml")?.use { input ->
                Files.copy(input, configFile)
            } ?: throw IllegalStateException("config.yml not found in resources")
        }
    }

    fun loadUser(uuid: UUID): User? {
        val userFile = usersDir.resolve("$uuid.json")
        if (!userFile.exists()) return null
        return gson.fromJson(userFile.readText(), User::class.java)
    }

    fun loadAllUsers(): List<User> {
        if (!Files.exists(usersDir)) return emptyList()
        return Files.list(usersDir)
            .filter { it.toString().endsWith(".json") }
            .map { path ->
                gson.fromJson(path.readText(), User::class.java)
            }
            .toList()
    }

    fun saveUser(user: User) {
        val userFile = usersDir.resolve("${user.uuid}.json")
        userFile.writeText(gson.toJson(user))
    }

    fun loadGroups(): MutableMap<String, Group> {
        if (!groupsFile.exists()) return mutableMapOf()
        val type = object : TypeToken<MutableMap<String, Group>>() {}.type
        return gson.fromJson(groupsFile.readText(), type)
    }

    fun saveGroups(groups: Map<String, Group>) {
        groupsFile.writeText(gson.toJson(groups))
    }

    fun loadDefaultGroup(): Group {
        return Group("default", 0).apply {
            nodes.add(Node("etherealperms.default", true))
        }
    }

    fun getConfigs(): Configs {
        return configs ?: loadConfigs()
    }

    fun reloadConfigs() {
        configs = loadConfigs()
    }

    fun loadConfigs(): Configs {
        val input = Files.newInputStream(configFile)
        val data = yaml.load<Map<String, Any>>(input)

        val chatSection = data["chat"] as? Map<*, *>
            ?: error("Missing 'chat' section in config.yml")

        val format = chatSection["format"] as? String
            ?: error("Missing chat.format in config.yml")

        val groupFormats = (chatSection["group-formats"] as? Map<*, *>)?.mapNotNull {
            val key = it.key as? String ?: return@mapNotNull null
            val value = it.value as? String ?: return@mapNotNull null
            key to value
        }?.toMap() ?: emptyMap()

        val config = Configs(format, groupFormats)
        configs = config
        return config
    }


}