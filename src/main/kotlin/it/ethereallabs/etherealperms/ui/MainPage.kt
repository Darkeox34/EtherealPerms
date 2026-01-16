package it.ethereallabs.etherealperms.ui

import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.permissions.models.Group
import it.ethereallabs.etherealperms.permissions.models.Node
import it.ethereallabs.etherealperms.permissions.models.User
import it.ethereallabs.etherealperms.ui.models.EditorEventData
import kotlinx.coroutines.launch
import java.util.UUID

class MainPage(playerRef: PlayerRef, lifetime: CustomPageLifetime) :
    InteractiveCustomUIPage<EditorEventData>(playerRef, lifetime, EditorEventData.CODEC) {

    private val MAIN_UI = "Pages/MainPage.ui"
    private val COMPONENT_GROUP_ROW = "Pages/GroupListEntry.ui"
    private val COMPONENT_USER_ROW = "Pages/UserListEntry.ui"
    private val COMPONENT_PERM_ROW = "Pages/PermissionEntry.ui"
    private val POPUP_DELETE_UI = "Pages/DeleteGroupPopup.ui"

    private var currentSelectionType: String? = null
    private var currentSelectionName: String? = null
    private var currentSearchQuery: String = ""
    private var currentViewMode: String = "GROUPS"
    private var groupPendingDeletion: String? = null

    private var tempNewGroupName: String = ""
    private var currentEditingNodes: MutableList<Node> = mutableListOf()

    private val localGroups = mutableMapOf<String, Group>()
    private val localUsers = mutableMapOf<UUID, User>()
    private val deletedGroups = mutableSetOf<String>()

    init {

        EtherealPerms.permissionManager.getAllGroups().forEach { group ->
            localGroups[group.name] = group.copy(nodes = group.nodes.map { it.copy() }.toMutableSet())
        }

        EtherealPerms.permissionManager.getAllUsers().forEach { playerRef ->
            val user = EtherealPerms.permissionManager.getUser(playerRef.uuid)
            if (user != null) {
                localUsers[user.uuid] = user.copy(nodes = user.nodes.map { it.copy() }.toMutableSet())
            }
        }
    }

    override fun build(
        ref: Ref<EntityStore>,
        commands: UICommandBuilder,
        events: UIEventBuilder,
        store: Store<EntityStore>
    ) {
        commands.append(MAIN_UI)

        commands.set("#HeaderContainer.Visible", false)
        commands.set("#TableHeader.Visible", false)
        commands.set("#PermissionsContent.Visible", false)
        commands.set("#AddGroupContainer.Visible", false)
        commands.set("#CreateGroupPopup.Visible", false)

        events.addEventBinding(CustomUIEventBindingType.Activating, "#GroupsButton", createData("LIST_GROUPS"))
        events.addEventBinding(CustomUIEventBindingType.Activating, "#UsersButton", createData("LIST_USERS"))
        events.addEventBinding(CustomUIEventBindingType.Activating, "#ExitButton", createData("QUIT"))

        events.addEventBinding(CustomUIEventBindingType.Activating, "#SaveButton", createData("SAVE"))

        events.addEventBinding(CustomUIEventBindingType.Activating, "#OpenCreateGroupBtn", createData("SHOW_CREATE_POPUP"))
        events.addEventBinding(CustomUIEventBindingType.Activating, "#CancelCreateGroup", createData("HIDE_CREATE_POPUP"))

        events.addEventBinding(
            CustomUIEventBindingType.ValueChanged,
            "#SearchTextField",
            createData("SEARCH").append("@Value", "#SearchTextField.Value")
        )

        events.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#ConfirmCreateGroup",
            createData("CREATE_GROUP")
                .append("@Value", "#NewGroupNameInput.Value")
        )
    }

    override fun handleDataEvent(
        ref: Ref<EntityStore>,
        store: Store<EntityStore>,
        data: EditorEventData
    ) {
        val update = UICommandBuilder()
        val events = UIEventBuilder()

        when (data.action) {
            "LIST_GROUPS" -> {
                currentViewMode = "GROUPS"
                resetSearch(update)
                loadGroupsList(update, events)
            }

            "LIST_USERS" -> {
                currentViewMode = "USERS"
                resetSearch(update)
                loadUsersList(update, events)
            }

            "SEARCH" -> {
                currentSearchQuery = data.value.trim().lowercase()
                if (currentViewMode == "GROUPS") {
                    loadGroupsList(update, events)
                } else {
                    loadUsersList(update, events)
                }
            }

            "SHOW_CREATE_POPUP" -> {
                update.set("#MainContainer.Visible", false)
                update.set("#CreateGroupPopup.Visible", true)
                update.set("#NewGroupNameInput.Value", "")
                tempNewGroupName = ""
            }
            "HIDE_CREATE_POPUP" -> {
                update.set("#CreateGroupPopup.Visible", false)
                update.set("#MainContainer.Visible", true)
            }
            "UPDATE_INPUT_NAME" -> {
                tempNewGroupName = data.value
            }
            "CREATE_GROUP" -> {
                handleCreateGroup(data.value, update, events)
            }

            "CONFIRM_DELETE" -> {
                val groupName = groupPendingDeletion
                if (groupName != null) {
                    if (localGroups.containsKey(groupName)) {
                        deletedGroups.add(groupName)
                    }
                    if (currentSelectionName == groupName) {
                        update.set("#HeaderContainer.Visible", false)
                        update.set("#TableHeader.Visible", false)
                        update.set("#PermissionsContent.Visible", false)
                        currentSelectionName = null
                    }
                    loadGroupsList(update, events)
                }
                closeDeletePopup(update)
            }
            "CANCEL_DELETE" -> closeDeletePopup(update)
            "SELECT" -> loadSubjectDetails(data.targetName, data.targetType, update, events)
            "DELETE_GROUP" -> openDeletePopup(data.targetName, update, events)
            "ADD_PERMISSION" -> handleAddPermission(update, events)
            "DELETE_PERMISSION" -> handleDeletePermission(data.targetName, update, events)
            "UPDATE_PERM_KEY" -> handleUpdatePermKey(data.targetName, data.value)
            "UPDATE_PERM_VALUE" -> handleUpdatePermValue(data.targetName, data.booleanValue)
            "QUIT" -> this.close()
            "SAVE" -> handleSave(update)
        }
        sendUpdate(update, events, false)
    }

    private fun openDeletePopup(groupName: String, builder: UICommandBuilder, events: UIEventBuilder) {
        groupPendingDeletion = groupName

        builder.append(POPUP_DELETE_UI)

        builder.set("#DeleteMessage.Text", "Do you want to delete group $groupName?")

        events.addEventBinding(CustomUIEventBindingType.Activating, "#ConfirmDeleteBtn", createData("CONFIRM_DELETE"))
        events.addEventBinding(CustomUIEventBindingType.Activating, "#CancelDeleteBtn", createData("CANCEL_DELETE"))
    }

    private fun closeDeletePopup(builder: UICommandBuilder) {
        groupPendingDeletion = null

        builder.remove("#DeleteGroupOverlay")
    }

    private fun resetSearch(builder: UICommandBuilder) {
        currentSearchQuery = ""
        builder.set("#SearchTextField.Value", "")
    }

    private fun handleUpdatePermKey(indexStr: String, newValue: String) {
        val index = indexStr.toIntOrNull() ?: return
        if (index in currentEditingNodes.indices) {
            currentEditingNodes[index] = currentEditingNodes[index].copy(key = newValue)
            syncToLocal()
        }
    }

    private fun handleUpdatePermValue(indexStr: String, newValue: Boolean) {
        val index = indexStr.toIntOrNull() ?: return
        if (index in currentEditingNodes.indices) {

            currentEditingNodes[index] = currentEditingNodes[index].copy(value = newValue)
            syncToLocal()
        }
    }

    private fun syncToLocal() {
        val name = currentSelectionName ?: return
        val type = currentSelectionType ?: return
        val nodes = currentEditingNodes.toMutableSet()

        if (type == "group") {
            localGroups[name]?.nodes?.apply {
                clear()
                addAll(nodes)
            }
        } else {
            val uuid = try { UUID.fromString(name) } catch (e: Exception) { return }
            localUsers[uuid]?.nodes?.apply {
                clear()
                addAll(nodes)
            }
        }
    }

    private fun handleAddPermission(builder: UICommandBuilder, events: UIEventBuilder) {
        val name = currentSelectionName ?: return
        val type = currentSelectionType ?: return
        
        if (type == "group") {
            val group = localGroups[name]
            if (group != null) {
                group.nodes.add(Node("new.permission", true))
                loadSubjectDetails(name, type, builder, events)
            }
        } else {
            val uuid = try { UUID.fromString(name) } catch (e: Exception) { return }
            val user = localUsers[uuid]
            if (user != null) {
                user.nodes.add(Node("new.permission", true))
                loadSubjectDetails(name, type, builder, events)
            }
        }
    }

    private fun handleDeletePermission(indexStr: String, builder: UICommandBuilder, events: UIEventBuilder) {
        val index = indexStr.toIntOrNull() ?: return
        if (index in currentEditingNodes.indices) {
            currentEditingNodes.removeAt(index)
            syncToLocal()
            loadSubjectDetails(currentSelectionName!!, currentSelectionType!!, builder, events)
        }
    }

    private fun loadGroupsList(builder: UICommandBuilder, events: UIEventBuilder) {
        builder.set("#GroupsButton.Disabled", true)
        builder.set("#UsersButton.Disabled", false)
        builder.set("#AddGroupContainer.Visible", true)
        builder.clear("#ElementList")

        val groups = localGroups.values
            .filter { !deletedGroups.contains(it.name) }
            .filter { it.name.contains(currentSearchQuery, ignoreCase = true) }
            .sortedBy { it.name }

        groups.forEachIndexed { index, group ->
            builder.append("#ElementList", COMPONENT_GROUP_ROW)
            val rowSelector = "#ElementList[$index]"

            builder.set("$rowSelector #Name.Text", group.name)

            events.addEventBinding(
                CustomUIEventBindingType.Activating,
                rowSelector,
                createData("SELECT", group.name, "group")
            )

            events.addEventBinding(
                CustomUIEventBindingType.Activating,
                "$rowSelector #DeleteBtn",
                createData("DELETE_GROUP", group.name)
            )
        }
    }

    private fun loadUsersList(builder: UICommandBuilder, events: UIEventBuilder) {
        builder.set("#GroupsButton.Disabled", false)
        builder.set("#UsersButton.Disabled", true)
        builder.set("#AddGroupContainer.Visible", false)
        builder.clear("#ElementList")

        val users = localUsers.values
            .filter { it.username.contains(currentSearchQuery, ignoreCase = true) }
            .sortedBy { it.username }

        users.forEachIndexed { index, user ->
            builder.append("#ElementList", COMPONENT_USER_ROW)
            val rowSelector = "#ElementList[$index]"
            builder.set("$rowSelector #Name.Text", user.username)
            events.addEventBinding(
                CustomUIEventBindingType.Activating,
                rowSelector,
                createData("SELECT", user.uuid.toString(), "user")
            )
        }
    }

    private fun handleCreateGroup(groupName: String, builder: UICommandBuilder, events: UIEventBuilder) {
        if (groupName.isBlank() || localGroups.containsKey(groupName.lowercase())) {
            return
        }

        val newGroup = Group(groupName.lowercase())
        localGroups[newGroup.name] = newGroup
        

        deletedGroups.remove(newGroup.name)

        builder.set("#CreateGroupPopup.Visible", false)
        builder.set("#MainContainer.Visible", true)

        loadGroupsList(builder, events)
    }

    private fun loadSubjectDetails(name: String, type: String, builder: UICommandBuilder, events: UIEventBuilder) {
        currentSelectionName = name
        currentSelectionType = type

        builder.set("#HeaderContainer.Visible", true)
        builder.set("#TableHeader.Visible", true)
        builder.set("#PermissionsContent.Visible", true)

        val prefix = if (type == "group") "Group: " else "User: "

        val displayName = if (type == "group") {
            name
        } else {
             try {
                localUsers[UUID.fromString(name)]?.username ?: name
             } catch(e: Exception) { name }
        }

        builder.set("#SubjectTypeLabel.Text", prefix)
        builder.set("#SelCatLabel.Text", displayName)

        events.addEventBinding(CustomUIEventBindingType.Activating, "#AddPermButton", createData("ADD_PERMISSION"))

        builder.clear("#PermissionsContent")

        val nodesSet = if (type == "group") {
            localGroups[name]?.nodes
        } else {
             try { localUsers[UUID.fromString(name)]?.nodes } catch(e: Exception) { null }
        } ?: mutableSetOf()

        currentEditingNodes.clear()
        currentEditingNodes.addAll(nodesSet)

        currentEditingNodes.forEachIndexed { index, node ->
            builder.append("#PermissionsContent", COMPONENT_PERM_ROW)
            val rowSelector = "#PermissionsContent[$index]"
            builder.set("$rowSelector #Key.Value", node.key)
            builder.set("$rowSelector #Value.Value", node.value)

            events.addEventBinding(
                CustomUIEventBindingType.ValueChanged,
                "$rowSelector #Key",
                createData("UPDATE_PERM_KEY", index.toString())
                    .append("@Value", "$rowSelector #Key.Value")
            )
            events.addEventBinding(
                CustomUIEventBindingType.ValueChanged,
                "$rowSelector #Value",
                createData("UPDATE_PERM_VALUE", index.toString())
                    .append("@BooleanValue", "$rowSelector #Value.Value") // <--- Usa la nuova chiave booleana
            )

            events.addEventBinding(
                CustomUIEventBindingType.Activating,
                "$rowSelector #DeletePermBtn",
                createData("DELETE_PERMISSION", index.toString())
            )
        }
    }

    private fun handleDeleteGroup(name: String, builder: UICommandBuilder, events: UIEventBuilder) {
        if (localGroups.containsKey(name)) {
            deletedGroups.add(name)
        }
        
        if (currentSelectionName == name) {
            builder.set("#HeaderContainer.Visible", false)
            builder.set("#TableHeader.Visible", false)
            builder.set("#PermissionsContent.Visible", false)
            currentSelectionName = null
        }
        loadGroupsList(builder, events)
    }

    private fun handleSave(builder: UICommandBuilder) {
        builder.set("#SaveButton.Disabled", true)
        builder.set("#SaveButton.Text", "SAVING...")

        EtherealPerms.storage.storageScope.launch {
            try {
                deletedGroups.forEach { groupName ->
                    EtherealPerms.permissionManager.deleteGroup(groupName)
                }

                localGroups.values.forEach { localGroup ->
                    if (!deletedGroups.contains(localGroup.name)) {
                        val existingGroup = EtherealPerms.permissionManager.getGroup(localGroup.name)
                        if (existingGroup == null) {
                            EtherealPerms.permissionManager.createGroup(localGroup.name)
                            val createdGroup = EtherealPerms.permissionManager.getGroup(localGroup.name)
                            createdGroup?.nodes?.addAll(localGroup.nodes)
                        } else {
                            existingGroup.nodes.clear()
                            existingGroup.nodes.addAll(localGroup.nodes)
                        }
                    }
                }

                localUsers.values.forEach { localUser ->
                    val realUser = EtherealPerms.permissionManager.getUser(localUser.uuid)
                    if (realUser != null) {
                        realUser.nodes.clear()
                        realUser.nodes.addAll(localUser.nodes)
                    }
                }

                EtherealPerms.permissionManager.saveData()

                showNotification("Saved Successfully!", true)

            } catch (e: Exception) {
                e.printStackTrace()
                showNotification("Error while saving!", false)
            } finally {
                val resetBtn = UICommandBuilder()
                resetBtn.set("#SaveButton.Disabled", false)
                resetBtn.set("#SaveButton.Text", "SAVE")
                sendUpdate(resetBtn)
            }
        }
    }

    private suspend fun showNotification(message: String, isSuccess: Boolean) {
        val show = UICommandBuilder()

        show.set("#NotificationMsg.Text", message)

        val color = if (isSuccess) "#28a745" else "#dc3545"
        show.set("#NotificationPopup.Background", color)

        show.set("#NotificationPopup.Visible", true)

        sendUpdate(show)

        kotlinx.coroutines.delay(3000)

        val hide = UICommandBuilder()
        hide.set("#NotificationPopup.Visible", false)
        sendUpdate(hide)
    }

    private fun createData(action: String, targetName: String = "", targetType: String = ""): com.hypixel.hytale.server.core.ui.builder.EventData {
        return com.hypixel.hytale.server.core.ui.builder.EventData()
            .append("Action", action)
            .append("TargetName", targetName)
            .append("TargetType", targetType)
    }
}