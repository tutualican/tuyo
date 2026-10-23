package io.github.tutualican.tuyo.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.tutualican.tuyo.R
import io.github.tutualican.tuyo.domain.model.Confidence
import io.github.tutualican.tuyo.domain.model.ErrorKind
import io.github.tutualican.tuyo.domain.model.Mistake
import io.github.tutualican.tuyo.domain.model.Subject
import io.github.tutualican.tuyo.domain.model.Topic
import io.github.tutualican.tuyo.ui.components.FieldLabel
import io.github.tutualican.tuyo.ui.components.LocalImage
import io.github.tutualican.tuyo.ui.components.TuyoButton
import io.github.tutualican.tuyo.ui.components.TuyoButtonVariant
import io.github.tutualican.tuyo.ui.components.TuyoChoiceChip
import io.github.tutualican.tuyo.ui.components.TuyoDialog
import io.github.tutualican.tuyo.ui.components.TuyoTextAction
import io.github.tutualican.tuyo.ui.theme.LocalTuyoColors
import io.github.tutualican.tuyo.ui.theme.Radius
import io.github.tutualican.tuyo.ui.theme.Space
import java.time.LocalDate
import java.util.UUID

/**
 * Deliberately few fields: subject, topic, error kind, note. A notebook entry
 * must be cheaper to create than the question that caused it, otherwise
 * students stop logging.
 */
@Composable
fun AddMistakeDialog(
    topics: List<Topic>,
    onDismiss: () -> Unit,
    onAdd: (Mistake) -> Unit,
    initialTopicId: String? = null,
) {
    if (topics.isEmpty()) return
    val colors = LocalTuyoColors.current
    val context = LocalContext.current
    val initialTopic = topics.firstOrNull { it.id == initialTopicId } ?: topics.first()
    var subjectName by rememberSaveable { mutableStateOf(initialTopic.subject.name) }
    var topicId by rememberSaveable { mutableStateOf(initialTopic.id) }
    var errorName by rememberSaveable { mutableStateOf(ErrorKind.BILGI.name) }
    var confidenceName by rememberSaveable { mutableStateOf(Confidence.SURE.name) }
    var note by rememberSaveable { mutableStateOf("") }
    var photoUri by rememberSaveable { mutableStateOf<String?>(null) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
            photoUri = uri.toString()
        }
    }

    val subject = Subject.entries.firstOrNull { it.name == subjectName } ?: initialTopic.subject
    val availableSubjects = topics.map { it.subject }.distinct().sortedBy { it.ordinal }
    val subjectTopics = topics.filter { it.subject == subject }
    val selectedTopic = subjectTopics.firstOrNull { it.id == topicId }
        ?: subjectTopics.firstOrNull()
        ?: initialTopic

    fun selectSubject(next: Subject) {
        subjectName = next.name
        topicId = topics.firstOrNull { it.subject == next }?.id ?: topicId
    }

    TuyoDialog(
        eyebrow = stringResource(R.string.add_mistake_eyebrow),
        title = stringResource(R.string.mistakes_add),
        onDismiss = onDismiss,
        footer = {
            TuyoButton(
                text = stringResource(R.string.action_cancel),
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                variant = TuyoButtonVariant.Ghost,
            )
            TuyoButton(
                text = stringResource(R.string.add_mistake_confirm),
                onClick = {
                    val kind = ErrorKind.entries.firstOrNull { it.name == errorName }
                        ?: ErrorKind.BILGI
                    val confidence = Confidence.entries
                        .firstOrNull { it.name == confidenceName } ?: Confidence.SURE
                    val today = LocalDate.now()
                    onAdd(
                        Mistake(
                            id = UUID.randomUUID().toString(),
                            subject = selectedTopic.subject,
                            topicId = selectedTopic.id,
                            topicTitle = selectedTopic.title,
                            questionRef = "Deftere elle eklendi",
                            note = note.trim(),
                            errorKind = kind,
                            createdOn = today,
                            dueOn = today.plusDays(1),
                            mastery = 0,
                            confidence = confidence,
                            photoUri = photoUri,
                        ),
                    )
                },
                modifier = Modifier.weight(1f),
            )
        },
    ) {
        FieldLabel(stringResource(R.string.add_mistake_subject))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            availableSubjects.forEach { option ->
                TuyoChoiceChip(
                    label = option.label,
                    selected = option == subject,
                    onClick = { selectSubject(option) },
                )
            }
        }
        Spacer(Modifier.height(Space.lg))
        FieldLabel(stringResource(R.string.add_mistake_topic))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            subjectTopics.forEach { topic ->
                TuyoChoiceChip(
                    label = topic.title,
                    selected = topic.id == selectedTopic.id,
                    onClick = { topicId = topic.id },
                )
            }
        }
        Spacer(Modifier.height(Space.lg))
        FieldLabel(stringResource(R.string.add_mistake_error_kind))
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            ErrorKind.entries.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    row.forEach { kind ->
                        TuyoChoiceChip(
                            label = kind.label,
                            selected = errorName == kind.name,
                            modifier = Modifier.weight(1f),
                            onClick = { errorName = kind.name },
                        )
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
        Spacer(Modifier.height(Space.lg))
        FieldLabel("Bu soruda ne kadar emindin?")
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Confidence.entries.forEach { option ->
                TuyoChoiceChip(
                    label = option.label,
                    selected = confidenceName == option.name,
                    onClick = { confidenceName = option.name },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Spacer(Modifier.height(Space.lg))
        FieldLabel("Soru fotoğrafı (isteğe bağlı)")
        if (photoUri == null) {
            TuyoButton(
                text = "Fotoğraf ekle",
                onClick = { photoPicker.launch(arrayOf("image/*")) },
                variant = TuyoButtonVariant.Secondary,
            )
        } else {
            LocalImage(
                uriString = photoUri!!,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(Radius.md)),
                contentDescription = "Yanlış sorunun fotoğrafı",
            )
            Spacer(Modifier.height(4.dp))
            TuyoTextAction(
                text = "Fotoğrafı kaldır",
                onClick = { photoUri = null },
                color = colors.danger,
                trailingArrow = false,
            )
        }
        Spacer(Modifier.height(Space.lg))
        FieldLabel(stringResource(R.string.add_mistake_note))
        val noteShape = RoundedCornerShape(Radius.md)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(82.dp)
                .clip(noteShape)
                .background(colors.surface)
                .border(1.dp, colors.hairline, noteShape)
                .padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            BasicTextField(
                value = note,
                onValueChange = { if (it.length <= 240) note = it },
                modifier = Modifier.fillMaxSize(),
                textStyle = MaterialTheme.typography.bodySmall.copy(
                    color = colors.textPrimary,
                ),
                cursorBrush = SolidColor(colors.accent),
                decorationBox = { inner ->
                    Box {
                        if (note.isEmpty()) {
                            Text(
                                text = stringResource(R.string.add_mistake_note_placeholder),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textTertiary,
                            )
                        }
                        inner()
                    }
                },
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = "${note.length}/240",
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary,
            modifier = Modifier.align(Alignment.End),
        )
    }
}
