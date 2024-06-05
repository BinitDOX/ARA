package com.dox.ara.ui.component

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.dox.ara.R
import com.dox.ara.ui.data.ActionItem
import com.dox.ara.ui.data.OverflowMode
import com.dox.ara.ui.data.RouteItem
import com.dox.ara.ui.theme.ARATheme

@Composable
fun TopBar(
    navController: NavController,
    @StringRes title: Int,
    showBackButton: Boolean,
) {
    val actionItems = listOf(
        ActionItem(R.string.btn_settings, Icons.Outlined.Settings, OverflowMode.ALWAYS_OVERFLOW) {
            if (navController.currentDestination?.route != RouteItem.Settings.route)
                navController.navigate(RouteItem.Settings.route)
        },
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .background(MaterialTheme.colorScheme.tertiary)
            .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
    ) {

        if(showBackButton){
            IconButton(onClick = { navController.popBackStack() } ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(id = R.string.cd_btn_back),
                    tint = MaterialTheme.colorScheme.onTertiary,
                )
            }
        }

        Text(
            text = stringResource(id = title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onTertiary
        )

        Spacer(modifier = Modifier.weight(1f))

        Row {
            ActionMenu(
                items = actionItems,
                numIcons = 2,
            )
        }
    }
}


@Preview
@Composable
fun TopBarPreview() {
    ARATheme {
        TopBar(
            NavHostController(LocalContext.current),
            R.string.page_chats,
            true
        )
    }
}