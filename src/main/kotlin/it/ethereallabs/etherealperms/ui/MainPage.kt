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
import io.sentry.Breadcrumb.user
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.permissions.models.Node
import kotlinx.coroutines.launch
import java.util.UUID

class MainPage(playerRef: PlayerRef, lifetime: CustomPageLifetime) :
    InteractiveCustomUIPage<EditorEventData>(playerRef, lifetime, EditorEventData.CODEC) {

    private val MAIN_UI = "Pages/MainPage.ui"
    private val COMPONENT_GROUP_ROW = "Pages/GroupListEntry.ui"
    private val COMPONENT_USER_ROW = "Pages/UserListEntry.ui"
    private val COMPONENT_PERM_ROW = "Pages/PermissionEntry.ui"

    private var currentSelectionType: String? = null
    private var currentSelectionName: String? = null

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

        events.addEventBinding(CustomUIEventBindingType.Activating, "#OpenCreateGroupBtn", createData("SHOW_CREATE_POPUP"))
        events.addEventBinding(CustomUIEventBindingType.Activating, "#CancelCreateGroup", createData("HIDE_CREATE_POPUP"))

        events.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#ConfirmCreateGroup",
            com.hypixel.hytale.server.core.ui.builder.EventData()
                .append("Action", "CREATE_GROUP")
                .append("TargetName", "#NewGroupNameInput.Value"),
            false
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
            "LIST_GROUPS" -> loadGroupsList(update, events)
            "LIST_USERS" -> loadUsersList(update, events)

            "SHOW_CREATE_POPUP" -> {
                update.set("#MainContainer.Visible", false)
                update.set("#CreateGroupPopup.Visible", true)
                update.set("#NewGroupNameInput.Value", "")
            }
            "HIDE_CREATE_POPUP" -> {
                update.set("#CreateGroupPopup.Visible", false)
                update.set("#MainContainer.Visible", true)
            }
            "CREATE_GROUP" -> {
                val newGroupName = data.targetName
                handleCreateGroup(newGroupName, update, events)
            }

            "SELECT" -> loadSubjectDetails(data.targetName, data.targetType, update, events)
            "DELETE_GROUP" -> handleDeleteGroup(data.targetName, update, events)
            "ADD_PERMISSION" -> handleAddPermission(update, events)
            "DELETE_PERMISSION" -> handleDeletePermission(data.targetName, update, events)
            "QUIT" -> this.close()
            "SAVE" -> handleSave(update)
        }
        sendUpdate(update, events, false)
    }

    private fun handleAddPermission(builder: UICommandBuilder, events: UIEventBuilder) {
        val name = currentSelectionName ?: return
        val type = currentSelectionType ?: return
        if (type == "group") {
            val group = EtherealPerms.permissionManager.getGroup(name)
            if (group != null) {
                group.nodes.add(Node("new.permission", true))
                loadSubjectDetails(name, type, builder, events)
            }
        }
        else{
            val user = EtherealPerms.permissionManager.getUser(UUID.fromString(name))
            if(user != null){
                user.nodes.add(Node("new.permission", true))
                loadSubjectDetails(name, type, builder, events)
            }
        }
    }

    private fun handleDeletePermission(permKey: String, builder: UICommandBuilder, events: UIEventBuilder) {
        val name = currentSelectionName ?: return
        val type = currentSelectionType ?: return
        if (type == "group") {
            val group = EtherealPerms.permissionManager.getGroup(name)
            group?.nodes?.removeIf { it.key == permKey }
            loadSubjectDetails(name, type, builder, events)
        }
        else{
            val user = EtherealPerms.permissionManager.getUser(UUID.fromString(name))
            user?.nodes?.removeIf { it.key == permKey }
            loadSubjectDetails(name, type, builder, events)
        }
    }

    private fun loadGroupsList(builder: UICommandBuilder, events: UIEventBuilder) {
        builder.set("#GroupsButton.Disabled", true)
        builder.set("#UsersButton.Disabled", false)
        builder.set("#AddGroupContainer.Visible", true)
        builder.clear("#ElementList")

        val groups = EtherealPerms.permissionManager.getAllGroups()

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

            events.addEventBinding(
                CustomUIEventBindingType.Activating,
                "$rowSelector #EditBtn",
                createData("RENAME_GROUP", group.name)
            )
        }
    }

    private fun loadUsersList(builder: UICommandBuilder, events: UIEventBuilder) {
        builder.set("#GroupsButton.Disabled", false)
        builder.set("#UsersButton.Disabled", true)
        builder.set("#AddGroupContainer.Visible", false)
        builder.clear("#ElementList")

        val users = EtherealPerms.permissionManager.getAllUsers()

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
        if (groupName.isBlank() || EtherealPerms.permissionManager.getGroup(groupName) != null) {
            return
        }

        EtherealPerms.storage.storageScope.launch {
            EtherealPerms.permissionManager.createGroup(groupName)
        }

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
        builder.set("#SubjectTypeLabel.Text", prefix)
        builder.set("#SelCatLabel.Text", name)

        events.addEventBinding(CustomUIEventBindingType.Activating, "#AddPermButton", createData("ADD_PERMISSION"))
        events.addEventBinding(CustomUIEventBindingType.Activating, "#SaveButton", createData("SAVE"))

        builder.clear("#PermissionsContent")

        val subject = if (type == "group") EtherealPerms.permissionManager.getGroup(name) else null
        val nodes = subject?.nodes ?: mutableSetOf()

        nodes.forEachIndexed { index, node ->
            builder.append("#PermissionsContent", COMPONENT_PERM_ROW)
            val rowSelector = "#PermissionsContent[$index]"
            builder.set("$rowSelector #Key.Value", node.key)
            builder.set("$rowSelector #Value.Value", node.value)
            events.addEventBinding(
                CustomUIEventBindingType.Activating,
                "$rowSelector #DeletePermBtn",
                createData("DELETE_PERMISSION", node.key)
            )
        }
    }

    private fun handleDeleteGroup(name: String, builder: UICommandBuilder, events: UIEventBuilder) {
        loadGroupsList(builder, events)
        if (currentSelectionName == name) {
            builder.set("#HeaderContainer.Visible", false)
            builder.set("#TableHeader.Visible", false)
            builder.set("#PermissionsContent.Visible", false)
        }
    }

    private fun handleSave(builder: UICommandBuilder) {}

    private fun createData(action: String, targetName: String = "", targetType: String = ""): com.hypixel.hytale.server.core.ui.builder.EventData {
        return com.hypixel.hytale.server.core.ui.builder.EventData()
            .append("Action", action)
            .append("TargetName", targetName)
            .append("TargetType", targetType)
    }
}