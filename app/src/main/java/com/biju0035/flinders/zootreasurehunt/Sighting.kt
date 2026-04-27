package com.biju0035.flinders.zootreasurehunt

import java.util.UUID
import kotlinx.serialization.Serializable

@Serializable
data class Sighting(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val isFound: Boolean = false,
    val notes: String = "",
    val imageUrl: String,
    val photoPath: String? = null
)