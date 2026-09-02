package com.prammmoe.pictrim.ui.batch

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.prammmoe.pictrim.domain.model.CropAspectRatio
import com.prammmoe.pictrim.domain.model.OutputFormat
import com.prammmoe.pictrim.ui.common.ImagePreview
import com.prammmoe.pictrim.ui.common.MetadataInfoButton
import androidx.core.net.toUri
import com.prammmoe.pictrim.ui.common.PicTrimCard
import com.prammmoe.pictrim.ui.common.PicTrimScreenPadding
import com.prammmoe.pictrim.ui.common.PicTrimTopBar
import com.prammmoe.pictrim.ui.common.PrimaryButton
import com.prammmoe.pictrim.ui.common.SecondaryButton
import com.prammmoe.pictrim.ui.common.PicTrimFilterChip
import com.prammmoe.pictrim.ui.common.PicTrimSlider
import com.prammmoe.pictrim.ui.common.PicTrimSwitch
import com.prammmoe.pictrim.R
import androidx.compose.ui.res.stringResource

@Composable fun BatchEditorScreen(state: BatchEditorUiState, onBack: () -> Unit, onSelect: () -> Unit, onStart: () -> Unit, onQuality: (Int) -> Unit, onTarget: (String) -> Unit, onWidth: (String) -> Unit, onHeight: (String) -> Unit, onFormat: (OutputFormat) -> Unit, onCrop: (CropAspectRatio) -> Unit, onRemoveMetadata: (Boolean) -> Unit) = Column(Modifier.fillMaxSize()) {
    PicTrimTopBar(stringResource(R.string.batch_processing), onBack)
    Column(Modifier.weight(1f).padding(horizontal = PicTrimScreenPadding).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
    SecondaryButton(stringResource(R.string.batch_choose), onSelect, Modifier.fillMaxWidth())
    PicTrimCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text(stringResource(R.string.editor_compression), style = MaterialTheme.typography.titleMedium); Text(stringResource(R.string.quality_value, state.quality)); PicTrimSlider(state.quality.toFloat(), { onQuality(it.toInt()) }, valueRange = 10f..95f); OutlinedTextField(state.targetKb, onTarget, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.target_size)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)) } }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedTextField(state.width, onWidth, Modifier.weight(1f), label = { Text("Width") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)); OutlinedTextField(state.height, onHeight, Modifier.weight(1f), label = { Text("Height") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)) }
    PicTrimCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { Text(stringResource(R.string.editor_output)); Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { OutputFormat.entries.forEach { PicTrimFilterChip(state.outputFormat == it, it.label, { onFormat(it) }) } }; Text(stringResource(R.string.editor_crop)); Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { CropAspectRatio.entries.forEach { PicTrimFilterChip(state.crop == it, it.label, { onCrop(it) }) } } } }
    PicTrimCard(Modifier.fillMaxWidth()) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Text(stringResource(R.string.remove_metadata)); MetadataInfoButton(); Spacer(Modifier.weight(1f)); PicTrimSwitch(state.removeMetadata, onRemoveMetadata) } }
    state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }; PrimaryButton(stringResource(R.string.batch_process, state.images.size), onStart, enabled = state.images.isNotEmpty() && !state.processing, modifier = Modifier.fillMaxWidth())
    }
}

@Composable fun BatchResultScreen(state: BatchResultUiState, onBack: () -> Unit, onSaveAll: () -> Unit, onOpenGallery: (String) -> Unit) = Column(Modifier.fillMaxSize()) { PicTrimTopBar(stringResource(R.string.batch_result), onBack); Column(Modifier.weight(1f).padding(horizontal = PicTrimScreenPadding).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) { val job = state.job; if (job == null) CircularProgressIndicator() else { val done = job.items.count { it.status != com.prammmoe.pictrim.domain.model.BatchItemStatus.PENDING && it.status != com.prammmoe.pictrim.domain.model.BatchItemStatus.PROCESSING }; Text(stringResource(R.string.processed_count, done, job.items.size), style = MaterialTheme.typography.titleLarge); job.items.forEach { item -> PicTrimCard(Modifier.fillMaxWidth()) { Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) { item.outputUri?.let { ImagePreview(
    it.toUri(), Modifier.size(72.dp)) }; Column(Modifier.weight(1f)) { Text(item.displayName); Text(item.error ?: item.status.name, color = if (item.error == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error); item.savedUri?.let { saved -> TextButton(onClick = { onOpenGallery(saved) }) { Text(stringResource(R.string.open_gallery)) } } } } } }; PrimaryButton(if (state.savedCount > 0) stringResource(R.string.saved_count, state.savedCount) else stringResource(R.string.save_all), onSaveAll, enabled = job.items.any { it.outputUri != null && it.savedUri == null }, modifier = Modifier.fillMaxWidth()) } } }
