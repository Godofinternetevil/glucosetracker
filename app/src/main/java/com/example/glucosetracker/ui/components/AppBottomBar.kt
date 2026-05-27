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

@Composable
fun AppBottomBar(modifier: Modifier = Modifier) {
    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        containerColor = AppColors.Card,
        tonalElevation = 0.dp
    ) {
        BottomItem(label = "Главная", selected = true, modifier = Modifier.weight(1f))
        BottomItem(label = "Графики", selected = false, modifier = Modifier.weight(1f))
        BottomItem(label = "События", selected = false, modifier = Modifier.weight(1f))
        BottomItem(label = "Профиль", selected = false, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun RowScope.BottomItem(label: String, selected: Boolean, modifier: Modifier = Modifier) {
    NavigationBarItem(
        modifier = modifier,
        selected = selected,
        onClick = { },
        icon = { Text(if (selected) "●" else "○") },
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