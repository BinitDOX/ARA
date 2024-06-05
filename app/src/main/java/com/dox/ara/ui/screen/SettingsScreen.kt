package com.dox.ara.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.dox.ara.R
import com.dox.ara.ui.component.ProfilePicture
import com.dox.ara.ui.component.SwipeButton
import com.dox.ara.ui.component.TopBar
import com.dox.ara.ui.theme.ARATheme
import com.dox.ara.viewmodel.SettingsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
) {
    val baseUrl by settingsViewModel.baseUrl.collectAsStateWithLifecycle()
    val paymentCode by settingsViewModel.paymentCode.collectAsStateWithLifecycle()
    val deviceUnlockCode by settingsViewModel.deviceUnlockCode.collectAsStateWithLifecycle()
    val assistantOpenTriggerSequence by settingsViewModel.assistantOpenTriggerSequence.collectAsStateWithLifecycle()
    val assistantListenTriggerSequence by settingsViewModel.assistantListenTriggerSequence.collectAsStateWithLifecycle()

    val isSaved by settingsViewModel.isSaved.collectAsStateWithLifecycle()

    val internalFieldSpacing = 8.dp
    val interFieldSpacing = 16.dp

    Scaffold(
        topBar = {
            TopBar(navController, R.string.screen_settings, true)
        }
    ) { innerPadding ->

        Surface(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(interFieldSpacing),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = baseUrl,
                    onValueChange = { settingsViewModel.setBaseUrl(it) },
                    label = { Text(stringResource(id = R.string.placeholder_base_url)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "This is the base backend server URL, which is required" +
                            " and must be set if the app was built with a different base URL",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(interFieldSpacing))

                TextField(
                    value = paymentCode,
                    onValueChange = { settingsViewModel.setPaymentCode(it) },
                    label = { Text(stringResource(id = R.string.placeholder_payment_code)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "This is an optional payment code that may be used with " +
                            "payment applications for automatic payment. The last confirmation of the " +
                            "payment will still need to be completed by the user",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(interFieldSpacing))

                TextField(
                    value = deviceUnlockCode,
                    onValueChange = { settingsViewModel.setDeviceUnlockCode(it) },
                    label = { Text(stringResource(id = R.string.placeholder_device_unlock_code)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "This is an optional device unlock code that may be used when " +
                            "unlocking the device is necessary, for performing certain actions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(interFieldSpacing))

                val isValidInput: (String) -> Boolean = { input ->
                    input.all { it == 'U' || it == 'D' }
                }

                TextField(
                    value = assistantOpenTriggerSequence,
                    onValueChange = { if(isValidInput(it)) {settingsViewModel.setAssistantOpenTriggerSequence(it)} },
                    label = { Text(stringResource(id = R.string.placeholder_assistant_open_trigger_sequence)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "This is an optional custom trigger sequence that can instantly " +
                            "open the default assistant chat using sequential volume button presses. " +
                            "The sequence must only include 'U' for volume up and 'D' for volume down.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(interFieldSpacing))

                TextField(
                    value = assistantListenTriggerSequence,
                    onValueChange = { if(isValidInput(it)) {settingsViewModel.setAssistantListenTriggerSequence(it)} },
                    label = { Text(stringResource(id = R.string.placeholder_assistant_listen_trigger_sequence)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "This is an optional custom trigger sequence that can instantly " +
                            "start listening to user voice input, using sequential volume button presses. " +
                            "The sequence must only include 'U' for volume up and 'D' for volume down.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(interFieldSpacing))


                SwipeButton(
                    text = stringResource(id = R.string.btn_save_settings),
                    isComplete = isSaved
                ) {
                    settingsViewModel.viewModelScope.launch {
                        settingsViewModel.saveSettings()
                        delay(500)
                        settingsViewModel.setIsSaved(true)
                        delay(1000)
                        navController.popBackStack()
                    }
                }
            }
        }
    }
}

@Composable
private fun AddProfilePicture(
    size: Dp,
    imageUri: String,
    setImageUri: (String) -> Unit
) {
    val context = LocalContext.current
    // TODO: Copy image to private storage, then use uri
    val launcher = rememberLauncherForActivityResult(
        contract =  ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            setImageUri(it.toString())
        }
    }

    Box(
        contentAlignment = Alignment.BottomEnd,
    ) {
        ProfilePicture(size = size, imageUri = imageUri)
        Icon(
            imageVector = Icons.Filled.Edit,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier
                .size(size / 4)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
                .clickable { launcher.launch(arrayOf("image/*")) }
            )
    }
}

@Preview
@Composable
private fun AddAssistantScreenPreview() {
    ARATheme {
        AddAssistantScreen(NavController(LocalContext.current))
    }
}