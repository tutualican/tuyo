package io.github.tutualican.tuyo.data

import io.github.tutualican.tuyo.domain.model.ExamType
import io.github.tutualican.tuyo.domain.model.rows
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class StudyDistributionsTest {

    @Test
    fun `TYT comes first and both YKS sessions have full subject tables`() {
        assertEquals(listOf("tyt", "ayt"), StudyDistributions.exams.map { it.id })
        assertEquals(ExamType.TYT, StudyDistributions.exams.first().sections.first().exam)
        assertEquals(10, StudyDistributions.tyt.sections.size)
        assertEquals(10, StudyDistributions.ayt.sections.size)
        assertEquals(
            StudyDistributions.tyt.sections.size + StudyDistributions.ayt.sections.size,
            StudyDistributions.exams.sumOf { it.sections.size },
        )
    }

    @Test
    fun `sections keep their published years and yearly totals`() {
        val sectionIds = mutableSetOf<String>()
        StudyDistributions.exams.forEach { exam ->
            exam.sections.forEach { section ->
                assertTrue("Duplicate section id ${section.id}", sectionIds.add(section.id))
                assertTrue("${section.id} has no years", section.years.isNotEmpty())
                assertEquals(
                    "${section.id} needs one yearly total per year",
                    section.years.size,
                    section.yearlyTotals.size,
                )
                assertTrue(
                    "${section.id} years must be ascending",
                    section.years.zipWithNext().all { (a, b) -> a < b },
                )
                assertTrue(
                    "${section.id} needs positive yearly totals",
                    section.yearlyTotals.all { it > 0 },
                )
                assertTrue("${section.id} has no topics", section.topics.isNotEmpty())
                assertTrue(
                    "${section.id} has a negative topic total",
                    section.topics.all { it.total >= 0 },
                )
            }
        }
    }

    @Test
    fun `every topic exposes one count for every published year`() {
        StudyDistributions.exams.forEach { exam ->
            exam.sections.forEach { section ->
                section.topics.forEach { topic ->
                    assertEquals(
                        "${section.id}/${topic.title} needs one count per year",
                        section.years.size,
                        topic.yearly.size,
                    )
                    assertTrue(
                        "${section.id}/${topic.title} has a negative yearly count",
                        topic.yearly.all { it >= 0 },
                    )
                    assertEquals(
                        "${section.id}/${topic.title} total must match its yearly counts",
                        topic.yearly.sum(),
                        topic.total,
                    )
                }
            }
        }
    }

    @Test
    fun `representative rows keep their published yearly counts`() {
        val turkce = StudyDistributions.tyt.sections.first { it.id == "tyt-turkce" }
        val paragraf = turkce.topics.first { it.title == "Paragraf" }
        assertEquals(listOf(22, 22, 26, 25, 26, 26, 26, 26), paragraf.yearly)

        val geometri = StudyDistributions.tyt.sections.first { it.id == "tyt-geometri" }
        val grafik = geometri.topics.first { it.title == "Grafik Okuma" }
        assertEquals(List(8) { 0 }, grafik.yearly)
        assertEquals(0, grafik.total)

        val aytMatematik = StudyDistributions.ayt.sections.first { it.id == "ayt-matematik" }
        val temel = aytMatematik.topics.first { it.title == "Temel Kavramlar" }
        assertEquals(listOf(4, 2, 2, 3, 2, 4, 2, 1), temel.yearly)

        val tytTarih = StudyDistributions.tyt.sections.first { it.id == "tyt-tarih" }
        assertEquals(8, tytTarih.years.size)
        assertTrue(tytTarih.topics.all { it.yearly.size == 8 })

        val aytTarih = StudyDistributions.ayt.sections.first { it.id == "ayt-tarih" }
        assertEquals(listOf(2019, 2020, 2021, 2022, 2023, 2024), aytTarih.years)
        assertTrue(aytTarih.topics.all { it.yearly.size == 6 })
    }

    @Test
    fun `every linked row points at a real Tuyo topic`() {
        val topicIds = StudyContent.topics.map { it.id }.toSet()
        StudyDistributions.exams.forEach { exam ->
            exam.sections.forEach { section ->
                section.topics.mapNotNull { it.linkedTopicId }.forEach { topicId ->
                    assertTrue(
                        "${section.id} references unknown topic $topicId",
                        topicId in topicIds,
                    )
                }
            }
        }
    }

    @Test
    fun `rows derive stable ids, rankings and shares`() {
        StudyDistributions.exams.forEach { exam ->
            exam.sections.forEach { section ->
                val rows = section.rows()
                assertEquals(section.topics.size, rows.size)
                assertEquals(
                    "${section.id} has duplicate row ids",
                    rows.size,
                    rows.map { it.id }.toSet().size,
                )
                assertTrue("${section.id} has an empty row id", rows.all { it.id.isNotBlank() })
                assertTrue(
                    "${section.id} rows must be sorted by total",
                    rows.zipWithNext().all { (a, b) -> a.total >= b.total },
                )
                rows.forEach { row ->
                    assertEquals(
                        "${section.id}/${row.title} row yearly total",
                        row.total,
                        row.yearly.sum(),
                    )
                    assertTrue(
                        "${section.id}/${row.title} rank out of range",
                        row.rank in 1..row.rankedTopics,
                    )
                    assertEquals(
                        "${section.id}/${row.title} average",
                        row.total.toDouble() / section.years.size,
                        row.average,
                        0.0001,
                    )
                    assertTrue(
                        "${section.id}/${row.title} share out of range",
                        row.sharePercent in 0.0..100.0,
                    )
                }
                val shareTotal = rows.sumOf { it.sharePercent }
                assertTrue(
                    "${section.id} shares should add up to about 100",
                    abs(shareTotal - 100.0) < 0.01,
                )
            }
        }
    }

    @Test
    fun `matematik and geometri stay separate in both sessions`() {
        val tytSubjects = StudyDistributions.tyt.sections.map { it.subject.label }
        val aytSubjects = StudyDistributions.ayt.sections.map { it.subject.label }

        assertTrue("TYT Matematik", "Matematik" in tytSubjects)
        assertTrue("TYT Geometri", "Geometri" in tytSubjects)
        assertTrue("AYT Matematik", "Matematik" in aytSubjects)
        assertTrue("AYT Geometri", "Geometri" in aytSubjects)
    }

    @Test
    fun `AYT history notes the missing 2025 column`() {
        val history = StudyDistributions.ayt.sections.first { it.id == "ayt-tarih" }
        assertEquals(2024, history.years.last())
        assertNotNull(history.note)
        assertTrue(history.note!!.contains("2025"))
    }
}
