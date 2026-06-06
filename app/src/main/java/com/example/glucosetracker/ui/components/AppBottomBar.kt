package com.example.glucosetracker.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.glucosetracker.ui.theme.AppColors

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
    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        containerColor = AppColors.Card,
        tonalElevation = 0.dp
    ) {
        items.forEach { item ->
            BottomItem(
                label = item.label,
                icon = item.icon,
                selected = item.route == currentRoute,
                onClick = { onItemClick(item) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun RowScope.BottomItem(
    label: String,
    icon: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBarItem(
        modifier = modifier,
        selected = selected,
        onClick = onClick,
        icon = { Text(if (selected) icon else "○") },
        label = {
            Text(
                text = label,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = AppColors.PrimaryGreen,
            selectedTextColor = AppColors.PrimaryGreen,
            unselectedIconColor = AppColors.TextSecondary,
            unselectedTextColor = AppColors.TextSecondary,
            indicatorColor = AppColors.Background
        )
    )
}