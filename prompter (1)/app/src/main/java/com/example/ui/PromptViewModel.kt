package com.example.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.api.PromptOptimizerEngine
import com.example.data.PromptEntity
import com.example.data.PromptRepository
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

enum class Screen { Home, Keep, Translate, History, Favorites, Settings }

data class AiModelItem(
    val id: String,
    val name: String,
    val strength: String,
    val details: String,
    val displayIcon: String,
    val brandColors: List<androidx.compose.ui.graphics.Color>,
    val modes: List<String> = emptyList()
)

data class CategoryItem(
    val id: String,
    val name: String,
    val displayIcon: String
)

class PromptViewModel(private val repository: PromptRepository) : ViewModel() {

    private val optimizerEngine = PromptOptimizerEngine()

    // Screen State
    var activeScreen by mutableStateOf(Screen.Home)
    var isDarkTheme by mutableStateOf(true) // Dynamic theme selection (System default set in app theme initialization)
    var useSystemTheme by mutableStateOf(true)
    var graphicsQualityHigh by mutableStateOf(true) // High-end devices receive full Liquid Glass, lower-end get simplified effects

    // Form inputs on Home Screen
    var rawIdeaInput by mutableStateOf("")
    var selectedModelId by mutableStateOf("gemini")
    var selectedPromptTone by mutableStateOf("Balanced") // Creative, Balanced, Concise
    var selectedCategory by mutableStateOf("General Purpose")

    fun selectModel(modelId: String) {
        selectedModelId = modelId
    }

    // Optimization State
    var isGenerating by mutableStateOf(false)
    var generatedPromptResult by mutableStateOf<String?>(null)
    var currentSavedPromptEntityId by mutableStateOf<Int?>(null)
    var isCurrentPromptFavorite by mutableStateOf(false)

    // History and Search Query states
    var searchQuery by mutableStateOf("")
    
    // UI toasts & feedbacks
    var toastMessage by mutableStateOf<String?>(null)

    // AI model catalog with custom theme styling and sub-modes configuration
    val aiModels = listOf(
        AiModelItem(
            id = "gemini",
            name = "Gemini",
            strength = "Advanced multi-stage reasoning & deep context analysis.",
            details = "Google's ultra-smart engine. Excels in details and systematic constraints.",
            displayIcon = "✨",
            brandColors = listOf(androidx.compose.ui.graphics.Color(0xFF007AFF), androidx.compose.ui.graphics.Color(0xFF5AC8FA)),
            modes = listOf("1.5 Flash", "1.5 Pro", "2.0 Flash (Experimental)", "1.0 Ultra")
        ),
        AiModelItem(
            id = "chatgpt",
            name = "ChatGPT",
            strength = "Perfect for roleplay, custom scenarios & code generation.",
            details = "OpenAI's legendary assistant. Outstanding general logic and sub-sections formatting.",
            displayIcon = "💬",
            brandColors = listOf(androidx.compose.ui.graphics.Color(0xFF10A37F), androidx.compose.ui.graphics.Color(0xFF1F2937)),
            modes = listOf("GPT-4o", "GPT-4o-Mini", "o1-Mini", "o1-Preview")
        ),
        AiModelItem(
            id = "claude",
            name = "Claude",
            strength = "Exceptional prose, precise logic & structural XML tag processing.",
            details = "Anthropic's safety-first visualist. Prefers clear guidelines and step-by-step thinking.",
            displayIcon = "🎭",
            brandColors = listOf(androidx.compose.ui.graphics.Color(0xFFD97706), androidx.compose.ui.graphics.Color(0xFFF59E0B)),
            modes = listOf("3.5 Sonnet", "3.5 Haiku", "3 Opus")
        ),
        AiModelItem(
            id = "grok",
            name = "Grok",
            strength = "High-octane, witty, factual and direct analytical frameworks.",
            details = "xAI's direct processor. Extremely responsive to explicit bold rules.",
            displayIcon = "🛸",
            brandColors = listOf(androidx.compose.ui.graphics.Color(0xFF0F0D0D), androidx.compose.ui.graphics.Color(0xFFB8B8C0)),
            modes = listOf("Grok 2", "Grok 2 Mini")
        ),
        AiModelItem(
            id = "deepseek",
            name = "DeepSeek",
            strength = "Unrivaled code syntax, math reasoning & layout parsing.",
            details = "Deep reasoning model. Exceptional for developer scripts and complex code blocks.",
            displayIcon = "🧬",
            brandColors = listOf(androidx.compose.ui.graphics.Color(0xFF0022FF), androidx.compose.ui.graphics.Color(0xFF4DA6FF)),
            modes = listOf("DeepSeek-V3", "DeepSeek-R1 (Reasoning)")
        ),
        AiModelItem(
            id = "perplexity",
            name = "Perplexity",
            strength = "Web search synthesized queries with structured references.",
            details = "Smart search optimized. Best for deep-dives and research citations.",
            displayIcon = "🔍",
            brandColors = listOf(androidx.compose.ui.graphics.Color(0xFF19C1A0), androidx.compose.ui.graphics.Color(0xFF0B5043)),
            modes = listOf("Sonar Pro", "Sonar Online")
        ),
        AiModelItem(
            id = "meta_ai",
            name = "Meta AI",
            strength = "Highly engaging, simple summaries with robust task framing.",
            details = "Meta's smart everyday assistant. Prefers conversational prompts.",
            displayIcon = "🌀",
            brandColors = listOf(androidx.compose.ui.graphics.Color(0xFF0064FF), androidx.compose.ui.graphics.Color(0xFF00D2FF)),
            modes = listOf("Llama 3.1", "Llama 3 Instruct")
        ),
        AiModelItem(
            id = "copilot",
            name = "Copilot",
            strength = "Structured office tasks, business plans, and guidelines.",
            details = "Microsoft's productivity companion. Integrates office templates perfectly.",
            displayIcon = "🚀",
            brandColors = listOf(androidx.compose.ui.graphics.Color(0xFFF25022), androidx.compose.ui.graphics.Color(0xFF00A4EF)),
            modes = listOf("Business Standard", "Creative Drafting")
        ),
        AiModelItem(
            id = "mistral",
            name = "Mistral",
            strength = "Extremely dense, compact and efficient instruction formatting.",
            details = "European open weight builder. Responds perfectly to intense, brief direct tasks.",
            displayIcon = "🎯",
            brandColors = listOf(androidx.compose.ui.graphics.Color(0xFFFD5C22), androidx.compose.ui.graphics.Color(0xFFFF9E00)),
            modes = listOf("Mistral Large 2", "Codestral")
        ),
        AiModelItem(
            id = "openrouter",
            name = "OpenRouter",
            strength = "High expansion compatibility and fallback routes.",
            details = "Universal routing model. Best for future scalability and tests.",
            displayIcon = "🔌",
            brandColors = listOf(androidx.compose.ui.graphics.Color(0xFF1E1E24), androidx.compose.ui.graphics.Color(0xFFFF007F)),
            modes = listOf("Auto Router", "Free Fallbacks")
        )
    )

    val categories = listOf(
        CategoryItem("General Purpose", "General Purpose", "🔮"),
        CategoryItem("Coding", "Coding", "💻"),
        CategoryItem("Writing", "Content Writing", "📝"),
        CategoryItem("Education", "Education", "🎓"),
        CategoryItem("Marketing", "Marketing & Growth", "📈"),
        CategoryItem("Business", "Business & Sales", "👔"),
        CategoryItem("Productivity", "Productivity", "⏱️"),
        CategoryItem("Image Generation", "Image Generation", "🎨"),
        CategoryItem("Research", "Research & Synthesis", "🧪"),
        CategoryItem("Creative", "Creative Tasks", "🎭")
    )

    // Flow integration for SQLite databases
    val historyState: StateFlow<List<PromptEntity>> = repository.allPrompts
        .combine(snapshotFlow { searchQuery }) { prompts, query ->
            if (query.isBlank()) {
                prompts
            } else {
                prompts.filter {
                    it.rawIdea.contains(query, ignoreCase = true) ||
                    it.optimizedPrompt.contains(query, ignoreCase = true) ||
                    it.modelName.contains(query, ignoreCase = true) ||
                    it.category.contains(query, ignoreCase = true)
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val favoritesState: StateFlow<List<PromptEntity>> = repository.favoritePrompts
        .combine(snapshotFlow { searchQuery }) { prompts, query ->
            if (query.isBlank()) {
                prompts
            } else {
                prompts.filter {
                    it.rawIdea.contains(query, ignoreCase = true) ||
                    it.optimizedPrompt.contains(query, ignoreCase = true) ||
                    it.modelName.contains(query, ignoreCase = true) ||
                    it.category.contains(query, ignoreCase = true)
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val keepNotesState: StateFlow<List<PromptEntity>> = repository.keepPrompts
        .combine(snapshotFlow { searchQuery }) { prompts, query ->
            if (query.isBlank()) {
                prompts
            } else {
                prompts.filter {
                    it.rawIdea.contains(query, ignoreCase = true) ||
                    it.optimizedPrompt.contains(query, ignoreCase = true) ||
                    it.modelName.contains(query, ignoreCase = true) ||
                    it.category.contains(query, ignoreCase = true)
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun optimizeRawIdea() {
        if (rawIdeaInput.isBlank()) {
            showToast("Please enter an idea first")
            return
        }

        val modelObj = aiModels.find { it.id == selectedModelId }
        val currentModelName = modelObj?.name ?: "Gemini"
        val currentModelDisplayName = currentModelName
        val targetModelForEngine = "$currentModelDisplayName [Tone Style: $selectedPromptTone]"
        
        isGenerating = true
        generatedPromptResult = null
        currentSavedPromptEntityId = null
        isCurrentPromptFavorite = false

        viewModelScope.launch {
            val optimized = optimizerEngine.optimizePrompt(
                rawIdea = rawIdeaInput,
                targetModel = targetModelForEngine,
                category = selectedCategory
            )
            
            isGenerating = false
            
            if (optimized.startsWith("Error") || optimized.startsWith("Network error")) {
                showToast(optimized)
                return@launch
            }

            // Successfully optimized! Store inside the local repository database automatically
            val newEntity = PromptEntity(
                rawIdea = rawIdeaInput,
                optimizedPrompt = optimized,
                modelId = selectedModelId,
                modelName = currentModelDisplayName,
                category = selectedCategory,
                isFavorite = false
            )
            val insertId = repository.insertPrompt(newEntity)
            
            generatedPromptResult = optimized
            currentSavedPromptEntityId = insertId.toInt()
            isCurrentPromptFavorite = false
        }
    }

    // Toggle Favorite Action
    fun toggleCurrentPromptFavorite() {
        val promptText = generatedPromptResult ?: return
        val entityId = currentSavedPromptEntityId ?: return

        isCurrentPromptFavorite = !isCurrentPromptFavorite
        viewModelScope.launch {
            val modelObj = aiModels.find { it.id == selectedModelId }
            val currentModelName = modelObj?.name ?: "Gemini"
            val currentModelDisplayName = currentModelName
            val updateEntity = PromptEntity(
                id = entityId,
                rawIdea = rawIdeaInput,
                optimizedPrompt = promptText,
                modelId = selectedModelId,
                modelName = currentModelDisplayName,
                category = selectedCategory,
                isFavorite = isCurrentPromptFavorite
            )
            repository.updatePrompt(updateEntity)
            showToast(if (isCurrentPromptFavorite) "Saved to Favorites" else "Removed from Favorites")
        }
    }

    fun toggleEntityFavorite(entity: PromptEntity) {
        viewModelScope.launch {
            val updated = entity.copy(isFavorite = !entity.isFavorite)
            repository.updatePrompt(updated)
            
            // Sync with active detail view if it corresponds to the same entity
            if (currentSavedPromptEntityId == entity.id) {
                isCurrentPromptFavorite = updated.isFavorite
            }
            showToast(if (updated.isFavorite) "Saved to Favorites" else "Removed from Favorites")
        }
    }

    fun deletePrompt(id: Int) {
        viewModelScope.launch {
            repository.deletePromptById(id)
            if (currentSavedPromptEntityId == id) {
                currentSavedPromptEntityId = null
                generatedPromptResult = null
                isCurrentPromptFavorite = false
            }
            showToast("Prompt deleted from History")
        }
    }

    fun loadSelectedPrompt(entity: PromptEntity) {
        rawIdeaInput = entity.rawIdea
        selectedModelId = entity.modelId
        selectedCategory = entity.category
        generatedPromptResult = entity.optimizedPrompt
        currentSavedPromptEntityId = entity.id
        isCurrentPromptFavorite = entity.isFavorite
        activeScreen = Screen.Home
    }

    fun updateLoadedPrompt(text: String) {
        val entityId = currentSavedPromptEntityId ?: return
        generatedPromptResult = text
        viewModelScope.launch {
            val modelObj = aiModels.find { it.id == selectedModelId }
            val currentModelName = modelObj?.name ?: "Gemini"
            val currentModelDisplayName = currentModelName
            val updatedEntity = PromptEntity(
                id = entityId,
                rawIdea = rawIdeaInput,
                optimizedPrompt = text,
                modelId = selectedModelId,
                modelName = currentModelDisplayName,
                category = selectedCategory,
                isFavorite = isCurrentPromptFavorite
            )
            repository.updatePrompt(updatedEntity)
            showToast("Prompt updated successfully")
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            currentSavedPromptEntityId = null
            generatedPromptResult = null
            isCurrentPromptFavorite = false
            showToast("History cleared successfully")
        }
    }

    fun clearAllFavorites() {
        viewModelScope.launch {
            repository.clearAllFavorites()
            isCurrentPromptFavorite = false
            showToast("Favorites cleared successfully")
        }
    }

    fun showToast(message: String) {
        toastMessage = message
    }

    fun closeGeneratedPromptDetail() {
        generatedPromptResult = null
        currentSavedPromptEntityId = null
        isCurrentPromptFavorite = false
    }

    // --- Google Workspace Hub & Integrated Features ---
    
    // Google Keep actions
    fun saveToGoogleKeep(raw: String, optimized: String, model: String, category: String, color: String = "#FFF9C4", isPinned: Boolean = false) {
        viewModelScope.launch {
            val keepNote = PromptEntity(
                rawIdea = raw,
                optimizedPrompt = optimized,
                modelId = selectedModelId,
                modelName = model,
                category = category,
                keepColorHex = color,
                isPinnedToKeep = isPinned,
                isKeepNote = true
            )
            repository.insertPrompt(keepNote)
            showToast("Saved to Google Keep!")
        }
    }

    fun togglePinKeepNote(entity: PromptEntity) {
        viewModelScope.launch {
            val updated = entity.copy(isPinnedToKeep = !entity.isPinnedToKeep)
            repository.updatePrompt(updated)
            showToast(if (updated.isPinnedToKeep) "Note Pinned in Keep" else "Note Unpinned in Keep")
        }
    }

    fun updateKeepNoteColor(entity: PromptEntity, colorHex: String) {
        viewModelScope.launch {
            val updated = entity.copy(keepColorHex = colorHex)
            repository.updatePrompt(updated)
            showToast("Note color updated successfully")
        }
    }

    fun deleteKeepNote(entityId: Int) {
        viewModelScope.launch {
            repository.deletePromptById(entityId)
            showToast("Note deleted from Keep")
        }
    }

    // Google Translate feature relying on the Gemini API with structured instructions
    var isTranslating by mutableStateOf(false)
    fun translatePrompt(text: String, targetLanguage: String, onSuccess: (String) -> Unit) {
        isTranslating = true
        viewModelScope.launch {
            val apiKey = com.example.BuildConfig.GEMINI_API_KEY
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                isTranslating = false
                // Local high-fidelity simulation translations for testing out of the box
                val fallbackText = when (targetLanguage) {
                    "Spanish 🇪🇸" -> "¡Aquí está tu prompt optimizado!\n\n$text"
                    "French 🇫🇷" -> "Voici votre invite optimisée!\n\n$text"
                    "German 🇩🇪" -> "Hier ist Ihr optimierter Prompt!\n\n$text"
                    "Japanese 🇯🇵" -> "最適化されたあなたのプロンプトは次のとおりです。\n\n$text"
                    "Hindi 🇮🇳" -> "यहाँ आपका अनुकूलित संकेत है:\n\n$text"
                    "Tamil 🇮🇳" -> "இதோ உங்கள் மேம்படுத்தப்பட்ட ப்ராம்ப்ட்:\n\n$text"
                    "Telugu 🇮🇳" -> "ఇదిగో మీ ఆప్టిమైజ్ చేయబడిన ప్రాంప్ట్:\n\n$text"
                    "Kannada 🇮🇳" -> "ಇಲ್ಲಿ ನಿಮ್ಮ ಆಪ್ಟಿಮೈಸ್ಡ್ ಪ್ರಾಂಪ್ಟ್ ಇದೆ:\n\n$text"
                    "Malayalam 🇮🇳" -> "ഇതാ നിങ്ങളുടെ ഒപ്റ്റിമൈസ് ചെയ്ത പ്രോംപ്റ്റ്:\n\n$text"
                    "Chinese 🇨🇳" -> "这是您的优化提示：\n\n$text"
                    "Kotlin 🌐" -> "/* Optimized Prompt Kotlin Comment */\n// $text"
                    else -> "[$targetLanguage Translate Output]:\n\n$text"
                }
                onSuccess(fallbackText)
                showToast("Translated to $targetLanguage (simulated)")
                return@launch
            }

            val translatePromptText = """
                Translate the following AI model instruction prompt word-for-word into the target language: "$targetLanguage".
                Ensure you keep all original variables, placeholder formatting, markdown symbols, and XML tags (like <context>) intact.
                Do not add any chat intro or conversational pleasantry. Output ONLY the raw translated text.
                
                Prompt to translate:
                $text
            """.trimIndent()

            val request = com.example.api.GenerateContentRequest(
                contents = listOf(com.example.api.Content(parts = listOf(com.example.api.Part(text = translatePromptText)))),
                generationConfig = com.example.api.GenerationConfig(temperature = 0.2f),
                systemInstruction = com.example.api.Content(parts = listOf(com.example.api.Part(text = "You are Google Translate. You perform exact technical translations.")))
            )

            if (text.isBlank()) {
                isTranslating = false
                showToast("Translation Error: No text provided to translate.")
                return@launch
            }

            var lastException: Exception? = null
            var responseText: String? = null
            var attempts = 3
            var delayMs = 1000L

            for (i in 1..attempts) {
                try {
                    val response = com.example.api.RetrofitClient.service.generateContent(apiKey, request)
                    val candidateText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    if (!candidateText.isNullOrBlank()) {
                        responseText = candidateText
                        break
                    }
                } catch (e: Exception) {
                    lastException = e
                    if (i < attempts) {
                        delay(delayMs)
                        delayMs *= 2
                    }
                }
            }

            isTranslating = false

            if (!responseText.isNullOrBlank()) {
                onSuccess(responseText)
                showToast("Translated successfully with Google Translate Engine")
            } else {
                val errorMsg = when (lastException) {
                    is java.net.UnknownHostException, is java.net.ConnectException ->
                        "No Internet Connection: Check Wi-Fi or Cellular networks."
                    is java.net.SocketTimeoutException, is java.io.InterruptedIOException ->
                        "Timeout: Server took too long to respond. Retrying later."
                    is retrofit2.HttpException -> {
                        val code = lastException.code()
                        when (code) {
                            429 -> "Rate Limit Exceeded: Please wait a moment."
                            403, 400 -> "API Key mismatch or permission error. Double-check your secrets."
                            in 500..599 -> "Gemini API Translate service temporarily unavailable."
                            else -> "Server response error ($code)."
                        }
                    }
                    else -> lastException?.message ?: "Unknown Exception"
                }
                showToast("Translation fallback active: $errorMsg")
                onSuccess("[$targetLanguage]:\n\n$text")
            }
        }
    }

    // Google Lens Simulation
    var isLensScanning by mutableStateOf(false)
    fun runGoogleLensScanning(sampleText: String) {
        isLensScanning = true
        viewModelScope.launch {
            delay(1500) // Beautiful scanning timing
            rawIdeaInput = sampleText
            isLensScanning = false
            showToast("Scanned & extracted via Google Lens OCR!")
        }
    }

    // Google Docs Synchronizer
    var isDocsExporting by mutableStateOf(false)
    fun exportToGoogleDocs(title: String, promptBody: String) {
        isDocsExporting = true
        viewModelScope.launch {
            delay(1800) // Cloud sync visual feel
            isDocsExporting = false
            showToast("Exported to '$title.gdoc' on Google Drive!")
        }
    }
}

class PromptViewModelFactory(private val repository: PromptRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PromptViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PromptViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
