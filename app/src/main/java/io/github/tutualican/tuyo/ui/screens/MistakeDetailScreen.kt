package io.github.tutualican.tuyo.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.tutualican.tuyo.R
import io.github.tutualican.tuyo.domain.ReviewOutcome
import io.github.tutualican.tuyo.domain.ReviewSchedule
import io.github.tutualican.tuyo.domain.model.Mistake
import io.github.tutualican.tuyo.domain.model.Tactic
import io.github.tutualican.tuyo.domain.model.Topic
import io.github.tutualican.tuyo.ui.components.EvidenceTag
import io.github.tutualican.tuyo.ui.components.Eyebrow
import io.github.tutualican.tuyo.ui.components.IconButtonGhost
import io.github.tutualican.tuyo.ui.components.MasteryDots
import io.github.tutualican.tuyo.ui.components.MetaText
import io.github.tutualican.tuyo.ui.components.Tag
import io.github.tutualican.tuyo.ui.components.TuyoButton
import io.github.tutualican.tuyo.ui.components.TuyoButtonVariant
import io.github.tutualican.tuyo.ui.components.TuyoCard
import io.github.tutualican.tuyo.ui.theme.LocalTuyoColors
import io.github.tutualican.tuyo.ui.theme.Radius
import io.github.tutualican.tuyo.ui.theme.Space
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun MistakeDetailScreen(
    mistake: Mistake,
    topic: Topic?,
    relatedTactic: Tactic?,
    onBack: () -> Unit,
    onOpenTactic: (String) -> Unit,
    modifier: Modifier = Modifier,
    onReview: (ReviewOutcome) -> Unit = {},
    onDelete: () -> Unit = {},
) {
    val colors = LocalTuyoColors.current
    var mastery by rememberSaveable(mistake.id) { mutableIntStateOf(mistake.mastery) }
    var dueEpoch by rememberSaveable(mistake.id) { mutableLongStateOf(mistake.dueOn.toEpochDay()) }
    var lastOutcome by rememberSaveable(mistake.id) { mutableStateOf<String?>(null) }
    var confirmDelete by rememberSaveable(mistake.id) { mutableStateOf(false) }

    val today = LocalDate.now()
    val due = LocalDate.ofEpochDay(dueEpoch)
    val dueText = when (val days = ChronoUnit.DAYS.between(today, due)) {
        in Long.MIN_VALUE until 0L -> stringResource(R.string.mistakes_due_overdue)
        0L -> "Bugün"
        1L -> "Yarın"
        else -> "$days gün sonra"
    }

    fun review(outcome: ReviewOutcome, label: String) {
        val plan = ReviewSchedule.plan(mastery, outcome, today)
        mastery = plan.mastery
        dueEpoch = plan.dueOn.toEpochDay()
        lastOutcome = label
        onReview(outcome)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(Space.xxl),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButtonGhost(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.detail_back),
                    onClick = onBack,
                )
                Spacer(Modifier.weight(1f))
                Tag(text = mistake.subject.label, color = colors.textSecondary)
            }
        }
        item {
            Column {
                Eyebrow(mistake.questionRef)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = mistake.topicTitle,
                    style = MaterialTheme.typography.displaySmall,
                    color = colors.textPrimary,
                )
                if (mistake.note.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = mistake.note,
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.textSecondary,
                    )
                }
            }
        }
        item { LoopTimeline(mastery = mastery) }
        item {
            Column {
                Eyebrow(stringResource(R.string.detail_why))
                Spacer(Modifier.height(8.dp))
                TuyoCard(contentPadding = PaddingValues(Space.lg)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Tag(text = mistake.errorKind.label, color = colors.danger)
                        Spacer(Modifier.weight(1f))
                        MetaText("Kalıcı kayıt")
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = mistake.errorKind.advice,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                    )
                }
            }
        }
        if (topic != null) {
            item {
                Column {
                    Eyebrow(stringResource(R.string.detail_critical), color = colors.accent)
                    Spacer(Modifier.height(8.dp))
                    TuyoCard(highlighted = true) {
                        Text(
                            text = topic.keyIdea,
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.textPrimary,
                        )
                    }
                }
            }
        }
        item {
            Column {
                Eyebrow(stringResource(R.string.detail_review))
                Spacer(Modifier.height(8.dp))
                TuyoCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Sonraki tekrar · $dueText",
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.textPrimary,
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = stringResource(R.string.detail_review_body),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary,
                            )
                        }
                        Spacer(Modifier.width(Space.md))
                        MasteryDots(level = mastery)
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                        ReviewButton(
                            label = "Tekrar",
                            modifier = Modifier.weight(1f),
                            onClick = { review(ReviewOutcome.AGAIN, "Tekrar") },
                        )
                        ReviewButton(
                            label = "Zor",
                            modifier = Modifier.weight(1f),
                            onClick = { review(ReviewOutcome.HARD, "Zor") },
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                        ReviewButton(
                            label = "İyi",
                            modifier = Modifier.weight(1f),
                            onClick = { review(ReviewOutcome.GOOD, "İyi") },
                            primary = true,
                        )
                        ReviewButton(
                            label = "Kolay",
                            modifier = Modifier.weight(1f),
                            onClick = { review(ReviewOutcome.EASY, "Kolay") },
                        )
                    }
                    AnimatedVisibility(
                        visible = lastOutcome != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically(),
                    ) {
                        Row(
                            modifier = Modifier.padding(top = Space.md),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = colors.success,
                                modifier = Modifier.size(14.dp),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "$lastOutcome · $dueText · $mastery/5",
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.success,
                            )
                        }
                    }
                }
            }
        }
        if (relatedTactic != null) {
            item {
                Column {
                    Eyebrow(stringResource(R.string.detail_related_tactic))
                    Spacer(Modifier.height(8.dp))
                    TuyoCard(onClick = { onOpenTactic(relatedTactic.id) }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            EvidenceTag(relatedTactic.evidence)
                            Spacer(Modifier.weight(1f))
                            MetaText(stringResource(R.string.tactics_minutes, relatedTactic.minutes))
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = relatedTactic.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = colors.textPrimary,
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = relatedTactic.summary,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
        item {
            TuyoButton(
                text = stringResource(R.string.detail_delete),
                onClick = { confirmDelete = true },
                variant = TuyoButtonVariant.Ghost,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.detail_delete_title)) },
            text = { Text(stringResource(R.string.detail_delete_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDelete = false
                        onDelete()
                    },
                ) {
                    Text(stringResource(R.string.detail_delete_confirm), color = colors.danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text(stringResource(R.string.detail_delete_cancel))
                }
            },
        )
    }
}

@Composable
private fun ReviewButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = false,
) {
    TuyoButton(
        text = label,
        onClick = onClick,
        modifier = modifier,
        variant = if (primary) TuyoButtonVariant.Primary else TuyoButtonVariant.Secondary,
    )
}

@Composable
private fun LoopTimeline(mastery: Int) {
    val colors = LocalTuyoColors.current
    Column {
        Eyebrow(stringResource(R.string.detail_loop))
        Spacer(Modifier.height(10.dp))
        TimelineStep(
            letter = "1",
            title = "Yanlış kaydedildi",
            body = "Soru, konu ve hata türü deftere işlendi.",
            done = true,
            active = false,
            last = false,
        )
        TimelineStep(
            letter = "2",
            title = "Konu tekrarı",
            body = "Kritik bilgiyi oku, kafa karışıklığını kapat.",
            done = mastery >= 1,
            active = mastery < 1,
            last = false,
        )
        TimelineStep(
            letter = "3",
            title = "Soru tekrarı",
            body = "Aralıklı tekrar ile aynı soruyu yeniden çöz.",
            done = mastery >= 3,
            active = mastery in 1..2,
            last = true,
        )
    }
}

@Composable
private fun TimelineStep(
    letter: String,
    title: String,
    body: String,
    done: Boolean,
    active: Boolean,
    last: Boolean,
) {
    val colors = LocalTuyoColors.current
    val circleColor = when {
        done -> colors.success
        active -> colors.accent
        else -> colors.hairlineStrong
    }
    Row {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(RoundedCornerShape(Radius.pill))
                    .background(if (done || active) circleColor.copy(alpha = 0.14f) else Color.Transparent)
                    .then(
                        if (!done && !active) {
                            Modifier.background(colors.raised, RoundedCornerShape(Radius.pill))
                        } else {
                            Modifier
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (done) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = colors.success,
                        modifier = Modifier.size(12.dp),
                    )
                } else {
                    Text(
                        text = letter,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (active) colors.accent else colors.textTertiary,
                    )
                }
            }
            if (!last) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(34.dp)
                        .background(colors.hairline),
                )
            }
        }
        Spacer(Modifier.width(Space.md))
        Column(
            modifier = Modifier.padding(bottom = if (last) 0.dp else 6.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (done || active) colors.textPrimary else colors.textTertiary,
                )
                Spacer(Modifier.width(Space.sm))
                Tag(
                    text = when {
                        done -> stringResource(R.string.detail_step_done)
                        active -> stringResource(R.string.detail_step_active)
                        else -> stringResource(R.string.detail_step_waiting)
                    },
                    color = when {
                        done -> colors.success
                        active -> colors.accent
                        else -> colors.textTertiary
                    },
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
            )
        }
    }
}
