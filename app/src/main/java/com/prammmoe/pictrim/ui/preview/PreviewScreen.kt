package com.prammmoe.pictrim.ui.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.prammmoe.pictrim.domain.model.EditorMode
import com.prammmoe.pictrim.domain.model.ImageInfo
import com.prammmoe.pictrim.ui.common.ImagePreview
import com.prammmoe.pictrim.ui.common.asFileSize
import com.prammmoe.pictrim.ui.common.OptionCard
import com.prammmoe.pictrim.ui.common.PicTrimCard
import com.prammmoe.pictrim.ui.common.PicTrimScreenPadding
import com.prammmoe.pictrim.ui.common.PicTrimTopBar
import com.prammmoe.pictrim.ui.common.PrimaryButton
import com.prammmoe.pictrim.R
import androidx.compose.ui.res.stringResource

@Composable
fun PreviewScreen(state: PreviewUiState, onBack: () -> Unit, onMode: (EditorMode) -> Unit, onRetry: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        PicTrimTopBar(stringResource(R.string.preview_title), onBack)
        when {
            state.loading -> Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) { CircularProgressIndicator() }
            state.error != null -> ErrorContent(state.error, onRetry)
            state.image != null -> PreviewContent(state.image, onMode)
        }
    }
}

@Composable private fun PreviewContent(image: ImageInfo, onMode: (EditorMode) -> Unit) = Column(Modifier.fillMaxSize().padding(PicTrimScreenPadding), verticalArrangement = Arrangement.spacedBy(12.dp)) {
    PicTrimCard(Modifier.fillMaxWidth()) { ImagePreview(image.uri, Modifier.fillMaxWidth().height(275.dp).padding(12.dp)) }
    Text(image.displayName, style = MaterialTheme.typography.titleMedium)
    Text("${image.format.label} · ${image.width} × ${image.height} · ${image.sizeBytes.asFileSize()}", color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(8.dp)); Text(stringResource(R.string.preview_choose_mode), style = MaterialTheme.typography.titleLarge)
    OptionCard(false, stringResource(R.string.mode_compress), stringResource(R.string.mode_compress_description), { onMode(EditorMode.COMPRESS) })
    OptionCard(false, stringResource(R.string.mode_resize), stringResource(R.string.mode_resize_description), { onMode(EditorMode.RESIZE) })
    OptionCard(false, stringResource(R.string.mode_both), stringResource(R.string.mode_both_description), { onMode(EditorMode.COMPRESS_AND_RESIZE) })
}

@Composable private fun ErrorContent(message: String, retry: () -> Unit) = Column(Modifier.fillMaxSize().padding(PicTrimScreenPadding), Arrangement.Center, Alignment.CenterHorizontally) { Text(message); Spacer(Modifier.height(12.dp)); PrimaryButton(stringResource(R.string.try_again), retry) }
