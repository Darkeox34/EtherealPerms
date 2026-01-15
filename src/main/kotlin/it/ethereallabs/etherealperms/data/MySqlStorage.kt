package it.ethereallabs.etherealperms.data

import it.ethereallabs.etherealperms.data.database.*
import it.ethereallabs.etherealperms.permissions.models.Group
import it.ethereallabs.etherealperms.permissions.models.Node
import it.ethereallabs.etherealperms.permissions.models.User
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

    override fun loadUser(uuid: UUID): User? {
        return transaction(db) {
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

    override fun saveUser(user: User) {
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

    override fun loadAllUsers(): List<User> {
        return transaction(db) {
            UsersTable.selectAll().mapNotNull {
                val uuid = UUID.fromString(it[UsersTable.uuid])
                loadUser(uuid)
            }
        }
    }

    override fun loadGroups(): MutableMap<String, Group> {
        return transaction(db) {
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

    override fun saveGroups(groups: Map<String, Group>) {
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

