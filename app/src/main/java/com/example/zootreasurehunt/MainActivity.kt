package com.example.zootreasurehunt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zootreasurehunt.ui.theme.ZooTreasureHuntTheme
import java.util.UUID

data class Sighting(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val isFound: Boolean = false,
    val notes: String = ""
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                ZooApp()
            }
        }
    }
}

@Composable
fun ZooApp() {

    val sightings = remember {
        mutableStateListOf(
            Sighting(name = "Lion"),
            Sighting(name = "Red Panda"),
            Sighting(name = "Giraffe"),
            Sighting(name = "Kangaroo"),
            Sighting(name = "Penguin")
        )
    }

    var selectedSighting by remember { mutableStateOf<Sighting?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    Scaffold { innerPadding ->

        LazyColumn(
            state = listState,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            item {
                Text(
                    text = stringResource(id = R.string.app_name),
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

                    onEditClick = {
                        selectedSighting = animal
                        showDialog = true
                    },

                    onSwipe = {
                        sightings.remove(animal)
                    }
                )
            }
        }

        if (showDialog) {
            selectedSighting?.let { sighting ->

                EditSightingDialog(
                    sighting = sighting,

                    onDismiss = {
                        showDialog = false
                    },

                    onSave = { updated ->

                        val index = sightings.indexOfFirst { it.id == updated.id }

                        if (index != -1) {
                            sightings[index] = updated
                        }

                        showDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun AnimalCard(
    sighting: Sighting,
    onClick: () -> Unit
) {

    val cardColor =
        if (sighting.isFound) Color(0xFFE8F5E9) else Color(0xFFF5F5F5)

    val textColor =
        if (sighting.isFound) Color(0xFF2E7D32) else Color.Black

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },

        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {

        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(modifier = Modifier.weight(1f)) {

                Text(
                    text = sighting.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )

                if (sighting.isFound && sighting.notes.isNotEmpty()) {
                    Text(
                        text = sighting.notes,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            if (sighting.isFound) {
                Text(
                    text = stringResource(id = R.string.found_label),
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
        }
    }
}

@Composable
fun EditSightingDialog(
    sighting: Sighting,
    onDismiss: () -> Unit,
    onSave: (Sighting) -> Unit
) {

    var notesText by remember { mutableStateOf(sighting.notes) }
    var isFoundChecked by remember { mutableStateOf(sighting.isFound) }

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text(text = stringResource(id = R.string.edit_animal))
        },

        text = {
            Column {

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text(stringResource(id = R.string.notes_hint)) }
                )

                Row(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Checkbox(
                        checked = isFoundChecked,
                        onCheckedChange = { isFoundChecked = it }
                    )

                    Text(text = stringResource(id = R.string.checkbox_found))
                }
            }
        },

        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        sighting.copy(
                            isFound = isFoundChecked,
                            notes = notesText
                        )
                    )
                }
            ) {
                Text(text = stringResource(id = R.string.save_btn))
            }
        },

        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.cancel_btn))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewApp() {
    ZooTreasureHuntTheme {
        ZooApp()
    }
}