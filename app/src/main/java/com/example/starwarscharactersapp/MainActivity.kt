package com.example.starwarscharactersapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
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
            val themeMode by viewModel.themeMode.collectAsState()

            StarWarsCharactersAppTheme(themeMode = themeMode) {
                StarWarsCharactersApp()
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun StarWarsCharactersApp() {
    var showSplash by rememberSaveable { mutableStateOf(true) }

    if (showSplash) {
        SplashScreen(
            onDataLoaded = {
                showSplash = false
            },
        )
    } else {
        NavigationRoot()
    }
}
