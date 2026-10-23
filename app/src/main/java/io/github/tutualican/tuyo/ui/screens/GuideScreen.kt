package io.github.tutualican.tuyo.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.tutualican.tuyo.R
import io.github.tutualican.tuyo.domain.model.ExamComment
import io.github.tutualican.tuyo.domain.model.ExamOverview
import io.github.tutualican.tuyo.domain.model.ExamStat
import io.github.tutualican.tuyo.domain.model.MotivationNote
import io.github.tutualican.tuyo.domain.model.MotivationQuote
import io.github.tutualican.tuyo.domain.model.MotivationTheme
import io.github.tutualican.tuyo.domain.model.Source
import io.github.tutualican.tuyo.domain.model.StudyData
import io.github.tutualican.tuyo.domain.model.Tactic
import io.github.tutualican.tuyo.domain.model.TacticCategory
import io.github.tutualican.tuyo.domain.model.TedTalk
import io.github.tutualican.tuyo.ui.components.EvidenceTag
import io.github.tutualican.tuyo.ui.components.Eyebrow
import io.github.tutualican.tuyo.ui.components.Hairline
import io.github.tutualican.tuyo.ui.components.MetaText
import io.github.tutualican.tuyo.ui.components.ScreenHeader
import io.github.tutualican.tuyo.ui.components.Tag
import io.github.tutualican.tuyo.ui.components.TuyoButton
import io.github.tutualican.tuyo.ui.components.TuyoButtonVariant
import io.github.tutualican.tuyo.ui.components.TuyoCard
import io.github.tutualican.tuyo.ui.components.TuyoChoiceChip
import io.github.tutualican.tuyo.ui.components.TuyoSegmented
import io.github.tutualican.tuyo.ui.theme.LocalTuyoColors
import io.github.tutualican.tuyo.ui.theme.Motion
import io.github.tutualican.tuyo.ui.theme.Radius
import io.github.tutualican.tuyo.ui.theme.Space
import java.time.LocalDate

enum class GuideSection(val label: String) {
    TACTICS("Taktikler"),
    EXAM("Sınav"),
    MOTIVATION("Motivasyon"),
}

@Composable
fun GuideScreen(
    data: StudyData,
    focusedTacticId: String?,
    initialSection: GuideSection?,
    onSubmitTactic: () -> Unit,
    onOpenSource: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalTuyoColors.current
    var sectionIndex by rememberSaveable { mutableIntStateOf(0) }
    var expandedTactic by rememberSaveable { mutableStateOf<String?>(null) }
    var categoryName by rememberSaveable { mutableStateOf<String?>(null) }
    var motivationViewIndex by rememberSaveable { mutableIntStateOf(0) }
    var quoteThemeName by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(initialSection) {
        if (initialSection != null) sectionIndex = initialSection.ordinal
    }
    LaunchedEffect(focusedTacticId) {
        val focused = focusedTacticId
            ?.let { id -> data.tactics.firstOrNull { it.id == id } }
        if (focused != null) {
            sectionIndex = GuideSection.TACTICS.ordinal
            categoryName = focused.category.name
            expandedTactic = focused.id
        }
    }

    val selectedSection = GuideSection.entries[sectionIndex]
    val todayQuote = data.quotes[
        Math.floorMod(LocalDate.now().dayOfYear, data.quotes.size)
    ]
    val motivationViews = listOf(
        stringResource(R.string.motivation_view_quotes),
        stringResource(R.string.motivation_view_ted),
        stringResource(R.string.motivation_view_notes),
    )
    val visibleQuotes = data.quotes.filter {
        quoteThemeName == null || it.theme.name == quoteThemeName
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(Space.lg),
    ) {
        item {
            ScreenHeader(
                eyebrow = "KÜTÜPHANE",
                title = stringResource(R.string.guide_title),
                subtitle = stringResource(R.string.guide_subtitle),
            )
        }
        item {
            TuyoSegmented(
                labels = GuideSection.entries.map { it.label },
                selectedIndex = sectionIndex,
                onSelect = { sectionIndex = it },
            )
        }
        when (selectedSection) {
            GuideSection.TACTICS -> {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        TuyoChoiceChip(
                            label = "Tümü",
                            selected = categoryName == null,
                            onClick = { categoryName = null },
                        )
                        TacticCategory.entries.forEach { category ->
                            TuyoChoiceChip(
                                label = category.label,
                                selected = categoryName == category.name,
                                onClick = {
                                    categoryName = if (categoryName == category.name) {
                                        null
                                    } else {
                                        category.name
                                    }
                                },
                            )
                        }
                    }
                }
                val visibleTactics = data.tactics.filter {
                    categoryName == null || it.category.name == categoryName
                }
                items(visibleTactics, key = { it.id }) { tactic ->
                    TacticCard(
                        tactic = tactic,
                        expanded = expandedTactic == tactic.id,
                        onToggle = {
                            expandedTactic = if (expandedTactic == tactic.id) null else tactic.id
                        },
                    )
                }
                item {
                    BundleNote(onSubmitTactic = onSubmitTactic)
                }
            }

            GuideSection.EXAM -> {
                item {
                    Text(
                        text = stringResource(R.string.guide_exam_intro),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                    )
                }
                items(data.examOverviews, key = { it.id }) { overview ->
                    ExamOverviewCard(overview = overview)
                }
                item {
                    ExamStatsCard(stats = data.examStats)
                }
                item {
                    Eyebrow(stringResource(R.string.guide_exam_comments))
                }
                items(data.examComments, key = { it.id }) { comment ->
                    ExamCommentCard(comment = comment)
                }
                item {
                    SourcesCard(sources = data.sources, onOpenSource = onOpenSource)
                }
            }

            GuideSection.MOTIVATION -> {
                item {
                    MotivationHero(quote = todayQuote)
                }
                item {
                    Text(
                        text = stringResource(R.string.guide_motivation_intro),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                    )
                }
                item {
                    TuyoSegmented(
                        labels = motivationViews,
                        selectedIndex = motivationViewIndex,
                        onSelect = { motivationViewIndex = it },
                    )
                }
                when (motivationViewIndex) {
                    0 -> {
                        item {
                            Eyebrow(stringResource(R.string.motivation_quotes_title))
                        }
                        item {
                            Text(
                                text = stringResource(R.string.motivation_quotes_note),
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textTertiary,
                            )
                        }
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                TuyoChoiceChip(
                                    label = "Tümü",
                                    selected = quoteThemeName == null,
                                    onClick = { quoteThemeName = null },
                                )
                                MotivationTheme.entries.forEach { theme ->
                                    TuyoChoiceChip(
                                        label = theme.label,
                                        selected = quoteThemeName == theme.name,
                                        onClick = {
                                            quoteThemeName = if (quoteThemeName == theme.name) {
                                                null
                                            } else {
                                                theme.name
                                            }
                                        },
                                    )
                                }
                            }
                        }
                        items(visibleQuotes, key = { it.id }) { quote ->
                            MotivationQuoteCard(quote = quote)
                        }
                    }

                    1 -> {
                        item {
                            Eyebrow(stringResource(R.string.motivation_ted_title))
                        }
                        item {
                            Text(
                                text = stringResource(R.string.motivation_ted_note),
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textTertiary,
                            )
                        }
                        items(data.tedTalks, key = { it.id }) { talk ->
                            TedTalkCard(talk = talk, onOpenSource = onOpenSource)
                        }
                    }

                    else -> {
                        item {
                            Eyebrow(stringResource(R.string.motivation_notes_title))
                        }
                        item {
                            Text(
                                text = stringResource(R.string.motivation_notes_note),
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textTertiary,
                            )
                        }
                        items(data.motivation, key = { it.id }) { note ->
                            MotivationRow(note = note)
                        }
                    }
                }
                item {
                    SourcesCard(
                        sources = data.motivationSources,
                        onOpenSource = onOpenSource,
                    )
                }
            }
        }
    }
}

@Composable
private fun BundleNote(onSubmitTactic: () -> Unit) {
    val colors = LocalTuyoColors.current
    TuyoCard {
        Eyebrow(stringResource(R.string.contribution_eyebrow))
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.tactics_bundled_note),
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary,
        )
        Spacer(Modifier.height(Space.md))
        TuyoButton(
            text = stringResource(R.string.tactics_submit),
            onClick = onSubmitTactic,
            variant = TuyoButtonVariant.Secondary,
        )
    }
}

@Composable
private fun TacticCard(
    tactic: Tactic,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    val colors = LocalTuyoColors.current
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(Motion.base, easing = Motion.standard),
        label = "tacticChevron",
    )
    TuyoCard(onClick = onToggle) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            EvidenceTag(tactic.evidence)
            Spacer(Modifier.width(6.dp))
            Tag(text = tactic.category.label, color = colors.textTertiary)
            Spacer(Modifier.weight(1f))
            MetaText(
                "${stringResource(R.string.tactics_minutes, tactic.minutes)} · ${tactic.steps.size} adım",
            )
        }
        Spacer(Modifier.height(11.dp))
        Text(
            text = tactic.title,
            style = MaterialTheme.typography.titleLarge,
            color = colors.textPrimary,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = tactic.summary,
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary,
            maxLines = if (expanded) Int.MAX_VALUE else 2,
            overflow = TextOverflow.Ellipsis,
        )
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Column {
                Spacer(Modifier.height(14.dp))
                Hairline()
                tactic.steps.forEachIndexed { index, step ->
                    Row(
                        modifier = Modifier.padding(vertical = Space.md),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Text(
                            text = (index + 1).toString().padStart(2, '0'),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFeatureSettings = "tnum",
                            ),
                            color = colors.textTertiary,
                            modifier = Modifier
                                .width(22.dp)
                                .padding(top = 2.dp),
                        )
                        Text(
                            text = step,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                if (tactic.sourceNote != null) {
                    Text(
                        text = stringResource(R.string.tactics_source, tactic.sourceNote),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textTertiary,
                        modifier = Modifier.padding(bottom = Space.md),
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Radius.sm))
                .background(colors.selection.copy(alpha = 0.5f))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(
                    if (expanded) R.string.tactics_collapse else R.string.tactics_expand,
                ),
                style = MaterialTheme.typography.labelMedium,
                color = colors.textSecondary,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                tint = colors.textTertiary,
                modifier = Modifier
                    .size(15.dp)
                    .rotate(rotation),
            )
        }
    }
}

@Composable
private fun ExamOverviewCard(overview: ExamOverview) {
    val colors = LocalTuyoColors.current
    TuyoCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = overview.title,
                style = MaterialTheme.typography.titleLarge,
                color = colors.textPrimary,
            )
            Spacer(Modifier.weight(1f))
            Tag(text = overview.meta, color = colors.accent)
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = overview.summary,
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary,
        )
        Spacer(Modifier.height(12.dp))
        Hairline()
        overview.sections.forEach { section ->
            Row(
                modifier = Modifier.padding(vertical = 9.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = section.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = section.detail,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textTertiary,
                    )
                }
                Spacer(Modifier.width(Space.md))
                Text(
                    text = section.questions,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.accent,
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = overview.note,
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary,
        )
    }
}

@Composable
private fun ExamStatsCard(stats: List<ExamStat>) {
    val colors = LocalTuyoColors.current
    TuyoCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Eyebrow(stringResource(R.string.guide_exam_stats))
            Spacer(Modifier.weight(1f))
            Tag(text = "2026", color = colors.textTertiary)
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.guide_exam_stats_body),
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary,
        )
        Spacer(Modifier.height(12.dp))
        stats.forEach { stat ->
            Row(
                modifier = Modifier.padding(vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stat.test,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = stat.average,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFeatureSettings = "tnum",
                    ),
                    color = colors.accent,
                )
                Text(
                    text = " / ${stat.outOf}",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary,
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.guide_exam_stats_note),
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary,
        )
    }
}

@Composable
private fun ExamCommentCard(comment: ExamComment) {
    val colors = LocalTuyoColors.current
    TuyoCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Tag(text = comment.label, color = colors.accent)
            Spacer(Modifier.weight(1f))
        }
        Spacer(Modifier.height(9.dp))
        Text(
            text = comment.title,
            style = MaterialTheme.typography.titleLarge,
            color = colors.textPrimary,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = comment.body,
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = comment.takeaway,
            style = MaterialTheme.typography.bodySmall,
            color = colors.textPrimary,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Radius.sm))
                .background(colors.accentSoft)
                .padding(Space.md),
        )
    }
}

@Composable
private fun SourcesCard(sources: List<Source>, onOpenSource: (String) -> Unit) {
    val colors = LocalTuyoColors.current
    TuyoCard {
        Eyebrow(stringResource(R.string.guide_sources))
        Spacer(Modifier.height(10.dp))
        sources.forEachIndexed { index, source ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onOpenSource(source.url) },
                    )
                    .padding(vertical = 7.dp),
            ) {
                Text(
                    text = source.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = source.publisher + " · " + source.url,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (index != sources.lastIndex) Hairline()
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.guide_sources_note),
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary,
        )
    }
}

@Composable
private fun MotivationHero(quote: MotivationQuote) {
    val colors = LocalTuyoColors.current
    TuyoCard(highlighted = true) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Eyebrow(stringResource(R.string.today_motivation_eyebrow), color = colors.accent)
            Spacer(Modifier.weight(1f))
            Tag(text = quote.theme.label, color = colors.accent)
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = "“${quote.text}”",
            style = MaterialTheme.typography.headlineSmall,
            color = colors.textPrimary,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "— ${quote.author}",
            style = MaterialTheme.typography.labelLarge,
            color = colors.textSecondary,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = quote.source,
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.guide_motivation_hero_note),
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary,
        )
    }
}

@Composable
private fun MotivationQuoteCard(quote: MotivationQuote) {
    val colors = LocalTuyoColors.current
    TuyoCard(contentPadding = PaddingValues(horizontal = Space.lg, vertical = Space.md)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Tag(text = quote.theme.label, color = colors.textTertiary)
            Spacer(Modifier.weight(1f))
            Text(
                text = quote.source,
                style = MaterialTheme.typography.labelSmall,
                color = colors.textTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = "“${quote.text}”",
            style = MaterialTheme.typography.bodyLarge,
            color = colors.textPrimary,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "— ${quote.author}",
            style = MaterialTheme.typography.labelMedium,
            color = colors.accent,
        )
    }
}

@Composable
private fun TedTalkCard(talk: TedTalk, onOpenSource: (String) -> Unit) {
    val colors = LocalTuyoColors.current
    TuyoCard(onClick = { onOpenSource(talk.url) }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Tag(text = "TED", color = colors.accent)
            Spacer(Modifier.width(Space.sm))
            Text(
                text = talk.meta,
                style = MaterialTheme.typography.labelSmall,
                color = colors.textTertiary,
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = talk.title,
            style = MaterialTheme.typography.titleLarge,
            color = colors.textPrimary,
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = talk.speaker,
            style = MaterialTheme.typography.labelMedium,
            color = colors.textSecondary,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = talk.summary,
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.motivation_watch) + "  ↗",
            style = MaterialTheme.typography.labelMedium,
            color = colors.accent,
        )
    }
}

@Composable
private fun MotivationRow(note: MotivationNote) {
    val colors = LocalTuyoColors.current
    TuyoCard(contentPadding = PaddingValues(horizontal = Space.lg, vertical = Space.md)) {
        Row(verticalAlignment = Alignment.Top) {
            Tag(text = note.theme.label, color = colors.textTertiary)
            Spacer(Modifier.width(Space.md))
            Text(
                text = note.text,
                style = MaterialTheme.typography.bodyLarge,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
