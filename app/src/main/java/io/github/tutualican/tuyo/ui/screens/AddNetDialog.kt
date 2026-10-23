package io.github.tutualican.tuyo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.tutualican.tuyo.R
import io.github.tutualican.tuyo.domain.model.ExamType
import io.github.tutualican.tuyo.ui.components.FieldLabel
import io.github.tutualican.tuyo.ui.components.TuyoButton
import io.github.tutualican.tuyo.ui.components.TuyoButtonVariant
import io.github.tutualican.tuyo.ui.components.TuyoChoiceChip
import io.github.tutualican.tuyo.ui.components.TuyoDialog
import io.github.tutualican.tuyo.ui.components.TuyoTextField
import io.github.tutualican.tuyo.ui.theme.LocalTuyoColors
import io.github.tutualican.tuyo.ui.theme.Space
import java.util.Locale

@Composable
fun AddNetDialog(
    onDismiss: () -> Unit,
    onAdd: (ExamType, Double) -> Unit,
) {
    val colors = LocalTuyoColors.current
    var examName by rememberSaveable { mutableStateOf(ExamType.TYT.name) }
    var netText by rememberSaveable { mutableStateOf("") }
    val exam = ExamType.entries.firstOrNull { it.name == examName } ?: ExamType.TYT
    val value = netText.replace(',', '.').toDoubleOrNull()
    val valid = value != null && value in 0.0..exam.maxNet

    TuyoDialog(
        eyebrow = stringResource(R.string.add_net_eyebrow),
        title = stringResource(R.string.today_net_add),
        onDismiss = onDismiss,
        footer = {
            TuyoButton(
                text = stringResource(R.string.action_cancel),
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                variant = TuyoButtonVariant.Ghost,
            )
            TuyoButton(
                text = stringResource(R.string.add_net_confirm),
                onClick = {
                    value?.takeIf { valid }?.let { onAdd(exam, it) }
                },
                enabled = valid,
                modifier = Modifier.weight(1f),
            )
        },
    ) {
        FieldLabel(stringResource(R.string.add_net_exam))
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
        FieldLabel(stringResource(R.string.add_net_value))
        TuyoTextField(
            value = netText,
            onValueChange = { next ->
                if (next.length <= 6) netText = next
            },
            placeholder = "Örn. ${
                String.format(Locale.forLanguageTag("tr"), "%.1f", exam.maxNet / 3)
            }",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = if (exam == ExamType.AYT) {
                stringResource(R.string.add_net_hint_ayt)
            } else {
                stringResource(R.string.add_net_hint, exam.maxNet.toInt())
            },
            style = MaterialTheme.typography.labelSmall,
            color = if (netText.isNotEmpty() && !valid) colors.danger else colors.textTertiary,
        )
    }
}
