package com.example.starwarscharactersapp.data.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class DatabankCharacterDto(
    @SerializedName("_id")
    val id: String = "",
    val name: String = "",
    val image: String = "",
    val description: String = "",
)
