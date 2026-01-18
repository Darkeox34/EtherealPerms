package it.ethereallabs.etherealperms.data

import com.mongodb.client.result.UpdateResult
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.permissions.models.Group
import it.ethereallabs.etherealperms.permissions.models.User
import java.util.UUID

interface IStorageMethod {
    suspend fun loadUser(uuid: UUID): User?
    suspend fun saveUser(user: User): Any
    suspend fun loadAllUsers(): List<User>
    suspend fun loadGroups(): MutableMap<String, Group>
    suspend fun saveGroups(groups: Map<String, Group>)
    suspend fun loadDefaultGroup(): Group
}
