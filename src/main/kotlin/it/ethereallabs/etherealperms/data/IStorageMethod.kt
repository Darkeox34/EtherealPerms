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

    suspend fun sync(){
        if (this is FileStorage) {
            EtherealPerms.instance.logger.atWarning().log("Sync is not available for local file storage.")
            return
        }
        EtherealPerms.instance.logger.atInfo().log("Syncing data from database...")

        val fileStorage = FileStorage(EtherealPerms.instance)
        val groups = loadGroups()
        val users = loadAllUsers()

        if (groups.isEmpty() && users.isEmpty()) {
            EtherealPerms.instance.logger.atInfo().log("Database is empty, loading default data.")
            val defaultGroup = fileStorage.loadDefaultGroup()
            fileStorage.saveGroups(mapOf(defaultGroup.name to defaultGroup))
        } else {
            fileStorage.saveGroups(groups)
            users.forEach { fileStorage.saveUser(it) }
        }
        EtherealPerms.instance.logger.atInfo().log("Sync complete.")
    }
    suspend fun syncUpload(){
        if (this is FileStorage) {
            EtherealPerms.instance.logger.atWarning().log("Sync upload is not available for local file storage.")
            return
        }
        EtherealPerms.instance.logger.atInfo().log("Uploading local data to the database...")
        val fileStorage = FileStorage(EtherealPerms.instance)
        val groups = fileStorage.loadGroups()
        val users = fileStorage.loadAllUsers()

        saveGroups(groups)
        users.forEach { saveUser(it) }
        EtherealPerms.instance.logger.atInfo().log("Upload complete.")
    }
}
