package com.dox.ara.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.dox.ara.manager.PermissionManager
import com.dox.ara.ui.navigation.MainNavigation
import com.dox.ara.ui.theme.ARATheme
import com.dox.ara.utility.Constants.NAVIGATE_TO_EXTRA
import com.dox.ara.utility.Constants.START_PAGE
import com.dox.ara.utility.Constants.START_PAGE_EXTRA
import com.dox.ara.utility.Constants.START_ROUTE_EXTRA
import com.dox.ara.utility.Constants.SpecialPermission.ACCESSIBILITY
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var permissionManager: PermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionManager.getPermissions(
            { Timber.d("[onCreate] Runtime permissions granted") },
            { Timber.w("[onCreate] Runtime permissions denied") }
        )

        if(!permissionManager.isAccessibilityPermissionGranted(this)){
            permissionManager.startSpecialPermissionActivity(this, ACCESSIBILITY)
            Toast.makeText(this, "Please grant the ${ACCESSIBILITY.value} permission.",
                Toast.LENGTH_SHORT).show()
        }

        val startRoute = intent.getStringExtra(START_ROUTE_EXTRA)
        val startPage = intent.getIntExtra(START_PAGE_EXTRA, START_PAGE)
        val navigateTo = intent.getStringExtra(NAVIGATE_TO_EXTRA)

        setContent {
            ARATheme {
                MainNavigation(startRoute, startPage, navigateTo)
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ARAPreview() {
    ARATheme {
        MainNavigation()
    }
}