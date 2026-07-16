package com.example.starwarscharactersapp.ui.features.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.starwarscharactersapp.R
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Spacer
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.starwarscharactersapp.domain.model.SyncProgress
import com.example.starwarscharactersapp.ui.theme.StarWarsCharactersAppTheme
import com.example.starwarscharactersapp.ui.theme.StarWarsYellow

@Composable
fun SplashScreen(
    viewModel: SplashViewModel = hiltViewModel(),
    onDataLoaded: () -> Unit,
) {
    val isDataLoaded by viewModel.isDataLoaded.collectAsStateWithLifecycle()
    val syncProgress by viewModel.syncProgress.collectAsStateWithLifecycle()

    LaunchedEffect(isDataLoaded) {
        if (isDataLoaded) {
            onDataLoaded()
        }
    }

    SplashContent(syncProgress = syncProgress)
}

@Composable
private fun SplashContent(syncProgress: SyncProgress? = null) {
    Scaffold(
        containerColor = Color.Black,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp),
            ) {
                Image(
                    painter = painterResource(id = R.drawable.star_wars_logo),
                    contentDescription = stringResource(R.string.splash_logo_description),
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Fit,
                )
                CircularProgressIndicator(
                    color = StarWarsYellow,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (syncProgress is SyncProgress.InProgress) {
                        stringResource(
                            R.string.splash_sync_progress,
                            syncProgress.completed,
                            syncProgress.total,
                        )
                    } else {
                        ""
                    },
                    color = Color.White,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SplashScreenPreview() {
    StarWarsCharactersAppTheme {
        SplashContent()
    }
}
