package io.github.tutualican.tuyo.domain

import io.github.tutualican.tuyo.domain.model.TopicFrequency
import kotlin.math.ceil
import kotlin.math.floor

/** How the last three years compare with the whole reference period. */
enum class TopicTrend {
    ABOVE,
    NEAR,
    BELOW,
}

/**
 * A derived, deliberately modest view of a topic's exam weight.
 *
 * The expected range is the floor and ceiling of the simple mean. It is not a
 * regression or a forecast; it says "this is where the yearly counts have
 * usually landed". The trend uses the last three years against the full
 * period, with a small margin so a one-question wobble is not called a trend.
 */
data class TopicWeight(
    val average: Double,
    val expectedLow: Int,
    val expectedHigh: Int,
    val sharePercent: Double,
    val rank: Int,
    val rankedTopics: Int,
    val lastThreeAverage: Double,
    val trend: TopicTrend,
    val years: List<Int>,
    val counts: List<Int>,
    val periodLabel: String,
    val examLabel: String,
    val note: String?,
) {
    val lastThreeStartYear: Int
        get() = years[maxOf(0, years.size - 3)]
}

private const val TREND_MARGIN = 0.2

fun TopicFrequency.toTopicWeight(): TopicWeight {
    require(years.isNotEmpty()) { "Topic frequency needs at least one year" }
    require(years.size == counts.size) { "Every year needs a topic count" }
    require(years.size == totals.size) { "Every year needs a subject total" }
    require(years.zipWithNext().all { (a, b) -> a < b }) { "Years must be ascending" }
    require(counts.all { it >= 0 }) { "Question counts cannot be negative" }
    require(totals.all { it > 0 }) { "Subject totals must be positive" }
    require(counts.indices.all { counts[it] <= totals[it] }) {
        "A topic cannot have more questions than its subject"
    }

    val average = counts.average()
    val yearlyShares = counts.indices.map { index ->
        if (totals[index] > 0) counts[index] * 100.0 / totals[index] else 0.0
    }
    val lastThreeAverage = counts.takeLast(3).average()
    val delta = lastThreeAverage - average
    val trend = when {
        delta >= TREND_MARGIN -> TopicTrend.ABOVE
        delta <= -TREND_MARGIN -> TopicTrend.BELOW
        else -> TopicTrend.NEAR
    }

    return TopicWeight(
        average = average,
        expectedLow = floor(average).toInt(),
        expectedHigh = ceil(average).toInt(),
        sharePercent = yearlyShares.average(),
        rank = rank,
        rankedTopics = rankedTopics,
        lastThreeAverage = lastThreeAverage,
        trend = trend,
        years = years,
        counts = counts,
        periodLabel = "${years.first()}–${years.last()}",
        examLabel = exam.label,
        note = note,
    )
}
