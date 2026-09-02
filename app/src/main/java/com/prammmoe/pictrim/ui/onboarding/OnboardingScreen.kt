package com.prammmoe.pictrim.ui.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.prammmoe.pictrim.R
import com.prammmoe.pictrim.ui.common.PrimaryButton
import com.prammmoe.pictrim.ui.common.SecondaryButton

@Composable
fun OnboardingScreen(page: Int, onNext: () -> Unit, onBack: () -> Unit, onSkip: () -> Unit, onFinish: () -> Unit) {
    val title = when (page) { 0 -> R.string.onboarding_compress_title; 1 -> R.string.onboarding_resize_title; else -> R.string.onboarding_private_title }
    val body = when (page) { 0 -> R.string.onboarding_compress_body; 1 -> R.string.onboarding_resize_body; else -> R.string.onboarding_private_body }
    Column(Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 18.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            if (page > 0) Text(stringResource(R.string.onboarding_back), style = MaterialTheme.typography.labelLarge, modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable(onClick = onBack).padding(10.dp)) else Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.onboarding_skip), style = MaterialTheme.typography.labelLarge, modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable(onClick = onSkip).padding(10.dp))
        }
        Spacer(Modifier.height(28.dp))
        OnboardingIllustration(page, Modifier.fillMaxWidth().height(290.dp))
        Spacer(Modifier.height(36.dp))
        Text(stringResource(title), style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(12.dp))
        Text(stringResource(body), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.weight(1f))
        PageIndicator(page)
        Spacer(Modifier.height(22.dp))
        if (page == OnboardingViewModel.PageCount - 1) PrimaryButton(stringResource(R.string.onboarding_get_started), onFinish, Modifier.fillMaxWidth()) else PrimaryButton(stringResource(R.string.onboarding_continue), onNext, Modifier.fillMaxWidth())
        if (page > 0) { Spacer(Modifier.height(10.dp)); SecondaryButton(stringResource(R.string.onboarding_back), onBack, Modifier.fillMaxWidth()) }
        Spacer(Modifier.height(14.dp))
        Text(stringResource(R.string.onboarding_skip), style = MaterialTheme.typography.labelLarge, modifier = Modifier.align(Alignment.CenterHorizontally).clip(RoundedCornerShape(12.dp)).clickable(onClick = onSkip).padding(10.dp))
    }
}

@Composable private fun PageIndicator(page: Int) = Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
    repeat(OnboardingViewModel.PageCount) { index ->
        Box(Modifier.padding(horizontal = 4.dp).height(6.dp).width(if (index == page) 26.dp else 6.dp).clip(RoundedCornerShape(10.dp)).background(if (index == page) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline))
    }
}

@Composable private fun OnboardingIllustration(page: Int, modifier: Modifier = Modifier) {
    val ink = MaterialTheme.colorScheme.onSurface
    val muted = MaterialTheme.colorScheme.outline
    Box(modifier.clip(RoundedCornerShape(36.dp)).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(214.dp)) {
            val w = size.width; val h = size.height
            when (page) {
                0 -> { drawRoundRect(ink.copy(alpha = .12f), size = androidx.compose.ui.geometry.Size(w * .78f, h * .72f), topLeft = androidx.compose.ui.geometry.Offset(w * .11f, h * .14f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(28f)); drawRoundRect(ink, size = androidx.compose.ui.geometry.Size(w * .47f, h * .57f), topLeft = androidx.compose.ui.geometry.Offset(w * .26f, h * .22f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(24f)); drawLine(muted, androidx.compose.ui.geometry.Offset(w * .36f, h * .47f), androidx.compose.ui.geometry.Offset(w * .64f, h * .47f), 8f) }
                1 -> { drawRoundRect(ink, size = androidx.compose.ui.geometry.Size(w * .54f, h * .54f), topLeft = androidx.compose.ui.geometry.Offset(w * .23f, h * .23f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(22f), style = Stroke(10f)); drawLine(ink, androidx.compose.ui.geometry.Offset(w * .16f, h * .16f), androidx.compose.ui.geometry.Offset(w * .35f, h * .16f), 9f); drawLine(ink, androidx.compose.ui.geometry.Offset(w * .16f, h * .16f), androidx.compose.ui.geometry.Offset(w * .16f, h * .35f), 9f); drawLine(ink, androidx.compose.ui.geometry.Offset(w * .84f, h * .84f), androidx.compose.ui.geometry.Offset(w * .65f, h * .84f), 9f); drawLine(ink, androidx.compose.ui.geometry.Offset(w * .84f, h * .84f), androidx.compose.ui.geometry.Offset(w * .84f, h * .65f), 9f) }
                else -> { drawRoundRect(ink, size = androidx.compose.ui.geometry.Size(w * .48f, h * .38f), topLeft = androidx.compose.ui.geometry.Offset(w * .26f, h * .43f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(22f)); drawArc(ink, 180f, 180f, false, topLeft = androidx.compose.ui.geometry.Offset(w * .33f, h * .14f), size = androidx.compose.ui.geometry.Size(w * .34f, h * .42f), style = Stroke(18f)); drawCircle(muted, w * .5f, androidx.compose.ui.geometry.Offset(w * .5f, h * .62f)); drawRect(muted, androidx.compose.ui.geometry.Offset(w * .47f, h * .62f), androidx.compose.ui.geometry.Size(w * .06f, h * .1f)) }
            }
        }
    }
}
