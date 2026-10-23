package io.github.tutualican.tuyo.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.tutualican.tuyo.R
import io.github.tutualican.tuyo.domain.model.Confidence
import io.github.tutualican.tuyo.domain.model.Mistake
import io.github.tutualican.tuyo.ui.theme.LocalTuyoColors
import io.github.tutualican.tuyo.ui.theme.Space
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun MistakeRow(
    mistake: Mistake,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now(),
) {
    val colors = LocalTuyoColors.current
    val interaction = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = Space.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SubjectTile(subject = mistake.subject)
        Spacer(Modifier.width(Space.md))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = mistake.topicTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.width(Space.sm))
                MetaText(text = dueLabel(mistake, today))
            }
            Spacer(Modifier.height(3.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Space.sm),
            ) {
                Text(
                    text = mistake.errorKind.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary,
                    maxLines = 1,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (mistake.confidence != Confidence.SURE) {
                    Tag(
                        text = mistake.confidence.shortLabel,
                        color = colors.warning,
                    )
                }
                Spacer(Modifier.weight(1f))
                MasteryDots(level = mistake.mastery)
            }
        }
    }
}

@Composable
private fun dueLabel(mistake: Mistake, today: LocalDate): String {
    val days = ChronoUnit.DAYS.between(today, mistake.dueOn)
    return when {
        days < 0L -> stringResource(R.string.mistakes_due_overdue)
        days == 0L -> stringResource(R.string.mistakes_due_today)
        days == 1L -> stringResource(R.string.mistakes_due_tomorrow)
        else -> stringResource(R.string.mistakes_due_in, days)
    }
}

fun netText(value: Double): String =
    String.format(java.util.Locale.forLanguageTag("tr"), "%.1f", value)
