package com.example.starwarscharactersapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.starwarscharactersapp.domain.model.Vehicle

@Entity(tableName = "vehicles")
data class VehicleEntity(
    @PrimaryKey val id: String,
    val url: String,
    val name: String,
)

fun VehicleEntity.toVehicle(): Vehicle {
    return Vehicle(
        id = id,
        name = name,
        url = url,
    )
}
