package io.github.tutualican.tuyo.ui.screens

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import io.github.tutualican.tuyo.R
import io.github.tutualican.tuyo.data.BackupManager
import io.github.tutualican.tuyo.domain.model.ExamTrack
import io.github.tutualican.tuyo.domain.model.UserData
import io.github.tutualican.tuyo.domain.model.UserProfile
import io.github.tutualican.tuyo.ui.components.Eyebrow
import io.github.tutualican.tuyo.ui.components.FieldLabel
import io.github.tutualican.tuyo.ui.components.Hairline
import io.github.tutualican.tuyo.ui.components.ScreenHeader
import io.github.tutualican.tuyo.ui.components.TuyoButton
import io.github.tutualican.tuyo.ui.components.TuyoButtonVariant
import io.github.tutualican.tuyo.ui.components.TuyoCard
import io.github.tutualican.tuyo.ui.components.TuyoChoiceChip
import io.github.tutualican.tuyo.ui.components.TuyoConfirmDialog
import io.github.tutualican.tuyo.ui.components.TuyoSwitch
import io.github.tutualican.tuyo.ui.components.TuyoTextField
import io.github.tutualican.tuyo.ui.theme.LocalTuyoColors
import io.github.tutualican.tuyo.ui.theme.Space
import java.time.LocalDate
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    user: UserData,
    onBack: () -> Unit,
    onSaveProfile: (String, ExamTrack) -> Unit,
    onSetReminder: (Boolean, Int, Int) -> Unit,
    onReset: () -> Unit,
    onSetExamDate: (LocalDate) -> Unit = {},
    onSetStudyGoals: (Int, Int, Int, Int) -> Unit = { _, _, _, _ -> },
    onImportData: (UserData) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val colors = LocalTuyoColors.current
    val context = LocalContext.current
    val profile = user.profile
    val scope = rememberCoroutineScope()
    var name by rememberSaveable(profile.name) { mutableStateOf(profile.name) }
    var trackName by rememberSaveable(profile.track) {
        mutableStateOf(profile.track?.name)
    }
    var reminderEnabled by rememberSaveable(profile.reminderEnabled) {
        mutableStateOf(profile.reminderEnabled)
    }
    var hour by rememberSaveable(profile.reminderHour) {
        mutableIntStateOf(profile.reminderHour)
    }
    var minute by rememberSaveable(profile.reminderMinute) {
        mutableIntStateOf(profile.reminderMinute)
    }
    var dailyQuestions by rememberSaveable(profile.dailyQuestionTarget) {
        mutableStateOf(profile.dailyQuestionTarget.toString())
    }
    var dailyMinutes by rememberSaveable(profile.dailyMinuteTarget) {
        mutableStateOf(profile.dailyMinuteTarget.toString())
    }
    var weeklyQuestions by rememberSaveable(profile.weeklyQuestionTarget) {
        mutableStateOf(profile.weeklyQuestionTarget.toString())
    }
    var weeklyMinutes by rememberSaveable(profile.weeklyMinuteTarget) {
        mutableStateOf(profile.weeklyMinuteTarget.toString())
    }
    var permissionDenied by rememberSaveable { mutableStateOf(false) }
    var confirmReset by rememberSaveable { mutableStateOf(false) }
    var saved by rememberSaveable { mutableStateOf(false) }
    var hasPermission by remember { mutableStateOf(notificationPermission(context)) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasPermission = notificationPermission(context)
                if (hasPermission) permissionDenied = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        permissionDenied = !granted
        hasPermission = granted
        reminderEnabled = granted
        if (granted) onSetReminder(true, hour, minute)
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri != null) {
            scope.launch {
                BackupManager.export(context, uri, user)
                    .onSuccess {
                        Toast.makeText(context, "Yedek dışa aktarıldı", Toast.LENGTH_SHORT).show()
                    }
                    .onFailure {
                        Toast.makeText(context, "Yedek oluşturulamadı", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            scope.launch {
                BackupManager.import(context, uri)
                    .onSuccess { imported ->
                        onImportData(imported)
                        Toast.makeText(context, "Yedek geri yüklendi", Toast.LENGTH_SHORT).show()
                    }
                    .onFailure {
                        Toast.makeText(context, "Geçerli bir Tuyo yedeği değil", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    fun toggleReminder(next: Boolean) {
        if (!next) {
            reminderEnabled = false
            onSetReminder(false, hour, minute)
            return
        }
        if (hasPermission) {
            reminderEnabled = true
            onSetReminder(true, hour, minute)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            reminderEnabled = true
            onSetReminder(true, hour, minute)
        }
    }

    fun openNotificationSettings() {
        runCatching {
            context.startActivity(
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                },
            )
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(Space.xl),
    ) {
        item {
            ScreenHeader(
                eyebrow = "AYARLAR",
                title = stringResource(R.string.settings_title),
                subtitle = stringResource(R.string.settings_subtitle),
                onBack = onBack,
                backContentDescription = stringResource(R.string.detail_back),
            )
        }
        item {
            TuyoCard {
                Eyebrow(stringResource(R.string.settings_profile))
                Spacer(Modifier.height(10.dp))
                FieldLabel(stringResource(R.string.settings_name))
                TuyoTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        saved = false
                    },
                    placeholder = stringResource(R.string.onboarding_name_placeholder),
                    maxLength = 36,
                )
                Spacer(Modifier.height(Space.md))
                FieldLabel(stringResource(R.string.settings_track))
                Column(verticalArrangement = Arrangement.spacedBy(Space.sm)) {
                    ExamTrack.entries.chunked(2).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Space.sm),
                        ) {
                            row.forEach { track ->
                                TuyoChoiceChip(
                                    label = track.label,
                                    selected = trackName == track.name,
                                    onClick = {
                                        trackName = track.name
                                        saved = false
                                    },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
                Spacer(Modifier.height(Space.md))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (saved) {
                        Text(
                            text = stringResource(R.string.settings_saved),
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.success,
                        )
                        Spacer(Modifier.weight(1f))
                    }
                    TuyoButton(
                        text = stringResource(R.string.settings_save),
                        onClick = {
                            val selected = trackName
                                ?.let { name -> ExamTrack.entries.firstOrNull { it.name == name } }
                            if (selected != null) {
                                onSaveProfile(name, selected)
                                saved = true
                            }
                        },
                        enabled = name.trim().isNotEmpty() && trackName != null,
                        variant = TuyoButtonVariant.Secondary,
                    )
                }
            }
        }
        item {
            TuyoCard {
                Eyebrow("SINAV VE HEDEFLER")
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "YKS tarihi",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.textPrimary,
                        )
                        Text(
                            text = profile.examDate.format(
                                java.time.format.DateTimeFormatter.ofPattern(
                                    "d MMMM yyyy",
                                    java.util.Locale.forLanguageTag("tr"),
                                ),
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary,
                        )
                    }
                    TuyoButton(
                        text = "Değiştir",
                        onClick = {
                            DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    onSetExamDate(LocalDate.of(year, month + 1, day))
                                },
                                profile.examDate.year,
                                profile.examDate.monthValue - 1,
                                profile.examDate.dayOfMonth,
                            ).show()
                        },
                        variant = TuyoButtonVariant.Secondary,
                    )
                }
                Spacer(Modifier.height(Space.lg))
                Hairline()
                Spacer(Modifier.height(Space.md))
                Text(
                    text = "Günlük hedefler",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary,
                )
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                    GoalField("Soru", dailyQuestions, Modifier.weight(1f)) { dailyQuestions = it }
                    GoalField("Dakika", dailyMinutes, Modifier.weight(1f)) { dailyMinutes = it }
                }
                Spacer(Modifier.height(Space.sm))
                Text(
                    text = "Haftalık hedefler",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary,
                )
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                    GoalField("Soru", weeklyQuestions, Modifier.weight(1f)) { weeklyQuestions = it }
                    GoalField("Dakika", weeklyMinutes, Modifier.weight(1f)) { weeklyMinutes = it }
                }
                Spacer(Modifier.height(Space.md))
                TuyoButton(
                    text = "Hedefleri kaydet",
                    onClick = {
                        onSetStudyGoals(
                            dailyQuestions.toIntOrNull() ?: profile.dailyQuestionTarget,
                            dailyMinutes.toIntOrNull() ?: profile.dailyMinuteTarget,
                            weeklyQuestions.toIntOrNull() ?: profile.weeklyQuestionTarget,
                            weeklyMinutes.toIntOrNull() ?: profile.weeklyMinuteTarget,
                        )
                    },
                    variant = TuyoButtonVariant.Secondary,
                )
            }
        }
        item {
            TuyoCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Eyebrow(stringResource(R.string.settings_reminder))
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = stringResource(R.string.settings_reminder_toggle),
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.textPrimary,
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = stringResource(R.string.settings_reminder_body),
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
                if (reminderEnabled) {
                    Spacer(Modifier.height(Space.md))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        listOf(
                            8 to 0,
                            12 to 0,
                            18 to 0,
                            20 to 0,
                            21 to 30,
                            22 to 0,
                        ).forEach { (h, m) ->
                            TuyoChoiceChip(
                                label = "%02d:%02d".format(h, m),
                                selected = hour == h && minute == m,
                                onClick = {
                                    hour = h
                                    minute = m
                                    onSetReminder(true, h, m)
                                },
                            )
                        }
                    }
                }
                if (permissionDenied || (reminderEnabled && !hasPermission)) {
                    Spacer(Modifier.height(Space.sm))
                    Text(
                        text = stringResource(R.string.settings_permission_denied),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.warning,
                    )
                    Spacer(Modifier.height(Space.xs))
                    TuyoButton(
                        text = stringResource(R.string.settings_permission_open),
                        onClick = ::openNotificationSettings,
                        variant = TuyoButtonVariant.Ghost,
                    )
                }
            }
        }
        item {
            TuyoCard {
                Eyebrow(stringResource(R.string.settings_data))
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.settings_data_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                )
                Spacer(Modifier.height(Space.sm))
                Text(
                    text = "Her değişiklikte tarihli bir yedek uygulama içinde tutulur. Dışa aktarma ve geri yükleme tamamen cihazda çalışır.",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary,
                )
                Spacer(Modifier.height(Space.md))
                Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                    TuyoButton(
                        text = "Yedek al",
                        onClick = {
                            exportLauncher.launch("tuyo-yedek-${LocalDate.now()}.json")
                        },
                        modifier = Modifier.weight(1f),
                        variant = TuyoButtonVariant.Secondary,
                    )
                    TuyoButton(
                        text = "Yedekten yükle",
                        onClick = { importLauncher.launch(arrayOf("application/json", "text/plain")) },
                        modifier = Modifier.weight(1f),
                        variant = TuyoButtonVariant.Ghost,
                    )
                }
                Spacer(Modifier.height(Space.md))
                Hairline()
                Spacer(Modifier.height(Space.md))
                TuyoButton(
                    text = stringResource(R.string.settings_reset),
                    onClick = { confirmReset = true },
                    variant = TuyoButtonVariant.Ghost,
                )
            }
        }
        item {
            TuyoCard {
                Eyebrow(stringResource(R.string.settings_about))
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.settings_about_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.settings_no_internet),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.success,
                )
            }
        }
    }

    if (confirmReset) {
        TuyoConfirmDialog(
            eyebrow = "AYARLAR",
            title = stringResource(R.string.settings_reset_title),
            body = stringResource(R.string.settings_reset_body),
            confirmLabel = stringResource(R.string.settings_reset_confirm),
            dismissLabel = stringResource(R.string.settings_reset_cancel),
            onConfirm = {
                confirmReset = false
                onReset()
                onBack()
            },
            onDismiss = { confirmReset = false },
            destructive = true,
        )
    }
}

@Composable
private fun GoalField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onChange: (String) -> Unit,
) {
    Column(modifier = modifier) {
        FieldLabel(label)
        TuyoTextField(
            value = value,
            onValueChange = { next -> onChange(next.filter(Char::isDigit).take(5)) },
            placeholder = "0",
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
            ),
        )
    }
}

private fun notificationPermission(context: android.content.Context): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
