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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
fun AiSuggestionsKeyboardView() {    val context = LocalContext.current
    val keyboardManager by context.keyboardManager()
    val aiEnhancementManager by context.aiEnhancementManager()
    val editorInstance by context.editorInstance()
    val coroutineScope = rememberCoroutineScope()
    
    val suggestions = aiEnhancementManager.quickReplySuggestions.collectAsState(initial = emptyList()).value
    val isLoading = remember { mutableStateOf(false) }
    val currentMessage = aiEnhancementManager.currentMessage.collectAsState(initial = "").value
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
                
                Row {
                    // Refresh button
                    IconButton(
                        onClick = {
                            isLoading.value = true
                            coroutineScope.launch {
                                try {
                                    if (currentMessage.isNotEmpty()) {
                                        // For current text, generate enhanced versions
                                        val newSuggestions = aiEnhancementManager.enhancerService.generateQuickEnhancements(
                                            originalMessage = currentMessage
                                        )
                                        aiEnhancementManager.setQuickReplySuggestions(newSuggestions)
                                    } else if (aiEnhancementManager.lastReceivedMessage != null) {
                                        // For received messages, generate quick replies
                                        aiEnhancementManager.generateQuickReplies()
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
            
            // Loading indicator or suggestions
            if (isLoading.value) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (suggestions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No suggestions available",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 180.dp, max = 220.dp)
                ) {
                    items(suggestions) { suggestion ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    try {
                                        // Handle suggestion selection differently based on context
                                        if (currentMessage.isNotEmpty()) {
                                            // For enhancements, replace the current text
                                            editorInstance.deleteBackwards()
                                            editorInstance.commitText(suggestion)
                                            flogInfo(LogTopic.IME) { "Applied AI text enhancement" }
                                        } else {
                                            // For quick replies, just insert the text
                                            editorInstance.commitText(suggestion)
                                            flogInfo(LogTopic.IME) { "Applied AI quick reply" }
                                        }
                                        // Hide suggestions view after selection
                                        aiEnhancementManager.toggleQuickReplyVisibility()
                                    } catch (e: Exception) {
                                        flogError(LogTopic.IME) { "Error applying AI suggestion: ${e.message}" }
                                    }
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = suggestion,
                                modifier = Modifier.padding(12.dp),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}
