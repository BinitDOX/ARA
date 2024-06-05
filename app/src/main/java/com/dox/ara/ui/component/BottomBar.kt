package com.dox.ara.ui.component

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.dox.ara.ui.data.navItems
import com.dox.ara.ui.theme.ARATheme
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun BottomBar(navController: NavController, pageController: PagerState) {
    val scope = rememberCoroutineScope()

    NavigationBar (
        containerColor = MaterialTheme.colorScheme.tertiary,
    ){
        navItems.forEach { item ->
            val page = item.page
            val selected = pageController.currentPage == page
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        scope.launch {
                            pageController.animateScrollToPage(page)
                        }
                    }
                },
                label = {
                    Text(
                        text = stringResource(item.title),
                        style = MaterialTheme.typography.labelMedium.merge(
                            TextStyle(
                                color = if(selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                                        else MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.75f),
                                fontWeight = if(selected) FontWeight.Bold
                                             else FontWeight.SemiBold
                            )
                        )
                    )
                },
                icon = @Composable {
                    BadgedBox(
                        badge = {
                            if (item.badgeCount != null) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ) {
                                    Text(
                                        text = item.badgeCount.toString(),
                                        style = MaterialTheme.typography.labelSmall.merge(
                                            TextStyle(color = MaterialTheme.colorScheme.onPrimary)
                                        )

                                    )
                                }
                            } else if (item.hasNews) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (selected) item.selectedIcon
                            else item.unselectedIcon,
                            contentDescription = stringResource(id = item.title)
                        )
                    }
                }
            )
        }
    }

    BackHandler {
        if(pageController.currentPage != 0){
            scope.launch {
                pageController.animateScrollToPage(0)
            }
        } else {
            navController.popBackStack()
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
fun BottomBarPreview() {
    ARATheme {
        BottomBar(
            NavHostController(LocalContext.current),
            rememberPagerState(0) { navItems.size }
        )
    }
}