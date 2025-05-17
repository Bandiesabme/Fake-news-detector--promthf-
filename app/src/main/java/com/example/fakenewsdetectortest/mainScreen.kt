package com.example.fakenewsdetectortest

// Removed image-related imports if no longer used elsewhere:
// import android.graphics.BitmapFactory
// import androidx.compose.foundation.Image
// import androidx.compose.foundation.border
// import androidx.compose.foundation.clickable
// import androidx.compose.foundation.layout.Row
// import androidx.compose.foundation.layout.requiredSize
// import androidx.compose.foundation.lazy.LazyRow
// import androidx.compose.foundation.lazy.itemsIndexed
// import androidx.compose.ui.res.painterResource

import androidx.compose.ui.graphics.Color // Import Color
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
// import androidx.compose.material3.TextField // Replaced by OutlinedTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
// import androidx.compose.runtime.mutableIntStateOf // No longer needed for selectedImage
import androidx.compose.runtime.mutableStateOf
// import androidx.compose.runtime.remember // Only if selectedImage was the only use
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
// import androidx.compose.ui.platform.LocalContext // Keep if used by ViewModel or other parts
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

val InputCardBackgroundColor = Color(0xFFE3F2FD) // Light Blue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BakingScreen(
    mainScreenViewModel: mainScreenViewModel = viewModel()
) {


    var newsArticleText by rememberSaveable { mutableStateOf("") }
    var analysisScore by rememberSaveable { mutableStateOf<Int?>(null) }
    val placeholderResult = stringResource(R.string.results_placeholder)
    var result by rememberSaveable { mutableStateOf(placeholderResult) }

    val uiState by mainScreenViewModel.uiState.collectAsState()


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = stringResource(R.string.title), // Assuming R.string.title is "Fake News Detector"
            style = MaterialTheme.typography.headlineMedium, // Slightly larger title
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6200EE),
            modifier = Modifier
                .padding(bottom = 24.dp, top = 52.dp)

                .align(Alignment.CenterHorizontally)
        )



        OutlinedTextField(
            value = newsArticleText,
            onValueChange = { newsArticleText = it },
            label = { stringResource(R.string.prompt_placeholder) },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp) // Give it more height for pasting text
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (newsArticleText.isNotEmpty()) {
                    mainScreenViewModel.analyzeNewsText(newsArticleText)
                } else {
                    analysisScore = null
                    result = placeholderResult // Reset result if text is empty
                }
            },
            enabled = newsArticleText.isNotEmpty(),
            modifier = Modifier.align(Alignment.CenterHorizontally)
                //.background(color = Color.Red)
            //make the color  red


        ) {
            Text(text = "Analyze")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState is UiState.Loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            var textColor = MaterialTheme.colorScheme.onSurface
            if (uiState is UiState.Error) {
                textColor = MaterialTheme.colorScheme.error
                result = (uiState as UiState.Error).errorMessage
                analysisScore = null
            } else if (uiState is UiState.Success) {
                val analysisResultText = (uiState as UiState.Success).outputText
                textColor = MaterialTheme.colorScheme.onSurface
                analysisScore = extractScoreFromResult(analysisResultText)
                result = analysisResultText
            }

            analysisScore?.let { score ->
                Text(
                    text = "Fake News Score: $score/5",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 8.dp) // Add some padding below the score
                )
            }

            val scrollState = rememberScrollState()
            Text(
                text = result,
                textAlign = TextAlign.Start,
                color = textColor,
                modifier = Modifier
                    // .align(Alignment.CenterHorizontally) // Text can be start-aligned
                    .fillMaxWidth() // Use fillMaxWidth for better text layout
                    .weight(1f) // Allow this text to take remaining space
                    .verticalScroll(scrollState)
                    .padding(top = 8.dp) // Add padding above the result text
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun BakingScreenPreview() {
    BakingScreen()
}

fun extractScoreFromResult(resultText: String): Int? {
    val regex = Regex("Score: (\\d)/5")
    val matchResult = regex.find(resultText)
    return matchResult?.groupValues?.get(1)?.toIntOrNull()
}