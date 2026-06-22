package com.example.starwarscharactersapp.ui.features.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FireTruck
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.starwarscharactersapp.R
import com.example.starwarscharactersapp.data.local.ThemeMode
import com.example.starwarscharactersapp.domain.model.Film
import com.example.starwarscharactersapp.domain.model.Planet
import com.example.starwarscharactersapp.domain.model.StarWarsCharacter
import com.example.starwarscharactersapp.domain.model.Starship
import com.example.starwarscharactersapp.domain.model.Vehicle
import com.example.starwarscharactersapp.ui.features.detail.model.CharacterDetailEvent
import com.example.starwarscharactersapp.ui.helper.UiState
import com.example.starwarscharactersapp.ui.shared.ErrorView
import com.example.starwarscharactersapp.ui.shared.LoadingView
import com.example.starwarscharactersapp.ui.theme.StarWarsCharactersAppTheme

@Composable
fun CharacterDetailScreen(
    viewModel: CharacterDetailViewModel,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val reloadKey by viewModel.reloadKey.collectAsState()

    CharacterDetailContent(
        state = state,
        reloadKey = reloadKey,
        onEvent = { viewModel.onEvent(it) },
    )
}

@Composable
fun CharacterDetailContent(
    state: UiState<StarWarsCharacter>,
    reloadKey: Int,
    onEvent: (CharacterDetailEvent) -> Unit
) {
    val character = (state as? UiState.Success<StarWarsCharacter>)?.data

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            androidx.compose.material.TopAppBar(
                title = {
                    Text(
                        text = character?.name ?: "",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                },
                actions = {
                    if (character != null) {
                        IconButton(onClick = { onEvent(CharacterDetailEvent.OnToggleFavorite) }) {
                            Icon(
                                imageVector = if (character.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = stringResource(R.string.favorite_icon_description),
                                tint = if (character.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    }
                },
                backgroundColor = Color.Transparent,
                elevation = 0.dp,
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding()),
        ) {
            when (state) {
                UiState.Loading -> {
                    LoadingView()
                }

                is UiState.Error -> {
                    ErrorView(
                        onRetry = { onEvent(CharacterDetailEvent.OnReloadData) },
                        modifier = Modifier.fillMaxSize(),
                        icon = Icons.Default.PersonOff,
                    )
                }

                is UiState.Success -> {
                    CharacterDetails(
                        character = state.data,
                        reloadKey = reloadKey,
                        onEvent = onEvent,
                    )
                }
            }
        }
    }
}

@Composable
fun CharacterDetails(
    character: StarWarsCharacter,
    reloadKey: Int,
    onEvent: (CharacterDetailEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(dimensionResource(R.dimen.margin_medium)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.margin_medium)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var isPlaceholder by remember { mutableStateOf(true) }

        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            shape = RoundedCornerShape(dimensionResource(R.dimen.margin_medium)),
        ) {
            key(reloadKey) {
                AsyncImage(
                    model = character.imageUrl,
                    contentDescription = character.name,
                    placeholder = rememberVectorPainter(Icons.Default.Person),
                    error = rememberVectorPainter(Icons.Default.Person),
                    onLoading = {
                        isPlaceholder = true
                    },
                    onSuccess = {
                        isPlaceholder = false
                    },
                    onError = {
                        isPlaceholder = true
                        onEvent(CharacterDetailEvent.OnImageError(error = it.result.throwable.message))
                    },
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    colorFilter = if (isPlaceholder) ColorFilter.tint(Color.Gray) else null,
                )
            }
        }


        if (character.description.isNotEmpty()) {
            InfoSection(title = stringResource(R.string.character_detail_screen_description_title)) {
                Text(
                    text = character.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                )
            }
        }

        InfoSection(title = stringResource(R.string.character_detail_screen_general_info_title)) {
            DetailItem(
                stringResource(
                    R.string.character_detail_screen_birth_year,
                    character.birthYear,
                ),
            )
            DetailItem(stringResource(R.string.character_detail_screen_gender, character.gender))
            DetailItem(stringResource(R.string.character_detail_screen_height, character.height))
            DetailItem(stringResource(R.string.character_detail_screen_mass, character.mass))
        }

        InfoSection(title = stringResource(R.string.character_detail_screen_physical_traits_title)) {
            DetailItem(
                stringResource(
                    R.string.character_detail_screen_eye_colour,
                    character.eyeColor,
                ),
            )
            DetailItem(
                stringResource(
                    R.string.character_detail_screen_hair_colour,
                    character.hairColor,
                ),
            )
            DetailItem(
                stringResource(
                    R.string.character_detail_screen_skin_colour,
                    character.skinColor,
                ),
            )
        }

        if (character.planetError != null) {
            SectionErrorView(
                title = stringResource(R.string.character_detail_screen_planet_title),
                onRetry = { onEvent(CharacterDetailEvent.OnRetryPlanet) },
                icon = Icons.Default.Place
            )
        } else if (character.planet != null) {
            PlanetSection(planet = character.planet)
        } else {
            SectionLoadingIndicator(stringResource(R.string.character_detail_screen_planet_title))
        }

        if (character.filmsError != null) {
            SectionErrorView(
                title = stringResource(R.string.character_detail_screen_films_title),
                onRetry = { onEvent(CharacterDetailEvent.OnRetryFilms) },
                icon = Icons.Default.Movie
            )
        } else if (character.films.isNotEmpty()) {
            FilmSection(films = character.films)
        } else {
            SectionLoadingIndicator(stringResource(R.string.character_detail_screen_films_title))
        }

        if (character.starshipsError != null) {
            SectionErrorView(
                title = stringResource(R.string.character_detail_screen_starships_title),
                onRetry = { onEvent(CharacterDetailEvent.OnRetryStarships) },
                icon = Icons.Default.RocketLaunch
            )
        } else if (character.starships.isNotEmpty()) {
            StarshipSection(starships = character.starships)
        } else if (character.starshipUrls.isNotEmpty()) {
            SectionLoadingIndicator(stringResource(R.string.character_detail_screen_starships_title))
        }

        if (character.vehiclesError != null) {
            SectionErrorView(
                title = stringResource(R.string.character_detail_screen_vehicles_title),
                onRetry = { onEvent(CharacterDetailEvent.OnRetryVehicles) },
                icon = Icons.Default.FireTruck
            )
        } else if (character.vehicles.isNotEmpty()) {
            VehicleSection(vehicles = character.vehicles)
        } else if (character.vehicleUrls.isNotEmpty()) {
            SectionLoadingIndicator(stringResource(R.string.character_detail_screen_vehicles_title))
        }

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.margin_medium)))
    }
}

@Composable
fun InfoSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.elevatedCardColors(
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.margin_medium)),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            content()
        }
    }
}

@Composable
fun DetailItem(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.padding(vertical = 2.dp),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
    )
}

@Composable
fun PlanetSection(planet: Planet) {
    InfoSection(title = stringResource(R.string.character_detail_screen_planet_title)) {
        DetailItem(stringResource(R.string.character_detail_screen_planet_name, planet.name))
        DetailItem(stringResource(R.string.character_detail_screen_planet_climate, planet.climate))
        DetailItem(stringResource(R.string.character_detail_screen_planet_terrain, planet.terrain))
        DetailItem(
            stringResource(
                R.string.character_detail_screen_planet_population,
                planet.population
            )
        )
        DetailItem(stringResource(R.string.character_detail_screen_planet_gravity, planet.gravity))
        DetailItem(
            stringResource(
                R.string.character_detail_screen_planet_diameter,
                planet.diameter
            )
        )
    }
}

@Composable
fun FilmSection(films: List<Film>) {
    InfoSection(title = stringResource(R.string.character_detail_screen_films_title)) {
        films.forEachIndexed { index, film ->
            DetailItem(
                stringResource(
                    R.string.character_detail_screen_film_title,
                    index + 1,
                    film.title,
                    film.releaseYear,
                    film.director,
                )
            )
        }
    }
}

@Composable
fun StarshipSection(starships: List<Starship>) {
    InfoSection(title = stringResource(R.string.character_detail_screen_starships_title)) {
        starships.forEachIndexed { index, starship ->
            DetailItem(
                stringResource(
                    R.string.character_detail_screen_starship_name,
                    index + 1,
                    starship.name
                )
            )
        }
    }
}

@Composable
fun VehicleSection(vehicles: List<Vehicle>) {
    InfoSection(title = stringResource(R.string.character_detail_screen_vehicles_title)) {
        vehicles.forEachIndexed { index, vehicle ->
            DetailItem(
                stringResource(
                    R.string.character_detail_screen_vehicle_name,
                    index + 1,
                    vehicle.name
                )
            )
        }
    }
}

@Composable
fun SectionErrorView(
    title: String,
    onRetry: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.Warning
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.elevatedCardColors(
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.margin_medium)),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(R.dimen.margin_medium)),
                contentAlignment = Alignment.Center
            ) {
                ErrorView(onRetry = onRetry, icon = icon)
            }
        }
    }
}

@Composable
fun SectionLoadingIndicator(title: String) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.elevatedCardColors(
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.margin_medium)),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(R.dimen.margin_medium))
            ) {
                LoadingView()
            }
        }
    }
}

@Preview(name = "Light Mode")
@Composable
private fun CharacterDetailLightPreview() {
    StarWarsCharactersAppTheme(themeMode = ThemeMode.LIGHT) {
        CharacterDetailPreviewContent()
    }
}

@Preview(name = "Dark Mode")
@Composable
private fun CharacterDetailDarkPreview() {
    StarWarsCharactersAppTheme(themeMode = ThemeMode.DARK) {
        CharacterDetailPreviewContent()
    }
}

@Composable
private fun CharacterDetailPreviewContent() {
    CharacterDetailContent(
        state = UiState.Success(
            StarWarsCharacter(
                id = "1",
                name = "Luke Skywalker",
                birthYear = "19BBY",
                eyeColor = "blue",
                skinColor = "fair",
                gender = "male",
                hairColor = "blond",
                height = "172",
                mass = "77",
                description = "Luke Skywalker, a Force-sensitive human male, was a legendary Jedi Master who fought in the Galactic Civil War during the reign of the Galactic Empire.",
                imageUrl = "https://starwars-visualguide.com/assets/img/characters/1.jpg",
                isFavorite = true,
                planet = Planet(
                    name = "Tatooine",
                    climate = "arid",
                    terrain = "desert",
                    population = "200000",
                    gravity = "1 standard",
                    diameter = "10465"
                ),
                films = listOf(
                    Film(title = "A New Hope", releaseYear = "1977", director = "George Lucas"),
                    Film(
                        title = "The Empire Strikes Back",
                        releaseYear = "1980",
                        director = "Irvin Kershner"
                    )
                ),
                starships = listOf(
                    Starship(name = "X-wing"),
                    Starship(name = "Imperial shuttle")
                ),
                vehicles = listOf(
                    Vehicle(name = "Snowspeeder"),
                    Vehicle(name = "Imperial Speeder Bike")
                )
            )
        ),
        reloadKey = 0,
        onEvent = {}
    )
}
