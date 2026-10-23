package io.github.tutualican.tuyo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.github.tutualican.tuyo.R
import io.github.tutualican.tuyo.ui.theme.LocalTuyoColors
import io.github.tutualican.tuyo.ui.theme.Radius
import io.github.tutualican.tuyo.ui.theme.Space

/**
 * The one modal surface used by capture and confirmation flows. Keeping the
 * scrim, header, scroll behaviour and footer in one place makes every dialog
 * feel like the same instrument rather than a separate screen.
 */
@Composable
fun TuyoDialog(
    eyebrow: String,
    title: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    footer: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = LocalTuyoColors.current
    val shape = RoundedCornerShape(Radius.lg)
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
                modifier = modifier
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
                        .padding(Space.xl),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Eyebrow(eyebrow)
                            Spacer(Modifier.height(5.dp))
                            Text(
                                text = title,
                                style = MaterialTheme.typography.headlineSmall,
                                color = colors.textPrimary,
                            )
                        }
                        IconButtonGhost(
                            icon = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.dialog_close),
                            onClick = onDismiss,
                        )
                    }
                    Spacer(Modifier.height(Space.xl))
                    content()
                }
                if (footer != null) {
                    Hairline(modifier = Modifier.padding(horizontal = Space.xl))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Space.xl, vertical = Space.md),
                        horizontalArrangement = Arrangement.spacedBy(Space.sm),
                        content = footer,
                    )
                }
            }
        }
    }
}

@Composable
fun TuyoConfirmDialog(
    eyebrow: String,
    title: String,
    body: String,
    confirmLabel: String,
    dismissLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    destructive: Boolean = false,
) {
    TuyoDialog(
        eyebrow = eyebrow,
        title = title,
        onDismiss = onDismiss,
        footer = {
            TuyoButton(
                text = dismissLabel,
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                variant = TuyoButtonVariant.Ghost,
            )
            TuyoButton(
                text = confirmLabel,
                onClick = onConfirm,
                modifier = Modifier.weight(1f),
                variant = if (destructive) TuyoButtonVariant.Danger else TuyoButtonVariant.Primary,
            )
        },
    ) {
        Text(
            text = body,
            style = MaterialTheme.typography.bodySmall,
            color = LocalTuyoColors.current.textSecondary,
        )
    }
}
