package com.dox.ara.ui.data

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class RouteItem(val route: String, val arguments: List<NamedNavArgument> = listOf()) {
    data object Home : RouteItem("home")
    data object AddAssistant : RouteItem("add_assistant")
    data object Chat : RouteItem("chat", listOf(navArgument("chatId") { type = NavType.LongType }))
    data object Settings : RouteItem("settings")
}

val routeItems = listOf(
    RouteItem.Home,
    RouteItem.AddAssistant,
    RouteItem.Chat,
    RouteItem.Settings
)