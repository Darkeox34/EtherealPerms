package it.ethereallabs.etherealperms.data

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.permissions.models.Group
import it.ethereallabs.etherealperms.permissions.models.Node
import it.ethereallabs.etherealperms.permissions.models.User
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

class FileStorage(plugin: EtherealPerms) : IStorageMethod {

    private val dataFolder: Path = plugin.dataDirectory
    private val gson: Gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()

    private val usersDir: Path by lazy { dataFolder.resolve("users") }
    private val groupsFile: Path by lazy { dataFolder.resolve("groups.json") }

    init {
        if (!Files.exists(dataFolder)) Files.createDirectories(dataFolder)
        if (!Files.exists(usersDir)) Files.createDirectories(usersDir)
    }

    override fun loadUser(uuid: UUID): User?{
        val userFile = usersDir.resolve("$uuid.json")
        if (!userFile.exists()) return null
        return gson.fromJson(userFile.readText(), User::class.java)
    }

    override fun saveUser(user: User) {
        val userFile = usersDir.resolve("${user.uuid}.json")
        userFile.writeText(gson.toJson(user))
    }

    override fun loadAllUsers(): List<User> {
        if (!Files.exists(usersDir)) return  emptyList()
        return Files.list(usersDir)
            .filter { it.toString().endsWith(".json") }
            .map { path ->
                gson.fromJson(path.readText(), User::class.java)
            }
            .toList()
    }

    override fun loadGroups(): MutableMap<String, Group> {
        if (!groupsFile.exists()) return mutableMapOf()
        val type = object : TypeToken<MutableMap<String, Group>>() {}.type
        return gson.fromJson(groupsFile.readText(), type)
    }

    override fun saveGroups(groups: Map<String, Group>) {
        groupsFile.writeText(gson.toJson(groups))
    }

    override fun loadDefaultGroup(): Group {
        return Group("default", 0).apply {
            nodes.add(Node("etherealperms.default", true))
        }
    }

    override fun sync() {
    }

    override fun syncUpload() {
    }
}
