package io.github.tutualican.tuyo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.tutualican.tuyo.R
import io.github.tutualican.tuyo.domain.model.StudyData
import io.github.tutualican.tuyo.domain.model.Subject
import io.github.tutualican.tuyo.ui.components.Eyebrow
import io.github.tutualican.tuyo.ui.components.Hairline
import io.github.tutualican.tuyo.ui.components.MistakeRow
import io.github.tutualican.tuyo.ui.components.TuyoButton
import io.github.tutualican.tuyo.ui.components.TuyoButtonVariant
import io.github.tutualican.tuyo.ui.components.TuyoCard
import io.github.tutualican.tuyo.ui.theme.LocalTuyoColors
import io.github.tutualican.tuyo.ui.theme.Radius
import io.github.tutualican.tuyo.ui.theme.Space

@Composable
fun MistakesScreen(
    data: StudyData,
    onOpenMistake: (String) -> Unit,
    onAddMistake: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalTuyoColors.current
    var filter by rememberSaveable { mutableStateOf<String?>(null) }
    val subjects = remember(data.mistakes) {
        Subject.entries.filter { subject -> data.mistakes.any { it.subject == subject } }
    }
    val visible = remember(data.mistakes, filter) {
        data.mistakes.filter { filter == null || it.subject.name == filter }
    }
    val due = data.mistakes.count { it.isDue(java.time.LocalDate.now()) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(Space.lg),
    ) {
        item {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Eyebrow("DEFTER")
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.mistakes_title),
                        style = MaterialTheme.typography.headlineLarge,
                        color = colors.textPrimary,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(
                            R.string.mistakes_subtitle,
                            data.mistakes.size,
                            due,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                    )
                }
                Spacer(Modifier.width(Space.md))
                TuyoButton(
                    text = stringResource(R.string.mistakes_add),
                    onClick = onAddMistake,
                    leadingIcon = Icons.Filled.Add,
                    variant = TuyoButtonVariant.Secondary,
                )
            }
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                FilterPill(
                    label = stringResource(R.string.mistakes_filter_all),
                    selected = filter == null,
                    onClick = { filter = null },
                )
                subjects.forEach { subject ->
                    FilterPill(
                        label = subject.label,
                        selected = filter == subject.name,
                        onClick = {
                            filter = if (filter == subject.name) null else subject.name
                        },
                    )
                }
            }
        }
        item {
            if (visible.isEmpty()) {
                EmptyMistakes()
            } else {
                TuyoCard(contentPadding = PaddingValues(horizontal = Space.lg, vertical = 2.dp)) {
                    visible.forEachIndexed { index, mistake ->
                        MistakeRow(
                            mistake = mistake,
                            onClick = { onOpenMistake(mistake.id) },
                        )
                        if (index != visible.lastIndex) Hairline()
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterPill(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = LocalTuyoColors.current
    val shape = RoundedCornerShape(Radius.pill)
    Box(
        modifier = Modifier
            .clip(shape)
            .background(if (selected) colors.selection else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (selected) colors.hairlineStrong else colors.hairline,
                shape = shape,
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 11.dp, vertical = 6.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) colors.textPrimary else colors.textSecondary,
        )
    }
}

@Composable
private fun EmptyMistakes() {
    val colors = LocalTuyoColors.current
    TuyoCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Space.xxl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.mistakes_empty_title),
                style = MaterialTheme.typography.titleLarge,
                color = colors.textPrimary,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.mistakes_empty_body),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
            )
        }
    }
}
