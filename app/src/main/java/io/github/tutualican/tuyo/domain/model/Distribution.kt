package io.github.tutualican.tuyo.domain.model

import java.util.Locale

/**
 * A full, browsable topic table for one exam subject.
 *
 * The source tables publish yearly question counts. The app keeps the subject's
 * yearly totals so the reader can see the period and the per-year exam size,
 * while [topics] holds every published topic row, including rows that do not
 * have a Tuyo topic card.
 */
data class DistributionSection(
    val id: String,
    val exam: ExamType,
    val subject: Subject,
    val title: String,
    val years: List<Int>,
    val yearlyTotals: List<Int>,
    val topics: List<DistributionTopic>,
    val note: String? = null,
)

data class DistributionTopic(
    val title: String,
    val total: Int,
    /** Published question count for each year in [DistributionSection.years]. */
    val yearly: List<Int>,
    /** Optional bridge to one of the detailed Tuyo topic cards. */
    val linkedTopicId: String? = null,
    val note: String? = null,
)

/**
 * A row derived from a [DistributionSection]. Percentages and averages are
 * computed here, never stored in the content file.
 *
 * The share is the row's share of all published topic rows in that section.
 * This makes the listed rows add up to 100% even when the source table contains
 * overlapping or slightly inconsistent rows.
 */
data class DistributionRow(
    /** Stable id for the row's own progress mark. */
    val id: String,
    val title: String,
    val total: Int,
    /** Question count for each year, in the same order as the section years. */
    val yearly: List<Int>,
    val average: Double,
    val sharePercent: Double,
    val rank: Int,
    val rankedTopics: Int,
    val linkedTopicId: String? = null,
    val note: String? = null,
)

data class ExamDistribution(
    val id: String,
    val label: String,
    val title: String,
    val subtitle: String,
    val sections: List<DistributionSection>,
)

fun DistributionSection.rows(): List<DistributionRow> {
    require(years.isNotEmpty()) { "A distribution needs at least one year" }
    require(topics.isNotEmpty()) { "A distribution needs at least one topic" }
    require(topics.all { it.total >= 0 }) { "Question totals cannot be negative" }
    require(topics.all { it.yearly.size == years.size }) {
        "Every topic needs one count per year"
    }
    require(topics.all { topic -> topic.yearly.all { it >= 0 } }) {
        "Yearly question counts cannot be negative"
    }
    require(topics.all { it.total == it.yearly.sum() }) {
        "Topic total must equal its yearly counts"
    }

    val listedTotal = topics.sumOf { it.total }.coerceAtLeast(1)
    return topics
        .sortedWith(compareByDescending<DistributionTopic> { it.total }.thenBy { it.title })
        .map { topic ->
            DistributionRow(
                id = "${id}-${topic.title.progressSlug()}",
                title = topic.title,
                total = topic.total,
                yearly = topic.yearly,
                average = topic.total.toDouble() / years.size,
                sharePercent = topic.total * 100.0 / listedTotal,
                rank = 1 + topics.count { it.total > topic.total },
                rankedTopics = topics.size,
                linkedTopicId = topic.linkedTopicId,
                note = topic.note,
            )
        }
}

val DistributionSection.periodLabel: String
    get() = "${years.first()}–${years.last()}"

val DistributionSection.totalQuestions: Int
    get() = yearlyTotals.sum()

private fun String.progressSlug(): String = lowercase(Locale.forLanguageTag("tr"))
    .replace('ı', 'i')
    .replace('ğ', 'g')
    .replace('ü', 'u')
    .replace('ş', 's')
    .replace('ö', 'o')
    .replace('ç', 'c')
    .replace(Regex("[^a-z0-9]+"), "-")
    .trim('-')
