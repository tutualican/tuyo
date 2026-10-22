package io.github.tutualican.tuyo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.github.tutualican.tuyo.R
import io.github.tutualican.tuyo.ui.theme.LocalTuyoColors
import io.github.tutualican.tuyo.ui.theme.Radius
import io.github.tutualican.tuyo.ui.theme.Space
import java.util.Locale

/**
 * The command palette is the keyboard-first surface for the whole app. It is
 * also the accessibility shortcut: every destination and primary action is
 * reachable without hunting through the layout.
 */
data class CommandEntry(
    val id: String,
    val title: String,
    val section: String,
    val icon: ImageVector,
    val run: () -> Unit,
)

@Composable
fun CommandPalette(
    entries: List<CommandEntry>,
    onDismiss: () -> Unit,
) {
    val colors = LocalTuyoColors.current
    val shape = RoundedCornerShape(Radius.lg)
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableIntStateOf(0) }
    val focusRequester = remember { FocusRequester() }

    val normalized = query.trim().lowercase(Locale.forLanguageTag("tr"))
    val filtered = remember(entries, normalized) {
        if (normalized.isEmpty()) {
            entries
        } else {
            entries.filter {
                it.title.lowercase(Locale.forLanguageTag("tr")).contains(normalized)
            }
        }
    }
    LaunchedEffect(normalized) { selected = 0 }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    fun runSelected() {
        filtered.getOrNull(selected)?.let {
            onDismiss()
            it.run()
        }
    }

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
                ),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = 84.dp, start = 16.dp, end = 16.dp)
                    .widthIn(max = 560.dp)
                    .fillMaxWidth()
                    .clip(shape)
                    .background(colors.overlay)
                    .border(1.dp, colors.hairlineStrong, shape)
                    .pointerInput(Unit) { detectTapGestures { } }
                    .padding(vertical = 6.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        tint = colors.textTertiary,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    BasicTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester)
                            .onPreviewKeyEvent { event ->
                                if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                                when (event.key) {
                                    Key.DirectionDown -> {
                                        if (filtered.isNotEmpty()) {
                                            selected = (selected + 1).coerceAtMost(filtered.lastIndex)
                                        }
                                        true
                                    }
                                    Key.DirectionUp -> {
                                        selected = (selected - 1).coerceAtLeast(0)
                                        true
                                    }
                                    Key.Enter -> {
                                        runSelected()
                                        true
                                    }
                                    Key.Escape -> {
                                        onDismiss()
                                        true
                                    }
                                    else -> false
                                }
                            },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.titleMedium.copy(color = colors.textPrimary),
                        cursorBrush = SolidColor(colors.accent),
                        decorationBox = { inner ->
                            Box {
                                if (query.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.command_placeholder),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = colors.textTertiary,
                                    )
                                }
                                inner()
                            }
                        },
                    )
                    KeyCap(text = "esc")
                }
                Hairline(modifier = Modifier.padding(horizontal = 8.dp))
                Column(
                    modifier = Modifier
                        .heightIn(max = 360.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                ) {
                    if (filtered.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(88.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = stringResource(R.string.command_empty),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textTertiary,
                            )
                        }
                    }
                    var section: String? = null
                    filtered.forEachIndexed { index, entry ->
                        if (entry.section != section) {
                            section = entry.section
                            Eyebrow(
                                text = entry.section,
                                modifier = Modifier.padding(
                                    start = 10.dp,
                                    top = 8.dp,
                                    bottom = 4.dp,
                                ),
                            )
                        }
                        val isSelected = index == selected
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .clip(RoundedCornerShape(Radius.sm))
                                .background(if (isSelected) colors.selection else Color.Transparent)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                ) {
                                    onDismiss()
                                    entry.run()
                                }
                                .padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = entry.icon,
                                contentDescription = null,
                                tint = if (isSelected) colors.textPrimary else colors.textSecondary,
                                modifier = Modifier.size(15.dp),
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = entry.title,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSelected) colors.textPrimary else colors.textSecondary,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            if (isSelected) {
                                Text(
                                    text = "↵",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = colors.textTertiary,
                                )
                            }
                        }
                    }
                }
                Hairline(modifier = Modifier.padding(horizontal = 8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Hint("↑↓", stringResource(R.string.command_hint_navigate))
                    Hint("↵", stringResource(R.string.command_hint_open))
                    Spacer(Modifier.weight(1f))
                    Hint("⌘K", "komut")
                }
            }
        }
    }
}

@Composable
private fun KeyCap(text: String) {
    val colors = LocalTuyoColors.current
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
        color = colors.textTertiary,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .border(1.dp, colors.hairline, RoundedCornerShape(4.dp))
            .padding(horizontal = 5.dp, vertical = 2.dp),
    )
}

@Composable
private fun Hint(keys: String, label: String) {
    val colors = LocalTuyoColors.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        KeyCap(keys)
        Spacer(Modifier.width(5.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary,
        )
    }
}
