package com.prammmoe.pictrim.ui.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.prammmoe.pictrim.ui.common.ImagePreview
import com.prammmoe.pictrim.ui.common.asFileSize
import com.prammmoe.pictrim.ui.common.PicTrimCard
import com.prammmoe.pictrim.ui.common.PicTrimScreenPadding
import com.prammmoe.pictrim.ui.common.PicTrimTopBar
import com.prammmoe.pictrim.ui.common.PrimaryButton
import com.prammmoe.pictrim.ui.common.SecondaryButton
import com.prammmoe.pictrim.R
import androidx.compose.ui.res.stringResource

@Composable
fun ResultScreen(state: ResultUiState, onBack: () -> Unit, onSave: () -> Unit, onShowInGallery: () -> Unit, onShare: () -> Unit, onAnother: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        PicTrimTopBar(stringResource(R.string.result_title), onBack)
        if (state.loading) Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) { CircularProgressIndicator() } else if (state.error != null) Column(Modifier.fillMaxSize().padding(24.dp), Arrangement.Center) { Text(state.error, color = MaterialTheme.colorScheme.error) } else {
            val original = state.original!!; val output = state.result!!.info
            val savedPercent = if (original.sizeBytes > 0) ((1 - output.sizeBytes.toDouble() / original.sizeBytes) * 100).toInt() else 0
            Column(Modifier.fillMaxSize().padding(PicTrimScreenPadding), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) { PicTrimCard(Modifier.weight(1f)) { Column(Modifier.padding(10.dp)) { Text(stringResource(R.string.result_original), style = MaterialTheme.typography.titleMedium); ImagePreview(original.uri, Modifier.fillMaxWidth().height(160.dp)); Text("${original.sizeBytes.asFileSize()}\n${original.width} × ${original.height}", style = MaterialTheme.typography.bodyMedium) } }; PicTrimCard(Modifier.weight(1f)) { Column(Modifier.padding(10.dp)) { Text(stringResource(R.string.result_pictrim), style = MaterialTheme.typography.titleMedium); ImagePreview(output.uri, Modifier.fillMaxWidth().height(160.dp)); Text("${output.sizeBytes.asFileSize()}\n${output.width} × ${output.height}", style = MaterialTheme.typography.bodyMedium) } } }
                PicTrimCard(Modifier.fillMaxWidth()) { Text(if (savedPercent >= 0) stringResource(R.string.result_saved, savedPercent) else stringResource(R.string.result_larger), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(18.dp)) }
                state.savedUri?.let { Text(stringResource(R.string.result_saved_location), color = MaterialTheme.colorScheme.primary) }
                state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Spacer(Modifier.weight(1f))
                PrimaryButton(if (state.savedUri == null) stringResource(R.string.result_save) else stringResource(R.string.result_saved_button), onSave, enabled = !state.saving && state.savedUri == null, modifier = Modifier.fillMaxWidth())
                if (state.savedUri != null) SecondaryButton(stringResource(R.string.result_show_gallery), onShowInGallery, Modifier.fillMaxWidth())
                SecondaryButton(stringResource(R.string.result_share), onShare, Modifier.fillMaxWidth())
                SecondaryButton(stringResource(R.string.result_another), onAnother, Modifier.fillMaxWidth())
            }
        }
    }
}
