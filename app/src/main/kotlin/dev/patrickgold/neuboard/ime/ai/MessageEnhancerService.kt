/*
 * Copyright (C) 2022-2025 The NeuBoard Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.patrickgold.neuboard.ime.ai

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import dev.patrickgold.neuboard.lib.devtools.LogTopic
import dev.patrickgold.neuboard.lib.devtools.flogDebug
import dev.patrickgold.neuboard.lib.devtools.flogInfo
import dev.patrickgold.neuboard.lib.devtools.flogError
import dev.patrickgold.neuboard.lib.devtools.flogWarning

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

/**
 * Service responsible for enhancing messages using AI capabilities.
 * Provides functionality to modify messages based on recipient type, tone, and intent.
 */
class MessageEnhancerService {

    /**
     * Types of recipients for contextual message enhancement.
     */
    enum class RecipientType {
        FRIEND, LOVER, MANAGER, PROFESSOR, PARENT, STRANGER, OTHER;
        
        companion object {
            fun fromString(value: String): RecipientType {
                return try {
                    valueOf(value.uppercase())
                } catch (e: IllegalArgumentException) {
                    OTHER
                }
            }
        }
    }
    
    /**
     * Different tones that can be applied to messages.
     */
    enum class MessageTone {
        FORMAL, INFORMAL, FRIENDLY, RESPECTFUL, FLIRTY, PROFESSIONAL, 
        APOLOGETIC, ASSERTIVE, ENCOURAGING, NEUTRAL,
        CASUAL, POLITE, FUNNY, SOCIAL_POST, WITTY;
        
        companion object {
            fun fromString(value: String): MessageTone {
                return try {
                    valueOf(value.uppercase())
                } catch (e: IllegalArgumentException) {
                    NEUTRAL
                }
            }
        }
    }
    
    /**
     * Intent types for messages.
     */
    enum class MessageIntent {
        POSITIVE, NEGATIVE, NEUTRAL, CLARIFYING, REQUEST, APPRECIATION, FEEDBACK;
        
        companion object {
            fun fromString(value: String): MessageIntent {
                return try {
                    valueOf(value.uppercase())
                } catch (e: IllegalArgumentException) {
                    NEUTRAL
                }
            }
        }
    }
    
    /**
     * Parameters for enhancing a message.
     */
    @Serializable
    data class EnhancementParams(
        val recipientType: String = RecipientType.FRIEND.name,
        val tone: String = MessageTone.FRIENDLY.name,
        val intent: String = MessageIntent.NEUTRAL.name,
        val originalMessage: String = ""
    )
    
    /**
     * Represents a saved style preset.
     */
    @Serializable
    data class SavedStyle(
        val name: String,
        val params: EnhancementParams
    )
    
    private val _savedStyles: MutableState<List<SavedStyle>> = mutableStateOf(emptyList())
    val savedStyles get() = _savedStyles.value
    
    private val json = Json { prettyPrint = true }
    
    // Configure HTTP client with timeouts for improved reliability
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()
    
    // OpenRouter API configuration
    private val aiApiUrl = "https://openrouter.ai/api/v1/chat/completions"
    private val aiApiKey = "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"
    private val defaultModel = "gpt-3.5-turbo" // Can be changed based on needs
    
    /**
     * Enhances a message using AI based on the provided parameters.
     * 
     * @param params The enhancement parameters.
     * @return The enhanced message.
     */
    suspend fun enhanceMessage(params: EnhancementParams): String {
        return withContext(Dispatchers.IO) {
            flogInfo(LogTopic.IME) { "Enhancing message with params: \\${json.encodeToString(params)}" }
            val recipientType = RecipientType.fromString(params.recipientType)
            val tone = MessageTone.fromString(params.tone)
            val intent = MessageIntent.fromString(params.intent)
            enhanceMessageInternal(recipientType, tone, intent, params.originalMessage)
        }
    }

    /**
     * Internal implementation of message enhancement.
     */
    private suspend fun enhanceMessageInternal(
        recipientType: RecipientType,
        tone: MessageTone,
        intent: MessageIntent,
        originalMessage: String
    ): String {
        // Simple rule-based enhancement for demonstration purposes
        return when (recipientType) {
            RecipientType.MANAGER -> {
                when {
                    intent == MessageIntent.POSITIVE -> "I'm pleased to inform you that $originalMessage"
                    intent == MessageIntent.APPRECIATION -> "I greatly appreciate your understanding that $originalMessage"
                    intent == MessageIntent.REQUEST -> "I would like to respectfully request that $originalMessage"
                    tone == MessageTone.APOLOGETIC -> "I apologize, but $originalMessage. Thank you for your understanding."
                    else -> "Please be informed that $originalMessage"
                }
            }
            RecipientType.FRIEND -> {
                when (tone) {
                    MessageTone.INFORMAL -> "Hey! $originalMessage"
                    MessageTone.FRIENDLY -> "Just wanted to let you know, $originalMessage"
                    MessageTone.FLIRTY -> "You know what? $originalMessage 😉"
                    else -> autocorrectText(originalMessage, tone)
                }
            }
            RecipientType.LOVER -> {
                when (tone) {
                    MessageTone.FLIRTY -> "Sweetheart, $originalMessage 💕"
                    MessageTone.FRIENDLY -> "My love, $originalMessage"
                    else -> autocorrectText(originalMessage, tone)
                }
            }
            RecipientType.PROFESSOR -> {
                when (tone) {
                    MessageTone.FORMAL -> "Dear Professor, $originalMessage"
                    MessageTone.RESPECTFUL -> "I would like to inform you that $originalMessage"
                    else -> autocorrectText(originalMessage, tone)
                }
            }
            else -> {
                when (tone) {
                    MessageTone.FORMAL -> "I would like to inform you that $originalMessage"
                    MessageTone.PROFESSIONAL -> "Please note that $originalMessage"
                    else -> autocorrectText(originalMessage, tone)
                }
            }
        }
    }

    /**
     * AI-powered autocorrect and tone enhancement for text messages
     * Uses OpenRouter API to improve text quality and adjust tone
     *
     * @param text The original text to correct
     * @param tone The desired tone for the corrected text
     * @return The corrected text with appropriate tone
     */
    private suspend fun autocorrectText(text: String, tone: MessageTone = MessageTone.NEUTRAL): String {
        if (text.isBlank()) {
            flogDebug(LogTopic.IME) { "Empty text provided for autocorrection, returning as-is" }
            return text
        }
        
        return try {
            val prompt = buildString {
                append("Rewrite the following message with correct spelling, grammar, and a ")
                append(tone.name.lowercase().replace('_', ' '))
                append(" tone. Message: \"")
                append(text)
                append("\"")
            }
            
            flogDebug(LogTopic.IME) { "Sending AI autocorrect prompt: $prompt" }
            
            val jsonBody = JSONObject()
            jsonBody.put("model", defaultModel) 
            jsonBody.put("messages", listOf(mapOf("role" to "user", "content" to prompt)))
            jsonBody.put("temperature", 0.7) // Adding temperature for more natural responses
            jsonBody.put("max_tokens", 150) // Limiting response size for faster processing
            
            val requestJson = jsonBody.toString()
            flogDebug(LogTopic.IME) { "Autocorrect request payload: $requestJson" }
            flogDebug(LogTopic.IME) { "API request: $requestJson" }
            
            val body = requestJson.toRequestBody("application/json".toMediaTypeOrNull())
            val request = Request.Builder()
                .url(aiApiUrl)
                .addHeader("Authorization", "Bearer $aiApiKey")
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build()
            
            flogInfo(LogTopic.IME) { "Sending request to OpenRouter API..." }
            val response = httpClient.newCall(request).execute()
            
            if (response.isSuccessful) {
                flogInfo(LogTopic.IME) { "OpenRouter API request successful (${response.code})" }
                val responseBody = response.body?.string()
                
                if (responseBody != null) {
                    flogDebug(LogTopic.IME) { "API response: $responseBody" }
                    val json = JSONObject(responseBody)
                    val choices = json.optJSONArray("choices")
                    
                    if (choices != null && choices.length() > 0) {
                        val message = choices.getJSONObject(0).getJSONObject("message")
                        val result = message.getString("content").trim()
                        flogInfo(LogTopic.IME) { "Successfully enhanced text via AI" }
                        return result
                    } else {
                        flogError(LogTopic.IME) { "No choices in API response" }
                    }
                } else {
                    flogError(LogTopic.IME) { "Empty response body from API" }
                }
            } else {
                flogError(LogTopic.IME) { "API request failed with code: ${response.code}, message: ${response.message}" }
                val errorBody = response.body?.string()
                if (errorBody != null) {
                    flogError(LogTopic.IME) { "Error details: $errorBody" }
                }
            }
            
            // Fallback to original text if API fails
            flogInfo(LogTopic.IME) { "Falling back to original text due to API issues" }
            text
        } catch (e: Exception) {
            // Log error and fallback
            flogError(LogTopic.IME) { "AI autocorrect error: ${e.message}" }
            flogError(LogTopic.IME) { "Stack trace: ${e.stackTraceToString()}" }
            text
        }
    }
    
    /**
     * Generates quick reply suggestions based on a received message.
     * 
     * @param receivedMessage The message to generate replies for.
     * @param recipientType The type of recipient.
     * @param preferredTone The preferred tone for the replies.
     * @return A list of suggested replies.
     */
    suspend fun generateQuickReplies(
        receivedMessage: String, 
        recipientType: RecipientType = RecipientType.FRIEND,
        preferredTone: MessageTone = MessageTone.FRIENDLY
    ): List<String> {
        return withContext(Dispatchers.IO) {
            flogDebug(LogTopic.IME) { "Generating quick replies for message: $receivedMessage" }
            
            // Simple template-based quick replies for demonstration
            val lowercaseMessage = receivedMessage.lowercase()
            
            val replies = when {
                lowercaseMessage.contains("how are you") -> {
                    listOf(
                        "I'm doing well, thanks for asking!",
                        "All good here, how about you?",
                        "Pretty good, thanks!"
                    )
                }
                lowercaseMessage.contains("meeting") -> {
                    listOf(
                        "I'll be there on time.",
                        "Looking forward to the meeting.",
                        "Is there anything I should prepare for the meeting?"
                    )
                }
                lowercaseMessage.contains("hello") || lowercaseMessage.contains("hi") -> {
                    listOf(
                        "Hello! How are you doing?",
                        "Hi there! What's up?",
                        "Hey! How's your day going?"
                    )
                }
                lowercaseMessage.contains("thank") -> {
                    listOf(
                        "You're welcome!",
                        "No problem at all.",
                        "Happy to help!"
                    )
                }
                lowercaseMessage.endsWith("?") -> {
                    listOf(
                        "Let me think about that.",
                        "That's a good question.",
                        "I'll get back to you on that."
                    )
                }
                else -> {
                    listOf(
                        "Thanks for letting me know.",
                        "Got it.",
                        "I understand."
                    )
                }
            }
            
            // Adjust based on recipient and tone
            replies.map { reply ->
                when (recipientType) {
                    RecipientType.MANAGER -> {
                        when (preferredTone) {
                            MessageTone.FORMAL -> "Dear Manager, $reply"
                            MessageTone.PROFESSIONAL -> reply
                            else -> reply
                        }
                    }
                    else -> reply
                }
            }
        }
    }
    
    // First implementation removed to fix duplicate method issue
    
    /**
     * Saves a style preset.
     * 
     * @param style The style to save.
     */
    fun saveStyle(style: SavedStyle) {
        val currentStyles = _savedStyles.value.toMutableList()
        val existingIndex = currentStyles.indexOfFirst { it.name == style.name }
        
        if (existingIndex != -1) {
            currentStyles[existingIndex] = style
        } else {
            currentStyles.add(style)
        }
        
        _savedStyles.value = currentStyles
    }
    
    /**
     * Removes a saved style preset.
     * 
     * @param styleName The name of the style to remove.
     */
    fun removeStyle(styleName: String) {
        _savedStyles.value = _savedStyles.value.filterNot { it.name == styleName }
    }
    
    /**
     * Generates quick enhancement options for the current message being typed.
     * 
     * @param originalMessage The message currently being typed.
     * @param preferredTone The preferred tone for the enhancements.
     * @return A list of enhanced versions of the message.
     */
    suspend fun generateQuickEnhancements(
        originalMessage: String,
        preferredTone: MessageTone = MessageTone.FRIENDLY
    ): List<String> {
        return withContext(Dispatchers.IO) {
            flogInfo(LogTopic.IME) { "Generating quick enhancements for message: $originalMessage" }
            
            if (originalMessage.isEmpty()) {
                return@withContext emptyList()
            }
            
            // Try to use the AI API first for better quality enhancements
            val aiEnhancements = try {
                val prompt = buildString {
                    append("Generate 4 different versions of this message: \"$originalMessage\"\n\n")
                    append("1. A more formal version\n")
                    append("2. A friendlier version with appropriate emoji\n")
                    append("3. A more concise version\n")
                    append("4. A version with improved grammar and spelling\n\n")
                    append("Format your response as a clean JSON object with keys 'version1', 'version2', 'version3', and 'version4', with the corresponding message versions as values. No explanation text.")
                }
                
                flogDebug(LogTopic.IME) { "AI enhancement prompt: $prompt" }
                
                val jsonBody = JSONObject()
                jsonBody.put("model", defaultModel)
                jsonBody.put("response_format", JSONObject().put("type", "json_object"))
                jsonBody.put("messages", listOf(mapOf("role" to "user", "content" to prompt)))
                jsonBody.put("temperature", 0.7) // Control creativity level
                jsonBody.put("max_tokens", 300) // Reasonable limit for message enhancements
                
                val requestJson = jsonBody.toString()
                flogDebug(LogTopic.IME) { "Enhancement request payload: $requestJson" }
                
                val body = requestJson.toRequestBody("application/json".toMediaTypeOrNull())
                val request = Request.Builder()
                    .url(aiApiUrl)
                    .addHeader("Authorization", "Bearer $aiApiKey")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("X-Client", "NeuBoard-Keyboard")
                    .post(body)
                    .build()
                
                flogInfo(LogTopic.IME) { "Sending enhancement request to OpenRouter API..." }
                
                val startTime = System.currentTimeMillis()
                val response = httpClient.newCall(request).execute()
                val requestDuration = System.currentTimeMillis() - startTime
                
                flogInfo(LogTopic.IME) { "OpenRouter API request completed in ${requestDuration}ms with status: ${response.code}" }
                
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    
                    if (responseBody != null) {
                        flogDebug(LogTopic.IME) { "Enhancement API response: $responseBody" }
                        
                        val json = JSONObject(responseBody)
                        val choices = json.optJSONArray("choices")
                        
                        if (choices != null && choices.length() > 0) {
                            val message = choices.getJSONObject(0).getJSONObject("message")
                            val content = message.getString("content")
                            
                            try {
                                // Try to parse the JSON response
                                val contentJson = JSONObject(content)
                                val versions = mutableListOf<String>()
                                
                                // First look for version keys (preferred format)
                                for (i in 1..4) {
                                    val key = "version$i"
                                    if (contentJson.has(key)) {
                                        val version = contentJson.getString(key).trim()
                                        if (version.isNotEmpty()) {
                                            versions.add(version)
                                        }
                                    }
                                }
                                
                                // If we didn't find version keys, try numeric keys
                                if (versions.isEmpty()) {
                                    for (i in 1..4) {
                                        val key = "$i"
                                        if (contentJson.has(key)) {
                                            val version = contentJson.getString(key).trim()
                                            if (version.isNotEmpty()) {
                                                versions.add(version)
                                            }
                                        }
                                    }
                                }
                                
                                // If we found versions, return them
                                if (versions.isNotEmpty()) {
                                    flogInfo(LogTopic.IME) { "Successfully retrieved ${versions.size} AI enhancements" }
                                    return@withContext versions
                                } else {
                                    flogWarning(LogTopic.IME) { "No valid versions found in JSON response: $content" }
                                }
                            } catch (e: Exception) {
                                flogError(LogTopic.IME) { "Failed to parse AI enhancement JSON: ${e.message}" }
                                flogDebug(LogTopic.IME) { "Raw content: $content" }
                            }
                        } else {
                            flogWarning(LogTopic.IME) { "No choices found in API response" }
                        }
                    } else {
                        flogError(LogTopic.IME) { "Empty response body from API" }
                    }
                } else {
                    val errorBody = response.body?.string() ?: "No error details"
                    flogError(LogTopic.IME) { "API enhancement request failed with code: ${response.code}" }
                    flogError(LogTopic.IME) { "Error response: $errorBody" }
                }
                
                null // Return null if AI enhancement failed
            } catch (e: Exception) {
                // Enhanced error logging with detailed categorization
                when (e) {
                    is ConnectException -> flogError(LogTopic.IME) { "Connection error during enhancement: ${e.message}" }
                    is SocketTimeoutException -> flogError(LogTopic.IME) { "Enhancement request timed out: ${e.message}" }
                    else -> flogError(LogTopic.IME) { "Error during AI enhancement: ${e.message}" }
                }
                
                if (e.cause != null) {
                    flogError(LogTopic.IME) { "Cause: ${e.cause?.message}" }
                }
                
                flogDebug(LogTopic.IME) { "Stack trace: ${e.stackTraceToString()}" }
                null
            }
            
            // If AI enhancements are available, use them
            if (aiEnhancements != null) {
                return@withContext aiEnhancements
            }
            
            // Fallback to rule-based enhancements if API fails
            flogInfo(LogTopic.IME) { "Using fallback rule-based enhancements" }
            val enhancements = mutableListOf<String>()
            
            // 1. Formal version with improved grammar
            val formal = originalMessage
                .replace("hey", "Hello")
                .replace("yeah", "Yes")
                .replace("nope", "No")
                .replace("gonna", "going to")
                .replace("wanna", "want to")
                .replace("gotta", "have to")
                .replace("dunno", "don't know")
                .replace("y'all", "all of you")
                .replace("u ", "you ")
                .replace("ur ", "your ")
                .replace("r ", "are ")
                .replace("n ", "and ")
                .replace("!", ".")
                .replaceFirstChar { it.uppercase() }
            
            // Ensure the formal version ends with proper punctuation
            val formalWithPunctuation = if (!formal.endsWith(".") && !formal.endsWith("?") && !formal.endsWith("!")) {
                "$formal."
            } else {
                formal
            }
            enhancements.add(formalWithPunctuation)
                
            // 2. Friendly version with emojis based on context
            val friendlyBase = originalMessage
                .replace("Hello", "Hey")
                .replace("Greetings", "Hi there")
                .replaceFirstChar { it.lowercase() }
            
            val friendly = when {
                friendlyBase.contains("thank", ignoreCase = true) -> "$friendlyBase 🙏"
                friendlyBase.contains("happy", ignoreCase = true) || friendlyBase.contains("glad", ignoreCase = true) -> 
                    "$friendlyBase 😊"
                friendlyBase.contains("love", ignoreCase = true) || friendlyBase.contains("like", ignoreCase = true) -> 
                    "$friendlyBase ❤️"
                friendlyBase.contains("congratulation", ignoreCase = true) -> "$friendlyBase 🎉"
                friendlyBase.contains("question", ignoreCase = true) || friendlyBase.endsWith("?") -> 
                    "$friendlyBase 🤔"
                else -> "$friendlyBase 😊"
            }
            enhancements.add(friendly)
                
            // 3. Concise version - make it shorter
            val concise = originalMessage
                .replace("I would like to", "I want to")
                .replace("I am planning to", "I'll")
                .replace("Please let me know if", "Let me know if")
                .replace("I was wondering if", "Can")
                .replace("Do you think you could", "Can you")
                .replace("Is it possible for you to", "Can you")
                .replace("In my opinion", "I think")
                .replace("As far as I'm concerned", "I think")
            
            // Further shorten by removing filler words
            val fillerWords = listOf(" just ", " actually ", " basically ", " really ", " very ", " quite ", " simply ", " you know ")
            var shortenedMessage = concise
            fillerWords.forEach { word ->
                shortenedMessage = shortenedMessage.replace(word, " ", ignoreCase = true)
            }
            enhancements.add(shortenedMessage.trim())
            
            // 4. Enhanced version with emojis based on content analysis
            val withEmojis = buildString {
                append(originalMessage)
                
                // Content-specific emojis
                if (originalMessage.contains("meeting", ignoreCase = true) || 
                    originalMessage.contains("call", ignoreCase = true)) {
                    append(" 📞")
                } else if (originalMessage.contains("deadline", ignoreCase = true) || 
                    originalMessage.contains("time", ignoreCase = true)) {
                    append(" ⏰")
                } else if (originalMessage.contains("money", ignoreCase = true) || 
                    originalMessage.contains("payment", ignoreCase = true) ||
                    originalMessage.contains("cost", ignoreCase = true)) {
                    append(" 💰")
                } else if (originalMessage.contains("happy", ignoreCase = true) || 
                    originalMessage.contains("glad", ignoreCase = true) ||
                    originalMessage.contains("great", ignoreCase = true)) {
                    append(" 😄")
                } else if (originalMessage.contains("sad", ignoreCase = true) || 
                    originalMessage.contains("sorry", ignoreCase = true) ||
                    originalMessage.contains("unfortunately", ignoreCase = true)) {
                    append(" 😔")
                } else if (originalMessage.contains("agree", ignoreCase = true) || 
                    originalMessage.contains("yes", ignoreCase = true) ||
                    originalMessage.contains("good idea", ignoreCase = true)) {
                    append(" 👍")
                } else if (originalMessage.contains("food", ignoreCase = true) || 
                    originalMessage.contains("eat", ignoreCase = true) || 
                    originalMessage.contains("lunch", ignoreCase = true) || 
                    originalMessage.contains("dinner", ignoreCase = true)) {
                    append(" 🍽️")
                } else if (originalMessage.contains("trip", ignoreCase = true) || 
                    originalMessage.contains("travel", ignoreCase = true) ||
                    originalMessage.contains("vacation", ignoreCase = true)) {
                    append(" ✈️")
                } else if (originalMessage.contains("celebrate", ignoreCase = true) || 
                    originalMessage.contains("party", ignoreCase = true) ||
                    originalMessage.contains("congratulations", ignoreCase = true)) {
                    append(" 🎉")
                } else if (originalMessage.contains("document", ignoreCase = true) || 
                    originalMessage.contains("report", ignoreCase = true) ||
                    originalMessage.contains("file", ignoreCase = true)) {
                    append(" 📄")
                } else {
                    // Default emoji for general communication
                    append(" 👌")
                }
            }
            enhancements.add(withEmojis)
            
            // Return unique enhancements (avoid duplicates)
            return@withContext enhancements.distinct()
        }
    }
}
