package com.prammmoe.pictrim.ui.common

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ImagePreview(uri: Uri, modifier: Modifier = Modifier) {
    AsyncImage(model = uri, contentDescription = null, modifier = modifier, contentScale = ContentScale.Fit)
}

fun Long.asFileSize(): String = when {
    this < 1024 -> "$this B"
    this < 1024 * 1024 -> String.format(Locale.getDefault(), "%.1f KB", this / 1024f)
    else -> String.format(Locale.getDefault(), "%.1f MB", this / 1024f / 1024f)
}

fun Int.asPercent(): String = "$this%"
