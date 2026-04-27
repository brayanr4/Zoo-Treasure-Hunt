package com.biju0035.flinders.zootreasurehunt

import android.Manifest
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import com.biju0035.flinders.zootreasurehunt.ui.theme.ZooTreasureHuntTheme
import com.biju0035.flinders.zootreasurehunt.viewmodel.ZooViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZooTreasureHuntTheme {
                ZooApp()
            }
        }
    }
}

/**
 * Stateful version of the ZooApp. This version interacts with the ViewModel.
 */
@Composable
fun ZooApp(
    zooViewModel: ZooViewModel = viewModel()
) {
    val sightings by zooViewModel.sightings.collectAsState()
    val isSortByName by zooViewModel.isSortByName.collectAsState()

    ZooContent(
        sightings = sightings,
        isSortByName = isSortByName,
        onSortChange = { zooViewModel.toggleSortOrder(it) },
        onUpdateSighting = { updated, wasPreviouslyFound ->
            zooViewModel.updateSighting(updated, wasPreviouslyFound)
        },
        onDeleteSighting = { sighting ->
            zooViewModel.deleteSighting(sighting)
        }
    )
}

/**
 * Stateless version of the ZooApp. This version is easier to preview as it doesn't
 * depend on the ViewModel directly.
 */
@Composable
fun ZooContent(
    sightings: List<Sighting>,
    isSortByName: Boolean,
    onSortChange: (Boolean) -> Unit,
    onUpdateSighting: (Sighting, Boolean) -> Unit,
    onDeleteSighting: (Sighting) -> Unit
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    var selectedSighting by remember { mutableStateOf<Sighting?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { }
    )

    LaunchedEffect(Unit) {
        val permissions = mutableListOf(Manifest.permission.CAMERA)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(permissions.toTypedArray())
    }

    val bottomItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Settings,
        BottomNavItem.About
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomItems.forEach { item ->
                    val isSelected =
                        currentDestination?.route == item.route::class.qualifiedName

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HomeDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<HomeDestination> {
                ListScreen(
                    sightings = sightings,
                    onEditClick = { animal ->
                        selectedSighting = animal
                        showDialog = true
                    },
                    onDelete = { animal ->
                        onDeleteSighting(animal)
                    }
                )
            }

            composable<SettingsDestination> {
                SettingsScreen(
                    isSortByName = isSortByName,
                    onSortChange = onSortChange
                )
            }

            composable<AboutDestination> {
                AboutScreen()
            }
        }

        if (showDialog && selectedSighting != null) {
            EditSightingDialog(
                sighting = selectedSighting!!,
                onDismiss = { showDialog = false },
                onSave = { updated ->
                    onUpdateSighting(
                        updated,
                        selectedSighting?.isFound == true
                    )
                    showDialog = false
                }
            )
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

    val imageModel = sighting.photoPath?.let { Uri.parse(it) } ?: sighting.imageUrl

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
            AsyncImage(
                model = imageModel,
                contentDescription = sighting.name,
                modifier = Modifier
                    .size(64.dp)
                    .padding(end = 8.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sighting.name,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )

                if (sighting.isFound && sighting.notes.isNotEmpty()) {
                    Text(
                        text = sighting.notes,
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
    val context = LocalContext.current
    val fileUtils = remember { FileUtils(context) }
    
    var name by remember { mutableStateOf(sighting.name) }
    var notesText by remember { mutableStateOf(sighting.notes) }
    var isFoundChecked by remember { mutableStateOf(sighting.isFound) }
    var currentPhotoPath by remember { mutableStateOf(sighting.photoPath) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri != null) {
            currentPhotoPath = tempPhotoUri.toString()
        }
    }

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
                    label = {
                        Text(stringResource(id = R.string.notes_hint))
                    }
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

                Button(
                    onClick = {
                        val file = fileUtils.createImageFile()
                        val uri = fileUtils.getUriForFile(file)
                        tempPhotoUri = uri
                        cameraLauncher.launch(uri)
                    },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(
                        if (currentPhotoPath == null) "Take Photo"
                        else "Retake Photo"
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        sighting.copy(
                            name = name,
                            isFound = isFoundChecked,
                            notes = notesText,
                            photoPath = currentPhotoPath
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
fun ZooAppPreview() {
    ZooTreasureHuntTheme {
        ZooContent(
            sightings = listOf(
                Sighting(
                    id = "1",
                    name = "Lion",
                    imageUrl = "https://example.com/lion.jpg",
                    isFound = false,
                    notes = "",
                    photoPath = null
                )
            ),
            isSortByName = true,
            onSortChange = {},
            onUpdateSighting = { _, _ -> },
            onDeleteSighting = { }
        )
    }
}
