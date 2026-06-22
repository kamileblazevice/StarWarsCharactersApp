# Star Wars Characters App

An Android application that displays characters from the Star Wars universe, featuring offline-first support and detailed character information.

## Demo

![Demo](docs/demo.gif)

## Features

- **Character List**: Browse a list of Star Wars characters fetched from the SWAPI (Star Wars API).
- **Offline Support**: Characters and their details are cached locally using Room, allowing for offline viewing.
- **Detailed View**: See detailed information for each character, including their homeworld, films, starships, and vehicles.
- **Favorites**: Mark characters as favorites for quick access.
- **Data Sync**: Automatically synchronizes character data, including associated images and descriptions from the Star Wars Databank API.

## Tech Stack

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose)
- **Architecture**: MVVM (Model-View-ViewModel) with Clean Architecture principles.
- **Concurrency**: [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & [Flow](https://kotlinlang.org/docs/flow.html)
- **Dependency Injection**: [Dagger Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
- **Local Database**: [Room](https://developer.android.com/training/data-storage/room)
- **Networking**: [Retrofit](https://square.github.io/retrofit/) & [OkHttp](https://square.github.io/okhttp/)
- **Image Loading**: [Coil](https://coil-kt.github.io/coil/)
- **Testing**: [JUnit4](https://junit.org/junit4/), [MockK](https://mockk.io/), [Turbine](https://github.com/cashapp/turbine) (for Flow testing)
- **Navigation**: [Jetpack Compose Navigation 3](https://developer.android.com/guide/navigation/navigation-3)

## Project Structure

- `data/`: Contains repositories, APIs (Retrofit services), local database (Room), and data models/entities.
- `domain/`: Contains domain models and use cases (if applicable).
- `ui/`: Contains Compose screens, ViewModels, and UI-related logic, organized by feature.
  - `features/list/`: Character list feature.
  - `features/detail/`: Character detail feature.
  - `features/favorites/`: Favorite characters feature.
  - `features/splash/`: Splash screen and initial data sync.
- `navigation/`: Navigation routes and graph definition.
- `ui/theme/`: App theme, colors, and typography.

## Getting Started

1.  **Clone the repository**:
    ```bash
    git clone https://github.com/yourusername/StarWarsCharactersApp.git
    ```
2.  **Open in Android Studio**: Open the project folder in Android Studio (Jellyfish or newer recommended).
3.  **Build and Run**: Build the project and run it on an emulator or a physical device.

## APIs Used

- [SWAPI (The Star Wars API)](https://swapi.dev/): Primary source for character information.
- [Star Wars Databank API](https://starwars-databank-server.vercel.app/): Used for retrieving character images and descriptions.

## Testing

To run the unit tests:
```bash
./gradlew test
```
The tests cover ViewModels and Repositories, ensuring business logic and data handling (including offline fallback) work as expected.
