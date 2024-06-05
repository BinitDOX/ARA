package com.dox.ara.ui.data

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Description
import androidx.compose.ui.graphics.vector.ImageVector
import com.dox.ara.R

sealed class NavItem (
    @StringRes val title: Int,
    val page: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val hasNews: Boolean,
    val badgeCount: Int? = null
) {
    data object Chats : NavItem(
        title = R.string.page_chats,
        page = 0,
        selectedIcon = Icons.Filled.Chat,
        unselectedIcon = Icons.Outlined.Chat,
        hasNews = false
    )

    data object Logs : NavItem(
        title = R.string.page_logs,
        page = 1,
        selectedIcon = Icons.Filled.Description,
        unselectedIcon = Icons.Outlined.Description,
        hasNews = false
    )

    data object Reminders : NavItem(
        title = R.string.page_reminders,
        page = 2,
        selectedIcon = Icons.Filled.Alarm,
        unselectedIcon = Icons.Outlined.Alarm,
        hasNews = false
    )
}

val navItems = listOf(
    NavItem.Chats,
    NavItem.Logs,
    NavItem.Reminders
)