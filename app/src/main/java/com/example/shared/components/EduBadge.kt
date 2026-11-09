package com.example.shared.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.EduError
import com.example.core.theme.EduErrorContainer
import com.example.core.theme.EduInfo
import com.example.core.theme.EduInfoContainer
import com.example.core.theme.EduSuccess
import com.example.core.theme.EduSuccessContainer
import com.example.core.theme.EduWarning
import com.example.core.theme.EduWarningContainer

enum class BadgeType {
    SUCCESS,
    WARNING,
    ERROR,
    INFO,
    PRIMARY
}

@Composable
fun EduBadge(
    text: String,
    modifier: Modifier = Modifier,
    type: BadgeType = BadgeType.PRIMARY
) {
    val (backgroundColor, textColor) = when (type) {
        BadgeType.SUCCESS -> EduSuccessContainer to EduSuccess
        BadgeType.WARNING -> EduWarningContainer to EduWarning
        BadgeType.ERROR -> EduErrorContainer to EduError
        BadgeType.INFO -> EduInfoContainer to EduInfo
        BadgeType.PRIMARY -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = modifier
            .background(color = backgroundColor, shape = RoundedCornerShape(100.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelSmall
        )
    }
}
