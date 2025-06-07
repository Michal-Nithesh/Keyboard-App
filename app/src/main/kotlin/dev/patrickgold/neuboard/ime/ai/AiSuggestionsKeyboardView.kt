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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import dev.patrickgold.neuboard.aiEnhancementManager
import dev.patrickgold.neuboard.editorInstance
import dev.patrickgold.neuboard.ime.keyboard.KeyboardMode
import dev.patrickgold.neuboard.ime.keyboard.KeyboardState
import dev.patrickgold.neuboard.ime.theme.NeuboardImeUi
import dev.patrickgold.neuboard.keyboardManager
import dev.patrickgold.neuboard.lib.devtools.LogTopic
import dev.patrickgold.neuboard.lib.devtools.flogError
import dev.patrickgold.neuboard.lib.devtools.flogInfo
import kotlinx.coroutines.launch
import org.neuboard.lib.snygg.ui.SnyggBox

/**
 * A view that replaces the keyboard keys with AI-generated suggestions.
 * This component is shown instead of the regular keyboard when AI suggestions are active.
 */
@Composable
fun AiSuggestionsKeyboardView() {
    val context = LocalContext.current
    val keyboardManager by context.keyboardManager()
    val aiEnhancementManager by context.aiEnhancementManager()
    val editorInstance by context.editorInstance()
    val coroutineScope = rememberCoroutineScope()
    
    val suggestions = aiEnhancementManager.quickReplySuggestions.collectAsState(initial = emptyList()).value
    val isLoading = remember { mutableStateOf(false) }
    val currentMessage = aiEnhancementManager.currentMessage.collectAsState(initial = "").value
      // Track the previously requested message to avoid duplicate requests
    var lastRequestedMessage by remember { mutableStateOf("") }
    // Store the debounce job to cancel previous requests
    var debounceJob by remember { mutableStateOf<Job?>(null) }
    // Currently selected message tone
    var selectedTone by remember { mutableStateOf(MessageEnhancerService.MessageTone.FRIENDLY) }
      // Reset message tracking when view is first shown
    LaunchedEffect(Unit) {
        // Reset state to ensure fresh suggestions
        flogInfo(LogTopic.IME) { "AiSuggestionsKeyboardView launched, resetting message tracking" }
        lastRequestedMessage = ""
        debounceJob?.cancel()
        debounceJob = null
        
        // Check if we have a valid current message different from the last one
        if (currentMessage.isNotEmpty() && currentMessage != lastRequestedMessage) {
            // Force refresh of suggestions for the current message
            debounceJob = coroutineScope.launch {
                delay(500) // Short delay to allow view to initialize
                isLoading.value = true
                try {
                    flogInfo(LogTopic.IME) { "Initial load of AI suggestions for: $currentMessage" }
                    val newSuggestions = aiEnhancementManager.enhancerService.generateQuickEnhancements(
                        originalMessage = currentMessage,
                        preferredTone = selectedTone
                    )
                    aiEnhancementManager.setQuickReplySuggestions(newSuggestions)
                    lastRequestedMessage = currentMessage
                } catch (e: Exception) {
                    flogError(LogTopic.IME) { "Error generating initial AI suggestions: ${e.message}" }
                } finally {
                    isLoading.value = false
                }
            }
        }
    }
      /**
     * Helper function to determine if the new message is significantly different from the last one
     * This helps avoid unnecessary API calls for minor edits
     */    fun hasSignificantChange(newText: String, previousText: String): Boolean {
        // If either string is empty, consider it a significant change
        if (newText.isEmpty() || previousText.isEmpty()) return true
        
        // If we used a suggestion, always consider it a significant change
        if (lastRequestedMessage.isEmpty()) return true
        
        // If there's a complete sentence in one but not the other
        val sentenceEndRegex = "[.!?]"
        val newHasSentenceEnd = newText.contains(Regex(sentenceEndRegex))
        val prevHasSentenceEnd = previousText.contains(Regex(sentenceEndRegex))
        if (newHasSentenceEnd != prevHasSentenceEnd) return true
        
        // Count sentences - if different, it's significant
        val newSentences = newText.split(Regex("[$sentenceEndRegex]+")).filter { it.isNotBlank() }.size
        val prevSentences = previousText.split(Regex("[$sentenceEndRegex]+")).filter { it.isNotBlank() }.size
        if (newSentences != prevSentences) return true
        
        // Count words to detect significant content changes
        val newWords = newText.split(Regex("\\s+")).filter { it.isNotEmpty() }
        val prevWords = previousText.split(Regex("\\s+")).filter { it.isNotEmpty() }
        val wordCountDiff = kotlin.math.abs(newWords.size - prevWords.size)
        
        // More sensitive word count change detection - if word count changed, it's significant
        if (wordCountDiff >= 1) return true
        
        // If length difference is over 15%, consider it significant (more sensitive than before)
        val lengthDiff = kotlin.math.abs(newText.length - previousText.length)
        val avgLength = (newText.length + previousText.length) / 2.0
        if (lengthDiff / avgLength > 0.15) return true
        
        // If the text has been completely changed
        if (!newText.startsWith(previousText) && 
            !previousText.startsWith(newText) && 
            !newText.endsWith(previousText) && 
            !previousText.endsWith(newText)) {
            
            // Check for similar word usage to determine if context changed
            val commonWords = newWords.intersect(prevWords.toSet())
            if (commonWords.size < kotlin.math.min(newWords.size, prevWords.size) / 2) {
                return true
            }
        }
        
        // Check if there are any non-alphanumeric characters added or removed
        val newSpecialChars = newText.count { !it.isLetterOrDigit() && !it.isWhitespace() }
        val prevSpecialChars = previousText.count { !it.isLetterOrDigit() && !it.isWhitespace() }
        if (newSpecialChars != prevSpecialChars) return true
        
        // More sensitive character difference detection
        val commonPrefix = newText.zip(previousText).takeWhile { it.first == it.second }.count()
        val maxLength = kotlin.math.max(newText.length, previousText.length)
        return (maxLength - commonPrefix) / maxLength.toDouble() > 0.2
    }
      // Auto-trigger enhancements when message changes (with debounce)
    LaunchedEffect(currentMessage) {
        // Don't process empty messages
        if (currentMessage.isEmpty()) {
            return@LaunchedEffect
        }
        
        // Check if the current message is significantly different to force new suggestions
        val significant = hasSignificantChange(currentMessage, lastRequestedMessage)
        
        // Only trigger if the message has actual content and either:
        // 1. Is significantly different from last request OR
        // 2. Hasn't been requested yet (empty lastRequestedMessage)
        if (significant || lastRequestedMessage.isEmpty() || currentMessage != lastRequestedMessage) {
            flogInfo(LogTopic.IME) { "Text change detected, scheduling AI suggestions after delay" }
            
            // Cancel previous debounce job if it exists
            debounceJob?.cancel()
            
            // Create new debounce job with 3-second delay
            debounceJob = launch {
                delay(3000) // 3-second delay
                
                // After delay, check if message is still the same (user might have continued typing)
                if (currentMessage != aiEnhancementManager.currentMessage.value) {
                    flogInfo(LogTopic.IME) { "Message changed during delay, cancelling suggestion generation" }
                    return@launch
                }
                
                // Only generate suggestions if we haven't already generated for this exact text
                if (currentMessage != lastRequestedMessage) {
                    isLoading.value = true
                    try {
                        flogInfo(LogTopic.IME) { "Auto-triggering AI suggestions for: $currentMessage" }
                        val newSuggestions = aiEnhancementManager.enhancerService.generateQuickEnhancements(
                            originalMessage = currentMessage,
                            preferredTone = selectedTone
                        )
                        aiEnhancementManager.setQuickReplySuggestions(newSuggestions)
                        lastRequestedMessage = currentMessage // Update last requested message
                    } catch (e: Exception) {
                        flogError(LogTopic.IME) { "Error generating AI suggestions: ${e.message}" }
                        // Reset tracking to allow retry
                        if (lastRequestedMessage == currentMessage) {
                            lastRequestedMessage = ""
                        }
                    } finally {
                        isLoading.value = false
                    }
                }
            }
        }
    }
    
    SnyggBox(
        elementName = NeuboardImeUi.Window.elementName,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            // Header with title and close button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (currentMessage.isNotEmpty()) "AI Enhancements" else "Quick Replies",
                    style = MaterialTheme.typography.titleMedium
                )
                
                if (isLoading.value) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(16.dp)
                            .padding(start = 8.dp),
                        strokeWidth = 2.dp
                    )
                }
                
                Row {                    // Refresh button
                    IconButton(
                        onClick = {
                            isLoading.value = true
                            // Always reset lastRequestedMessage to force new suggestions
                            lastRequestedMessage = ""
                            coroutineScope.launch {
                                try {
                                    if (currentMessage.isNotEmpty()) {
                                        // For current text, generate enhanced versions
                                        val newSuggestions = aiEnhancementManager.enhancerService.generateQuickEnhancements(
                                            originalMessage = currentMessage,
                                            preferredTone = selectedTone
                                        )
                                        aiEnhancementManager.setQuickReplySuggestions(newSuggestions)
                                        // Only update tracking after successful refresh
                                        lastRequestedMessage = currentMessage
                                        flogInfo(LogTopic.IME) { "Manually refreshed AI suggestions for: $currentMessage" }
                                    } else if (aiEnhancementManager.lastReceivedMessage != null) {
                                        // For received messages, generate quick replies
                                        aiEnhancementManager.generateQuickReplies()
                                        flogInfo(LogTopic.IME) { "Manually refreshed quick replies" }
                                    }
                                } catch (e: Exception) {
                                    // Log error and show fallback suggestions if needed
                                    flogError(LogTopic.IME) { "Error refreshing AI suggestions: ${e.message}" }
                                    
                                    // If we have no suggestions, add a basic one to show something
                                    if (aiEnhancementManager.quickReplySuggestions.value.isEmpty()) {
                                        val fallbackMsg = if (currentMessage.isNotEmpty()) {
                                            "Enhanced: $currentMessage"
                                        } else {
                                            "I understand."
                                        }
                                        aiEnhancementManager.setQuickReplySuggestions(listOf(fallbackMsg))
                                    }
                                } finally {
                                    isLoading.value = false
                                }
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Regenerate suggestions",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                      // Close button
                    IconButton(
                        onClick = {
                            // Cancel any pending debounce jobs
                            debounceJob?.cancel()
                            debounceJob = null
                            // Reset message tracking to ensure fresh start next time
                            lastRequestedMessage = ""
                            aiEnhancementManager.toggleQuickReplyVisibility()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close suggestions",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
              // Tone selection row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val tones = listOf(
                    MessageEnhancerService.MessageTone.FRIENDLY,
                    MessageEnhancerService.MessageTone.FORMAL,
                    MessageEnhancerService.MessageTone.PROFESSIONAL,
                    MessageEnhancerService.MessageTone.WITTY,
                    MessageEnhancerService.MessageTone.CASUAL,
                    MessageEnhancerService.MessageTone.ASSERTIVE
                )
                
                items(tones) { tone ->
                    val isSelected = selectedTone == tone
                    Surface(
                        modifier = Modifier
                            .clip(CircleShape)                            .clickable {                                if (selectedTone != tone) {
                                    selectedTone = tone
                                    
                                    // Always reset tracking when tone changes to force regeneration
                                    lastRequestedMessage = ""
                                    
                                    // Regenerate suggestions with new tone if we have current message
                                    if (currentMessage.isNotEmpty()) {
                                        isLoading.value = true
                                        coroutineScope.launch {
                                            try {
                                                val newSuggestions = aiEnhancementManager.enhancerService.generateQuickEnhancements(
                                                    originalMessage = currentMessage,
                                                    preferredTone = tone
                                                )
                                                aiEnhancementManager.setQuickReplySuggestions(newSuggestions)
                                                // Update last requested message to match the current
                                                lastRequestedMessage = currentMessage
                                                flogInfo(LogTopic.IME) { "Regenerated suggestions with tone: ${tone.name}" }
                                            } catch (e: Exception) {
                                                flogError(LogTopic.IME) { "Error regenerating suggestions: ${e.message}" }
                                                // Keep tracking reset to allow retry
                                                lastRequestedMessage = ""
                                            } finally {
                                                isLoading.value = false
                                            }
                                        }
                                    }
                                }
                            },
                        color = if (isSelected) 
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = tone.name.lowercase().replaceFirstChar { it.uppercase() },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            color = if (isSelected) 
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Loading indicator, empty state, or suggestions
            if (isLoading.value) {
                // Loading skeleton animation
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 140.dp, max = 160.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(3) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                                Color.Transparent
                                            ),
                                            startX = 0f,
                                            endX = 1000f
                                        )
                                    )
                            )
                        }
                    }
                }            } else if (suggestions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No suggestions available",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        TextButton(
                            onClick = {
                                // Reset message tracking and generate new suggestions
                                lastRequestedMessage = ""
                                if (currentMessage.isNotEmpty()) {
                                    isLoading.value = true
                                    coroutineScope.launch {
                                        try {
                                            val newSuggestions = aiEnhancementManager.enhancerService.generateQuickEnhancements(
                                                originalMessage = currentMessage,
                                                preferredTone = selectedTone
                                            )
                                            aiEnhancementManager.setQuickReplySuggestions(newSuggestions)
                                            lastRequestedMessage = currentMessage
                                        } catch (e: Exception) {
                                            flogError(LogTopic.IME) { "Error generating suggestions: ${e.message}" }
                                        } finally {
                                            isLoading.value = false
                                        }
                                    }
                                }
                            }
                        ) {
                            Text("Try Again")
                        }
                    }
                }
            } else {
                // Horizontal suggestions display
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 140.dp, max = 160.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    items(suggestions) { suggestion ->
                        Surface(
                            modifier = Modifier
                                .width(200.dp)
                                .heightIn(min = 80.dp)                                .clickable {                                    try {
                                        // Handle suggestion selection differently based on context
                                        if (currentMessage.isNotEmpty()) {
                                            // For enhancements, replace the current text
                                            editorInstance.deleteBackwards()
                                            editorInstance.commitText(suggestion)
                                            // Reset lastRequestedMessage to ensure new text will trigger suggestions
                                            lastRequestedMessage = ""
                                            flogInfo(LogTopic.IME) { "Applied AI text enhancement" }
                                        } else {
                                            // For quick replies, just insert the text
                                            editorInstance.commitText(suggestion)
                                            // Reset message tracking when applying a suggestion
                                            lastRequestedMessage = ""
                                            flogInfo(LogTopic.IME) { "Applied AI quick reply" }
                                        }
                                        // Cancel any pending debounce jobs
                                        debounceJob?.cancel()
                                        debounceJob = null
                                        // Hide suggestions view after selection
                                        aiEnhancementManager.toggleQuickReplyVisibility()
                                    } catch (e: Exception) {
                                        flogError(LogTopic.IME) { "Error applying AI suggestion: ${e.message}" }
                                    }
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = suggestion,
                                modifier = Modifier.padding(12.dp),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
