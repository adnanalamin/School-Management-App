package com.example.shared.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.core.theme.EduError
import com.example.core.theme.EduShapes

enum class EduButtonVariant {
    PRIMARY,
    SECONDARY,
    OUTLINED,
    DANGER
}

@Composable
fun EduButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: EduButtonVariant = EduButtonVariant.PRIMARY,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    testTag: String = "edu_button"
) {
    val isInteractive = enabled && !isLoading
    val shape = EduShapes.medium

    when (variant) {
        EduButtonVariant.PRIMARY -> {
            Button(
                onClick = onClick,
                modifier = modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag(testTag),
                enabled = isInteractive,
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
            ) {
                ButtonContent(text, isLoading, leadingIcon, trailingIcon, MaterialTheme.colorScheme.onPrimary)
            }
        }

        EduButtonVariant.SECONDARY -> {
            Button(
                onClick = onClick,
                modifier = modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag(testTag),
                enabled = isInteractive,
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
            ) {
                ButtonContent(text, isLoading, leadingIcon, trailingIcon, MaterialTheme.colorScheme.onSecondary)
            }
        }

        EduButtonVariant.OUTLINED -> {
            OutlinedButton(
                onClick = onClick,
                modifier = modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag(testTag),
                enabled = isInteractive,
                shape = shape,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
            ) {
                ButtonContent(text, isLoading, leadingIcon, trailingIcon, MaterialTheme.colorScheme.primary)
            }
        }

        EduButtonVariant.DANGER -> {
            Button(
                onClick = onClick,
                modifier = modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag(testTag),
                enabled = isInteractive,
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = EduError,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
            ) {
                ButtonContent(text, isLoading, leadingIcon, trailingIcon, Color.White)
            }
        }
    }
}

@Composable
private fun ButtonContent(
    text: String,
    isLoading: Boolean,
    leadingIcon: (@Composable () -> Unit)?,
    trailingIcon: (@Composable () -> Unit)?,
    contentColor: Color
) {
    if (isLoading) {
        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            color = contentColor,
            strokeWidth = 2.dp
        )
    } else {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
            if (trailingIcon != null) {
                Spacer(modifier = Modifier.width(8.dp))
                trailingIcon()
            }
        }
    }
}
