package com.prammmoe.pictrim.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.prammmoe.pictrim.domain.model.EditorMode
import com.prammmoe.pictrim.domain.model.OutputFormat
import com.prammmoe.pictrim.domain.model.CropAspectRatio
import com.prammmoe.pictrim.ui.common.MetadataInfoButton
import com.prammmoe.pictrim.ui.common.PicTrimCard
import com.prammmoe.pictrim.ui.common.PicTrimScreenPadding
import com.prammmoe.pictrim.ui.common.PicTrimTopBar
import com.prammmoe.pictrim.ui.common.PrimaryButton
import com.prammmoe.pictrim.ui.common.PicTrimFilterChip
import com.prammmoe.pictrim.ui.common.PicTrimSlider
import com.prammmoe.pictrim.ui.common.PicTrimSwitch
import com.prammmoe.pictrim.R
import androidx.compose.ui.res.stringResource

@Composable
fun EditorScreen(state: EditorUiState, onBack: () -> Unit, onProcess: () -> Unit, onQuality: (Int) -> Unit, onTarget: (String) -> Unit, onFormat: (OutputFormat) -> Unit, onWidth: (String) -> Unit, onHeight: (String) -> Unit, onPercent: (Int) -> Unit, onKeep: (Boolean) -> Unit, onCrop: (CropAspectRatio) -> Unit, onRemoveMetadata: (Boolean) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        PicTrimTopBar(stringResource(if (state.mode == EditorMode.RESIZE) R.string.editor_resize_title else R.string.editor_process_title), onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = PicTrimScreenPadding), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            if (state.mode != EditorMode.RESIZE) PicTrimCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { CompressionSection(state, onQuality, onTarget) } }
            if (state.mode != EditorMode.COMPRESS) PicTrimCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { ResizeSection(state, onWidth, onHeight, onPercent, onKeep) } }
            PicTrimCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { Text(stringResource(R.string.editor_output), style = MaterialTheme.typography.titleMedium); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutputFormat.entries.forEach { format -> PicTrimFilterChip(state.outputFormat == format, format.label, { onFormat(format) }) } } } }
            PicTrimCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { Text(stringResource(R.string.editor_crop), style = MaterialTheme.typography.titleMedium); Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { CropAspectRatio.entries.forEach { crop -> PicTrimFilterChip(state.crop == crop, crop.label, { onCrop(crop) }) } } } }
            PicTrimCard(Modifier.fillMaxWidth()) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Text(stringResource(R.string.remove_metadata)); MetadataInfoButton(); Spacer(Modifier.weight(1f)); PicTrimSwitch(checked = state.removeMetadata, onCheckedChange = onRemoveMetadata) } }
            if (state.sourceFormat == OutputFormat.PNG && state.outputFormat == OutputFormat.JPEG) Text(stringResource(R.string.png_transparency_warning), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
        PrimaryButton(stringResource(R.string.editor_process), onProcess, enabled = !state.processing, modifier = Modifier.fillMaxWidth().padding(PicTrimScreenPadding))
    }
}

@Composable private fun CompressionSection(state: EditorUiState, onQuality: (Int) -> Unit, onTarget: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.editor_compression), style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf(90 to R.string.quality_high, 75 to R.string.quality_balanced, 50 to R.string.quality_small, 30 to R.string.quality_maximum).forEach { (value, label) -> PicTrimFilterChip(state.quality == value && state.targetKb.isBlank(), stringResource(label), { onQuality(value) }) } }
        Text(stringResource(R.string.quality_value, state.quality))
        PicTrimSlider(value = state.quality.toFloat(), onValueChange = { onQuality(it.toInt()) }, valueRange = 10f..95f)
        OutlinedTextField(value = state.targetKb, onValueChange = onTarget, modifier = Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.target_size)) }, supportingText = { Text(stringResource(R.string.target_size_supporting)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
    }
}

@Composable private fun ResizeSection(state: EditorUiState, onWidth: (String) -> Unit, onHeight: (String) -> Unit, onPercent: (Int) -> Unit, onKeep: (Boolean) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.editor_resize), style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf(25, 50, 75).forEach { value -> PicTrimFilterChip(state.percentage == value, "$value%", { onPercent(value) }) } }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedTextField(value = state.width, onValueChange = onWidth, modifier = Modifier.weight(1f), label = { Text(stringResource(R.string.width)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)); OutlinedTextField(value = state.height, onValueChange = onHeight, modifier = Modifier.weight(1f), label = { Text(stringResource(R.string.height)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)) }
        Row(verticalAlignment = Alignment.CenterVertically) { Text(stringResource(R.string.keep_aspect), Modifier.weight(1f)); PicTrimSwitch(checked = state.keepAspectRatio, onCheckedChange = onKeep) }
    }
}
