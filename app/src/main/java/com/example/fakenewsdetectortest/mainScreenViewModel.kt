package com.example.fakenewsdetectortest

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class mainScreenViewModel : ViewModel() {
    private val _uiState: MutableStateFlow<UiState> =
        MutableStateFlow(UiState.Initial)
    val uiState: StateFlow<UiState> =
        _uiState.asStateFlow()

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.apiKey
    )

    fun sendPrompt(
        bitmap: Bitmap,
        prompt: String
    ) {
        _uiState.value = UiState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = generativeModel.generateContent(
                    content {
                        image(bitmap)
                        text(prompt)
                    }
                )
                response.text?.let { outputContent ->
                    _uiState.value = UiState.Success(outputContent)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.localizedMessage ?: "")
            }
        }
    }


    fun analyzeNewsText(newsText: String) {
        // Launch a coroutine for background processing
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading

                // Craft your prompt for fake news detection, asking for a score 1-5
                val prompt = """
                    You are a veteran journalist analyzing the following article.
                     Examine the language for any signs of manipulation (sensational adjectives, emotional appeals, vague claims) 
                     and consider whether it aligns with known facts or biases. Step by step, explain why the article is likely true or false.
                Analyze the following news article and determine its likelihood of being fake news. 
                Provide a score between 1 (Very Likely Real) and 5 (Very Likely Fake).
                Present the score clearly in the format: "Score: X/5".
                You can optionally provide a brief explanation below the score.

                News Article:
                $newsText
                """.trimIndent() // Use trimIndent for cleaner multi-line prompts

                // Make the Gemini API call
                val response = generativeModel.generateContent(prompt)

                // Update UI state with the result
                _uiState.value = UiState.Success(response.text ?: "Analysis failed: No text in response.")

            } catch (e: Exception) {
                _uiState.value = UiState.Error("Analysis failed: ${e.localizedMessage}")
            }
        }
    }
}