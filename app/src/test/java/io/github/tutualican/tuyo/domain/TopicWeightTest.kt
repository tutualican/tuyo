package io.github.tutualican.tuyo.domain

import io.github.tutualican.tuyo.domain.model.ExamType
import io.github.tutualican.tuyo.domain.model.TopicFrequency
import org.junit.Assert.assertEquals
import org.junit.Test

class TopicWeightTest {

    @Test
    fun `average share and range come from the yearly counts`() {
        val frequency = TopicFrequency(
            exam = ExamType.TYT,
            years = listOf(2020, 2021, 2022, 2023, 2024),
            counts = listOf(2, 3, 2, 3, 4),
            totals = List(5) { 10 },
            rank = 2,
            rankedTopics = 7,
        )

        val weight = frequency.toTopicWeight()

        assertEquals(2.8, weight.average, 0.0001)
        assertEquals(2, weight.expectedLow)
        assertEquals(3, weight.expectedHigh)
        assertEquals(28.0, weight.sharePercent, 0.0001)
        assertEquals("2020–2024", weight.periodLabel)
        assertEquals("TYT", weight.examLabel)
        assertEquals(2, weight.rank)
        assertEquals(7, weight.rankedTopics)
        assertEquals(2022, weight.lastThreeStartYear)
    }

    @Test
    fun `share is averaged per year instead of pooling different totals`() {
        val frequency = TopicFrequency(
            exam = ExamType.AYT,
            years = listOf(2023, 2024),
            counts = listOf(1, 2),
            totals = listOf(10, 20),
            rank = 1,
            rankedTopics = 1,
        )

        assertEquals(10.0, frequency.toTopicWeight().sharePercent, 0.0001)

        val uneven = frequency.copy(counts = listOf(1, 4), totals = listOf(10, 20))
        // 10% and 20% -> 15%.
        assertEquals(15.0, uneven.toTopicWeight().sharePercent, 0.0001)
    }

    @Test
    fun `a whole-number average produces a single expected value`() {
        val frequency = TopicFrequency(
            exam = ExamType.TYT,
            years = listOf(2022, 2023, 2024),
            counts = listOf(3, 3, 3),
            totals = List(3) { 10 },
            rank = 1,
            rankedTopics = 3,
        )

        val weight = frequency.toTopicWeight()

        assertEquals(3, weight.expectedLow)
        assertEquals(3, weight.expectedHigh)
    }

    @Test
    fun `trend compares the last three years with the full period`() {
        val above = frequency(counts = listOf(1, 1, 1, 1, 4, 4, 4))
        val below = frequency(counts = listOf(4, 4, 4, 4, 1, 1, 1))
        val near = frequency(counts = listOf(2, 3, 2, 3, 2, 3, 2))

        assertEquals(TopicTrend.ABOVE, above.toTopicWeight().trend)
        assertEquals(TopicTrend.BELOW, below.toTopicWeight().trend)
        assertEquals(TopicTrend.NEAR, near.toTopicWeight().trend)
    }

    private fun frequency(counts: List<Int>): TopicFrequency = TopicFrequency(
        exam = ExamType.TYT,
        years = counts.indices.map { 2018 + it },
        counts = counts,
        totals = List(counts.size) { 10 },
        rank = 1,
        rankedTopics = 1,
    )
}
