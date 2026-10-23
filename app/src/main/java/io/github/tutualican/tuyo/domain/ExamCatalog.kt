package io.github.tutualican.tuyo.domain

import io.github.tutualican.tuyo.domain.model.ExamTrack
import io.github.tutualican.tuyo.domain.model.ExamType
import io.github.tutualican.tuyo.domain.model.Subject

data class SectionTemplate(
    val subject: Subject,
    val questions: Int,
)

/**
 * Official section sizes. AYT depends on the learner's track; showing all
 * tracks at once would make a single exam look like it has 160 questions.
 */
fun sectionTemplates(exam: ExamType, track: ExamTrack?): List<SectionTemplate> =
    when (exam) {
        ExamType.TYT -> listOf(
            SectionTemplate(Subject.TURKCE, 40),
            SectionTemplate(Subject.MATEMATIK, 40),
            SectionTemplate(Subject.FIZIK, 7),
            SectionTemplate(Subject.KIMYA, 7),
            SectionTemplate(Subject.BIYOLOJI, 6),
            SectionTemplate(Subject.TARIH, 5),
            SectionTemplate(Subject.COGRAFYA, 5),
            SectionTemplate(Subject.FELSEFE, 5),
            SectionTemplate(Subject.DIN, 5),
        )
        ExamType.AYT -> when (track) {
            ExamTrack.ESIT_AGIRLIK -> listOf(
                SectionTemplate(Subject.MATEMATIK, 40),
                SectionTemplate(Subject.EDEBIYAT, 24),
                SectionTemplate(Subject.TARIH, 10),
                SectionTemplate(Subject.COGRAFYA, 6),
            )
            ExamTrack.SOZEL -> listOf(
                SectionTemplate(Subject.EDEBIYAT, 24),
                SectionTemplate(Subject.TARIH, 21),
                SectionTemplate(Subject.COGRAFYA, 17),
                SectionTemplate(Subject.FELSEFE, 12),
                SectionTemplate(Subject.DIN, 6),
            )
            ExamTrack.DIL -> listOf(SectionTemplate(Subject.INGILIZCE, 80))
            ExamTrack.SAYISAL, null -> listOf(
                SectionTemplate(Subject.MATEMATIK, 40),
                SectionTemplate(Subject.FIZIK, 14),
                SectionTemplate(Subject.KIMYA, 13),
                SectionTemplate(Subject.BIYOLOJI, 13),
            )
        }
        ExamType.YDT -> listOf(SectionTemplate(Subject.INGILIZCE, 80))
    }
