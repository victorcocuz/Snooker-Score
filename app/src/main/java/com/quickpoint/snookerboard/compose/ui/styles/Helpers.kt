package com.quickpoint.snookerboard.compose.ui.styles

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.quickpoint.snookerboard.compose.ui.theme.Beige
import com.quickpoint.snookerboard.compose.ui.theme.spacing

@Composable
fun HorizontalDivider() = Divider(color = Beige)

@Composable
fun RowScope.RowHorizontalDivider() = Divider(
    modifier = Modifier
        .weight(1f)
        .padding(MaterialTheme.spacing.extraSmall),
    color = Beige, thickness = 1.dp
)


@Composable
fun VerticalDivider(spacing: Dp) = Row {
    Spacer(Modifier.width(spacing))
    Divider(
        Modifier
            .fillMaxHeight()
            .width(1.dp), color = Beige
    )
    Spacer(Modifier.width(spacing))
}
