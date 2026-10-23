package io.github.tutualican.tuyo.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.tutualican.tuyo.R
import io.github.tutualican.tuyo.data.StudyContent
import io.github.tutualican.tuyo.data.StudyTimer
import io.github.tutualican.tuyo.data.TacticSubmission
import io.github.tutualican.tuyo.domain.model.Mistake
import io.github.tutualican.tuyo.domain.model.PlanPeriod
import io.github.tutualican.tuyo.domain.model.Subject
import io.github.tutualican.tuyo.domain.model.TimerMode
import io.github.tutualican.tuyo.domain.model.UserData
import io.github.tutualican.tuyo.ui.components.CaptureButton
import io.github.tutualican.tuyo.ui.components.CommandEntry
import io.github.tutualican.tuyo.ui.components.CommandPalette
import io.github.tutualican.tuyo.ui.components.Hairline
import io.github.tutualican.tuyo.ui.screens.AddExamDialog
import io.github.tutualican.tuyo.ui.screens.AddMistakeDialog
import io.github.tutualican.tuyo.ui.screens.AddNetDialog
import io.github.tutualican.tuyo.ui.screens.AddPlanTaskDialog
import io.github.tutualican.tuyo.ui.screens.AddQuestionDialog
import io.github.tutualican.tuyo.ui.screens.ExamAnalysisDialog
import io.github.tutualican.tuyo.ui.screens.GuideScreen
import io.github.tutualican.tuyo.ui.screens.GuideSection
import io.github.tutualican.tuyo.ui.screens.MistakeDetailScreen
import io.github.tutualican.tuyo.ui.screens.MistakesScreen
import io.github.tutualican.tuyo.ui.screens.NetGoalDialog
import io.github.tutualican.tuyo.ui.screens.OnboardingScreen
import io.github.tutualican.tuyo.ui.screens.SettingsScreen
import io.github.tutualican.tuyo.ui.screens.TodayScreen
import io.github.tutualican.tuyo.ui.screens.TopicsScreen
import io.github.tutualican.tuyo.ui.screens.WorkScreen
import io.github.tutualican.tuyo.ui.theme.LocalTuyoColors
import io.github.tutualican.tuyo.ui.theme.Motion
import io.github.tutualican.tuyo.ui.theme.Radius
import io.github.tutualican.tuyo.ui.theme.Space
import java.time.LocalDate

enum class Destination(
    val labelRes: Int,
    val icon: ImageVector,
) {
    TODAY(R.string.nav_today, Icons.Filled.Home),
    MISTAKES(R.string.nav_mistakes, Icons.AutoMirrored.Filled.List),
    WORK(R.string.nav_work, Icons.Filled.PlayArrow),
    TOPICS(R.string.nav_topics, Icons.Filled.Info),
    GUIDE(R.string.nav_guide, Icons.Filled.Star),
}

@Composable
fun TuyoApp() {
    val colors = LocalTuyoColors.current
    val context = LocalContext.current
    val viewModel: TuyoViewModel = viewModel()
    val userState by viewModel.user.collectAsStateWithLifecycle()

    val user = userState
    when {
        user == null -> {
            LoadingScreen()
            return
        }
        !user.profile.onboardingComplete -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.canvas),
            ) {
                OnboardingScreen(
                    onComplete = { name, track, reminders, hour, minute ->
                        viewModel.completeOnboarding(name, track, reminders, hour, minute)
                    },
                )
            }
            return
        }
    }

    val data = remember(user) { StudyContent.studyData(user) }
    var destination by rememberSaveable { mutableStateOf(Destination.TODAY) }
    var detailId by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingDetailId by rememberSaveable { mutableStateOf<String?>(null) }
    var tacticFocusId by rememberSaveable { mutableStateOf<String?>(null) }
    var guideSection by rememberSaveable { mutableStateOf<GuideSection?>(null) }
    var settingsOpen by rememberSaveable { mutableStateOf(false) }
    var paletteOpen by remember { mutableStateOf(false) }
    var addOpen by remember { mutableStateOf(false) }
    var addTopicId by rememberSaveable { mutableStateOf<String?>(null) }
    var netOpen by remember { mutableStateOf(false) }
    var questionOpen by remember { mutableStateOf(false) }
    var questionDateEpoch by rememberSaveable { mutableLongStateOf(LocalDate.now().toEpochDay()) }
    var planOpen by remember { mutableStateOf(false) }
    var planPeriodName by rememberSaveable { mutableStateOf(PlanPeriod.DAILY.name) }
    var planDateEpoch by rememberSaveable { mutableLongStateOf(LocalDate.now().toEpochDay()) }
    var examOpen by remember { mutableStateOf(false) }
    var analysisExamId by rememberSaveable { mutableStateOf<String?>(null) }
    var netGoalOpen by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    // A new mistake is persisted asynchronously. Wait for the emission instead
    // of opening an id that is not in the notebook yet (which would flash blank).
    LaunchedEffect(data.mistakes, pendingDetailId) {
        val pending = pendingDetailId
        if (pending != null && data.mistakes.any { it.id == pending }) {
            detailId = pending
            pendingDetailId = null
        }
    }

    BackHandler(enabled = settingsOpen || detailId != null) {
        if (settingsOpen) settingsOpen = false else detailId = null
    }

    fun openDestination(next: Destination) {
        destination = next
        detailId = null
        pendingDetailId = null
        settingsOpen = false
        guideSection = null
        tacticFocusId = null
    }

    fun openMistake(id: String) {
        settingsOpen = false
        detailId = id
    }

    fun openTactic(id: String) {
        settingsOpen = false
        tacticFocusId = id
        guideSection = GuideSection.TACTICS
        destination = Destination.GUIDE
        detailId = null
    }

    fun openMotivation() {
        tacticFocusId = null
        guideSection = GuideSection.MOTIVATION
        destination = Destination.GUIDE
        detailId = null
        settingsOpen = false
    }

    fun addMistake(topicId: String?) {
        addTopicId = topicId
        addOpen = true
    }

    fun openQuestion(date: LocalDate = LocalDate.now()) {
        questionDateEpoch = date.toEpochDay()
        questionOpen = true
    }

    fun openPlanTask(period: PlanPeriod, date: LocalDate = LocalDate.now()) {
        planPeriodName = period.name
        planDateEpoch = date.toEpochDay()
        planOpen = true
    }

    val commands = listOf(
        CommandEntry(
            id = "today",
            title = stringResource(R.string.nav_today),
            section = stringResource(R.string.command_section_go),
            icon = Icons.Filled.Home,
        ) { openDestination(Destination.TODAY) },
        CommandEntry(
            id = "mistakes",
            title = stringResource(R.string.nav_mistakes),
            section = stringResource(R.string.command_section_go),
            icon = Icons.AutoMirrored.Filled.List,
        ) { openDestination(Destination.MISTAKES) },
        CommandEntry(
            id = "topics",
            title = stringResource(R.string.nav_topics),
            section = stringResource(R.string.command_section_go),
            icon = Icons.Filled.Info,
        ) { openDestination(Destination.TOPICS) },
        CommandEntry(
            id = "work",
            title = stringResource(R.string.nav_work),
            section = stringResource(R.string.command_section_go),
            icon = Icons.Filled.PlayArrow,
        ) { openDestination(Destination.WORK) },
        CommandEntry(
            id = "guide",
            title = stringResource(R.string.nav_guide),
            section = stringResource(R.string.command_section_go),
            icon = Icons.Filled.Star,
        ) { openDestination(Destination.GUIDE) },
        CommandEntry(
            id = "new-question",
            title = "Bugünün sorularını gir",
            section = stringResource(R.string.command_section_actions),
            icon = Icons.Filled.Add,
        ) { openQuestion() },
        CommandEntry(
            id = "new-exam",
            title = "Ders bazlı deneme ekle",
            section = stringResource(R.string.command_section_actions),
            icon = Icons.Filled.Add,
        ) { examOpen = true },
        CommandEntry(
            id = "new-mistake",
            title = stringResource(R.string.command_action_new_mistake),
            section = stringResource(R.string.command_section_actions),
            icon = Icons.Filled.Add,
        ) { addMistake(null) },
        CommandEntry(
            id = "review",
            title = stringResource(R.string.command_action_review),
            section = stringResource(R.string.command_section_actions),
            icon = Icons.Filled.Refresh,
        ) { openDestination(Destination.MISTAKES) },
        CommandEntry(
            id = "motivation",
            title = stringResource(R.string.command_action_motivation),
            section = stringResource(R.string.command_section_actions),
            icon = Icons.Filled.Star,
        ) { openMotivation() },
        CommandEntry(
            id = "settings",
            title = stringResource(R.string.settings_title),
            section = stringResource(R.string.command_section_actions),
            icon = Icons.Filled.Settings,
        ) {
            settingsOpen = true
            detailId = null
        },
    )

    val screenKey = when {
        settingsOpen -> "settings"
        detailId != null -> "detail:${detailId}"
        else -> destination.name
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.canvas)
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(colors.accent.copy(alpha = 0.075f), Color.Transparent),
                        center = Offset(size.width * 0.82f, -size.height * 0.03f),
                        radius = size.width * 0.95f,
                    ),
                )
            }
            .onPreviewKeyEvent { event ->
                val commandKey = event.isCtrlPressed || event.isMetaPressed
                if (event.type == KeyEventType.KeyDown && commandKey && event.key == Key.K) {
                    paletteOpen = true
                    true
                } else {
                    false
                }
            }
            .focusRequester(focusRequester)
            .focusable(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(
                            WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
                        ),
                    ),
            ) {
                AnimatedContent(
                    targetState = screenKey,
                    transitionSpec = {
                        if (targetState.startsWith("detail:") || targetState == "settings") {
                            (slideInHorizontally(
                                animationSpec = tween(Motion.base, easing = Motion.enter),
                            ) { it / 8 } + fadeIn(tween(Motion.base))) togetherWith
                                fadeOut(tween(Motion.fast))
                        } else {
                            fadeIn(tween(Motion.base)) togetherWith fadeOut(tween(Motion.fast))
                        }
                    },
                    label = "screen",
                ) { key ->
                    when {
                        key == "settings" -> SettingsScreen(
                            user = user,
                            onBack = { settingsOpen = false },
                            onSaveProfile = viewModel::updateProfile,
                            onSetReminder = viewModel::setReminder,
                            onReset = viewModel::resetAll,
                            onSetExamDate = viewModel::setExamDate,
                            onSetStudyGoals = viewModel::setStudyGoals,
                            onImportData = viewModel::replaceAll,
                        )
                        key.startsWith("detail:") -> {
                            val mistake = data.mistakes.firstOrNull {
                                it.id == key.removePrefix("detail:")
                            }
                            if (mistake != null) {
                                MistakeDetailScreen(
                                    mistake = mistake,
                                    topic = data.topics.firstOrNull { it.id == mistake.topicId },
                                    relatedTactic = data.tactics.firstOrNull {
                                        mistake.topicId in it.topicIds
                                    },
                                    onBack = { detailId = null },
                                    onOpenTactic = ::openTactic,
                                    onReview = { outcome ->
                                        viewModel.reviewMistake(mistake.id, outcome)
                                    },
                                    onDelete = {
                                        viewModel.deleteMistake(mistake.id)
                                        detailId = null
                                    },
                                )
                            } else {
                                Box(Modifier.fillMaxSize())
                            }
                        }
                        else -> when (Destination.valueOf(key)) {
                            Destination.TODAY -> TodayScreen(
                                data = data,
                                onOpenMistake = ::openMistake,
                                onSeeAllMistakes = { openDestination(Destination.MISTAKES) },
                                onOpenTactic = ::openTactic,
                                onOpenPalette = { paletteOpen = true },
                                onOpenSettings = { settingsOpen = true },
                                onAddNet = { netOpen = true },
                                onOpenMotivation = ::openMotivation,
                                onOpenWork = { openDestination(Destination.WORK) },
                                onOpenTopics = { openDestination(Destination.TOPICS) },
                                onAddQuestion = { openQuestion() },
                                onTogglePlanTask = viewModel::togglePlanTask,
                            )
                            Destination.MISTAKES -> MistakesScreen(
                                data = data,
                                onOpenMistake = ::openMistake,
                            )
                            Destination.WORK -> WorkScreen(
                                data = data,
                                onAddQuestion = ::openQuestion,
                                onDeleteQuestionLog = viewModel::deleteQuestionLog,
                                onAddPlanTask = ::openPlanTask,
                                onTogglePlanTask = viewModel::togglePlanTask,
                                onDeletePlanTask = viewModel::deletePlanTask,
                                onCarryOver = viewModel::carryOverPlanTasks,
                                onToggleRestDay = viewModel::toggleRestDay,
                                onApplySuggestions = { viewModel.applyPlanSuggestions(data.topics) },
                                onStartTimer = { mode, subject, target ->
                                    StudyTimer.start(context, mode, subject, target)
                                },
                                onPauseTimer = { StudyTimer.pause(context) },
                                onResumeTimer = { StudyTimer.resume(context) },
                                onStopTimer = { StudyTimer.stop(context) },
                                onResetTimer = { StudyTimer.reset() },
                                onAddExam = { examOpen = true },
                                onAnalyzeExam = { exam -> analysisExamId = exam.id },
                                onAddNetGoal = { netGoalOpen = true },
                                onDeleteNetGoal = viewModel::deleteNetGoal,
                            )
                            Destination.TOPICS -> TopicsScreen(
                                topics = data.topics,
                                mistakes = data.mistakes,
                                onAddMistake = ::addMistake,
                                topicProgress = data.topicProgress,
                                onSetTopicStatus = viewModel::setTopicStatus,
                                onReviewTopic = viewModel::reviewTopic,
                            )
                            Destination.GUIDE -> GuideScreen(
                                data = data,
                                focusedTacticId = tacticFocusId,
                                initialSection = guideSection,
                                onSubmitTactic = { TacticSubmission.share(context) },
                                onOpenSource = { TacticSubmission.openUrl(context, it) },
                            )
                        }
                    }
                }
                if (settingsOpen.not() && detailId == null) {
                    CaptureButton(
                        text = stringResource(R.string.mistakes_add),
                        onClick = { addMistake(null) },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = Space.xl, bottom = Space.lg),
                    )
                }
            }
            BottomNav(
                selected = destination,
                onSelect = { next ->
                    guideSection = null
                    tacticFocusId = null
                    openDestination(next)
                },
            )
        }
    }

    if (paletteOpen) {
        CommandPalette(
            entries = commands,
            onDismiss = { paletteOpen = false },
        )
    }

    if (addOpen) {
        AddMistakeDialog(
            topics = data.topics,
            initialTopicId = addTopicId,
            onDismiss = {
                addOpen = false
                addTopicId = null
            },
            onAdd = { mistake: Mistake ->
                viewModel.addMistake(mistake)
                addOpen = false
                addTopicId = null
                destination = Destination.MISTAKES
                detailId = null
                pendingDetailId = mistake.id
            },
        )
    }

    if (netOpen) {
        AddNetDialog(
            onDismiss = { netOpen = false },
            onAdd = { exam, net ->
                viewModel.addNet(exam, net)
                netOpen = false
            },
        )
    }

    if (questionOpen) {
        AddQuestionDialog(
            date = LocalDate.ofEpochDay(questionDateEpoch),
            onDismiss = { questionOpen = false },
            onAdd = { date, subject, correct, wrong, blank ->
                viewModel.addQuestionLog(date, subject, correct, wrong, blank)
                questionOpen = false
            },
        )
    }

    if (planOpen) {
        val period = PlanPeriod.entries.firstOrNull { it.name == planPeriodName }
            ?: PlanPeriod.DAILY
        AddPlanTaskDialog(
            initialPeriod = period,
            date = LocalDate.ofEpochDay(planDateEpoch),
            onDismiss = { planOpen = false },
            onAdd = { title, subject, taskPeriod, metric, target, date ->
                viewModel.addPlanTask(title, subject, taskPeriod, metric, target, date)
                planOpen = false
            },
        )
    }

    if (examOpen) {
        AddExamDialog(
            track = data.profile.track,
            onDismiss = { examOpen = false },
            onAdd = { exam, sections, duration, reminder ->
                viewModel.addExamResult(exam, sections, duration, reminder)
                examOpen = false
            },
        )
    }

    val analysisExam = analysisExamId?.let { id -> data.nets.firstOrNull { it.id == id } }
    if (analysisExam != null) {
        ExamAnalysisDialog(
            exam = analysisExam,
            onDismiss = { analysisExamId = null },
            onSave = { note, lossTopics, reminder ->
                viewModel.saveExamAnalysis(analysisExam.id, note, lossTopics, reminder)
                analysisExamId = null
            },
        )
    }

    if (netGoalOpen) {
        NetGoalDialog(
            onDismiss = { netGoalOpen = false },
            onAdd = { exam, subject, target ->
                viewModel.addNetGoal(exam, subject, target)
                netGoalOpen = false
            },
        )
    }
}

@Composable
private fun LoadingScreen() {
    val colors = LocalTuyoColors.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.canvas),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Tuyo",
                style = MaterialTheme.typography.displaySmall,
                color = colors.textPrimary,
            )
            Text(
                text = "YKS için sakin bir yanlış defteri",
                style = MaterialTheme.typography.labelMedium,
                color = colors.textTertiary,
            )
        }
    }
}

@Composable
private fun BottomNav(
    selected: Destination,
    onSelect: (Destination) -> Unit,
) {
    val colors = LocalTuyoColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.canvas)
            .navigationBarsPadding(),
    ) {
        Hairline()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = Space.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Destination.entries.forEach { destination ->
                val isSelected = destination == selected
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(Radius.md))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.Tab,
                        ) { onSelect(destination) }
                        .padding(vertical = 5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .width(38.dp)
                            .height(24.dp)
                            .clip(RoundedCornerShape(Radius.sm))
                            .background(
                                if (isSelected) colors.selection else Color.Transparent,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = null,
                            tint = if (isSelected) colors.textPrimary else colors.textTertiary,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                    Text(
                        text = stringResource(destination.labelRes),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                        color = if (isSelected) colors.textPrimary else colors.textTertiary,
                    )
                }
            }
        }
    }
}
