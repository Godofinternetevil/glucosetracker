package com.example.glucosetracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.glucosetracker.ui.theme.AppColors
import com.example.glucosetracker.ui.theme.AppDimens

data class BottomBarItem(
    val route: String,
    val label: String,
    val icon: String
)

@Composable
fun AppBottomBar(
    items: List<BottomBarItem>,
    currentRoute: String?,
    onItemClick: (BottomBarItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(AppDimens.BottomBarRadius),
            color = AppColors.Card,
            tonalElevation = 0.dp,
            shadowElevation = AppDimens.CardElevation,
            border = BorderStroke(AppDimens.SoftBorderWidth, AppColors.Border)
        ) {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AppDimens.BottomBarRadius)),
                containerColor = Color.Transparent,
                tonalElevation = 0.dp
            ) {
                items.forEach { item ->
                    BottomItem(
                        item = item,
                        selected = item.route == currentRoute,
                        onClick = { onItemClick(item) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.BottomItem(
    item: BottomBarItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBarItem(
        modifier = modifier,
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = navIconFor(item.route),
                contentDescription = item.label
            )
        },
        label = {
            Text(
                text = item.label,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = AppColors.PrimaryGreen,
            selectedTextColor = AppColors.PrimaryGreen,
            unselectedIconColor = AppColors.TextSecondary,
            unselectedTextColor = AppColors.TextSecondary,
            indicatorColor = AppColors.PrimaryGreenSoft
        )
    )
}

@Composable
private fun navIconFor(route: String): ImageVector = remember(route) {
    when (route) {
        "home" -> chartIcon()
        "history" -> clockIcon()
        "reports" -> clipboardIcon()
        "profile" -> userIcon()
        else -> chartIcon()
    }
}

private fun chartIcon(): ImageVector = ImageVector.Builder(
    name = "ChartIcon",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path(fill = SolidColor(Color.Black)) {
        moveTo(3.5f, 18.49f)
        lineTo(9.5f, 12.48f)
        lineTo(13.5f, 16.48f)
        lineTo(22f, 6.92f)
        lineTo(20.59f, 5.5f)
        lineTo(13.5f, 13.47f)
        lineTo(9.5f, 9.47f)
        lineTo(2f, 16.99f)
        close()
    }
}.build()

private fun clockIcon(): ImageVector = ImageVector.Builder(
    name = "ClockIcon",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path(fill = SolidColor(Color.Black)) {
        moveTo(12f, 2f)
        curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
        reflectiveCurveTo(6.48f, 22f, 12f, 22f)
        reflectiveCurveTo(22f, 17.52f, 22f, 12f)
        reflectiveCurveTo(17.52f, 2f, 12f, 2f)
        close()
        moveTo(12f, 20f)
        curveTo(7.59f, 20f, 4f, 16.41f, 4f, 12f)
        reflectiveCurveTo(7.59f, 4f, 12f, 4f)
        reflectiveCurveTo(20f, 7.59f, 20f, 12f)
        reflectiveCurveTo(16.41f, 20f, 12f, 20f)
        close()
        moveTo(12.5f, 7f)
        horizontalLineTo(11f)
        verticalLineTo(13f)
        lineTo(16.25f, 16.15f)
        lineTo(17f, 14.92f)
        lineTo(12.5f, 12.25f)
        close()
    }
}.build()

private fun clipboardIcon(): ImageVector = ImageVector.Builder(
    name = "ClipboardIcon",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path(fill = SolidColor(Color.Black)) {
        moveTo(19f, 3f)
        horizontalLineTo(14.82f)
        curveTo(14.4f, 1.84f, 13.3f, 1f, 12f, 1f)
        reflectiveCurveTo(9.6f, 1.84f, 9.18f, 3f)
        horizontalLineTo(5f)
        curveTo(3.9f, 3f, 3f, 3.9f, 3f, 5f)
        verticalLineTo(21f)
        curveTo(3f, 22.1f, 3.9f, 23f, 5f, 23f)
        horizontalLineTo(19f)
        curveTo(20.1f, 23f, 21f, 22.1f, 21f, 21f)
        verticalLineTo(5f)
        curveTo(21f, 3.9f, 20.1f, 3f, 19f, 3f)
        close()
        moveTo(12f, 3f)
        curveTo(12.55f, 3f, 13f, 3.45f, 13f, 4f)
        reflectiveCurveTo(12.55f, 5f, 12f, 5f)
        reflectiveCurveTo(11f, 4.55f, 11f, 4f)
        reflectiveCurveTo(11.45f, 3f, 12f, 3f)
        close()
        moveTo(19f, 21f)
        horizontalLineTo(5f)
        verticalLineTo(5f)
        horizontalLineTo(7f)
        verticalLineTo(8f)
        horizontalLineTo(17f)
        verticalLineTo(5f)
        horizontalLineTo(19f)
        close()
        moveTo(7f, 12f)
        horizontalLineTo(17f)
        verticalLineTo(14f)
        horizontalLineTo(7f)
        close()
        moveTo(7f, 16f)
        horizontalLineTo(14f)
        verticalLineTo(18f)
        horizontalLineTo(7f)
        close()
    }
}.build()

private fun userIcon(): ImageVector = ImageVector.Builder(
    name = "UserIcon",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path(fill = SolidColor(Color.Black)) {
        moveTo(12f, 12f)
        curveTo(14.21f, 12f, 16f, 10.21f, 16f, 8f)
        reflectiveCurveTo(14.21f, 4f, 12f, 4f)
        reflectiveCurveTo(8f, 5.79f, 8f, 8f)
        reflectiveCurveTo(9.79f, 12f, 12f, 12f)
        close()
        moveTo(12f, 14f)
        curveTo(9.33f, 14f, 4f, 15.34f, 4f, 18f)
        verticalLineTo(20f)
        horizontalLineTo(20f)
        verticalLineTo(18f)
        curveTo(20f, 15.34f, 14.67f, 14f, 12f, 14f)
        close()
    }
}.build()