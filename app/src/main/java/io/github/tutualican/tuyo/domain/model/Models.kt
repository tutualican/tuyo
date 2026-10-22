package io.github.tutualican.tuyo.domain.model

import kotlinx.serialization.Serializable
import java.time.LocalDate

/**
 * YKS is the exam. The notebook's three steps (wrong answer → topic → question)
 * are a review loop, not an acronym; the product labels them in plain Turkish.
 */
@Serializable
enum class Subject(val label: String, val shortLabel: String) {
    TURKCE("Türkçe", "TÜR"),
    MATEMATIK("Matematik", "MAT"),
    GEOMETRI("Geometri", "GEO"),
    FIZIK("Fizik", "FİZ"),
    KIMYA("Kimya", "KİM"),
    BIYOLOJI("Biyoloji", "BİY"),
    TARIH("Tarih", "TAR"),
    COGRAFYA("Coğrafya", "COĞ"),
    EDEBIYAT("Edebiyat", "EDB"),
}

/**
 * Error kinds are behavioural, not moral. The wording tells the student what to
 * do next instead of labelling them.
 */
@Serializable
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
        "Seçeneklere bakmadan önce isteneni kendi cümlelerinle yaz.",
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

@Serializable
enum class ExamType(val label: String, val maxNet: Double) {
    TYT("TYT", 120.0),
    AYT("AYT", 80.0),
    YDT("YDT", 80.0),
}

@Serializable
enum class ExamTrack(val label: String, val detail: String) {
    SAYISAL("Sayısal", "Matematik · Fen"),
    ESIT_AGIRLIK("Eşit ağırlık", "Matematik · Edebiyat-Sosyal"),
    SOZEL("Sözel", "Edebiyat · Sosyal"),
    DIL("Dil", "Yabancı dil"),
}

@Serializable
data class UserProfile(
    val name: String = "",
    val track: ExamTrack? = null,
    val onboardingComplete: Boolean = false,
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = 20,
    val reminderMinute: Int = 0,
)

@Serializable
data class ExamNet(
    val id: String,
    @Serializable(with = LocalDateSerializer::class)
    val date: LocalDate,
    val exam: ExamType,
    val net: Double,
)

@Serializable
data class Mistake(
    val id: String,
    val subject: Subject,
    val topicId: String,
    val topicTitle: String,
    val questionRef: String,
    val note: String,
    val errorKind: ErrorKind,
    @Serializable(with = LocalDateSerializer::class)
    val createdOn: LocalDate,
    @Serializable(with = LocalDateSerializer::class)
    val dueOn: LocalDate,
    val mastery: Int,
) {
    /** The wrong answer is logged; that is always the first completed step. */
    val topicStepDone: Boolean get() = mastery >= 1

    /** Complete once the item has survived a spaced re-solve. */
    val questionStepDone: Boolean get() = mastery >= 3

    fun isDue(today: LocalDate): Boolean = !dueOn.isAfter(today)
}

@Serializable
data class UserData(
    val schemaVersion: Int = 1,
    val profile: UserProfile = UserProfile(),
    val mistakes: List<Mistake> = emptyList(),
    val nets: List<ExamNet> = emptyList(),
    val streakDays: Int = 0,
    val lastActivityEpochDay: Long? = null,
    val reviewsCompleted: Int = 0,
) {
    val dueMistakes: List<Mistake>
        get() = mistakes
            .filter { it.isDue(LocalDate.now()) }
            .sortedWith(compareBy({ it.dueOn }, { it.createdOn }))
}

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
)

enum class TacticCategory(val label: String) {
    CALISMA("Çalışma"),
    TYT("TYT"),
    AYT("AYT"),
    SINAV_STRATEJISI("Sınav stratejisi"),
    SINAV_GUNU("Sınav günü"),
    IYI_HAL("İyi hâl"),
}

data class Tactic(
    val id: String,
    val title: String,
    val summary: String,
    val steps: List<String>,
    val evidence: EvidenceLevel,
    val minutes: Int,
    val category: TacticCategory,
    val topicIds: List<String> = emptyList(),
    val sourceNote: String? = null,
)

enum class MotivationTheme(val label: String) {
    BASLANGIC("Başlangıç"),
    DEVAM("Devam"),
    TOPARLANMA("Toparlanma"),
    SINAV_KAYGISI("Sınav kaygısı"),
    OZ_SEFKAT("Öz şefkat"),
}

data class MotivationNote(
    val id: String,
    val text: String,
    val theme: MotivationTheme,
)

data class ExamSection(
    val name: String,
    val questions: String,
    val detail: String,
)

data class ExamOverview(
    val id: String,
    val title: String,
    val meta: String,
    val summary: String,
    val sections: List<ExamSection>,
    val note: String,
)

data class ExamStat(
    val test: String,
    val average: String,
    val outOf: String,
)

data class ExamComment(
    val id: String,
    val label: String,
    val title: String,
    val body: String,
    val takeaway: String,
)

data class Source(
    val title: String,
    val publisher: String,
    val url: String,
)

data class StudyData(
    val profile: UserProfile,
    val streakDays: Int,
    val reviewsCompleted: Int,
    val nets: List<ExamNet>,
    val mistakes: List<Mistake>,
    val topics: List<Topic>,
    val tactics: List<Tactic>,
    val motivation: List<MotivationNote>,
    val examOverviews: List<ExamOverview>,
    val examStats: List<ExamStat>,
    val examComments: List<ExamComment>,
    val sources: List<Source>,
    val featuredTacticId: String,
) {
    val learnerName: String get() = profile.name
    val latestNet: ExamNet? get() = nets.lastOrNull()
    val previousNet: ExamNet?
        get() {
            val latest = latestNet ?: return null
            return nets.dropLast(1).lastOrNull { it.exam == latest.exam }
        }
    val netDelta: Double?
        get() {
            val latest = latestNet
            val previous = previousNet
            return if (latest != null && previous != null) latest.net - previous.net else null
        }

    /** Sparklines compare like with like; a TYT value is not plotted against AYT. */
    val netTrend: List<ExamNet>
        get() {
            val latest = latestNet ?: return emptyList()
            return nets.filter { it.exam == latest.exam }
        }

    val dueMistakes: List<Mistake>
        get() = mistakes
            .filter { it.isDue(LocalDate.now()) }
            .sortedWith(compareBy({ it.dueOn }, { it.createdOn }))
}

/**
 * Applies a review outcome to one mistake without mutating the aggregate.
 * Unknown ids are a no-op so a stale row can never crash the screen.
 */
fun UserData.withReview(
    mistakeId: String,
    mastery: Int,
    dueOn: LocalDate,
): UserData = copy(
    mistakes = mistakes.map { mistake ->
        if (mistake.id == mistakeId) {
            mistake.copy(mastery = mastery, dueOn = dueOn)
        } else {
            mistake
        }
    },
)
