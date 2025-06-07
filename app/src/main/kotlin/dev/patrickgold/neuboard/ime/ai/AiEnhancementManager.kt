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
     */
    fun updateCurrentMessage(text: String) {
        _currentMessage.value = text
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
        
        // Handle based on available context
        if (currentText.isNotEmpty()) {
            // If we have text input, show enhancement options in keyboard area
            flogInfo(LogTopic.IME) { "Showing AI enhancement suggestions for current text" }
            _showKeyboardSuggestions.value = true
            
            // Generate enhancement suggestions for the current text
            generateTextEnhancements(currentText)
        } else if (lastReceivedMessage != null) {
            // If no text input but we have a received message, show reply suggestions
            flogInfo(LogTopic.IME) { "Showing AI quick reply suggestions for received message" }
            _showKeyboardSuggestions.value = true
            generateQuickReplies()
        } else {
            // If neither, show the dialog as fallback
            flogInfo(LogTopic.IME) { "No context for AI suggestions, showing enhancement dialog instead" }
            isEnhancementDialogVisible = true
        }
    }
    
    /**
     * Generate text enhancements for the current input text.
     * This will create variations of the provided text with different tones and styles.
     * 
     * @param text The text to enhance
     */
    fun generateTextEnhancements(text: String) {
        if (text.isBlank()) {
            flogDebug(LogTopic.IME) { "Cannot generate enhancements for empty text" }
            return
        }
        
        launch {
            flogDebug(LogTopic.IME) { "Generating enhancements for text: $text" }
            
            // Show loading state (if needed)
            // We could update UI to show loading here
            
            val suggestions = enhancerService.generateQuickEnhancements(
                originalMessage = text,
                preferredTone = MessageEnhancerService.MessageTone.FRIENDLY
            )
            
            flogDebug(LogTopic.IME) { "Generated ${suggestions.size} enhancement suggestions" }
            _quickReplySuggestions.value = suggestions
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
    fun setLastReceivedMessage(message: String) {
        lastReceivedMessage = message
        generateQuickReplies()
    }
    
    /**
     * Generates quick reply suggestions based on the last received message.
     * This method generates responses that would be appropriate replies to 
     * the previously received message.
     */
    fun generateQuickReplies() {
        lastReceivedMessage?.let { receivedMessage ->
            if (receivedMessage.isBlank()) {
                flogDebug(LogTopic.IME) { "Cannot generate replies for blank message" }
                return
            }
            
            launch {
                flogInfo(LogTopic.IME) { "Generating quick replies for received message" }
                
                try {
                    val suggestions = enhancerService.generateQuickReplies(
                        receivedMessage = receivedMessage,
                        recipientType = MessageEnhancerService.RecipientType.FRIEND,
                        preferredTone = MessageEnhancerService.MessageTone.FRIENDLY
                    )
                    
                    if (suggestions.isNotEmpty()) {
                        flogInfo(LogTopic.IME) { "Generated ${suggestions.size} quick reply suggestions" }
                        _quickReplySuggestions.value = suggestions
                        
                        // Show suggestions in keyboard area if they're not already visible
                        if (!_showKeyboardSuggestions.value && _currentMessage.value.isEmpty()) {
                            _showQuickReplySuggestions.value = true
                        }
                    } else {
                        flogInfo(LogTopic.IME) { "No quick reply suggestions were generated" }
                    }
                } catch (e: Exception) {
                    flogInfo(LogTopic.IME) { "Error generating quick replies: ${e.message}" }
                }
            }
        } ?: run {
            flogDebug(LogTopic.IME) { "No received message to generate replies for" }
        }
    }
    
    /**
     * Clears any ongoing operations and releases resources.
     */
    fun destroy() {
        job.cancelChildren()
        job.cancel()
    }
}
