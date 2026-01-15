package it.ethereallabs.etherealperms.data

import it.ethereallabs.etherealperms.data.database.*
import it.ethereallabs.etherealperms.permissions.models.Group
import it.ethereallabs.etherealperms.permissions.models.Node
import it.ethereallabs.etherealperms.permissions.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class MySqlStorage(
    config: Map<String, Any>
) : IStorageMethod {

    private val db: Database

    init {
        val host = config["host"] as String
        val port = config["port"] as Int
        val database = config["database"] as String
        val user = config["username"] as String
        val password = config["password"] as String
        db = Database.connect(
            "jdbc:mysql://$host:$port/$database",
            driver = "com.mysql.cj.jdbc.Driver",
            user = user,
            password = password
        )

        transaction(db) {
            SchemaUtils.create(
                GroupsTable, NodesTable, GroupNodesTable, UsersTable, UserNodesTable
            )
        }
    }

    override suspend fun loadUser(uuid: UUID): User? = withContext(Dispatchers.IO) {
        return@withContext transaction(db) {
            UsersTable.select { UsersTable.uuid eq uuid.toString() }.firstOrNull()?.let {
                val userNodes = UserNodesTable.innerJoin(NodesTable)
                    .select { UserNodesTable.userId eq uuid.toString() }
                    .map { row ->
                        Node(
                            row[NodesTable.key],
                            row[NodesTable.value],
                            row[NodesTable.expiry]
                        )
                    }.toMutableSet()
                User(uuid, it[UsersTable.username], userNodes)
            }
        }
    }

    override suspend fun saveUser(user: User) = withContext(Dispatchers.IO) {
        transaction(db) {
            UserNodesTable.deleteWhere { UserNodesTable.userId eq user.uuid.toString() }

            UsersTable.replace {
                it[uuid] = user.uuid.toString()
                it[username] = user.username
            }

            user.nodes.forEach { node ->
                val nodeId = NodesTable.insertAndGetId {
                    it[key] = node.key
                    it[value] = node.value
                    it[expiry] = node.expiry
                }
                UserNodesTable.insert {
                    it[userId] = user.uuid.toString()
                    it[UserNodesTable.nodeId] = nodeId.value
                }
            }
        }
    }

    override suspend fun loadAllUsers(): List<User> = withContext(Dispatchers.IO) {
        transaction(db) {
            val rows = (UsersTable leftJoin UserNodesTable leftJoin NodesTable)
                .selectAll()
                .toList()

            rows.groupBy { it[UsersTable.uuid] }
                .map { (uuidString, userRows) ->
                    val uuid = UUID.fromString(uuidString)
                    val firstRow = userRows.first()

                    val userNodes = userRows.mapNotNull { row ->
                        row.getOrNull(NodesTable.key)?.let {
                            Node(
                                it,
                                row[NodesTable.value],
                                row[NodesTable.expiry]
                            )
                        }
                    }.toMutableSet()

                    User(uuid, firstRow[UsersTable.username], userNodes)
                }
        }
    }

    override suspend fun loadGroups(): MutableMap<String, Group> = withContext(Dispatchers.IO) {
        return@withContext transaction(db) {
            GroupsTable.selectAll().map {
                val groupName = it[GroupsTable.name]
                val groupNodes = GroupNodesTable.innerJoin(NodesTable)
                    .select { GroupNodesTable.groupId eq groupName }
                    .map { row ->
                        Node(
                            row[NodesTable.key],
                            row[NodesTable.value],
                            row[NodesTable.expiry]
                        )
                    }.toMutableSet()
                Group(groupName, it[GroupsTable.weight], groupNodes)
            }.associateBy { it.name }.toMutableMap()
        }
    }

    override suspend fun saveGroups(groups: Map<String, Group>) = withContext(Dispatchers.IO) {
        transaction(db) {
            GroupNodesTable.deleteAll()
            GroupsTable.deleteAll()
            groups.values.forEach { group ->
                GroupsTable.insert {
                    it[name] = group.name
                    it[weight] = group.weight
                }
                group.nodes.forEach { node ->
                    val nodeId = NodesTable.insertAndGetId {
                        it[key] = node.key
                        it[value] = node.value
                        it[expiry] = node.expiry
                    }
                    GroupNodesTable.insert {
                        it[groupId] = group.name
                        it[GroupNodesTable.nodeId] = nodeId.value
                    }
                }
            }
        }
    }

    override suspend fun loadDefaultGroup(): Group = withContext(Dispatchers.IO) {
        return@withContext Group("default", 0).apply {
            nodes.add(Node("etherealperms.default", true))
        }
    }
}

