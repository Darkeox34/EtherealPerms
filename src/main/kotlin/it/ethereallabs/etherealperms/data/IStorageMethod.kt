package it.ethereallabs.etherealperms.data

import it.ethereallabs.etherealperms.permissions.models.Group
import it.ethereallabs.etherealperms.permissions.models.User
import java.util.UUID

interface IStorageMethod {
    fun loadUser(uuid: UUID): User?
    fun saveUser(user: User)
    fun loadAllUsers(): List<User>
    fun loadGroups(): MutableMap<String, Group>
    fun saveGroups(groups: Map<String, Group>)
    fun loadDefaultGroup(): Group

    fun sync()
    fun syncUpload()
}
