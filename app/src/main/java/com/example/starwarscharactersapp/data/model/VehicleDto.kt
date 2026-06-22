package com.example.starwarscharactersapp.data.model

import com.example.starwarscharactersapp.data.local.entity.VehicleEntity
import com.example.starwarscharactersapp.domain.model.Vehicle

data class VehicleDto(
    val name: String,
    val url: String,
) {
    val id: String get() = url.filter { it.isDigit() }
}

fun VehicleDto.toVehicle(): Vehicle {
    return Vehicle(
        id = id,
        name = name,
        url = url,
    )
}

fun VehicleDto.toVehicleEntity(): VehicleEntity {
    return VehicleEntity(
        id = id,
        url = url,
        name = name,
    )
}
