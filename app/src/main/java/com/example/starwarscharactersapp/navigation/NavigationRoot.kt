package com.example.starwarscharactersapp.navigation

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.example.starwarscharactersapp.ui.features.detail.CharacterDetailViewModel
import com.example.starwarscharactersapp.ui.features.detail.CharacterDetailScreen as CharacterDetailComposable
import com.example.starwarscharactersapp.ui.features.favorites.FavoriteListScreen as FavoriteListComposable
import com.example.starwarscharactersapp.ui.features.list.CharacterListScreen as CharacterListComposable
import com.example.starwarscharactersapp.ui.features.settings.SettingsScreen as SettingsComposable

@Composable
fun NavigationRoot(
    modifier: Modifier = Modifier,
) {
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val navigationState = rememberNavigationState(
        startRoute = Route.CharactersList,
        topLevelRoutes = TOP_LEVEL_DESTINATIONS.keys,
    )
    val navigator = remember {
        Navigator(navigationState)
    }
    Scaffold(
        modifier = modifier,
        bottomBar = {
            CharactersNavigationBar(
                selectedKey = navigationState.topLevelRoute,
                onSelectKey = {
                    navigator.navigate(it)
                },
            )
        },
    ) { innerPadding ->
        NavDisplay(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
            onBack = navigator::goBack,
            sceneStrategies = listOf(TwoPaneSceneStrategy(adaptiveInfo.windowSizeClass)),
            entries = navigationState.toEntries(
                entryProvider {
                    entry<Route.CharactersList>(
                        metadata = TwoPaneScene.twoPane(Route.CharactersList),
                    ) {
                        CharacterListComposable(
                            onCharacterClicked = { characterId ->
                                navigator.navigate(Route.CharacterDetail(characterId = characterId))
                            },
                        )
                    }
                    entry<Route.CharacterDetail>(
                        metadata = TwoPaneScene.twoPane(Route.CharacterDetail),
                    ) {
                        CharacterDetailComposable(
                            viewModel = hiltViewModel { factory: CharacterDetailViewModel.Factory ->
                                factory.create(it.characterId)
                            },
                        )
                    }
                    entry<Route.Favorites>(
                        metadata = TwoPaneScene.twoPane(Route.Favorites),
                    ) {
                        FavoriteListComposable(
                            onCharacterClicked = { characterId ->
                                navigator.navigate(Route.CharacterDetail(characterId = characterId))
                            },
                        )
                    }
                    entry<Route.Settings> {
                        SettingsComposable()
                    }
                },
            ),
        )
    }
}
