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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.tutualican.tuyo.R
import io.github.tutualican.tuyo.data.StudyDistributions
import io.github.tutualican.tuyo.domain.TopicStat
import io.github.tutualican.tuyo.domain.TopicTrend
import io.github.tutualican.tuyo.domain.TopicWeight
import io.github.tutualican.tuyo.domain.WeeklySummary
import io.github.tutualican.tuyo.domain.model.DistributionRow
import io.github.tutualican.tuyo.domain.model.DistributionSection
import io.github.tutualican.tuyo.domain.model.ExamDistribution
import io.github.tutualican.tuyo.domain.model.Mistake
import io.github.tutualican.tuyo.domain.model.Subject
import io.github.tutualican.tuyo.domain.model.Topic
import io.github.tutualican.tuyo.domain.model.TopicProgress
import io.github.tutualican.tuyo.domain.model.TopicStatus
import io.github.tutualican.tuyo.domain.model.periodLabel
import io.github.tutualican.tuyo.domain.model.rows
import io.github.tutualican.tuyo.domain.model.totalQuestions
import io.github.tutualican.tuyo.domain.toTopicWeight
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
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

@Composable
fun TopicsScreen(
    topics: List<Topic>,
    mistakes: List<Mistake>,
    onAddMistake: (String?) -> Unit,
    topicProgress: List<TopicProgress> = emptyList(),
    weeklySummary: WeeklySummary? = null,
    onSetTopicStatus: (String, TopicStatus) -> Unit = { _, _ -> },
    onReviewTopic: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val colors = LocalTuyoColors.current
    var query by rememberSaveable { mutableStateOf("") }
    var expanded by rememberSaveable { mutableStateOf<String?>(null) }
    var section by rememberSaveable { mutableIntStateOf(if (mistakes.isEmpty()) 1 else 0) }
    var distributionExamIndex by rememberSaveable { mutableIntStateOf(0) }
    var distributionSectionId by rememberSaveable { mutableStateOf<String?>(null) }
    var distributionQuery by rememberSaveable { mutableStateOf("") }

    val distributionExam = StudyDistributions.exams[
        distributionExamIndex.coerceIn(0, StudyDistributions.exams.lastIndex)
    ]
    val distributionSection = distributionExam.sections.firstOrNull {
        it.id == distributionSectionId
    } ?: distributionExam.sections.first()
    val distributionNeedle = distributionQuery.trim()

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
    val today = remember { LocalDate.now() }
    val weekStart = remember(today) {
        today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    }
    val weeklyMarked = remember(topicProgress, weekStart, today) {
        topicProgress.count { !it.updatedOn.isBefore(weekStart) && !it.updatedOn.isAfter(today) }
    }
    val weeklyDone = remember(topicProgress, weekStart, today) {
        topicProgress.count {
            it.status == TopicStatus.DONE &&
                !it.updatedOn.isBefore(weekStart) &&
                !it.updatedOn.isAfter(today)
        }
    }
    val progressById = remember(topicProgress) {
        topicProgress.associateBy { it.topicId }
    }
    val distributionResults = remember(distributionExam, distributionNeedle) {
        if (distributionNeedle.isEmpty()) {
            emptyList()
        } else {
            distributionExam.sections.flatMap { distributionSection ->
                val sectionMatches =
                    distributionSection.title.contains(distributionNeedle, ignoreCase = true) ||
                        distributionSection.subject.label.contains(
                            distributionNeedle,
                            ignoreCase = true,
                        )
                distributionSection.rows()
                    .filter {
                        sectionMatches || it.title.contains(distributionNeedle, ignoreCase = true)
                    }
                    .map { distributionSection to it }
            }
        }
    }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(Space.xl),
    ) {
        item {
            val showingDistribution = section == 2
            ScreenHeader(
                eyebrow = stringResource(
                    if (showingDistribution) R.string.topics_distribution_eyebrow
                    else R.string.topics_eyebrow
                ),
                title = stringResource(
                    if (showingDistribution) R.string.topics_distribution_title
                    else R.string.topics_title
                ),
                subtitle = stringResource(
                    if (showingDistribution) R.string.topics_distribution_subtitle
                    else R.string.topics_subtitle
                ),
            )
        }
        if (trackedTopics > 0 && section != 2) {
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
                value = if (section == 2) distributionQuery else query,
                onValueChange = {
                    if (section == 2) distributionQuery = it else query = it
                },
                placeholder = stringResource(
                    if (section == 2) R.string.topics_distribution_search
                    else R.string.topics_search
                ),
            )
        }
        item {
            TuyoSegmented(
                labels = listOf(
                    stringResource(R.string.topics_weak_tab),
                    stringResource(R.string.topics_all_tab),
                    stringResource(R.string.topics_distribution_tab),
                ),
                selectedIndex = section,
                onSelect = { section = it },
            )
        }
        if (section == 0) {
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
        } else if (section == 1) {
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
        } else {
            item {
                DistributionMenu(
                    exams = StudyDistributions.exams,
                    selectedExamIndex = distributionExamIndex.coerceIn(
                        0,
                        StudyDistributions.exams.lastIndex,
                    ),
                    onSelectExam = {
                        distributionExamIndex = it
                        distributionSectionId = null
                    },
                    exam = distributionExam,
                    selectedSectionId = distributionSection.id,
                    onSelectSection = { distributionSectionId = it },
                    showSubjects = distributionNeedle.isEmpty(),
                )
            }
            item {
                DistributionWeekCard(
                    start = weekStart,
                    end = today,
                    marked = weeklyMarked,
                    done = weeklyDone,
                    weekly = weeklySummary,
                )
            }
            if (distributionNeedle.isEmpty()) {
                item {
                    DistributionSectionHeader(section = distributionSection)
                }
                item {
                    DistributionRowsCard(
                        rows = distributionSection.rows(),
                        years = distributionSection.years,
                        progressById = progressById,
                        onToggledDone = { row ->
                            val done = progressById[row.id]?.status == TopicStatus.DONE
                            onSetTopicStatus(
                                row.id,
                                if (done) TopicStatus.NOT_STARTED else TopicStatus.DONE,
                            )
                        },
                    )
                }
            } else {
                item {
                    DistributionResultsHeader(
                        count = distributionResults.size,
                    )
                }
                if (distributionResults.isEmpty()) {
                    item {
                        TuyoCard {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = Space.lg),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = stringResource(R.string.topics_distribution_empty_title),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = colors.textPrimary,
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = stringResource(R.string.topics_distribution_empty_body),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary,
                                )
                            }
                        }
                    }
                } else {
                    item {
                        DistributionResultsCard(
                            results = distributionResults,
                            progressById = progressById,
                            onToggledDone = { row ->
                                val done = progressById[row.id]?.status == TopicStatus.DONE
                                onSetTopicStatus(
                                    row.id,
                                    if (done) TopicStatus.NOT_STARTED else TopicStatus.DONE,
                                )
                            },
                        )
                    }
                }
            }
            item {
                DistributionSourceCard()
            }
        }
    }
}

@Composable
private fun DistributionMenu(
    exams: List<ExamDistribution>,
    selectedExamIndex: Int,
    onSelectExam: (Int) -> Unit,
    exam: ExamDistribution,
    selectedSectionId: String,
    onSelectSection: (String) -> Unit,
    showSubjects: Boolean,
) {
    Column {
        Eyebrow(stringResource(R.string.topics_distribution_exam_label))
        Spacer(Modifier.height(6.dp))
        TuyoSegmented(
            labels = exams.map { it.label },
            selectedIndex = selectedExamIndex,
            onSelect = onSelectExam,
        )
        Spacer(Modifier.height(6.dp))
        MetaText(exam.subtitle)
        if (showSubjects) {
            Spacer(Modifier.height(Space.md))
            Eyebrow(stringResource(R.string.topics_distribution_subject_label))
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                exam.sections.forEach { distributionSection ->
                    TuyoChoiceChip(
                        label = distributionSection.title,
                        selected = distributionSection.id == selectedSectionId,
                        onClick = { onSelectSection(distributionSection.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun DistributionWeekCard(
    start: LocalDate,
    end: LocalDate,
    marked: Int,
    done: Int,
    weekly: WeeklySummary?,
) {
    val colors = LocalTuyoColors.current
    val formatter = remember { DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("tr")) }
    val questions = weekly?.questions ?: 0
    val minutes = weekly?.minutes ?: 0
    TuyoCard(contentPadding = PaddingValues(horizontal = Space.lg, vertical = Space.md)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Eyebrow(stringResource(R.string.topics_distribution_week_title))
            Spacer(Modifier.width(Space.sm))
            Hairline(modifier = Modifier.weight(1f))
            Spacer(Modifier.width(Space.sm))
            MetaText(
                stringResource(
                    R.string.topics_distribution_week_range,
                    start.format(formatter),
                    end.format(formatter),
                ),
            )
        }
        Spacer(Modifier.height(Space.md))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Space.sm),
        ) {
            WeeklyMetric(
                value = marked.toString(),
                label = stringResource(R.string.topics_distribution_week_mark_label),
                modifier = Modifier.weight(1f),
            )
            WeeklyMetric(
                value = done.toString(),
                label = stringResource(R.string.topics_distribution_week_done_label),
                modifier = Modifier.weight(1f),
            )
            WeeklyMetric(
                value = questions.toString(),
                label = stringResource(R.string.topics_distribution_week_question_label),
                modifier = Modifier.weight(1f),
            )
            WeeklyMetric(
                value = minutes.toString(),
                label = stringResource(R.string.topics_distribution_week_minute_label),
                modifier = Modifier.weight(1f),
            )
        }
        if (marked == 0 && questions == 0) {
            Spacer(Modifier.height(Space.sm))
            Text(
                text = stringResource(R.string.topics_distribution_week_empty),
                style = MaterialTheme.typography.labelSmall,
                color = colors.textTertiary,
            )
        }
    }
}

@Composable
private fun WeeklyMetric(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    val colors = LocalTuyoColors.current
    Column(modifier = modifier) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary,
            maxLines = 1,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun formatYearly(years: List<Int>, values: List<Int>): String =
    years.zip(values).joinToString(" · ") { (year, count) -> "$year $count" }

@Composable
private fun DistributionSectionHeader(section: DistributionSection) {
    val colors = LocalTuyoColors.current
    val yearlyLabel = formatYearly(section.years, section.yearlyTotals)
    val rowYearly = section.years.indices.map { year ->
        section.topics.sumOf { it.yearly[year] }
    }
    val hasYearlyMismatch = rowYearly != section.yearlyTotals
    TuyoCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = section.title,
                style = MaterialTheme.typography.titleLarge,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(Space.sm))
            Tag(text = section.exam.label, color = colors.accent)
        }
        Spacer(Modifier.height(4.dp))
        MetaText(
            stringResource(
                R.string.topics_distribution_section_meta,
                section.periodLabel,
                section.years.size,
            ),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.topics_distribution_section_years, yearlyLabel),
            style = MaterialTheme.typography.labelSmall,
            color = colors.textSecondary,
        )
        if (hasYearlyMismatch) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.topics_distribution_year_mismatch),
                style = MaterialTheme.typography.labelSmall,
                color = colors.textTertiary,
            )
        }
        Spacer(Modifier.height(4.dp))
        MetaText(
            stringResource(
                R.string.topics_distribution_section_summary,
                section.totalQuestions,
                section.topics.size,
            ),
        )
        if (section.note != null) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = section.note,
                style = MaterialTheme.typography.labelSmall,
                color = colors.warning,
            )
        }
    }
}

@Composable
private fun DistributionRowsCard(
    rows: List<DistributionRow>,
    years: List<Int>,
    progressById: Map<String, TopicProgress>,
    onToggledDone: (DistributionRow) -> Unit,
) {
    val maxTotal = rows.maxOfOrNull { it.total }?.coerceAtLeast(1) ?: 1
    TuyoCard(contentPadding = PaddingValues(horizontal = Space.lg, vertical = 2.dp)) {
        rows.forEachIndexed { index, row ->
            DistributionRowItem(
                row = row,
                years = years,
                maxTotal = maxTotal,
                progress = progressById[row.id],
                onToggledDone = onToggledDone,
            )
            if (index != rows.lastIndex) Hairline()
        }
    }
}

@Composable
private fun DistributionResultsHeader(count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Eyebrow(stringResource(R.string.topics_distribution_results_title))
        Spacer(Modifier.width(Space.sm))
        Hairline(modifier = Modifier.weight(1f))
        Spacer(Modifier.width(Space.sm))
        MetaText(stringResource(R.string.topics_distribution_results_count, count))
    }
}

@Composable
private fun DistributionResultsCard(
    results: List<Pair<DistributionSection, DistributionRow>>,
    progressById: Map<String, TopicProgress>,
    onToggledDone: (DistributionRow) -> Unit,
) {
    TuyoCard(contentPadding = PaddingValues(horizontal = Space.lg, vertical = 2.dp)) {
        results.forEachIndexed { index, (distributionSection, row) ->
            DistributionRowItem(
                row = row,
                years = distributionSection.years,
                maxTotal = 1,
                progress = progressById[row.id],
                onToggledDone = onToggledDone,
                contextLabel = "${distributionSection.exam.label} · ${distributionSection.title}",
                showBar = false,
            )
            if (index != results.lastIndex) Hairline()
        }
    }
}

@Composable
private fun DistributionRowItem(
    row: DistributionRow,
    years: List<Int>,
    maxTotal: Int,
    progress: TopicProgress?,
    onToggledDone: (DistributionRow) -> Unit,
    contextLabel: String? = null,
    showBar: Boolean = true,
) {
    val colors = LocalTuyoColors.current
    val done = progress?.status == TopicStatus.DONE
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Space.md),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(Radius.pill))
                    .background(colors.accentSoft),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = row.rank.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.accent,
                )
            }
            Spacer(Modifier.width(Space.sm))
            Column(
                modifier = Modifier.weight(1f),
            ) {
                if (contextLabel != null) {
                    MetaText(contextLabel)
                    Spacer(Modifier.height(2.dp))
                }
                Text(
                    text = row.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                )
                Spacer(Modifier.height(3.dp))
                MetaText(
                    stringResource(
                        R.string.topics_distribution_row_meta,
                        row.total,
                    ),
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = stringResource(
                        R.string.topics_distribution_row_years,
                        formatYearly(years, row.yearly),
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary,
                )
            }
            Spacer(Modifier.width(Space.sm))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(
                        R.string.topics_distribution_share_value,
                        formatDecimal(row.sharePercent),
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.textSecondary,
                )
                Text(
                    text = stringResource(R.string.topics_distribution_share_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary,
                )
                Spacer(Modifier.height(6.dp))
                DistributionMarkButton(
                    done = done,
                    onClick = { onToggledDone(row) },
                )
            }
        }
        if (showBar) {
            Spacer(Modifier.height(6.dp))
            ProgressLine(
                fraction = row.total.toFloat() / maxTotal,
                color = colors.hairlineStrong,
            )
        }
    }
}

@Composable
private fun DistributionMarkButton(
    done: Boolean,
    onClick: () -> Unit,
) {
    val colors = LocalTuyoColors.current
    val shape = RoundedCornerShape(Radius.pill)
    val label = stringResource(
        if (done) R.string.topics_distribution_unmark_done
        else R.string.topics_distribution_mark_done,
    )
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(shape)
            .background(if (done) colors.accentSoft else colors.surface)
            .border(1.dp, if (done) colors.accent else colors.hairline, shape)
            .clickable(
                role = Role.Checkbox,
                onClickLabel = label,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (done) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = label,
                tint = colors.accent,
                modifier = Modifier.size(16.dp),
            )
        } else {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(shape)
                    .background(colors.hairlineStrong),
            )
        }
    }
}

@Composable
private fun DistributionSourceCard() {
    val colors = LocalTuyoColors.current
    TuyoCard(contentPadding = PaddingValues(horizontal = Space.lg, vertical = Space.md)) {
        Eyebrow(stringResource(R.string.topics_distribution_source_title))
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.topics_distribution_source_body),
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary,
        )
        Spacer(Modifier.height(Space.sm))
        Text(
            text = stringResource(R.string.topics_distribution_method),
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.topics_distribution_mark_hint),
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.topics_distribution_other_exams),
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary,
        )
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
    val weight = remember(stat.topic.frequency) {
        stat.topic.frequency?.toTopicWeight()
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
                if (weight != null) {
                    Spacer(Modifier.height(Space.md))
                    TopicWeightBlock(weight = weight, subject = stat.topic.subject)
                    Spacer(Modifier.height(Space.lg))
                    Hairline()
                }
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

@Composable
private fun TopicWeightBlock(
    weight: TopicWeight,
    subject: Subject,
) {
    val colors = LocalTuyoColors.current
    val expected = if (weight.expectedLow == weight.expectedHigh) {
        stringResource(R.string.topic_weight_single_value, weight.expectedHigh)
    } else {
        stringResource(
            R.string.topic_weight_range_value,
            weight.expectedLow,
            weight.expectedHigh,
        )
    }
    val share = formatDecimal(weight.sharePercent)
    val average = formatDecimal(weight.average)
    val trendLabel = when (weight.trend) {
        TopicTrend.ABOVE -> stringResource(R.string.topic_weight_trend_above)
        TopicTrend.NEAR -> stringResource(R.string.topic_weight_trend_near)
        TopicTrend.BELOW -> stringResource(R.string.topic_weight_trend_below)
    }
    val trendColor = when (weight.trend) {
        TopicTrend.ABOVE -> colors.accent
        TopicTrend.NEAR -> colors.textTertiary
        TopicTrend.BELOW -> colors.textSecondary
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.topic_weight_title),
                style = MaterialTheme.typography.labelSmall,
                color = colors.textTertiary,
                modifier = Modifier.weight(1f),
            )
            Tag(text = weight.examLabel, color = colors.accent)
        }
        Spacer(Modifier.height(4.dp))
        MetaText(
            text = stringResource(
                R.string.topic_weight_context,
                subject.label,
                weight.periodLabel,
            ),
        )
        Spacer(Modifier.height(Space.md))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Space.md),
        ) {
            TopicWeightMetric(
                label = stringResource(R.string.topic_weight_expected_label),
                value = expected,
                modifier = Modifier.weight(1f),
            )
            TopicWeightMetric(
                label = stringResource(R.string.topic_weight_share_label),
                value = stringResource(R.string.topic_weight_share_value, share),
                modifier = Modifier.weight(1f),
            )
            TopicWeightMetric(
                label = stringResource(R.string.topic_weight_rank_label),
                value = stringResource(
                    R.string.topic_weight_rank_value,
                    weight.rank,
                    weight.rankedTopics,
                ),
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(Space.md))
        FrequencyBars(
            years = weight.years,
            counts = weight.counts,
            lastThreeStartYear = weight.lastThreeStartYear,
        )
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            MetaText(weight.years.first().toString())
            Spacer(Modifier.weight(1f))
            Text(
                text = stringResource(R.string.topic_weight_recent_legend),
                style = MaterialTheme.typography.labelSmall,
                color = colors.accent,
            )
            Spacer(Modifier.weight(1f))
            MetaText(weight.years.last().toString())
        }
        Spacer(Modifier.height(Space.sm))
        MetaText(
            text = stringResource(R.string.topic_weight_average, average),
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = trendLabel,
            style = MaterialTheme.typography.bodySmall,
            color = trendColor,
        )
        if (weight.note != null) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = weight.note,
                style = MaterialTheme.typography.labelSmall,
                color = colors.textTertiary,
            )
        }
        Spacer(Modifier.height(Space.sm))
        Text(
            text = stringResource(R.string.topic_weight_method),
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = stringResource(R.string.topic_weight_disclaimer),
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary,
        )
    }
}

@Composable
private fun TopicWeightMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    val colors = LocalTuyoColors.current
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = colors.textPrimary,
            maxLines = 1,
        )
    }
}

@Composable
private fun FrequencyBars(
    years: List<Int>,
    counts: List<Int>,
    lastThreeStartYear: Int,
) {
    val colors = LocalTuyoColors.current
    val maxCount = counts.maxOrNull()?.coerceAtLeast(1) ?: 1
    val chartHeight = 40.dp
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(chartHeight),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        years.forEachIndexed { index, year ->
            val count = counts.getOrElse(index) { 0 }
            val barHeight = (40f * count / maxCount).coerceAtLeast(3f).dp
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(chartHeight),
                contentAlignment = Alignment.BottomCenter,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.62f)
                        .height(barHeight)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            if (year >= lastThreeStartYear) {
                                colors.accent.copy(alpha = 0.82f)
                            } else {
                                colors.hairlineStrong
                            },
                        ),
                )
            }
        }
    }
}

private fun formatDecimal(value: Double): String =
    String.format(Locale.forLanguageTag("tr"), "%.1f", value)
