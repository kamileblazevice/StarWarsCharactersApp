package com.example.starwarscharactersapp.ui.features.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.starwarscharactersapp.R
import com.example.starwarscharactersapp.data.local.ThemeMode
import com.example.starwarscharactersapp.domain.model.StarWarsCharacter
import com.example.starwarscharactersapp.ui.features.list.model.CharacterListEvent
import com.example.starwarscharactersapp.ui.helper.UiState
import com.example.starwarscharactersapp.ui.shared.ErrorView
import com.example.starwarscharactersapp.ui.shared.LoadingView
import com.example.starwarscharactersapp.ui.theme.StarWarsCharactersAppTheme

@Composable
fun CharacterListScreen(
    viewModel: CharacterListViewModel = hiltViewModel(),
    onCharacterClicked: (String) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    CharacterListContent(
        state = state,
        searchQuery = searchQuery,
        onCharacterClicked = onCharacterClicked,
        onEvent = { viewModel.onEvent(it) },
    )
}

@Composable
fun CharacterListContent(
    state: UiState<List<StarWarsCharacter>>,
    searchQuery: String,
    onCharacterClicked: (String) -> Unit,
    onEvent: (CharacterListEvent) -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                if (state is UiState.Success) {
                    SearchBar(
                        query = searchQuery,
                        onQueryChange = {
                            onEvent(CharacterListEvent.OnSearchQueryChanged(it))
                        },
                        onClearQuery = { onEvent(CharacterListEvent.OnSearchQueryChanged("")) },
                    )
                }
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = padding.calculateTopPadding(),
                ),
        ) {
            when (state) {
                UiState.Loading -> {
                    LoadingView()
                }

                is UiState.Error -> {
                    ErrorView(
                        onRetry = { onEvent(CharacterListEvent.OnReloadData) },
                        modifier = Modifier.fillMaxSize(),
                        icon = Icons.Default.SignalWifiOff,
                    )
                }

                is UiState.Success<List<StarWarsCharacter>> -> {
                    CharacterList(
                        starWarsCharacters = state.data,
                        onCharacterClicked = onCharacterClicked,
                        onFavoriteToggled = { id ->
                            onEvent(CharacterListEvent.OnToggleFavorite(id))
                        },
                        onImageErrorChange = { id, error ->
                            onEvent(CharacterListEvent.OnImageErrorChange(id, error))
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(R.dimen.margin_small))
            .padding(bottom = dimensionResource(R.dimen.margin_small)),
        placeholder = { Text(text = stringResource(R.string.character_list_screen_search_placeholder)) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClearQuery) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = null,
                    )
                }
            }
        },
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
        ),
    )
}

@Composable
fun CharacterList(
    starWarsCharacters: List<StarWarsCharacter>,
    onCharacterClicked: (String) -> Unit,
    onFavoriteToggled: (String) -> Unit,
    onImageErrorChange: (String, String?) -> Unit,
) {
    if (starWarsCharacters.isEmpty()) {
        EmptySearchResult()
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(dimensionResource(R.dimen.margin_small))
        ) {
            items(starWarsCharacters) { character ->
                CharacterItem(
                    character = character,
                    onClick = { onCharacterClicked(character.id) },
                    onFavoriteClick = { onFavoriteToggled(character.id) },
                    onImageErrorChange = { error ->
                        onImageErrorChange(character.id, error)
                    }
                )
            }
        }
    }
}

@Composable
fun CharacterItem(
    character: StarWarsCharacter,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onImageErrorChange: (String?) -> Unit,
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensionResource(R.dimen.margin_small),
                vertical = 4.dp
            )
    ) {
        Row(
            modifier = Modifier
                .padding(dimensionResource(R.dimen.margin_small)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            var isPlaceholder by remember { mutableStateOf(true) }
            key(character.imageRetryKey) {
                AsyncImage(
                    model = character.imageUrl,
                    contentDescription = null,
                    placeholder = rememberVectorPainter(Icons.Default.Person),
                    error = rememberVectorPainter(Icons.Default.Person),
                    onLoading = { isPlaceholder = true },
                    onError = {
                        isPlaceholder = true
                        onImageErrorChange(it.result.throwable.message)
                    },
                    onSuccess = {
                        isPlaceholder = false
                        onImageErrorChange(null)
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    colorFilter = if (isPlaceholder) androidx.compose.ui.graphics.ColorFilter.tint(
                        Color.Gray
                    ) else null
                )
            }
            Spacer(modifier = Modifier.size(dimensionResource(R.dimen.margin_small)))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = character.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.margin_extra_small)))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${character.gender} • ${character.birthYear}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (character.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = stringResource(R.string.favorite_icon_description),
                    tint = if (character.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun EmptySearchResult() {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onTertiary
        )
        Text(text = stringResource(R.string.character_list_screen_no_characters_title))
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.margin_small)))
        Text(text = stringResource(R.string.character_list_screen_no_characters_subtitle))
    }
}

@Preview(showBackground = true, name = "Populated State -Light")
@Composable
private fun CharacterListLightPreview() {
    StarWarsCharactersAppTheme(themeMode = ThemeMode.LIGHT) {
        CharacterListPreviewContent()
    }
}

@Preview(showBackground = true, name = "Populated State - Dark")
@Composable
private fun CharacterListDarkPreview() {
    StarWarsCharactersAppTheme(themeMode = ThemeMode.DARK) {
        CharacterListPreviewContent()
    }
}

@Preview(showBackground = true, name = "Empty Search - Light")
@Composable
private fun CharacterListEmptySearchLightPreview() {
    StarWarsCharactersAppTheme(themeMode = ThemeMode.LIGHT) {
        CharacterListEmptySearchPreviewContent()
    }
}

@Preview(showBackground = true, name = "Empty Search - Dark")
@Composable
private fun CharacterListEmptySearchDarkPreview() {
    StarWarsCharactersAppTheme(themeMode = ThemeMode.DARK) {
        CharacterListEmptySearchPreviewContent()
    }
}

@Composable
private fun CharacterListEmptySearchPreviewContent() {
    CharacterListContent(
        state = UiState.Success(emptyList()),
        searchQuery = "Unknown Character",
        onCharacterClicked = {},
        onEvent = {}
    )
}

@Composable
private fun CharacterListPreviewContent() {
    CharacterListContent(
        state = UiState.Success(
            listOf(
                StarWarsCharacter(id = "1", name = "Luke Skywalker", gender = "male", birthYear = "19BBY"),
                StarWarsCharacter(id = "2", name = "C-3PO", gender = "n/a", birthYear = "112BBY"),
                StarWarsCharacter(id = "3", name = "R2-D2", gender = "n/a", birthYear = "33BBY", isFavorite = true),
                StarWarsCharacter(id = "4", name = "Darth Vader", gender = "male", birthYear = "41.9BBY"),
                StarWarsCharacter(id = "5", name = "Leia Organa", gender = "female", birthYear = "19BBY")
            )
        ),
        searchQuery = "",
        onCharacterClicked = {},
        onEvent = {}
    )
}
