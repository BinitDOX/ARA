package com.dox.ara.ui.navigation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dox.ara.ui.data.RouteItem
import com.dox.ara.ui.data.navItems
import com.dox.ara.ui.screen.AssistantScreen
import com.dox.ara.ui.screen.ChatScreen
import com.dox.ara.ui.screen.HomeScreen
import com.dox.ara.ui.screen.SettingsScreen
import com.dox.ara.ui.theme.ARATheme
import com.dox.ara.utility.Constants.START_PAGE
import com.dox.ara.utility.Constants.START_ROUTE

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainNavigation(
    startRoute: String? = START_ROUTE,
    startPage: Int? = START_PAGE,
    navigateTo: String? = null,
) {
    val navController = rememberNavController()
    val pageController = rememberPagerState(startPage ?: START_PAGE) { navItems.size }

    LaunchedEffect(Unit) {
        if(navigateTo != null){
            navController.navigate(navigateTo)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        NavHost(
            navController = navController,
            startDestination = startRoute ?: START_ROUTE,
        ) {
            composable(RouteItem.Home.route) {
                HomeScreen(navController, pageController)
            }
            composable("${RouteItem.Assistant.route}/{${RouteItem.Assistant.arguments.first().name}}",
                arguments = RouteItem.Assistant.arguments) {
                AssistantScreen(navController)
            }
            composable(
                "${RouteItem.Chat.route}/{${RouteItem.Chat.arguments.first().name}}",
                arguments = RouteItem.Chat.arguments
            ) {
                ChatScreen(navController)
            }
            composable(RouteItem.Settings.route) {
                SettingsScreen(navController)
            }
        }
    }
}


@Preview
@Composable
fun MainNavigationPreview() {
    ARATheme {
        MainNavigation()
    }
}