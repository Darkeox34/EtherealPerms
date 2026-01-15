package it.ethereallabs.etherealperms.data

import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.ReplaceOptions
import it.ethereallabs.etherealperms.permissions.models.Group
import it.ethereallabs.etherealperms.permissions.models.Node
import it.ethereallabs.etherealperms.permissions.models.User
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bson.Document
import java.util.*

class MongoStorage(
    connectionString: String,
    databaseName: String
) : IStorageMethod {

    private val client = MongoClients.create(connectionString)
    private val database = client.getDatabase(databaseName)
    private val usersCollection: MongoCollection<Document> = database.getCollection("users")
    private val groupsCollection: MongoCollection<Document> = database.getCollection("groups")

    override fun loadUser(uuid: UUID): User? {
        val doc = usersCollection.find(eq("uuid", uuid.toString())).first() ?: return null
        return Json.decodeFromString<User>(doc.toJson())
    }

    override fun saveUser(user: User)  {
        val doc = Document.parse(Json.encodeToString(user))
        usersCollection.replaceOne(eq("uuid", user.uuid.toString()), doc, ReplaceOptions().upsert(true))
    }

    override fun loadAllUsers(): List<User> {
        return usersCollection.find().map { Json.decodeFromString<User>(it.toJson()) }.toList()
    }

    override fun loadGroups(): MutableMap<String, Group> {
        return groupsCollection.find().map { Json.decodeFromString<Group>(it.toJson()) }
            .associateBy { it.name }.toMutableMap()
    }

    override fun saveGroups(groups: Map<String, Group>){
        groupsCollection.deleteMany(Document())
        if (groups.isNotEmpty()) {
            val docs = groups.values.map { Document.parse(Json.encodeToString(it)) }
            groupsCollection.insertMany(docs)
        }
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
