package com.example.starwarscharactersapp.ui.features.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.starwarscharactersapp.R
import com.example.starwarscharactersapp.data.local.ThemeMode
import com.example.starwarscharactersapp.domain.model.StarWarsCharacter
import com.example.starwarscharactersapp.ui.features.favorites.model.FavoriteListEvent
import com.example.starwarscharactersapp.ui.features.list.CharacterList
import com.example.starwarscharactersapp.ui.helper.UiState
import com.example.starwarscharactersapp.ui.shared.LoadingView
import com.example.starwarscharactersapp.ui.theme.StarWarsCharactersAppTheme

@Composable
fun FavoriteListScreen(
    viewModel: FavoriteListViewModel = hiltViewModel(),
    onCharacterClicked: (String) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val imageReloadRevision by viewModel.imageReloadRevision.collectAsStateWithLifecycle()

    FavoriteListContent(
        state = state,
        imageReloadRevision = imageReloadRevision,
        onCharacterClicked = onCharacterClicked,
        onEvent = viewModel::onEvent,
    )
}

@Composable
fun FavoriteListContent(
    state: UiState<List<StarWarsCharacter>>,
    imageReloadRevision: Int,
    onCharacterClicked: (String) -> Unit,
    onEvent: (FavoriteListEvent) -> Unit,
) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { _ ->
        Box(modifier = Modifier.fillMaxSize()) {
            when (state) {
                UiState.Loading -> LoadingView()
                is UiState.Error -> { /* Favorites are local-only; errors cannot happen here */ }
                is UiState.Success -> {
                    if (state.data.isEmpty()) {
                        EmptyFavorites()
                    } else {
                        CharacterList(
                            starWarsCharacters = state.data,
                            imageReloadRevision = imageReloadRevision,
                            onCharacterClicked = onCharacterClicked,
                            onFavoriteToggled = { id -> onEvent(FavoriteListEvent.OnToggleFavorite(id)) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyFavorites() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onTertiary,
        )
        Text(
            text = stringResource(R.string.favorite_list_screen_empty),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Preview(showBackground = true, name = "Empty State - Light")
@Composable
fun FavoriteListEmptyLightPreview() {
    StarWarsCharactersAppTheme(themeMode = ThemeMode.LIGHT) {
        FavoriteListContent(state = UiState.Success(emptyList()), imageReloadRevision = 0, onCharacterClicked = {}, onEvent = {})
    }
}

@Preview(showBackground = true, name = "Empty State - Dark")
@Composable
fun FavoriteListEmptyDarkPreview() {
    StarWarsCharactersAppTheme(themeMode = ThemeMode.DARK) {
        FavoriteListContent(state = UiState.Success(emptyList()), imageReloadRevision = 0, onCharacterClicked = {}, onEvent = {})
    }
}

@Preview(showBackground = true, name = "Populated State - Light")
@Composable
fun FavoriteListPopulatedLightPreview() {
    StarWarsCharactersAppTheme(themeMode = ThemeMode.LIGHT) { FavoriteListPopulatedPreviewContent() }
}

@Preview(showBackground = true, name = "Populated State - Dark")
@Composable
fun FavoriteListPopulatedDarkPreview() {
    StarWarsCharactersAppTheme(themeMode = ThemeMode.DARK) { FavoriteListPopulatedPreviewContent() }
}

@Composable
private fun FavoriteListPopulatedPreviewContent() {
    FavoriteListContent(
        state = UiState.Success(
            listOf(
                StarWarsCharacter(id = "1", name = "Luke Skywalker", isFavorite = true, gender = "male", birthYear = "19BBY"),
                StarWarsCharacter(id = "2", name = "C-3PO", isFavorite = true, gender = "n/a", birthYear = "112BBY"),
                StarWarsCharacter(id = "3", name = "R2-D2", isFavorite = true, gender = "n/a", birthYear = "33BBY"),
            )
        ),
        imageReloadRevision = 0,
        onCharacterClicked = {},
        onEvent = {},
    )
}
