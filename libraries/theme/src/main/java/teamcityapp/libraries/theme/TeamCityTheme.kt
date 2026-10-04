package teamcityapp.libraries.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun TeamCityTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) darkColorScheme(primary = Color(0xFF90CAF9), secondary = Color(0xFFA5D6A7))
    else lightColorScheme(primary = Color(0xFF1565C0), secondary = Color(0xFF2E7D32))
    MaterialTheme(colorScheme = colors, content = content)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamCityScreen(title: String, onClose: () -> Unit, content: @Composable (Modifier) -> Unit) {
    Scaffold(topBar = {
        TopAppBar(title = { Text(title) }, navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(painterResource(R.drawable.ic_close_black_24dp), stringResource(R.string.action_close))
            }
        })
    }) { padding -> content(Modifier.fillMaxSize().padding(padding)) }
}

@Composable
fun LoadingContent(modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) { CircularProgressIndicator() }
}

@Composable
fun MessageContent(message: String, modifier: Modifier = Modifier, action: (@Composable () -> Unit)? = null) {
    Box(modifier.padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message, style = MaterialTheme.typography.bodyLarge)
            action?.invoke()
        }
    }
}

@Preview
@Composable
private fun ScreenPreview() {
    TeamCityTheme { TeamCityScreen("TeamCity", {}) { MessageContent("Content", it) } }
}

@Preview
@Composable
private fun LoadingPreview() {
    TeamCityTheme { LoadingContent(Modifier.fillMaxSize()) }
}
