package io.github.tutualican.tuyo.domain.model

import java.time.LocalDate

/**
 * The three axes of the product. A mistake is never just "wrong": it is a
 * wrong answer that points at a topic, which in turn schedules a question.
 */
enum class Subject(val label: String, val shortLabel: String) {
    MATEMATIK("Matematik", "MAT"),
    FIZIK("Fizik", "FİZ"),
    TURKCE("Türkçe", "TÜR"),
    KIMYA("Kimya", "KİM"),
    BIYOLOJI("Biyoloji", "BİY"),
    GEOMETRI("Geometri", "GEO"),
}

/**
 * Error kinds are deliberately behavioural, not moral. The wording tells the
 * student what to do next instead of labelling them.
 */
enum class ErrorKind(val label: String, val advice: String) {
    BILGI(
        "Bilgi eksikliği",
        "Önce konu kartını 10 dakikada tekrar et, sonra üç benzer soru çöz.",
    ),
    DIKKATSIZLIK(
        "Dikkatsizlik",
        "Soruyu iki kez oku; koşulların altını çiz, işlemi kâğıda yaz.",
    ),
    SORU_KOKU(
        "Soru kökü",
        "Seçeneklere bakmadan önce isteneni kendi cümlenle yaz.",
    ),
    ISLEM(
        "İşlem hatası",
        "Ara satırları atlama. Kafadan işlem yasak; tek satırda tek işlem.",
    ),
    ZAMAN(
        "Zaman yönetimi",
        "Tek soruya 90 saniye sınırı koy; takılırsan işaretle ve geç.",
    ),
}

enum class EvidenceLevel {
    RESEARCH,
    ANECDOTE,
    DEBATED,
}

data class ExamNet(
    val date: LocalDate,
    val net: Double,
)

data class Confusion(
    val question: String,
    val answer: String,
)

data class Topic(
    val id: String,
    val subject: Subject,
    val title: String,
    val summary: String,
    val keyIdea: String,
    val confusions: List<Confusion>,
    val mastery: Int,
) {
    val masteryFraction: Float get() = mastery.coerceIn(0, 5) / 5f
}

data class Mistake(
    val id: String,
    val subject: Subject,
    val topicId: String,
    val topicTitle: String,
    val questionRef: String,
    val note: String,
    val errorKind: ErrorKind,
    val createdOn: LocalDate,
    val dueOn: LocalDate,
    val mastery: Int,
) {
    /** Y is always complete: the mistake exists because it was logged. */
    val topicStepDone: Boolean get() = mastery >= 1

    /** S is complete once the item has survived a spaced re-solve. */
    val questionStepDone: Boolean get() = mastery >= 3

    fun isDue(today: LocalDate): Boolean = !dueOn.isAfter(today)
}

data class Tactic(
    val id: String,
    val title: String,
    val summary: String,
    val steps: List<String>,
    val evidence: EvidenceLevel,
    val minutes: Int,
    val topicIds: List<String>,
)

data class StudyData(
    val learnerName: String,
    val streakDays: Int,
    val nets: List<ExamNet>,
    val mistakes: List<Mistake>,
    val topics: List<Topic>,
    val tactics: List<Tactic>,
    val featuredTacticId: String,
) {
    val latestNet: Double? get() = nets.lastOrNull()?.net
    val previousNet: Double? get() = nets.dropLast(1).lastOrNull()?.net
    val netDelta: Double?
        get() {
            val latest = latestNet
            val previous = previousNet
            return if (latest != null && previous != null) latest - previous else null
        }
    val dueMistakes: List<Mistake> get() = mistakes.filter { it.isDue(today = LocalDate.now()) }
}

/**
 * Applies a review outcome to one mistake without mutating the aggregate.
 * Unknown ids are a no-op so a stale row can never crash the screen.
 */
fun StudyData.withReview(
    mistakeId: String,
    mastery: Int,
    dueOn: LocalDate,
): StudyData = copy(
    mistakes = mistakes.map { mistake ->
        if (mistake.id == mistakeId) {
            mistake.copy(mastery = mastery, dueOn = dueOn)
        } else {
            mistake
        }
    },
)
