package com.example.eatwise.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.eatwise.ui.theme.GreenDeep
import com.example.eatwise.ui.theme.GreenSoft
import com.example.eatwise.ui.theme.OrangePrimary
import com.example.eatwise.ui.theme.OrangeSoft

@Composable
fun MacroChip(label: String, value: Double?, modifier: Modifier = Modifier) {
    val icon: ImageVector
    val color: Color
    val background: Color
    when (label) {
        "蛋白质" -> {
            icon = Icons.Rounded.FitnessCenter
            color = GreenDeep
            background = GreenSoft
        }
        "碳水" -> {
            icon = Icons.Rounded.Eco
            color = Color(0xFFE69A00)
            background = Color(0xFFFFF5DC)
        }
        else -> {
            icon = Icons.Rounded.LocalFireDepartment
            color = OrangePrimary
            background = OrangeSoft
        }
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
                .padding(horizontal = 10.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .background(background, CircleShape),
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(23.dp))
            }
            Column {
                Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    value?.let { "%.1f g".format(it) } ?: "--",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
