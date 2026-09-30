package com.example.masterka.ui.spaces

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.masterka.data.Space

/**
 * Компактный dropdown выбора активного помещения.
 * Тап по элементу — onSelect(id).
 * Кнопка «Управлять помещениями →» — onManage().
 */
@Composable
fun SpaceDropdown(
    spaces: List<Space>,
    activeSpaceId: Long,
    onSelect: (Long) -> Unit,
    onManage: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val active = spaces.firstOrNull { it.id == activeSpaceId } ?: spaces.firstOrNull()

    Box(modifier) {
        AssistChip(
            onClick = { expanded = true },
            label = {
                Text(
                    active?.name ?: "Нет помещений",
                    fontWeight = FontWeight.Medium
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Warehouse,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = null
                )
            }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (spaces.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("Нет помещений", style = MaterialTheme.typography.bodySmall) },
                    onClick = { },
                    enabled = false
                )
            } else {
                spaces.forEach { space ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (space.id == activeSpaceId) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                }
                                Text(space.name)
                            }
                        },
                        onClick = {
                            expanded = false
                            onSelect(space.id)
                        }
                    )
                }
            }
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text("Управлять помещениями…") },
                onClick = {
                    expanded = false
                    onManage()
                }
            )
        }
    }
}