package io.github.tutualican.tuyo.ui.components

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.tutualican.tuyo.ui.theme.LocalTuyoColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val MAX_IMAGE_EDGE = 1280

/**
 * Small local photo preview. The URI is opened with the app's persisted
 * read permission from the document picker; no network image loader is needed.
 */
@Composable
fun LocalImage(
    uriString: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    val context = LocalContext.current
    val colors = LocalTuyoColors.current
    val loaded by produceState<Pair<Boolean, ImageBitmap?>>(
        initialValue = false to null,
        key1 = uriString,
    ) {
        val bitmap = withContext(Dispatchers.IO) {
            runCatching { decodePreview(context, uriString) }.getOrNull()
        }
        value = true to bitmap
    }
    val image = loaded.second
    if (image != null) {
        Image(
            bitmap = image,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Crop,
        )
    } else {
        Box(
            modifier = modifier.background(colors.raised),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (loaded.first) "Fotoğraf açılamadı" else "Fotoğraf yükleniyor…",
                style = MaterialTheme.typography.labelSmall,
                color = colors.textTertiary,
                modifier = Modifier.size(width = 120.dp, height = 16.dp),
            )
        }
    }
}

private fun decodePreview(context: Context, uriString: String): ImageBitmap? {
    val uri = Uri.parse(uriString)
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it, null, bounds)
    }
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
    var sampleSize = 1
    while (
        bounds.outWidth / sampleSize > MAX_IMAGE_EDGE ||
        bounds.outHeight / sampleSize > MAX_IMAGE_EDGE
    ) {
        sampleSize *= 2
    }
    val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
    return context.contentResolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, options)?.asImageBitmap()
    }
}
