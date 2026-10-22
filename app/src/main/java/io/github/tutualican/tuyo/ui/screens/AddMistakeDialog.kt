package io.github.tutualican.tuyo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.github.tutualican.tuyo.domain.model.ErrorKind
import io.github.tutualican.tuyo.domain.model.Mistake
import io.github.tutualican.tuyo.domain.model.Topic
import io.github.tutualican.tuyo.ui.components.Eyebrow
import io.github.tutualican.tuyo.ui.components.Hairline
import io.github.tutualican.tuyo.ui.components.IconButtonGhost
import io.github.tutualican.tuyo.ui.components.TuyoButton
import io.github.tutualican.tuyo.ui.components.TuyoButtonVariant
import io.github.tutualican.tuyo.ui.theme.LocalTuyoColors
import io.github.tutualican.tuyo.ui.theme.Radius
import io.github.tutualican.tuyo.ui.theme.Space
import java.time.LocalDate

/**
 * Deliberately three fields: topic, error kind, note. A notebook entry must be
 * cheaper to create than the question that caused it, otherwise students stop
 * logging. Everything else is inferred.
 */
@Composable
fun AddMistakeDialog(
    topics: List<Topic>,
    onDismiss: () -> Unit,
    onAdd: (Mistake) -> Unit,
) {
    if (topics.isEmpty()) return
    val colors = LocalTuyoColors.current
    val shape = RoundedCornerShape(Radius.lg)
    var topicId by rememberSaveable { mutableStateOf(topics.first().id) }
    var errorName by rememberSaveable { mutableStateOf(ErrorKind.BILGI.name) }
    var note by rememberSaveable { mutableStateOf("") }
    val selectedTopic = topics.firstOrNull { it.id == topicId } ?: topics.first()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                )
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = Space.lg)
                    .fillMaxWidth()
                    .widthIn(max = 560.dp)
                    .clip(shape)
                    .background(colors.overlay)
                    .border(1.dp, colors.hairlineStrong, shape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                    ),
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(
                            start = Space.xl,
                            end = Space.xl,
                            top = Space.xl,
                            bottom = Space.md,
                        ),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Eyebrow("DEFTER")
                            Spacer(Modifier.height(5.dp))
                            Text(
                                text = "Yanlış ekle",
                                style = MaterialTheme.typography.headlineSmall,
                                color = colors.textPrimary,
                            )
                        }
                        IconButtonGhost(
                            icon = Icons.Filled.Close,
                            contentDescription = "Kapat",
                            onClick = onDismiss,
                        )
                    }
                    Spacer(Modifier.height(Space.xl))
                    Eyebrow("KONU")
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        topics.forEach { topic ->
                            ChoiceChip(
                                label = topic.title,
                                selected = topic.id == selectedTopic.id,
                                onClick = { topicId = topic.id },
                            )
                        }
                    }
                    Spacer(Modifier.height(Space.lg))
                    Eyebrow("HATA TÜRÜ")
                    Spacer(Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        ErrorKind.entries.chunked(2).forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                row.forEach { kind ->
                                    ChoiceChip(
                                        label = kind.label,
                                        selected = errorName == kind.name,
                                        modifier = Modifier.weight(1f),
                                        onClick = { errorName = kind.name },
                                    )
                                }
                                if (row.size == 1) {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(Space.lg))
                    Eyebrow("NOT")
                    Spacer(Modifier.height(8.dp))
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
                                            text = "Neyi kaçırdın? Kısa tut.",
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
                Hairline(modifier = Modifier.padding(horizontal = Space.xl))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Space.xl, vertical = Space.md),
                    horizontalArrangement = Arrangement.spacedBy(Space.sm),
                ) {
                    TuyoButton(
                        text = "Vazgeç",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        variant = TuyoButtonVariant.Ghost,
                    )
                    TuyoButton(
                        text = "Deftere ekle",
                        onClick = {
                            val kind = ErrorKind.entries.firstOrNull { it.name == errorName }
                                ?: ErrorKind.BILGI
                            val today = LocalDate.now()
                            onAdd(
                                Mistake(
                                    id = "local-${System.currentTimeMillis()}",
                                    subject = selectedTopic.subject,
                                    topicId = selectedTopic.id,
                                    topicTitle = selectedTopic.title,
                                    questionRef = "Elle eklendi · $today",
                                    note = note.trim().ifEmpty { kind.advice },
                                    errorKind = kind,
                                    createdOn = today,
                                    dueOn = today.plusDays(1),
                                    mastery = 0,
                                ),
                            )
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun ChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalTuyoColors.current
    val shape = RoundedCornerShape(Radius.sm)
    Box(
        modifier = modifier
            .height(34.dp)
            .clip(shape)
            .background(if (selected) colors.accentSoft else colors.surface)
            .border(
                width = 1.dp,
                color = if (selected) colors.accent.copy(alpha = 0.4f) else colors.hairline,
                shape = shape,
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) colors.textPrimary else colors.textSecondary,
            maxLines = 1,
        )
    }
}
