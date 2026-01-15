package it.ethereallabs.etherealperms.data

import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.client.result.UpdateResult
import it.ethereallabs.etherealperms.permissions.models.Group
import it.ethereallabs.etherealperms.permissions.models.Node
import it.ethereallabs.etherealperms.permissions.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bson.Document
import java.util.*

private val json = Json { ignoreUnknownKeys = true }

class MongoStorage(
    connectionString: String,
    databaseName: String
) : IStorageMethod {

    private val client = MongoClients.create(connectionString)
    private val database = client.getDatabase(databaseName)
    private val usersCollection: MongoCollection<Document> = database.getCollection("users")
    private val groupsCollection: MongoCollection<Document> = database.getCollection("groups")

    override suspend fun loadUser(uuid: UUID): User? = withContext(Dispatchers.IO) {
        val doc = usersCollection.find(eq("uuid", uuid.toString())).first() ?: return@withContext null
        return@withContext json.decodeFromString<User>(doc.toJson())
    }

    override suspend fun saveUser(user: User): UpdateResult = withContext(Dispatchers.IO) {
        val doc = Document.parse(Json.encodeToString(user))
        usersCollection.replaceOne(eq("uuid", user.uuid.toString()), doc, ReplaceOptions().upsert(true))
    }

    override suspend fun loadAllUsers(): List<User> = withContext(Dispatchers.IO) {
        return@withContext usersCollection.find().map { json.decodeFromString<User>(it.toJson()) }.toList()
    }

    override suspend fun loadGroups(): MutableMap<String, Group> = withContext(Dispatchers.IO) {
        return@withContext groupsCollection.find().map { json.decodeFromString<Group>(it.toJson()) }
            .associateBy { it.name }.toMutableMap()
    }

    override suspend fun saveGroups(groups: Map<String, Group>) = withContext(Dispatchers.IO){
        groupsCollection.deleteMany(Document())
        if (groups.isNotEmpty()) {
            val docs = groups.values.map { Document.parse(Json.encodeToString(it)) }
            groupsCollection.insertMany(docs)
        }
    }

    override suspend fun loadDefaultGroup(): Group = withContext(Dispatchers.IO) {
        return@withContext Group("default", 0).apply {
            nodes.add(Node("etherealperms.default", true))
        }
    }
}
