package com.example.starwarscharactersapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.starwarscharactersapp.data.local.ThemeMode
import com.example.starwarscharactersapp.navigation.NavigationRoot
import com.example.starwarscharactersapp.ui.features.splash.SplashScreen
import com.example.starwarscharactersapp.ui.theme.StarWarsCharactersAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = hiltViewModel()
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            var isSplashDataLoaded by rememberSaveable { mutableStateOf(false) }

            // Wait for the persisted theme preference to load too, otherwise the status
            // bar appearance briefly falls back to the SYSTEM default on a cold start,
            // which can mismatch the user's saved Light/Dark choice.
            val showSplash = !isSplashDataLoaded || themeMode == null

            StarWarsCharactersAppTheme(
                themeMode = themeMode ?: ThemeMode.SYSTEM,
                isSplashVisible = showSplash,
            ) {
                StarWarsCharactersApp(
                    showSplash = showSplash,
                    onSplashDataLoaded = { isSplashDataLoaded = true },
                )
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun StarWarsCharactersApp(
    showSplash: Boolean = true,
    onSplashDataLoaded: () -> Unit = {},
) {
    if (showSplash) {
        SplashScreen(onDataLoaded = onSplashDataLoaded)
    } else {
        NavigationRoot()
    }
}
