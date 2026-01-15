package it.ethereallabs.etherealperms.data.database

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Table

object GroupsTable : Table("ep_groups") {
    val name = varchar("name", 50)
    val weight = integer("weight")
    override val primaryKey = PrimaryKey(name)
}

object NodesTable : IntIdTable("ep_nodes") {
    val key = varchar("key", 255)
    val value = bool("value")
    val expiry = long("expiry").nullable()
    // Context is not directly supported here for simplicity, would require a separate table
}

object GroupNodesTable : Table("ep_group_nodes") {
    val groupId = varchar("group_id", 50).references(GroupsTable.name)
    val nodeId = integer("node_id").references(NodesTable.id)
    override val primaryKey = PrimaryKey(groupId, nodeId)
}

object UsersTable : Table("ep_users") {
    val uuid = varchar("uuid", 36)
    val username = varchar("username", 16)
    override val primaryKey = PrimaryKey(uuid)
}

object UserNodesTable : Table("ep_user_nodes") {
    val userId = varchar("user_id", 36).references(UsersTable.uuid)
    val nodeId = integer("node_id").references(NodesTable.id)
    override val primaryKey = PrimaryKey(userId, nodeId)
}

