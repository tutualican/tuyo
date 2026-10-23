package io.github.tutualican.tuyo.domain.model

import kotlinx.serialization.Serializable
import java.time.LocalDate
import kotlin.math.roundToInt

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
    FELSEFE("Felsefe", "FEL"),
    DIN("Din kültürü", "DİN"),
    INGILIZCE("İngilizce", "İNG"),
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
    TAHMIN(
        "Tahmin",
        "Emin olmadığın seçeneği işaretle. Sonra nedenini tek cümleyle yaz; tahmin de bir veridir.",
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
    @Serializable(with = LocalDateSerializer::class)
    val examDate: LocalDate = LocalDate.of(2027, 6, 20),
    val dailyQuestionTarget: Int = 80,
    val dailyMinuteTarget: Int = 120,
    val weeklyQuestionTarget: Int = 500,
    val weeklyMinuteTarget: Int = 720,
)

@Serializable
data class ExamSectionResult(
    val subject: Subject,
    val correct: Int = 0,
    val wrong: Int = 0,
    val blank: Int = 0,
    val minutes: Int = 0,
) {
    val total: Int get() = correct + wrong + blank
    val net: Double get() = correct - wrong / 4.0
    val loss: Double get() = wrong / 4.0 + blank
}

/**
 * A captured exam. Quick net-only entries keep [sections] empty; detailed
 * entries fill it and use the sum as the stored net. The estimated score is a
 * transparent 100–500 heuristic, not an ÖSYM score.
 */
@Serializable
data class ExamNet(
    val id: String,
    @Serializable(with = LocalDateSerializer::class)
    val date: LocalDate,
    val exam: ExamType,
    val net: Double,
    val sections: List<ExamSectionResult> = emptyList(),
    val durationMinutes: Int = 0,
    val analysisNote: String = "",
    val lossTopics: String = "",
    val analysisCompleted: Boolean = false,
) {
    val calculatedNet: Double
        get() = if (sections.isEmpty()) net else sections.sumOf { it.net }

    val isDetailed: Boolean get() = sections.isNotEmpty()

    val estimatedScore: Int
        get() {
            val ratio = (calculatedNet / exam.maxNet).coerceIn(0.0, 1.0)
            return (100 + ratio * 400).roundToInt()
        }

    fun sectionNet(subject: Subject): Double? =
        sections.firstOrNull { it.subject == subject }?.net

    fun subjectSections(): List<ExamSectionResult> =
        sections.sortedWith(compareBy({ it.subject.ordinal }))
}

@Serializable
enum class Confidence(val label: String, val shortLabel: String) {
    SURE("Eminim", "emin"),
    UNSURE("Emin değilim", "şüpheli"),
    GUESS("Tahmin ettim", "tahmin"),
}

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
    val confidence: Confidence = Confidence.SURE,
    val photoUri: String? = null,
) {
    /** The wrong answer is logged; that is always the first completed step. */
    val topicStepDone: Boolean get() = mastery >= 1

    /** Complete once the item has survived a spaced re-solve. */
    val questionStepDone: Boolean get() = mastery >= 3

    fun isDue(today: LocalDate): Boolean = !dueOn.isAfter(today)
}

@Serializable
data class QuestionLog(
    val id: String,
    @Serializable(with = LocalDateSerializer::class)
    val date: LocalDate,
    val subject: Subject,
    val correct: Int,
    val wrong: Int,
    val blank: Int = 0,
) {
    val total: Int get() = correct + wrong + blank
    val net: Double get() = correct - wrong / 4.0
}

@Serializable
enum class TopicStatus(val label: String) {
    NOT_STARTED("Başlanmadı"),
    IN_PROGRESS("Çalışılıyor"),
    DONE("Bitti"),
    NEEDS_REVIEW("Tekrar gerekli"),
}

@Serializable
data class TopicProgress(
    val topicId: String,
    val status: TopicStatus = TopicStatus.NOT_STARTED,
    @Serializable(with = LocalDateSerializer::class)
    val updatedOn: LocalDate,
    val reviewStep: Int = 0,
    @Serializable(with = NullableLocalDateSerializer::class)
    val nextReviewOn: LocalDate? = null,
)

@Serializable
enum class PlanPeriod(val label: String) {
    DAILY("Günlük"),
    WEEKLY("Haftalık"),
}

@Serializable
enum class PlanMetric(val label: String) {
    QUESTIONS("Soru"),
    MINUTES("Dakika"),
}

@Serializable
data class PlanTask(
    val id: String,
    val title: String,
    val subject: Subject? = null,
    val period: PlanPeriod = PlanPeriod.DAILY,
    val metric: PlanMetric = PlanMetric.QUESTIONS,
    val target: Int,
    val completed: Boolean = false,
    @Serializable(with = LocalDateSerializer::class)
    val date: LocalDate,
    @Serializable(with = NullableLocalDateSerializer::class)
    val carriedFrom: LocalDate? = null,
)

@Serializable
enum class TimerMode(val label: String) {
    POMODORO("Pomodoro"),
    FREE("Serbest"),
    EXAM("Sınav simülasyonu"),
}

@Serializable
data class StudySession(
    val id: String,
    @Serializable(with = LocalDateSerializer::class)
    val date: LocalDate,
    val subject: Subject? = null,
    val minutes: Int,
    val mode: TimerMode = TimerMode.FREE,
)

@Serializable
data class NetGoal(
    val id: String,
    val exam: ExamType,
    val subject: Subject? = null,
    val targetNet: Double,
    val year: Int,
    val month: Int,
)

@Serializable
data class UserData(
    val schemaVersion: Int = 2,
    val profile: UserProfile = UserProfile(),
    val mistakes: List<Mistake> = emptyList(),
    val nets: List<ExamNet> = emptyList(),
    val streakDays: Int = 0,
    val lastActivityEpochDay: Long? = null,
    val reviewsCompleted: Int = 0,
    val questionLogs: List<QuestionLog> = emptyList(),
    val topicProgress: List<TopicProgress> = emptyList(),
    val planTasks: List<PlanTask> = emptyList(),
    val studySessions: List<StudySession> = emptyList(),
    val netGoals: List<NetGoal> = emptyList(),
    val restDayEpochDays: List<Long> = emptyList(),
    val reviewEpochDays: List<Long> = emptyList(),
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
    UMUT("Umut"),
}

data class MotivationNote(
    val id: String,
    val text: String,
    val theme: MotivationTheme,
)

/**
 * A short attributed quotation. [source] is shown to the reader so a line is
 * never presented as free-floating wisdom; "atfedilen söz" marks widely shared
 * lines whose original source is disputed.
 */
data class MotivationQuote(
    val id: String,
    val text: String,
    val author: String,
    val source: String,
    val theme: MotivationTheme,
)

/**
 * A link to an official TED talk. The app stores only a short editorial
 * description and opens the talk in the browser; it does not bundle transcripts.
 */
data class TedTalk(
    val id: String,
    val title: String,
    val speaker: String,
    val meta: String,
    val summary: String,
    val url: String,
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
    val quotes: List<MotivationQuote>,
    val tedTalks: List<TedTalk>,
    val motivationSources: List<Source>,
    val examOverviews: List<ExamOverview>,
    val examStats: List<ExamStat>,
    val examComments: List<ExamComment>,
    val sources: List<Source>,
    val featuredTacticId: String,
    val questionLogs: List<QuestionLog> = emptyList(),
    val topicProgress: List<TopicProgress> = emptyList(),
    val planTasks: List<PlanTask> = emptyList(),
    val studySessions: List<StudySession> = emptyList(),
    val netGoals: List<NetGoal> = emptyList(),
    val restDayEpochDays: List<Long> = emptyList(),
    val reviewEpochDays: List<Long> = emptyList(),
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
            return if (latest != null && previous != null) {
                latest.calculatedNet - previous.calculatedNet
            } else {
                null
            }
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
