package com.dox.ara.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAddAlt
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dox.ara.R
import com.dox.ara.ui.theme.ARATheme

@Composable
fun FloatingActionButton(
    imageVector: ImageVector,
    onClick: () -> Unit
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier.padding(16.dp)
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = stringResource(id = R.string.cd_fab_add_assistant),
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Preview
@Composable
fun FloatingActionButtonPreview() {
    ARATheme {
        FloatingActionButton(
            imageVector = Icons.Filled.PersonAddAlt,
            onClick = {}
        )
    }
}