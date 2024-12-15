package com.dox.ara.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlarm
import androidx.compose.material.icons.filled.PersonAddAlt
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.dox.ara.R
import com.dox.ara.ui.component.BottomBar
import com.dox.ara.ui.component.FloatingActionButton
import com.dox.ara.ui.component.TopBar
import com.dox.ara.ui.data.RouteItem
import com.dox.ara.ui.data.navItems
import com.dox.ara.ui.page.AlarmsPage
import com.dox.ara.ui.page.ChatsPage
import com.dox.ara.ui.page.LogsPage
import com.dox.ara.ui.theme.ARATheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(navController: NavController, pageController: PagerState) {
    val showAlarmDialog = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            when(pageController.currentPage) {
                0 -> TopBar(navController, R.string.page_chats, false)
                1 -> TopBar(navController, R.string.page_logs, false)
                2 -> TopBar(navController, R.string.page_reminders, false)
            }
        },
        bottomBar = {
            BottomBar(navController, pageController)
        },
        floatingActionButton = {
            when(pageController.currentPage) {
                0 -> FloatingActionButton(Icons.Filled.PersonAddAlt) {
                    navController.navigate("${RouteItem.Assistant.route}/-1")
                }
                1 -> {}
                2 -> FloatingActionButton(Icons.Filled.AddAlarm) {
                    showAlarmDialog.value = true
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pageController,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) { page ->
            when (page) {
                0 -> ChatsPage(navController)
                1 -> LogsPage()
                2 -> AlarmsPage(showAlarmDialog)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
fun HomeScreenPreview() {
    ARATheme {
        HomeScreen(
            rememberNavController(),
            rememberPagerState(0) { navItems.size }
        )
    }
}