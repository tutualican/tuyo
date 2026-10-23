package io.github.tutualican.tuyo.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.tutualican.tuyo.R
import io.github.tutualican.tuyo.domain.TopicStat
import io.github.tutualican.tuyo.domain.model.Mistake
import io.github.tutualican.tuyo.domain.model.Subject
import io.github.tutualican.tuyo.domain.model.Topic
import io.github.tutualican.tuyo.domain.model.TopicProgress
import io.github.tutualican.tuyo.domain.model.TopicStatus
import io.github.tutualican.tuyo.domain.topicStats
import io.github.tutualican.tuyo.ui.components.Eyebrow
import io.github.tutualican.tuyo.ui.components.Hairline
import io.github.tutualican.tuyo.ui.components.MetaText
import io.github.tutualican.tuyo.ui.components.ProgressLine
import io.github.tutualican.tuyo.ui.components.ScreenHeader
import io.github.tutualican.tuyo.ui.components.Tag
import io.github.tutualican.tuyo.ui.components.TuyoCard
import io.github.tutualican.tuyo.ui.components.TuyoChoiceChip
import io.github.tutualican.tuyo.ui.components.TuyoSegmented
import io.github.tutualican.tuyo.ui.components.TuyoTextAction
import io.github.tutualican.tuyo.ui.theme.LocalTuyoColors
import io.github.tutualican.tuyo.ui.theme.Motion
import io.github.tutualican.tuyo.ui.theme.Radius
import io.github.tutualican.tuyo.ui.theme.Space

@Composable
fun TopicsScreen(
    topics: List<Topic>,
    mistakes: List<Mistake>,
    onAddMistake: (String?) -> Unit,
    topicProgress: List<TopicProgress> = emptyList(),
    onSetTopicStatus: (String, TopicStatus) -> Unit = { _, _ -> },
    onReviewTopic: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val colors = LocalTuyoColors.current
    var query by rememberSaveable { mutableStateOf("") }
    var expanded by rememberSaveable { mutableStateOf<String?>(null) }
    var section by rememberSaveable { mutableIntStateOf(if (mistakes.isEmpty()) 1 else 0) }

    val stats = mistakes.topicStats(topics, progress = topicProgress)
    val needle = query.trim()
    val searchFilter: (TopicStat) -> Boolean = { stat ->
        needle.isEmpty() ||
            stat.topic.title.contains(needle, ignoreCase = true) ||
            stat.topic.summary.contains(needle, ignoreCase = true) ||
            stat.topic.confusions.any {
                it.question.contains(needle, ignoreCase = true) ||
                    it.answer.contains(needle, ignoreCase = true)
            }
    }
    val weak = remember(stats, needle) {
        stats
            .filter { it.hasUserData && it.isWeak && searchFilter(it) }
            .sortedWith(compareByDescending<TopicStat> { it.dueCount }.thenBy { it.mastery })
    }
    val allGrouped = remember(stats, needle) {
        Subject.entries.mapNotNull { subject ->
            val items = stats
                .filter { it.topic.subject == subject && searchFilter(it) }
            if (items.isEmpty()) null else subject to items
        }
    }
    val filteredWeakOnly = section == 0
    val searching = needle.isNotEmpty() && mistakes.isNotEmpty()
    val dueTotal = stats.sumOf { it.dueCount }
    val trackedTopics = stats.count { it.hasUserData }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(Space.xl),
    ) {
        item {
            ScreenHeader(
                eyebrow = stringResource(R.string.topics_eyebrow),
                title = stringResource(R.string.topics_title),
                subtitle = stringResource(R.string.topics_subtitle),
            )
        }
        if (trackedTopics > 0) {
            item {
                TuyoCard(contentPadding = PaddingValues(horizontal = Space.lg, vertical = Space.md)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.topics_summary, trackedTopics, dueTotal),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textPrimary,
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(Modifier.width(Space.sm))
                        Text(
                            text = stringResource(R.string.topics_summary_source),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textTertiary,
                        )
                    }
                }
            }
        }
        item {
            SearchField(
                value = query,
                onValueChange = { query = it },
                placeholder = stringResource(R.string.topics_search),
            )
        }
        item {
            TuyoSegmented(
                labels = listOf(
                    stringResource(R.string.topics_weak_tab),
                    stringResource(R.string.topics_all_tab),
                ),
                selectedIndex = section,
                onSelect = { section = it },
            )
        }
        if (filteredWeakOnly) {
            if (weak.isEmpty()) {
                item {
                    TuyoCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = Space.lg),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = when {
                                    mistakes.isEmpty() -> stringResource(R.string.topics_no_data_title)
                                    searching -> stringResource(R.string.topics_empty)
                                    else -> stringResource(R.string.topics_no_weak_title)
                                },
                                style = MaterialTheme.typography.titleLarge,
                                color = colors.textPrimary,
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = when {
                                    mistakes.isEmpty() -> stringResource(R.string.topics_no_data_body)
                                    searching -> stringResource(R.string.topics_no_match_body)
                                    else -> stringResource(R.string.topics_no_weak_body)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary,
                            )
                        }
                    }
                }
            } else {
                item {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Eyebrow(stringResource(R.string.topics_weak_eyebrow))
                            Spacer(Modifier.width(Space.sm))
                            Hairline(modifier = Modifier.weight(1f))
                            Spacer(Modifier.width(Space.sm))
                            MetaText("${weak.size}")
                        }
                    }
                }
                item {
                    TuyoCard(contentPadding = PaddingValues(horizontal = Space.lg, vertical = 2.dp)) {
                        weak.forEachIndexed { index, stat ->
                            TopicRow(
                                stat = stat,
                                expanded = expanded == stat.topic.id,
                                onToggle = {
                                    expanded = if (expanded == stat.topic.id) null else stat.topic.id
                                },
                                onAddMistake = { onAddMistake(stat.topic.id) },
                                onSetStatus = { status ->
                                    onSetTopicStatus(stat.topic.id, status)
                                },
                                onReview = { onReviewTopic(stat.topic.id) },
                            )
                            if (index != weak.lastIndex) Hairline()
                        }
                    }
                }
            }
        } else {
            if (allGrouped.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.topics_empty),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textTertiary,
                        )
                    }
                }
            }
            allGrouped.forEach { (subject, items) ->
                item(key = "header-${subject.name}") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Eyebrow(subject.label)
                        Spacer(Modifier.width(Space.sm))
                        Hairline(modifier = Modifier.weight(1f))
                        Spacer(Modifier.width(Space.sm))
                        MetaText("${items.size}")
                    }
                }
                item(key = "group-${subject.name}") {
                    TuyoCard(contentPadding = PaddingValues(horizontal = Space.lg, vertical = 2.dp)) {
                        items.forEachIndexed { index, stat ->
                            TopicRow(
                                stat = stat,
                                expanded = expanded == stat.topic.id,
                                onToggle = {
                                    expanded = if (expanded == stat.topic.id) null else stat.topic.id
                                },
                                onAddMistake = { onAddMistake(stat.topic.id) },
                                onSetStatus = { status ->
                                    onSetTopicStatus(stat.topic.id, status)
                                },
                                onReview = { onReviewTopic(stat.topic.id) },
                            )
                            if (index != items.lastIndex) Hairline()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
) {
    val colors = LocalTuyoColors.current
    val shape = RoundedCornerShape(Radius.md)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(shape)
            .background(colors.surface)
            .border(1.dp, colors.hairline, shape)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = null,
            tint = colors.textTertiary,
            modifier = Modifier.size(15.dp),
        )
        Spacer(Modifier.width(9.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodySmall.copy(color = colors.textPrimary),
            cursorBrush = SolidColor(colors.accent),
            decorationBox = { inner ->
                Box {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textTertiary,
                        )
                    }
                    inner()
                }
            },
        )
        if (value.isNotEmpty()) {
            Text(
                text = stringResource(R.string.topics_clear),
                style = MaterialTheme.typography.labelSmall,
                color = colors.textTertiary,
                modifier = Modifier
                    .clip(RoundedCornerShape(Radius.sm))
                    .clickable { onValueChange("") }
                    .padding(horizontal = 4.dp, vertical = 2.dp),
            )
        }
    }
}

@Composable
private fun TopicRow(
    stat: TopicStat,
    expanded: Boolean,
    onToggle: () -> Unit,
    onAddMistake: () -> Unit,
    onSetStatus: (TopicStatus) -> Unit,
    onReview: () -> Unit,
) {
    val colors = LocalTuyoColors.current
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(Motion.base, easing = Motion.standard),
        label = "topicChevron",
    )
    val statusColor = when (stat.status) {
        TopicStatus.DONE -> colors.success
        TopicStatus.IN_PROGRESS -> colors.accent
        TopicStatus.NEEDS_REVIEW -> colors.warning
        TopicStatus.NOT_STARTED -> colors.textTertiary
    }
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onToggle,
                )
                .padding(vertical = Space.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stat.topic.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stat.topic.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(9.dp))
                if (stat.hasUserData) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ProgressLine(
                            fraction = stat.masteryFraction,
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(Modifier.width(Space.sm))
                        MetaText(
                            text = stringResource(
                                R.string.topics_progress,
                                ((stat.mastery ?: 0) / 5f * 100).toInt(),
                            ),
                        )
                    }
                    Spacer(Modifier.height(7.dp))
                    MetaText(
                        text = stringResource(
                            R.string.topics_user_stats,
                            stat.mistakeCount,
                            stat.dueCount,
                        ),
                    )
                } else {
                    MetaText(text = stringResource(R.string.topics_no_record))
                }
            }
            if (stat.status != TopicStatus.NOT_STARTED) {
                Tag(text = stat.status.label, color = statusColor)
                Spacer(Modifier.width(Space.sm))
            }
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                tint = colors.textTertiary,
                modifier = Modifier
                    .size(18.dp)
                    .rotate(rotation),
            )
        }
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Column {
                Hairline()
                Spacer(Modifier.height(Space.md))
                Text(
                    text = "Konu durumu",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary,
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    TopicStatus.entries.forEach { status ->
                        TuyoChoiceChip(
                            label = status.label,
                            selected = stat.status == status,
                            onClick = { onSetStatus(status) },
                        )
                    }
                }
                if (stat.reviewDue) {
                    Spacer(Modifier.height(Space.sm))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Tag(text = "Tekrar günü", color = colors.warning)
                        Spacer(Modifier.weight(1f))
                        TuyoTextAction(
                            text = "Tekrar ettim",
                            onClick = onReview,
                            leadingIcon = Icons.Filled.Check,
                            trailingArrow = false,
                        )
                    }
                }
                Spacer(Modifier.height(Space.sm))
                Hairline()
                stat.topic.confusions.forEach { confusion ->
                    Column(modifier = Modifier.padding(vertical = Space.md)) {
                        Row(verticalAlignment = Alignment.Top) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(RoundedCornerShape(Radius.pill))
                                    .background(colors.accentSoft),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "?",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = colors.accent,
                                )
                            }
                            Spacer(Modifier.width(Space.sm))
                            Text(
                                text = confusion.question,
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.textPrimary,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Spacer(Modifier.height(5.dp))
                        Text(
                            text = confusion.answer,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary,
                            modifier = Modifier.padding(start = 26.dp),
                        )
                    }
                }
                TuyoTextAction(
                    text = stringResource(R.string.topics_add_mistake),
                    onClick = onAddMistake,
                    leadingIcon = Icons.Filled.Add,
                    trailingArrow = false,
                )
                Spacer(Modifier.height(Space.sm))
            }
        }
    }
}
