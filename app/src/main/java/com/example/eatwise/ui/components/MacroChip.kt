package com.example.eatwise.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MacroChip(label: String, value: Double?, modifier: Modifier = Modifier) {
    AssistChip(
        modifier = modifier,
        onClick = {},
        label = {
            Column(Modifier.padding(vertical = 4.dp)) {
                Text(label)
                Text(value?.let { "%.1f g".format(it) } ?: "--")
            }
        },
    )
}
