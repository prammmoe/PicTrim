package com.prammmoe.pictrim.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun MetadataInfoButton() {
    var open by remember { mutableStateOf(false) }
    IconButton(onClick = { open = true }) {
        Icon(Icons.Outlined.Info, contentDescription = "About removing metadata")
    }
    if (open) {
        AlertDialog(
            onDismissRequest = { open = false },
            title = { Text("Remove metadata") },
            text = { Text("Removes hidden information from the exported image, such as GPS/location, camera model, capture date, and other EXIF data. Your visible photo pixels are not changed.") },
            confirmButton = { TextButton(onClick = { open = false }) { Text("Got it") } }
        )
    }
}
