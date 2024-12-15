package com.dox.ara.ui.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dox.ara.R
import com.dox.ara.ui.component.DatePicker
import com.dox.ara.ui.theme.ARATheme
import com.dox.ara.viewmodel.LogsViewModel

@Composable
fun LogsPage(
    viewModel: LogsViewModel = hiltViewModel()
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val logs by viewModel.logs.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 0.dp, bottom = 8.dp, start = 8.dp, end = 8.dp),
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 0.dp, bottom = 8.dp, start = 0.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    modifier = Modifier.padding(0.dp),
                    onClick = { viewModel.refresh() }) {
                    Icon(Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.cd_btn_refresh),
                        modifier = Modifier.size(34.dp),
                        tint = MaterialTheme.colorScheme.primary)
                }

                Text(
                    selectedDate,
                    style = MaterialTheme.typography.titleMedium
                )

                DatePicker(selectedDate) { date ->
                    viewModel.setSelectedDate(date)
                }
            }
        }

        items(logs.reversed()) { log ->
            Text(
                text = log,
                style = MaterialTheme.typography.bodySmall,
                color = when {
                    log.contains("[ERROR]") -> Color(0xFFCE0025)
                    log.contains("[WARN]") -> Color(0xFFFA8A18)
                    log.contains("[INFO]") -> Color(0xFF009688)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Preview
@Composable
private fun LogsPagePreview() {
    ARATheme {
        LogsPage()
    }
}