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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
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
    private val httpClient = OkHttpClient()
    // Replace with your real AI API endpoint and key
    private val aiApiUrl = "https://openrouter.ai/api/v1/chat/completions"
    private val aiApiKey = "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"
    
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

    // Real AI-powered autocorrect and tone enhancement
    private suspend fun autocorrectText(text: String, tone: MessageTone = MessageTone.NEUTRAL): String {
        return try {
            val prompt = buildString {
                append("Rewrite the following message with correct spelling, grammar, and a ")
                append(tone.name.lowercase().replace('_', ' '))
                append(" tone. Message: \"")
                append(text)
                append("\"")
            }
            val jsonBody = JSONObject()
            jsonBody.put("model", "gpt-3.5-turbo") // Or your preferred model
            jsonBody.put("messages", listOf(mapOf("role" to "user", "content" to prompt)))
            val body = jsonBody.toString().toRequestBody("application/json".toMediaTypeOrNull())
            val request = Request.Builder()
                .url(aiApiUrl)
                .addHeader("Authorization", "Bearer $aiApiKey")
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build()
            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    val json = JSONObject(responseBody)
                    val choices = json.optJSONArray("choices")
                    if (choices != null && choices.length() > 0) {
                        val message = choices.getJSONObject(0).getJSONObject("message")
                        return message.getString("content").trim()
                    }
                }
            }
            // Fallback to original text if API fails
            text
        } catch (e: Exception) {
            // Log error and fallback
            flogDebug(LogTopic.IME) { "AI autocorrect error: \\${e.message}" }
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
}
