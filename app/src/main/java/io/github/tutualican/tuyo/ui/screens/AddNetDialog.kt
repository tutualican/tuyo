package io.github.tutualican.tuyo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.github.tutualican.tuyo.domain.model.ExamType
import io.github.tutualican.tuyo.ui.components.Eyebrow
import io.github.tutualican.tuyo.ui.components.IconButtonGhost
import io.github.tutualican.tuyo.ui.components.TuyoButton
import io.github.tutualican.tuyo.ui.components.TuyoButtonVariant
import io.github.tutualican.tuyo.ui.components.TuyoCard
import io.github.tutualican.tuyo.ui.components.TuyoChoiceChip
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

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Space.lg),
            contentAlignment = Alignment.Center,
        ) {
            TuyoCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Eyebrow("DENEME")
                        Spacer(Modifier.height(5.dp))
                        Text(
                            text = "Net ekle",
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
                Spacer(Modifier.height(Space.lg))
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
                TuyoTextField(
                    value = netText,
                    onValueChange = { next ->
                        if (next.length <= 6) netText = next
                    },
                    placeholder = "Örn. ${String.format(Locale.forLanguageTag("tr"), "%.1f", exam.maxNet / 3)}",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = if (exam == ExamType.AYT) {
                        "AYT'de alanına giren iki testin toplamı: 0 ile 80 net. Ondalık için virgül kullanabilirsin."
                    } else {
                        "0 ile ${exam.maxNet.toInt()} arasında bir net gir. Ondalık için virgül kullanabilirsin."
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (netText.isNotEmpty() && !valid) colors.danger else colors.textTertiary,
                )
                Spacer(Modifier.height(Space.lg))
                Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                    TuyoButton(
                        text = "Vazgeç",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        variant = TuyoButtonVariant.Ghost,
                    )
                    TuyoButton(
                        text = "Kaydet",
                        onClick = {
                            value?.takeIf { valid }?.let { onAdd(exam, it) }
                        },
                        enabled = valid,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}
