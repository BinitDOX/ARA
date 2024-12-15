package com.dox.ara.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.core.text.isDigitsOnly
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.dox.ara.R
import com.dox.ara.ui.component.ColorPickerDialog
import com.dox.ara.ui.component.DropDownSelector
import com.dox.ara.ui.component.ProfilePicture
import com.dox.ara.ui.component.SwipeButton
import com.dox.ara.ui.component.TopBar
import com.dox.ara.ui.theme.ARATheme
import com.dox.ara.viewmodel.AssistantViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AssistantScreen(
    navController: NavController,
    assistantViewModel: AssistantViewModel = hiltViewModel()
) {
    val assistant by assistantViewModel.assistant.collectAsStateWithLifecycle()

    val name by assistantViewModel.name.collectAsStateWithLifecycle()
    val about by assistantViewModel.about.collectAsStateWithLifecycle()
    val color by assistantViewModel.color.collectAsStateWithLifecycle()
    val prompt by assistantViewModel.prompt.collectAsStateWithLifecycle()
    val imageUri by assistantViewModel.imageUri.collectAsStateWithLifecycle()
    val edgeVoice by assistantViewModel.edgeVoiceModel.collectAsStateWithLifecycle()
    val edgePitch by assistantViewModel.edgeVoicePitch.collectAsStateWithLifecycle()
    val rvcVoice by assistantViewModel.rvcVoiceModel.collectAsStateWithLifecycle()

    val availableEdgeVoiceModels by assistantViewModel.availableEdgeVoiceModels.collectAsStateWithLifecycle()
    val availableRvcVoiceModels by assistantViewModel.availableRvcVoiceModels.collectAsStateWithLifecycle()

    val isSaved by assistantViewModel.isSaved.collectAsStateWithLifecycle()

    var showColorPickerDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopBar(navController,
                if(assistant == null) R.string.screen_add_assistant
                else R.string.screen_update_assistant,
                true)
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
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AddProfilePicture(
                    size = 128.dp,
                    imageUri = imageUri,
                    setImageUri = assistantViewModel::setImageUri
                )

                Spacer(modifier = Modifier.height(32.dp))

                TextField(
                    value = name,
                    onValueChange = { assistantViewModel.setName(it) },
                    label = { Text(stringResource(id = R.string.placeholder_assistant_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = about,
                    onValueChange = { assistantViewModel.setAbout(it) },
                    label = { Text(stringResource(id = R.string.placeholder_assistant_about)) },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .height(50.dp)
                            .width(86.dp)
                            .border(width = 2.dp, color = MaterialTheme.colorScheme.outline)
                            .background(
                                try {
                                    Color(color.toColorInt())
                                } catch (e: Exception) {
                                    Color.Unspecified
                                }
                            )
                            .clickable { showColorPickerDialog = true }
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    TextField(
                        value = color,
                        onValueChange = { assistantViewModel.setColor(it) },
                        label = { Text(stringResource(id = R.string.placeholder_assistant_color)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (showColorPickerDialog) {
                    ColorPickerDialog(
                        onDismiss = { showColorPickerDialog = false },
                        onColorSelected = { color ->
                            assistantViewModel.setColor(color.toHexCodeWithAlpha())
                        },
                        selectedColor = try{Color(color.toColorInt())} catch (e: Exception) { Color.Unspecified }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                DropDownSelector(
                    label = stringResource(id = R.string.placeholder_assistant_edge_voice),
                    options = availableEdgeVoiceModels,
                    selectedOption = edgeVoice,
                    onOptionSelected = { assistantViewModel.setEdgeVoiceModel(it) },
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = edgePitch,
                    onValueChange = {
                        if (it.isDigitsOnly()) assistantViewModel.setEdgeVoicePitch(it)
                    },
                    label = { Text(stringResource(id = R.string.placeholder_assistant_edge_pitch)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(16.dp))

                DropDownSelector(
                    label = stringResource(id = R.string.placeholder_assistant_rvc_voice),
                    options = availableRvcVoiceModels,
                    selectedOption = rvcVoice,
                    onOptionSelected = { assistantViewModel.setRvcVoiceModel(it) },
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = prompt,
                    onValueChange = { assistantViewModel.setPrompt(it) },
                    label = { Text(stringResource(id = R.string.placeholder_assistant_prompt)) },
                    maxLines = 25,
                    minLines = 15,
                    modifier = Modifier
                        .fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                SwipeButton(
                    text = stringResource(id = if(assistant == null) R.string.btn_add_assistant
                        else R.string.btn_update_assistant),
                    isComplete = isSaved
                ) {
                    assistantViewModel.viewModelScope.launch {
                        assistantViewModel.createAssistant()
                        delay(500)
                        assistantViewModel.setIsSaved(true)
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
        Box (
            modifier = Modifier
                .size(size / 3)
                .border(width = 6.dp, color = MaterialTheme.colorScheme.tertiary, CircleShape)
                .padding(1.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .clickable { launcher.launch(arrayOf("image/*")) }
            )
        }
    }
}

fun Color.toHexCodeWithAlpha(): String {
    val alpha = this.alpha*255
    val red = this.red * 255
    val green = this.green * 255
    val blue = this.blue * 255
    return String.format("#%02x%02x%02x%02x", alpha.toInt(),red.toInt(), green.toInt(), blue.toInt())
}

@Preview
@Composable
private fun AssistantScreenPreview() {
    ARATheme {
        AssistantScreen(NavController(LocalContext.current))
    }
}