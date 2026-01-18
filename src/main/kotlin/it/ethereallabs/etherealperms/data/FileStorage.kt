package it.ethereallabs.etherealperms.data

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.mongodb.client.result.UpdateResult
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.permissions.models.Group
import it.ethereallabs.etherealperms.permissions.models.Node
import it.ethereallabs.etherealperms.permissions.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    override suspend fun loadUser(uuid: UUID): User? =withContext(Dispatchers.IO){
        val userFile = usersDir.resolve("$uuid.json")
        if (!userFile.exists()) return@withContext null

        val user = gson.fromJson(userFile.readText(), User::class.java)

        user.nodes.removeIf { node ->
            if (node.key.startsWith("group.")) {
                val groupName = node.key.removePrefix("group.")
                !EtherealPerms.permissionManager.groups.containsKey(groupName)
            } else {
                false
            }
        }

        if (user.nodes.none { it.key.startsWith("group.") }) {
            val defaultGroupName = EtherealPerms.permissionManager.getDefaultGroup()?.name ?: "group.default"
            user.nodes.add(Node("group.$defaultGroupName"))
        }

        saveUser(user)

        return@withContext user
    }

    override suspend fun saveUser(user: User): Any = withContext(Dispatchers.IO) {
        try {
            val userFile = usersDir.resolve("${user.uuid}.json")
            userFile.writeText(gson.toJson(user))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun loadAllUsers(): List<User> =withContext(Dispatchers.IO){
        if (!Files.exists(usersDir)) return@withContext  emptyList()
        return@withContext Files.list(usersDir)
            .filter { it.toString().endsWith(".json") }
            .map { path ->
                gson.fromJson(path.readText(), User::class.java)
            }
            .toList()
    }

    override suspend fun loadGroups(): MutableMap<String, Group> =withContext(Dispatchers.IO){
        if (!groupsFile.exists()) return@withContext mutableMapOf()
        val type = object : TypeToken<MutableMap<String, Group>>() {}.type
        val groups: MutableMap<String, Group> = gson.fromJson(groupsFile.readText(), type) ?: mutableMapOf()

        return@withContext groups.mapValues { (key, value) -> 
            if (value.name.isEmpty()) value.copy(name = key) else value 
        }.toMutableMap()
    }

    override suspend fun saveGroups(groups: Map<String, Group>) =withContext(Dispatchers.IO){
        groupsFile.writeText(gson.toJson(groups))
    }

    override suspend fun loadDefaultGroup(): Group =withContext(Dispatchers.IO){
        return@withContext Group("default", 0).apply {
            nodes.add(Node("etherealperms.default", true))
        }
    }
}
