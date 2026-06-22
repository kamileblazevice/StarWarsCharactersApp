package com.example.starwarscharactersapp.navigation

import androidx.navigation3.runtime.NavKey

class Navigator(val navigationState: NavigationState) {

    fun navigate(route: NavKey) {
        // when clicking on the bottom nav button while being in this bottom nav already,
        // navigate to the root
        if (navigationState.topLevelRoute == route) {
            navigationState.currentBackStack?.run {
                if (size > 1) {
                    val root = first()
                    clear()
                    add(root)
                }
            }
        } else if (route in navigationState.backStacks.keys) {
            navigationState.topLevelRoute = route
        } else {
            // when in landscape mode with two panes disallowing two screens of the same type in the same backstack to be opened at the same time
            if (navigationState.currentBackStack?.lastOrNull()
                    ?.let { it::class == route::class } == true
            ) {
                goBack()
            }
            navigationState.backStacks[navigationState.topLevelRoute]?.add(route)
        }
    }

    fun goBack() {
        val currentStack = navigationState.backStacks[navigationState.topLevelRoute]
            ?: error("Back stack for ${navigationState.topLevelRoute} doesn't exist")
        val currentRoute = currentStack.last()
        if (currentRoute == navigationState.topLevelRoute) {
            navigationState.topLevelRoute = navigationState.startRoute
        } else {
            currentStack.removeLastOrNull()
        }
    }

}