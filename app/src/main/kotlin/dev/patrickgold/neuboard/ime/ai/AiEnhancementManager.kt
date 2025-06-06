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
     */
    fun generateQuickReplies() {
        lastReceivedMessage?.let { receivedMessage ->
            launch {
                flogDebug(LogTopic.IME) { "Generating quick replies for: $receivedMessage" }
                val suggestions = enhancerService.generateQuickReplies(
                    receivedMessage = receivedMessage,
                    recipientType = MessageEnhancerService.RecipientType.FRIEND,
                    preferredTone = MessageEnhancerService.MessageTone.FRIENDLY
                )
                _quickReplySuggestions.value = suggestions
            }
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
