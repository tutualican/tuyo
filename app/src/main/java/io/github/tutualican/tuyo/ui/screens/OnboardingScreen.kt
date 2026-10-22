package io.github.tutualican.tuyo.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import io.github.tutualican.tuyo.R
import io.github.tutualican.tuyo.domain.model.ExamTrack
import io.github.tutualican.tuyo.ui.components.Eyebrow
import io.github.tutualican.tuyo.ui.components.TuyoButton
import io.github.tutualican.tuyo.ui.components.TuyoButtonVariant
import io.github.tutualican.tuyo.ui.components.TuyoCard
import io.github.tutualican.tuyo.ui.components.TuyoChoiceChip
import io.github.tutualican.tuyo.ui.components.TuyoSwitch
import io.github.tutualican.tuyo.ui.components.TuyoTextField
import io.github.tutualican.tuyo.ui.theme.LocalTuyoColors
import io.github.tutualican.tuyo.ui.theme.Radius
import io.github.tutualican.tuyo.ui.theme.Space

@Composable
fun OnboardingScreen(
    onComplete: (
        name: String,
        track: ExamTrack,
        reminderEnabled: Boolean,
        hour: Int,
        minute: Int,
    ) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalTuyoColors.current
    val context = LocalContext.current
    var step by rememberSaveable { mutableIntStateOf(0) }
    var name by rememberSaveable { mutableStateOf("") }
    var trackName by rememberSaveable { mutableStateOf<String?>(null) }
    var reminderEnabled by rememberSaveable { mutableStateOf(false) }
    var hour by rememberSaveable { mutableIntStateOf(20) }
    var minute by rememberSaveable { mutableIntStateOf(0) }
    var permissionRequested by rememberSaveable { mutableStateOf(false) }

    val track = trackName?.let { name -> ExamTrack.entries.firstOrNull { it.name == name } }
    val hasPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        permissionRequested = true
        reminderEnabled = granted
    }

    fun toggleReminder(next: Boolean) {
        if (!next) {
            reminderEnabled = false
            return
        }
        if (hasPermission) {
            reminderEnabled = true
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            reminderEnabled = true
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .imePadding(),
        contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 18.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(Space.xl),
    ) {
        item {
            Eyebrow("TUYO · YKS DEFTERİ")
            Spacer(Modifier.height(10.dp))
            Text(
                text = if (step == 0) {
                    stringResource(R.string.onboarding_title)
                } else {
                    stringResource(R.string.onboarding_reminder_title)
                },
                style = MaterialTheme.typography.displaySmall,
                color = colors.textPrimary,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (step == 0) {
                    stringResource(R.string.onboarding_body)
                } else {
                    stringResource(R.string.onboarding_reminder_body)
                },
                style = MaterialTheme.typography.bodyLarge,
                color = colors.textSecondary,
            )
        }

        if (step == 0) {
            item {
                Column {
                    Text(
                        text = stringResource(R.string.onboarding_name_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.textSecondary,
                    )
                    Spacer(Modifier.height(Space.sm))
                    TuyoTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = stringResource(R.string.onboarding_name_placeholder),
                        maxLength = 36,
                    )
                }
            }
            item {
                Column {
                    Text(
                        text = stringResource(R.string.onboarding_track_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.textSecondary,
                    )
                    Spacer(Modifier.height(Space.sm))
                    Column(verticalArrangement = Arrangement.spacedBy(Space.sm)) {
                        ExamTrack.entries.chunked(2).forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(Space.sm),
                            ) {
                                row.forEach { option ->
                                    TuyoChoiceChip(
                                        label = option.label,
                                        selected = track == option,
                                        onClick = { trackName = option.name },
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                                if (row.size == 1) Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
            item {
                TuyoCard(contentPadding = PaddingValues(Space.lg)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = colors.success,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                        Spacer(Modifier.width(Space.md))
                        Text(
                            text = stringResource(R.string.onboarding_privacy),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary,
                        )
                    }
                }
            }
            item {
                TuyoButton(
                    text = stringResource(R.string.onboarding_continue),
                    onClick = { step = 1 },
                    enabled = name.trim().isNotEmpty() && track != null,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        } else {
            item {
                TuyoCard(contentPadding = PaddingValues(Space.lg)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.onboarding_reminder_toggle),
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.textPrimary,
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = stringResource(R.string.onboarding_reminder_toggle_body),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary,
                            )
                        }
                        Spacer(Modifier.width(Space.md))
                        TuyoSwitch(
                            checked = reminderEnabled,
                            onCheckedChange = ::toggleReminder,
                        )
                    }
                }
            }
            if (reminderEnabled) {
                item {
                    Column {
                        Text(
                            text = stringResource(R.string.onboarding_reminder_time),
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.textSecondary,
                        )
                        Spacer(Modifier.height(Space.sm))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(
                                8 to 0,
                                12 to 0,
                                18 to 0,
                                20 to 0,
                                21 to 30,
                                22 to 0,
                            ).chunked(3).forEach { slots ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    slots.forEach { (h, m) ->
                                        TuyoChoiceChip(
                                            label = "%02d:%02d".format(h, m),
                                            selected = hour == h && minute == m,
                                            onClick = {
                                                hour = h
                                                minute = m
                                            },
                                            modifier = Modifier.weight(1f),
                                        )
                                    }
                                    repeat(3 - slots.size) {
                                        Spacer(Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (permissionRequested && !hasPermission) {
                item {
                    Text(
                        text = stringResource(R.string.onboarding_permission_denied),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.warning,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Radius.sm))
                            .background(colors.warning.copy(alpha = 0.08f))
                            .padding(Space.md),
                    )
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(Space.sm)) {
                    TuyoButton(
                        text = stringResource(R.string.onboarding_start),
                        onClick = {
                            val selectedTrack = track ?: return@TuyoButton
                            onComplete(name, selectedTrack, reminderEnabled, hour, minute)
                        },
                        enabled = track != null,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    TuyoButton(
                        text = stringResource(R.string.onboarding_back),
                        onClick = { step = 0 },
                        leadingIcon = Icons.AutoMirrored.Filled.ArrowBack,
                        variant = TuyoButtonVariant.Ghost,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
