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

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.patrickgold.neuboard.lib.devtools.LogTopic
import dev.patrickgold.neuboard.lib.devtools.flogDebug
import dev.patrickgold.neuboard.lib.devtools.flogInfo
import dev.patrickgold.neuboard.lib.devtools.flogError
import dev.patrickgold.neuboard.lib.devtools.flogWarning
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Manager class for the AI enhancement features.
 * Handles the communication between the keyboard service and the AI components.
 */
class AiEnhancementManager(
    private val context: Context
) : CoroutineScope {
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext = job + Dispatchers.Default

    // Service for enhancing messages
    val enhancerService = MessageEnhancerService()
    
    // Current message in the input field
    private val _currentMessage = MutableStateFlow("")
    val currentMessage = _currentMessage.asStateFlow()
    
    // Quick reply suggestions
    private val _quickReplySuggestions = MutableStateFlow<List<String>>(emptyList())
    val quickReplySuggestions = _quickReplySuggestions.asStateFlow()
    
    // Whether to show the AI enhancement dialog
    var isEnhancementDialogVisible by mutableStateOf(false)
        private set
        
    // Whether to show quick reply suggestions in the smartbar
    val _showQuickReplySuggestions = MutableStateFlow(false) // Made accessible for direct updates
    val showQuickReplySuggestions = _showQuickReplySuggestions.asStateFlow()
    
    // Whether to show AI suggestions in the keyboard area instead of keys
    private val _showKeyboardSuggestions = MutableStateFlow(false)
    val showKeyboardSuggestions = _showKeyboardSuggestions.asStateFlow()
    
    // The last received message (for quick replies)
    var lastReceivedMessage: String? = null
        private set
    
    // Initialize the manager
    init {
        flogInfo(LogTopic.IME) { "Initializing AiEnhancementManager" }
    }
    
    /**
     * Updates the current message.
     * 
     * @param text The current text in the input field.
     * @param regenerateSuggestions Whether to generate new suggestions immediately
     * @param isComposing Whether the text is currently being composed (e.g. partially typed)
     * @param selectionStart The current cursor position start
     * @param selectionEnd The current cursor position end
     */
    fun updateCurrentMessage(
        text: String,
        regenerateSuggestions: Boolean = false,
        isComposing: Boolean = false,
        selectionStart: Int = -1,
        selectionEnd: Int = -1
    ) {
        val previousText = _currentMessage.value
        _currentMessage.value = text
        
        // Log message updates to help with debugging
        flogDebug(LogTopic.IME) { "Updating current message from '$previousText' to '$text' (composing: $isComposing, selection: $selectionStart:$selectionEnd)" }
        
        // Track if we should update suggestions immediately
        var shouldUpdateSuggestions = regenerateSuggestions
        
        // Check if we're showing keyboard suggestions or if this is a good time to show them
        if (_showKeyboardSuggestions.value || (text.isNotEmpty() && text.length >= 3 && text != previousText)) {
            // Consider the change meaningful if any of these conditions are met:
            val meaningfulChange = 
                // Text has actually changed
                text != previousText &&
                // Either: added/removed multiple characters, or completed a word
                (text.length - previousText.length >= 2 || 
                 previousText.length - text.length >= 2 ||
                 text.contains(" ") && !previousText.contains(" ") ||
                 text.length == 0 || previousText.length == 0 || // Empty transition
                 text.endsWith(".") || text.endsWith("!") || text.endsWith("?")) // Completed sentence
            
            // If we have text and it's changed meaningfully, update suggestions
            if (text.isNotEmpty() && (meaningfulChange || !isComposing || 
                                     // Force update if longer than 5 chars and not updated recently
                                     (text.length > 5 && _quickReplySuggestions.value.isEmpty()))) {
                shouldUpdateSuggestions = true
                
                // If suggestions are already showing, update them
                if (_showKeyboardSuggestions.value) {
                    flogInfo(LogTopic.IME) { "Text updated meaningfully, regenerating suggestions. Text='$text'" }
                    _quickReplySuggestions.value = emptyList()
                    generateTextEnhancements(text)
                }
                // If suggestions view isn't showing but we have enough context, just prepare suggestions quietly
                else if (text.length >= 3 && !text.all { it.isWhitespace() }) {
                    flogInfo(LogTopic.IME) { "Preparing suggestions in background for text='$text'" }
                    generateTextEnhancements(text)
                }
            } else if (text.isEmpty()) {
                // If text is now empty but we had received messages before
                if (lastReceivedMessage != null && lastReceivedMessage!!.isNotBlank()) {
                    flogInfo(LogTopic.IME) { "Text cleared, showing quick replies for received message" }
                    if (_showKeyboardSuggestions.value) {
                        generateQuickReplies()
                    }
                } else if (_showKeyboardSuggestions.value) {
                    flogInfo(LogTopic.IME) { "Text cleared, no valid context for suggestions" }
                    // Hide suggestions since we don't have valid input or received message
                    _showKeyboardSuggestions.value = false
                }
            }
        }
    }
    
    /**
     * Shows the AI enhancement dialog.
     */
    fun showEnhancementDialog() {
        isEnhancementDialogVisible = true
    }
    
    /**
     * Hides the AI enhancement dialog.
     */
    fun hideEnhancementDialog() {
        isEnhancementDialogVisible = false
    }
    
    /**
     * Toggle visibility of AI suggestions in the keyboard area.
     * Shows message enhancements for current text or quick replies for received messages.
     */
    fun toggleQuickReplyVisibility() {
        val currentText = _currentMessage.value
        
        // If keyboard suggestions are currently shown, hide them
        if (_showKeyboardSuggestions.value) {
            flogDebug(LogTopic.IME) { "Hiding keyboard AI suggestions" }
            _showKeyboardSuggestions.value = false
            return
        }
        
        // Always force show keyboard regardless of context
        _showKeyboardSuggestions.value = true
        
        // Now handle suggestion content based on context
        if (_quickReplySuggestions.value.isNotEmpty()) {
            // We already have suggestions to show
            flogInfo(LogTopic.IME) { "Showing existing AI suggestions (${_quickReplySuggestions.value.size})" }
            return
        }
        
        // Handle based on available context - check if we have any text content
        if (currentText.isNotBlank()) {
            // We have actual text input - generate suggestions immediately
            flogInfo(LogTopic.IME) { "Generating AI enhancements for current text: '$currentText'" }
            
            // Immediately start generating enhancements to show
            try {
                // First show loading state by setting empty list
                _quickReplySuggestions.value = emptyList()
                
                // Launch enhancement generation without waiting
                generateTextEnhancements(currentText)
            } catch (e: Exception) {
                flogError(LogTopic.IME) { "Error initiating text enhancements: ${e.message}" }
                // Provide simple fallbacks in case of error
                _quickReplySuggestions.value = listOf(
                    currentText.replaceFirstChar { it.uppercase() },
                    "$currentText!",
                    "I meant: $currentText"
                )
            }
        } else if (lastReceivedMessage != null && lastReceivedMessage!!.isNotBlank()) {
            // No current text but we have a received message - show reply suggestions
            flogInfo(LogTopic.IME) { "Generating quick replies for message: '$lastReceivedMessage'" }
            
            // Generate reply suggestions
            generateQuickReplies()
        } else {
            // No valid context - show helpful starter suggestions
            flogInfo(LogTopic.IME) { "No input text detected, providing starter suggestions" }
            
            // Provide starter messages that are helpful in most contexts
            _quickReplySuggestions.value = listOf(
                "Hello! How are you?",
                "I wanted to check in with you",
                "Thanks for your message",
                "Can we talk about this later?",
                "I appreciate your help"
            )
        }
    }
    
    /**
     * Generate text enhancements for the current input text.
     * This will create variations of the provided text with different tones and styles.
     * 
     * @param text The text to enhance
     * @param preferredTone The preferred tone for enhancements (defaults to FRIENDLY)
     * @param priority Whether to prioritize this request (for immediate user actions)
     */
    fun generateTextEnhancements(
        text: String,
        preferredTone: MessageEnhancerService.MessageTone = MessageEnhancerService.MessageTone.FRIENDLY,
        priority: Boolean = false
    ) {
        // Check if there's any text to enhance
        if (text.isBlank()) {
            flogDebug(LogTopic.IME) { "Cannot generate enhancements for empty text" }
            // Immediately provide fallbacks for blank text if suggestions are being shown
            if (_showKeyboardSuggestions.value) {
                _quickReplySuggestions.value = listOf(
                    "Hello!",
                    "How are you?",
                    "Good to hear from you."
                )
            }
            return
        }
        
        // Check if text is too short for meaningful suggestions
        if (text.length < 2 && !priority) {
            flogDebug(LogTopic.IME) { "Text too short for meaningful suggestions: '$text'" }
            if (_showKeyboardSuggestions.value) {
                // For extremely short text, just provide simple completions
                _quickReplySuggestions.value = listOf(
                    "${text}s",
                    "${text}ed",
                    "${text}ing"
                )
            }
            return
        }
        
        // Sanitize the input text - remove any concatenated messages or malformed input
        val sanitizedText = sanitizeMessageText(text)
        if (sanitizedText != text) {
            flogInfo(LogTopic.IME) { "Sanitized input text from '$text' to '$sanitizedText'" }
        }
        
        launch {
            flogInfo(LogTopic.IME) { "Generating enhancements for: '$sanitizedText' with tone: ${preferredTone.name}" }
            
            try {
                // Track if this is a user-visible request that needs immediate feedback
                if (_showKeyboardSuggestions.value && _quickReplySuggestions.value.isEmpty()) {
                    // If we're showing keyboard suggestions but have no content, provide simple temporary suggestions
                    _quickReplySuggestions.value = listOf(
                        "Enhanced: $sanitizedText",
                        sanitizedText.replaceFirstChar { it.uppercase() }
                    )
                }
                
                val suggestions = enhancerService.generateQuickEnhancements(
                    originalMessage = sanitizedText,
                    preferredTone = preferredTone
                )
                
                // Handle API response
                if (suggestions.isEmpty()) {
                    flogWarning(LogTopic.IME) { "No enhancement suggestions were generated, using fallbacks" }
                    // Provide some basic fallback suggestions if API fails
                    val fallbackSuggestions = listOf(
                        "Enhanced: $sanitizedText",
                        "Improved: $sanitizedText",
                        sanitizedText.replaceFirstChar { it.uppercase() },
                        "$sanitizedText!"  // Add emphasis
                    )
                    _quickReplySuggestions.value = fallbackSuggestions
                } else {
                    // Only update if we had success and have better suggestions
                    flogInfo(LogTopic.IME) { "Generated ${suggestions.size} enhancement suggestions" }
                    _quickReplySuggestions.value = suggestions
                }
            } catch (e: Exception) {
                flogError(LogTopic.IME) { "Error generating enhancements: ${e.message}" }
                // Provide more varied fallbacks in case of error
                _quickReplySuggestions.value = listOf(
                    "Enhanced: $sanitizedText",
                    sanitizedText.replaceFirstChar { it.uppercase() },
                    "$sanitizedText!"
                )
            }
        }
    }
    
    /**
     * Sets the quick reply suggestions.
     */
    fun setQuickReplySuggestions(suggestions: List<String>) {
        _quickReplySuggestions.value = suggestions
    }
    
    /**
     * Sets the last received message and generates quick replies.
     * 
     * @param message The received message.
     */
    fun setLastReceivedMessage(message: String?) {
        // Only update if message is not null and not blank
        if (message != null && message.isNotBlank()) {
            lastReceivedMessage = message
            flogInfo(LogTopic.IME) { "Set last received message and generating quick replies" }
            generateQuickReplies()
        } else {
            flogDebug(LogTopic.IME) { "Ignoring attempt to set blank received message" }
        }
    }
    
    /**
     * Generates quick reply suggestions based on the last received message.
     * This method provides generic responses as the quick replies feature has been removed.
     */
    fun generateQuickReplies() {
        // Quick replies feature has been removed - always provide generic responses
        flogInfo(LogTopic.IME) { "Quick replies feature has been removed, providing generic responses" }
        
        try {
            // Provide context-aware generic responses if we have a received message
            if (lastReceivedMessage != null && lastReceivedMessage!!.isNotBlank()) {
                val receivedMessage = lastReceivedMessage!!
                
                // Analyze received message content to provide more relevant generic responses
                val containsQuestion = receivedMessage.contains("?")
                val isGreeting = receivedMessage.lowercase().contains("hello") || 
                                  receivedMessage.lowercase().contains("hi ") ||
                                  receivedMessage.lowercase().contains("hey")
                
                // Select appropriate fallback responses based on message content
                val fallbackReplies = when {
                    containsQuestion -> listOf(
                        "I'll check and get back to you.",
                        "Let me think about that.",
                        "That's a good question!"
                    )
                    isGreeting -> listOf(
                        "Hello! How are you?",
                        "Hi there!",
                        "Hey, good to hear from you!"
                    )
                    else -> listOf(
                        "I understand.",
                        "Thanks for letting me know.",
                        "Got it."
                    )
                }
                
                // Set the fallback replies
                _quickReplySuggestions.value = fallbackReplies
                
                // Show suggestions if they're not already visible and input field is empty
                if (!_showKeyboardSuggestions.value && _currentMessage.value.isEmpty()) {
                    _showQuickReplySuggestions.value = true
                }
            } else {
                // No received message - provide simple generic responses
                _quickReplySuggestions.value = listOf(
                    "Hello!",
                    "Good to hear from you.",
                    "I hope you're doing well."
                )
            }
        } catch (e: Exception) {
            flogError(LogTopic.IME) { "Error generating quick replies: ${e.message}" }
            // Provide fallbacks on error
            val errorFallbackReplies = listOf(
                "I understand.",
                "Thanks for letting me know.",
                "Got it."
            )
            _quickReplySuggestions.value = errorFallbackReplies
        }
    }
    
    /**
     * Clears any ongoing operations and releases resources.
     */
    fun destroy() {
        job.cancelChildren()
        job.cancel()
    }
    
    /**
     * Sanitize message text to fix potential issues with concatenated messages
     * This addresses problems like "find my device and get it doneNice to hear from you."
     * where suggestions may have been accidentally appended to the input text
     *
     * @param text The input text to sanitize
     * @return The sanitized text with issues fixed
     */
    private fun sanitizeMessageText(text: String): String {
        if (text.isBlank()) {
            return text
        }
        
        // Check for patterns that indicate text concatenation issues
        val commonSuggestions = listOf(
            "Nice to hear from you", 
            "Thanks for letting me know", 
            "I understand",
            "Got it",
            "Hello",
            "Good to hear",
            "How are you",
            "Let me check",
            "I'll check that out",
            "Enhanced:",
            "Improved:"
        )
        
        var cleanedText = text.trim()
        
        // First check for badly formed composing text with partial words
        val composingRegex = "(\\w{2,})(\\w{1})$".toRegex()
        if (composingRegex.containsMatchIn(cleanedText)) {
            // This might be composing text - leave it as is
            return cleanedText
        }
        
        // Look for common phrases that might have been concatenated
        for (phrase in commonSuggestions) {
            // Check for mixed case boundaries that indicate concatenation without spaces
            val firstChar = phrase.firstOrNull()?.uppercase() ?: continue
            val regex = "([a-z])([${firstChar}]${phrase.substring(1)})".toRegex(RegexOption.IGNORE_CASE)
            
            if (cleanedText.contains(regex)) {
                cleanedText = regex.replace(cleanedText, "$1 $2")
                flogDebug(LogTopic.IME) { "Fixed concatenation with regex: '$text' → '$cleanedText'" }
            }
            
            // Check for direct concatenation of suggestion phrases
            val index = cleanedText.indexOf(phrase, ignoreCase = true)
            if (index > 0 && index < cleanedText.length && !cleanedText[index-1].isWhitespace()) {
                // Only cut the text if we're confident this is a concatenation issue
                // Look for case change or punctuation
                val beforeChar = cleanedText[index-1]
                val phraseFirstChar = phrase.first()
                
                if (beforeChar.isLowerCase() && phraseFirstChar.isUpperCase()) {
                    cleanedText = cleanedText.substring(0, index).trim()
                    flogInfo(LogTopic.IME) { "Fixed concatenated message text with case change: '$text' → '$cleanedText'" }
                    break
                } else if (beforeChar.isLetterOrDigit() && !phraseFirstChar.isLetterOrDigit()) {
                    cleanedText = cleanedText.substring(0, index).trim()
                    flogInfo(LogTopic.IME) { "Fixed concatenated message text with symbol boundary: '$text' → '$cleanedText'" }
                    break
                }
            }
        }
        
        // Check for sentences that have been run together (missing space after punctuation)
        val punctuationRegex = "([.!?])([A-Z])".toRegex()
        cleanedText = punctuationRegex.replace(cleanedText) { matchResult ->
            "${matchResult.groupValues[1]} ${matchResult.groupValues[2]}"
        }
        
        // Avoid empty results due to overzealous cleaning
        if (cleanedText.isBlank() && text.isNotBlank()) {
            return text
        }
        
        return cleanedText
    }
}
