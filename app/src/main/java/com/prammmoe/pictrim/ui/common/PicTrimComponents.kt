package com.prammmoe.pictrim.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

val PicTrimRadius = 24.dp
val PicTrimSmallRadius = 16.dp
val PicTrimScreenPadding = 20.dp

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun PicTrimTopBar(title: String, onBack: (() -> Unit)? = null, action: (@Composable () -> Unit)? = null) {
    TopAppBar(
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        navigationIcon = {
            if (onBack != null) IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = { action?.invoke() },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}

@Composable
fun PicTrimCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(PicTrimRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        content = content
    )
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(onClick = onClick, enabled = enabled, modifier = modifier.height(54.dp), shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors()) { Text(text) }
}

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    OutlinedButton(onClick = onClick, enabled = enabled, modifier = modifier.height(52.dp), shape = RoundedCornerShape(18.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) { Text(text) }
}

/** Explicit neutral state colors prevent Material's tonal defaults from introducing purple. */
@Composable
fun PicTrimFilterChip(selected: Boolean, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurface,
            selectedContainerColor = MaterialTheme.colorScheme.onSurface,
            selectedLabelColor = MaterialTheme.colorScheme.surface
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = MaterialTheme.colorScheme.outline,
            selectedBorderColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
fun PicTrimSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        colors = SwitchDefaults.colors(
            checkedThumbColor = MaterialTheme.colorScheme.surface,
            checkedTrackColor = MaterialTheme.colorScheme.onSurface,
            checkedBorderColor = MaterialTheme.colorScheme.onSurface,
            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
            uncheckedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
}

@Composable
fun PicTrimSlider(value: Float, onValueChange: (Float) -> Unit, valueRange: ClosedFloatingPointRange<Float>, modifier: Modifier = Modifier) {
    Slider(value, onValueChange, modifier, valueRange = valueRange, colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.onSurface, activeTrackColor = MaterialTheme.colorScheme.onSurface, inactiveTrackColor = MaterialTheme.colorScheme.outline))
}

@Composable
fun FeatureRow(icon: ImageVector, title: String, description: String, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) { Icon(icon, null, Modifier.size(21.dp)) }
        Spacer(Modifier.width(14.dp))
        Column { Text(title, style = MaterialTheme.typography.titleMedium); Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable
fun OptionCard(selected: Boolean, title: String, description: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val border = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
    Card(modifier.clickable(onClick = onClick), shape = RoundedCornerShape(PicTrimSmallRadius), border = BorderStroke(if (selected) 2.dp else 1.dp, border), colors = CardDefaults.cardColors(containerColor = if (selected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(0.dp)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Text(title, style = MaterialTheme.typography.titleMedium); Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Box(Modifier.size(18.dp).clip(RoundedCornerShape(9.dp)).background(if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surface), contentAlignment = Alignment.Center) { if (selected) Box(Modifier.size(6.dp).clip(RoundedCornerShape(3.dp)).background(MaterialTheme.colorScheme.surface)) }
        }
    }
}
