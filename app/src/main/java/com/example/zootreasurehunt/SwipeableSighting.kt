package com.example.zootreasurehunt

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SwipeableSighting(
    sighting: Sighting,
    onEditClick: () -> Unit,
    onSwipe: () -> Unit
) {

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->

            if (value == SwipeToDismissBoxValue.EndToStart) {
                onSwipe()
            }

            value != SwipeToDismissBoxValue.StartToEnd
        }
    )

    SwipeToDismissBox(
        state = dismissState,

        backgroundContent = {

            if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {

                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",

                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Red)
                        .wrapContentSize(Alignment.CenterEnd)
                        .padding(16.dp),

                    tint = Color.White
                )
            }
        }
    ) {

        AnimalCard(
            sighting = sighting,
            onClick = onEditClick
        )
    }
}