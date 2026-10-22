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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.tutualican.tuyo.R
import io.github.tutualican.tuyo.data.SampleContent
import io.github.tutualican.tuyo.domain.model.Mistake
import io.github.tutualican.tuyo.ui.components.CommandEntry
import io.github.tutualican.tuyo.ui.components.CommandPalette
import io.github.tutualican.tuyo.ui.components.Hairline
import io.github.tutualican.tuyo.ui.screens.AddMistakeDialog
import io.github.tutualican.tuyo.ui.screens.MistakeDetailScreen
import io.github.tutualican.tuyo.ui.screens.MistakesScreen
import io.github.tutualican.tuyo.ui.screens.TacticsScreen
import io.github.tutualican.tuyo.ui.screens.TodayScreen
import io.github.tutualican.tuyo.ui.screens.TopicsScreen
import io.github.tutualican.tuyo.ui.theme.LocalTuyoColors
import io.github.tutualican.tuyo.ui.theme.Motion
import io.github.tutualican.tuyo.ui.theme.Radius
import io.github.tutualican.tuyo.ui.theme.Space

enum class Destination(
    val labelRes: Int,
    val icon: ImageVector,
) {
    TODAY(R.string.nav_today, Icons.Filled.Home),
    MISTAKES(R.string.nav_mistakes, Icons.AutoMirrored.Filled.List),
    TOPICS(R.string.nav_topics, Icons.Filled.Info),
    TACTICS(R.string.nav_tactics, Icons.Filled.Star),
}

@Composable
fun TuyoApp() {
    val colors = LocalTuyoColors.current
    var data by remember { mutableStateOf(SampleContent.studyData()) }
    var destination by rememberSaveable { mutableStateOf(Destination.TODAY) }
    var detailId by rememberSaveable { mutableStateOf<String?>(null) }
    var tacticFocusId by rememberSaveable { mutableStateOf<String?>(null) }
    var paletteOpen by remember { mutableStateOf(false) }
    var addOpen by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    BackHandler(enabled = detailId != null) { detailId = null }

    fun openDestination(next: Destination) {
        destination = next
        detailId = null
    }

    fun openMistake(id: String) {
        detailId = id
    }

    fun openTactic(id: String) {
        tacticFocusId = id
        openDestination(Destination.TACTICS)
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
            id = "tactics",
            title = stringResource(R.string.nav_tactics),
            section = stringResource(R.string.command_section_go),
            icon = Icons.Filled.Star,
        ) { openDestination(Destination.TACTICS) },
        CommandEntry(
            id = "new-mistake",
            title = stringResource(R.string.command_action_new_mistake),
            section = stringResource(R.string.command_section_actions),
            icon = Icons.Filled.Add,
        ) { addOpen = true },
        CommandEntry(
            id = "review",
            title = stringResource(R.string.command_action_review),
            section = stringResource(R.string.command_section_actions),
            icon = Icons.Filled.Refresh,
        ) { openDestination(Destination.MISTAKES) },
    )

    val screenKey = detailId?.let { "detail:$it" } ?: destination.name

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
                        if (targetState.startsWith("detail:")) {
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
                    if (key.startsWith("detail:")) {
                        val mistake = data.mistakes.firstOrNull { it.id == key.removePrefix("detail:") }
                        if (mistake == null) {
                            Box(Modifier.fillMaxSize())
                        } else {
                            MistakeDetailScreen(
                                mistake = mistake,
                                topic = data.topics.firstOrNull { it.id == mistake.topicId },
                                relatedTactic = data.tactics.firstOrNull {
                                    mistake.topicId in it.topicIds
                                },
                                onBack = { detailId = null },
                                onOpenTactic = ::openTactic,
                                onReview = { plan ->
                                    data = data.copy(
                                        mistakes = data.mistakes.map { item ->
                                            if (item.id == mistake.id) {
                                                item.copy(
                                                    mastery = plan.mastery,
                                                    dueOn = plan.dueOn,
                                                )
                                            } else {
                                                item
                                            }
                                        },
                                    )
                                },
                            )
                        }
                    } else {
                        when (Destination.valueOf(key)) {
                            Destination.TODAY -> TodayScreen(
                                data = data,
                                onOpenMistake = ::openMistake,
                                onSeeAllMistakes = { openDestination(Destination.MISTAKES) },
                                onOpenTactic = ::openTactic,
                                onOpenPalette = { paletteOpen = true },
                                onAddMistake = { addOpen = true },
                            )
                            Destination.MISTAKES -> MistakesScreen(
                                data = data,
                                onOpenMistake = ::openMistake,
                                onAddMistake = { addOpen = true },
                            )
                            Destination.TOPICS -> TopicsScreen(topics = data.topics)
                            Destination.TACTICS -> TacticsScreen(
                                tactics = data.tactics,
                                focusedId = tacticFocusId,
                            )
                        }
                    }
                }
            }
            BottomNav(
                selected = destination,
                onSelect = { next ->
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
            onDismiss = { addOpen = false },
            onAdd = { mistake: Mistake ->
                data = data.copy(mistakes = listOf(mistake) + data.mistakes)
                addOpen = false
                destination = Destination.MISTAKES
                detailId = mistake.id
            },
        )
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
