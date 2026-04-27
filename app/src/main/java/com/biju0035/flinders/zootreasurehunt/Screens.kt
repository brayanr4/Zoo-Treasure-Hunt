package com.biju0035.flinders.zootreasurehunt

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text

@Composable
fun AboutScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Zoo Treasure Hunt\nCreated by\nBrayan",
            textAlign = TextAlign.Center,
            fontSize = 24.sp
        )
    }
}

@Composable
fun SettingsScreen(
    isSortByName: Boolean,
    onSortChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Settings",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Text(
            text = "Sort Order",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Column(Modifier.selectableGroup()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .selectable(
                        selected = isSortByName,
                        onClick = { onSortChange(true) },
                        role = Role.RadioButton
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = isSortByName,
                    onClick = null
                )
                Text(
                    text = "Sort by Name",
                    modifier = Modifier.padding(start = 16.dp),
                    fontSize = 20.sp
                )
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .selectable(
                        selected = !isSortByName,
                        onClick = { onSortChange(false) },
                        role = Role.RadioButton
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = !isSortByName,
                    onClick = null
                )
                Text(
                    text = "Sort by Recency / Found Status",
                    modifier = Modifier.padding(start = 16.dp),
                    fontSize = 20.sp
                )
            }
        }
    }
}

@Composable
fun ListScreen(
    sightings: List<Sighting>,
    onEditClick: (Sighting) -> Unit,
    onDelete: (Sighting) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = "Zoo Treasure Hunt",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }

        items(
            items = sightings,
            key = { it.id }
        ) { animal ->
            SwipeableSighting(
                sighting = animal,
                onEditClick = { onEditClick(animal) },
                onSwipe = { onDelete(animal) }
            )
        }
    }
}