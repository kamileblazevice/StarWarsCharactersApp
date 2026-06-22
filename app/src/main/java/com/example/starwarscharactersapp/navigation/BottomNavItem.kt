package com.example.starwarscharactersapp.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.starwarscharactersapp.R

data class BottomNavItem(
    val icon: ImageVector,
    @get:StringRes val title: Int,
)

val TOP_LEVEL_DESTINATIONS = mapOf(
    Route.CharactersList to BottomNavItem(
        icon = Icons.Outlined.Contacts,
        title = R.string.bottom_nav_item_characters,
    ),
    Route.Favorites to BottomNavItem(
        icon = Icons.Outlined.StarBorder,
        title = R.string.bottom_nav_item_favorites,
    ),
    Route.Settings to BottomNavItem(
        icon = Icons.Outlined.Settings,
        title = R.string.bottom_nav_item_settings,
    ),
)
