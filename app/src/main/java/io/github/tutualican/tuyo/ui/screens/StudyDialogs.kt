package io.github.tutualican.tuyo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.tutualican.tuyo.R
import io.github.tutualican.tuyo.domain.SectionTemplate
import io.github.tutualican.tuyo.domain.sectionTemplates
import io.github.tutualican.tuyo.domain.model.ExamNet
import io.github.tutualican.tuyo.domain.model.ExamSectionResult
import io.github.tutualican.tuyo.domain.model.ExamTrack
import io.github.tutualican.tuyo.domain.model.ExamType
import io.github.tutualican.tuyo.domain.model.NetGoal
import io.github.tutualican.tuyo.domain.model.PlanMetric
import io.github.tutualican.tuyo.domain.model.PlanPeriod
import io.github.tutualican.tuyo.domain.model.Subject
import io.github.tutualican.tuyo.ui.components.FieldLabel
import io.github.tutualican.tuyo.ui.components.Hairline
import io.github.tutualican.tuyo.ui.components.MetaText
import io.github.tutualican.tuyo.ui.components.Tag
import io.github.tutualican.tuyo.ui.components.TuyoButton
import io.github.tutualican.tuyo.ui.components.TuyoButtonVariant
import io.github.tutualican.tuyo.ui.components.TuyoChoiceChip
import io.github.tutualican.tuyo.ui.components.TuyoDialog
import io.github.tutualican.tuyo.ui.components.TuyoSegmented
import io.github.tutualican.tuyo.ui.components.TuyoSwitch
import io.github.tutualican.tuyo.ui.components.TuyoTextArea
import io.github.tutualican.tuyo.ui.components.TuyoTextField
import io.github.tutualican.tuyo.ui.components.netText
import io.github.tutualican.tuyo.ui.theme.LocalTuyoColors
import io.github.tutualican.tuyo.ui.theme.Radius
import io.github.tutualican.tuyo.ui.theme.Space
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun AddQuestionDialog(
    date: LocalDate,
    onDismiss: () -> Unit,
    onAdd: (LocalDate, Subject, Int, Int, Int) -> Unit,
) {
    var subjectName by rememberSaveable { mutableStateOf(Subject.MATEMATIK.name) }
    var correct by rememberSaveable { mutableStateOf("") }
    var wrong by rememberSaveable { mutableStateOf("") }
    var blank by rememberSaveable { mutableStateOf("") }
    val subject = Subject.entries.firstOrNull { it.name == subjectName } ?: Subject.MATEMATIK
    val c = correct.toIntOrNull() ?: 0
    val w = wrong.toIntOrNull() ?: 0
    val b = blank.toIntOrNull() ?: 0
    val valid = c + w + b > 0

    TuyoDialog(
        eyebrow = "SORU KAYDI",
        title = "D/Y/B gir",
        onDismiss = onDismiss,
        footer = {
            TuyoButton(
                text = stringResource(R.string.action_cancel),
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                variant = TuyoButtonVariant.Ghost,
            )
            TuyoButton(
                text = "Kaydet",
                onClick = { onAdd(date, subject, c, w, b) },
                modifier = Modifier.weight(1f),
                enabled = valid,
            )
        },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Tag(text = date.format(DateTimeFormatter.ofPattern("d MMMM", Locale.forLanguageTag("tr"))))
            Spacer(Modifier.weight(1f))
            MetaText("D/Y/B")
        }
        Spacer(Modifier.height(Space.md))
        FieldLabel("Ders")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Subject.entries.forEach { option ->
                TuyoChoiceChip(
                    label = option.label,
                    selected = option == subject,
                    onClick = { subjectName = option.name },
                )
            }
        }
        Spacer(Modifier.height(Space.lg))
        Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
            NumberField("Doğru", correct, Modifier.weight(1f)) { correct = it }
            NumberField("Yanlış", wrong, Modifier.weight(1f)) { wrong = it }
            NumberField("Boş", blank, Modifier.weight(1f)) { blank = it }
        }
        Spacer(Modifier.height(Space.md))
        Text(
            text = "Net: ${netText(c - w / 4.0)} · Toplam ${c + w + b}",
            style = MaterialTheme.typography.labelMedium,
            color = LocalTuyoColors.current.textSecondary,
        )
    }
}

@Composable
fun AddPlanTaskDialog(
    initialPeriod: PlanPeriod,
    date: LocalDate,
    onDismiss: () -> Unit,
    onAdd: (String, Subject?, PlanPeriod, PlanMetric, Int, LocalDate) -> Unit,
) {
    var title by rememberSaveable { mutableStateOf("") }
    var subjectName by rememberSaveable { mutableStateOf<String?>(null) }
    var periodName by rememberSaveable { mutableStateOf(initialPeriod.name) }
    var metricName by rememberSaveable { mutableStateOf(PlanMetric.QUESTIONS.name) }
    var target by rememberSaveable { mutableStateOf("") }
    val period = PlanPeriod.entries.firstOrNull { it.name == periodName } ?: initialPeriod
    val metric = PlanMetric.entries.firstOrNull { it.name == metricName } ?: PlanMetric.QUESTIONS
    val subject = subjectName?.let { name -> Subject.entries.firstOrNull { it.name == name } }
    val targetValue = target.toIntOrNull() ?: 0
    val valid = targetValue > 0

    TuyoDialog(
        eyebrow = "PLAN",
        title = "Plana görev ekle",
        onDismiss = onDismiss,
        footer = {
            TuyoButton(
                text = stringResource(R.string.action_cancel),
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                variant = TuyoButtonVariant.Ghost,
            )
            TuyoButton(
                text = "Ekle",
                onClick = {
                    onAdd(
                        title.ifBlank { if (metric == PlanMetric.QUESTIONS) "Soru hedefi" else "Odak bloğu" },
                        subject,
                        period,
                        metric,
                        targetValue,
                        date,
                    )
                },
                modifier = Modifier.weight(1f),
                enabled = valid,
            )
        },
    ) {
        FieldLabel("Görev")
        TuyoTextField(
            value = title,
            onValueChange = { title = it },
            placeholder = "Örn. Türev 20 soru",
            maxLength = 80,
        )
        Spacer(Modifier.height(Space.md))
        TuyoSegmented(
            labels = PlanPeriod.entries.map { it.label },
            selectedIndex = PlanPeriod.entries.indexOf(period),
            onSelect = { periodName = PlanPeriod.entries[it].name },
        )
        Spacer(Modifier.height(Space.md))
        TuyoSegmented(
            labels = PlanMetric.entries.map { it.label },
            selectedIndex = PlanMetric.entries.indexOf(metric),
            onSelect = { metricName = PlanMetric.entries[it].name },
        )
        Spacer(Modifier.height(Space.md))
        NumberField(
            label = if (metric == PlanMetric.QUESTIONS) "Hedef soru" else "Hedef dakika",
            value = target,
            modifier = Modifier.fillMaxWidth(),
        ) { target = it }
        Spacer(Modifier.height(Space.md))
        FieldLabel("Ders (isteğe bağlı)")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            TuyoChoiceChip(
                label = "Genel",
                selected = subject == null,
                onClick = { subjectName = null },
            )
            Subject.entries.forEach { option ->
                TuyoChoiceChip(
                    label = option.label,
                    selected = subject == option,
                    onClick = { subjectName = option.name },
                )
            }
        }
    }
}

@Composable
fun AddExamDialog(
    track: ExamTrack?,
    onDismiss: () -> Unit,
    onAdd: (ExamType, List<ExamSectionResult>, Int, Boolean) -> Unit,
) {
    var examName by rememberSaveable { mutableStateOf(ExamType.TYT.name) }
    var duration by rememberSaveable { mutableStateOf("165") }
    var reminder by rememberSaveable { mutableStateOf(false) }
    val exam = ExamType.entries.firstOrNull { it.name == examName } ?: ExamType.TYT
    val templates = remember(examName, track) { sectionTemplates(exam, track) }
    var drafts by remember(examName, track) {
        mutableStateOf(templates.map { SectionDraft(it.subject, it.questions) })
    }
    val results = drafts.map { it.toResult() }
    val totalNet = results.sumOf { it.net }
    val valid = drafts.any { it.total > 0 } && drafts.all { it.total <= it.questionCount }
    val estimated = (100 + (totalNet / exam.maxNet).coerceIn(0.0, 1.0) * 400).roundToInt()

    TuyoDialog(
        eyebrow = "DENEME",
        title = "Ders bazlı deneme ekle",
        onDismiss = onDismiss,
        footer = {
            TuyoButton(
                text = stringResource(R.string.action_cancel),
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                variant = TuyoButtonVariant.Ghost,
            )
            TuyoButton(
                text = "Denemeyi kaydet",
                onClick = {
                    onAdd(exam, results, duration.toIntOrNull() ?: 0, reminder)
                },
                modifier = Modifier.weight(1f),
                enabled = valid,
            )
        },
    ) {
        FieldLabel("Sınav")
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ExamType.entries.forEach { option ->
                TuyoChoiceChip(
                    label = option.label,
                    selected = option == exam,
                    onClick = {
                        examName = option.name
                        duration = when (option) {
                            ExamType.TYT -> "165"
                            ExamType.AYT -> "180"
                            ExamType.YDT -> "120"
                        }
                    },
                )
            }
        }
        Spacer(Modifier.height(Space.md))
        Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
            NumberField("Süre (dk)", duration, Modifier.weight(1f)) { duration = it }
            Column(modifier = Modifier.weight(1f)) {
                FieldLabel("Tahmini net")
                Text(
                    text = netText(totalNet),
                    style = MaterialTheme.typography.headlineSmall,
                    color = LocalTuyoColors.current.textPrimary,
                )
            }
        }
        Spacer(Modifier.height(Space.md))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Yarın analiz hatırlat",
                    style = MaterialTheme.typography.titleMedium,
                    color = LocalTuyoColors.current.textPrimary,
                )
                Text(
                    text = "Yanlış nedenlerini taze hafızayla yazman için.",
                    style = MaterialTheme.typography.bodySmall,
                    color = LocalTuyoColors.current.textSecondary,
                )
            }
            TuyoSwitch(checked = reminder, onCheckedChange = { reminder = it })
        }
        Spacer(Modifier.height(Space.md))
        Hairline()
        Spacer(Modifier.height(Space.md))
        Text(
            text = "Dersler · D / Y / B / dakika",
            style = MaterialTheme.typography.labelSmall,
            color = LocalTuyoColors.current.textTertiary,
        )
        Spacer(Modifier.height(Space.sm))
        drafts.forEachIndexed { index, draft ->
            SectionDraftRow(
                draft = draft,
                onChange = { next ->
                    drafts = drafts.toMutableList().also { it[index] = next }
                },
            )
            if (index != drafts.lastIndex) Spacer(Modifier.height(6.dp))
        }
        Spacer(Modifier.height(Space.md))
        Text(
            text = "Tahmini puan: $estimated / 500 · resmî ÖSYM puanı değildir",
            style = MaterialTheme.typography.labelSmall,
            color = LocalTuyoColors.current.textTertiary,
        )
    }
}

@Composable
fun ExamAnalysisDialog(
    exam: ExamNet,
    onDismiss: () -> Unit,
    onSave: (String, String, Boolean) -> Unit,
) {
    val colors = LocalTuyoColors.current
    var note by rememberSaveable(exam.id) { mutableStateOf(exam.analysisNote) }
    var lossTopics by rememberSaveable(exam.id) { mutableStateOf(exam.lossTopics) }
    var reminder by rememberSaveable(exam.id) { mutableStateOf(false) }
    val sections = exam.subjectSections().sortedByDescending { it.loss }

    TuyoDialog(
        eyebrow = "ANALİZ",
        title = "${exam.exam.label} · ${exam.date}",
        onDismiss = onDismiss,
        footer = {
            TuyoButton(
                text = stringResource(R.string.action_cancel),
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                variant = TuyoButtonVariant.Ghost,
            )
            TuyoButton(
                text = "Analizi kaydet",
                onClick = { onSave(note, lossTopics, reminder) },
                modifier = Modifier.weight(1f),
            )
        },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Tag(text = "${netText(exam.calculatedNet)} net", color = colors.accent)
            Spacer(Modifier.width(Space.sm))
            Tag(text = "~${exam.estimatedScore} puan", color = colors.textSecondary)
            Spacer(Modifier.weight(1f))
            if (exam.analysisCompleted) {
                Tag(text = "Analiz tamam", color = colors.success)
            }
        }
        Spacer(Modifier.height(Space.md))
        Text(
            text = "Ders bazlı kayıp (yanlış + boş)",
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary,
        )
        Spacer(Modifier.height(6.dp))
        if (sections.isEmpty()) {
            MetaText("Bu deneme net olarak girilmiş; ders kırılımı yok.")
        } else {
            sections.forEach { section ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = section.subject.label,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textPrimary,
                        modifier = Modifier.weight(1f),
                    )
                    MetaText("${section.correct}/${section.wrong}/${section.blank}")
                    Spacer(Modifier.width(Space.sm))
                    Text(
                        text = "${netText(section.net)} net",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (section.net < 0) colors.danger else colors.textSecondary,
                        modifier = Modifier.width(58.dp),
                    )
                    MetaText(if (section.minutes > 0) "${section.minutes} dk" else "—")
                }
            }
        }
        Spacer(Modifier.height(Space.lg))
        FieldLabel("Kayıp konular")
        TuyoTextField(
            value = lossTopics,
            onValueChange = { lossTopics = it },
            placeholder = "Örn. Türev zincir kuralı, paragraf ana fikir",
            maxLength = 160,
        )
        Spacer(Modifier.height(Space.md))
        FieldLabel("Ne oldu? Bir sonraki denemede ne değişecek?")
        TuyoTextArea(
            value = note,
            onValueChange = { note = it },
            placeholder = "Kısa tut: hata türü, konu ve tek bir düzeltme.",
            maxLength = 500,
        )
        Spacer(Modifier.height(Space.md))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Yarın tekrar hatırlat",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                modifier = Modifier.weight(1f),
            )
            TuyoSwitch(checked = reminder, onCheckedChange = { reminder = it })
        }
    }
}

@Composable
fun NetGoalDialog(
    onDismiss: () -> Unit,
    onAdd: (ExamType, Subject?, Double) -> Unit,
) {
    var examName by rememberSaveable { mutableStateOf(ExamType.TYT.name) }
    var subjectName by rememberSaveable { mutableStateOf<String?>(null) }
    var targetText by rememberSaveable { mutableStateOf("") }
    val exam = ExamType.entries.firstOrNull { it.name == examName } ?: ExamType.TYT
    val subject = subjectName?.let { name -> Subject.entries.firstOrNull { it.name == name } }
    val target = targetText.replace(',', '.').toDoubleOrNull()
    val valid = target != null && target in 0.0..exam.maxNet
    val month = YearMonth.now().format(
        DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("tr")),
    )

    TuyoDialog(
        eyebrow = "HEDEF",
        title = "Net hedefi koy",
        onDismiss = onDismiss,
        footer = {
            TuyoButton(
                text = stringResource(R.string.action_cancel),
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                variant = TuyoButtonVariant.Ghost,
            )
            TuyoButton(
                text = "Hedefi kaydet",
                onClick = { target?.let { onAdd(exam, subject, it) } },
                modifier = Modifier.weight(1f),
                enabled = valid,
            )
        },
    ) {
        MetaText("Dönem: $month")
        Spacer(Modifier.height(Space.md))
        FieldLabel("Sınav")
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ExamType.entries.forEach { option ->
                TuyoChoiceChip(
                    label = option.label,
                    selected = option == exam,
                    onClick = { examName = option.name },
                )
            }
        }
        Spacer(Modifier.height(Space.md))
        FieldLabel("Ders (isteğe bağlı)")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            TuyoChoiceChip(
                label = "Toplam",
                selected = subject == null,
                onClick = { subjectName = null },
            )
            Subject.entries.forEach { option ->
                TuyoChoiceChip(
                    label = option.label,
                    selected = subject == option,
                    onClick = { subjectName = option.name },
                )
            }
        }
        Spacer(Modifier.height(Space.md))
        FieldLabel("Hedef net")
        TuyoTextField(
            value = targetText,
            onValueChange = { if (it.length <= 6) targetText = it },
            placeholder = "Örn. 30",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "0 ile ${exam.maxNet.toInt()} arasında. Bu ayın en yeni denemesiyle ölçülür.",
            style = MaterialTheme.typography.labelSmall,
            color = if (targetText.isNotEmpty() && !valid) {
                LocalTuyoColors.current.danger
            } else {
                LocalTuyoColors.current.textTertiary
            },
        )
    }
}

@Composable
private fun NumberField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onChange: (String) -> Unit,
) {
    Column(modifier = modifier) {
        FieldLabel(label)
        TuyoTextField(
            value = value,
            onValueChange = { if (it.length <= 4) onChange(it.filter(Char::isDigit)) },
            placeholder = "0",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
    }
}

@Composable
private fun SectionDraftRow(
    draft: SectionDraft,
    onChange: (SectionDraft) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Column(modifier = Modifier.width(78.dp)) {
            Text(
                text = draft.subject.shortLabel,
                style = MaterialTheme.typography.labelMedium,
                color = LocalTuyoColors.current.textPrimary,
                maxLines = 1,
            )
            MetaText("/${draft.questionCount}")
        }
        DraftField("D", draft.correct, Modifier.weight(1f)) {
            onChange(draft.copy(correct = it))
        }
        DraftField("Y", draft.wrong, Modifier.weight(1f)) {
            onChange(draft.copy(wrong = it))
        }
        DraftField("B", draft.blank, Modifier.weight(1f)) {
            onChange(draft.copy(blank = it))
        }
        DraftField("Dk", draft.minutes, Modifier.weight(1f)) {
            onChange(draft.copy(minutes = it))
        }
    }
}

@Composable
private fun DraftField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onChange: (String) -> Unit,
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = LocalTuyoColors.current.textTertiary,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .clip(RoundedCornerShape(Radius.sm))
                .background(LocalTuyoColors.current.surface)
                .border(
                    1.dp,
                    LocalTuyoColors.current.hairlineStrong,
                    RoundedCornerShape(Radius.sm),
                )
                .padding(horizontal = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            androidx.compose.foundation.text.BasicTextField(
                value = value,
                onValueChange = { next ->
                    if (next.length <= 3) onChange(next.filter(Char::isDigit))
                },
                textStyle = MaterialTheme.typography.bodySmall.copy(
                    color = LocalTuyoColors.current.textPrimary,
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
            )
        }
    }
}

private data class SectionDraft(
    val subject: Subject,
    val questionCount: Int,
    val correct: String = "",
    val wrong: String = "",
    val blank: String = "",
    val minutes: String = "",
) {
    val total: Int
        get() = (correct.toIntOrNull() ?: 0) +
            (wrong.toIntOrNull() ?: 0) +
            (blank.toIntOrNull() ?: 0)

    fun toResult(): ExamSectionResult = ExamSectionResult(
        subject = subject,
        correct = correct.toIntOrNull() ?: 0,
        wrong = wrong.toIntOrNull() ?: 0,
        blank = blank.toIntOrNull() ?: 0,
        minutes = minutes.toIntOrNull() ?: 0,
    )
}
