package com.prammmoe.pictrim.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.prammmoe.pictrim.R
import com.prammmoe.pictrim.ui.common.FeatureRow
import com.prammmoe.pictrim.ui.common.PicTrimCard
import com.prammmoe.pictrim.ui.common.PicTrimScreenPadding
import com.prammmoe.pictrim.ui.common.PrimaryButton
import com.prammmoe.pictrim.ui.common.SecondaryButton

@Composable
fun HomeScreen(onChooseImage: () -> Unit, onChooseImages: () -> Unit = {}) {
    Column(Modifier.fillMaxSize().padding(PicTrimScreenPadding), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Spacer(Modifier.height(18.dp))
        Text(stringResource(R.string.home_title), style = MaterialTheme.typography.displaySmall)
        Text(stringResource(R.string.home_description), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        PicTrimCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) { FeatureRow(Icons.Outlined.AutoAwesome, stringResource(R.string.home_single_title), stringResource(R.string.home_single_description)); PrimaryButton(stringResource(R.string.choose_image), onChooseImage, Modifier.fillMaxWidth()) } }
        PicTrimCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) { FeatureRow(Icons.Outlined.PhotoLibrary, stringResource(R.string.home_batch_title), stringResource(R.string.home_batch_description)); SecondaryButton(stringResource(R.string.choose_images_batch), onChooseImages, Modifier.fillMaxWidth()) } }
    }
}
