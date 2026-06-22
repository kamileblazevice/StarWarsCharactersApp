package com.example.starwarscharactersapp.ui.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.starwarscharactersapp.R
import com.example.starwarscharactersapp.data.local.ThemeMode
import com.example.starwarscharactersapp.ui.theme.StarWarsCharactersAppTheme

@Composable
fun ErrorView(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Warning,
) {
    Surface(
        modifier = modifier,
        color = Color.Transparent,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onTertiary,
            )
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.margin_small)))
            Text(
                text = stringResource(R.string.error_screen_text),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.margin_small)))
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                onClick = { onRetry() },
            ) {
                Text(text = stringResource(R.string.error_screen_button_text))
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0XFFFFFFFF, name = "Error - Light")
@Composable
private fun ErrorViewLightPreview() {
    StarWarsCharactersAppTheme(themeMode = ThemeMode.LIGHT) {
        ErrorView(onRetry = {}, modifier = Modifier.fillMaxSize())
    }
}

@Preview(showBackground = true, backgroundColor = 0XFF000000, name = "Error - Dark")
@Composable
private fun ErrorViewDarkPreview() {
    StarWarsCharactersAppTheme(themeMode = ThemeMode.DARK) {
        ErrorView(onRetry = {}, modifier = Modifier.fillMaxSize())
    }
}
