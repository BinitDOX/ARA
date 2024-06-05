package com.dox.ara.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.dox.ara.R
import com.dox.ara.ui.theme.ARATheme

@Composable
fun ProfilePicture(
    imageUri: String? = null,
    size: Dp,
) {
    val painter = rememberAsyncImagePainter(
        ImageRequest.Builder(LocalContext.current)
            .data(imageUri)
            .crossfade(true)
            .build()
    )

    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painter,
            contentDescription = stringResource(id = R.string.cd_img_profile),
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentScale = ContentScale.Crop,
        )

        if (imageUri.isNullOrBlank() ||
            painter.state is AsyncImagePainter.State.Error ||
            painter.state is AsyncImagePainter.State.Empty ||
            painter.state is AsyncImagePainter.State.Loading
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = stringResource(id = R.string.cd_icon_profile),
                modifier = Modifier.size(size),
                tint = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}


@Preview
@Composable
fun ProfilePicturePreview() {
    ARATheme {
        ProfilePicture(size = 32.dp)
    }
}
