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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.patrickgold.neuboard.aiEnhancementManager
import dev.patrickgold.neuboard.ime.keyboard.KeyboardState
import dev.patrickgold.neuboard.lib.compose.NeuboardDropdownMenu
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Button to trigger the AI enhancement panel.
 */
@Composable
fun EnhanceButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val aiEnhancementManager by context.aiEnhancementManager()
    // Using the proper collectAsState syntax with an initial value
    val showSmartbarSuggestions = aiEnhancementManager.showQuickReplySuggestions.collectAsState(initial = false).value
    val showKeyboardSuggestions = aiEnhancementManager.showKeyboardSuggestions.collectAsState(initial = false).value
    
    val isActive = showSmartbarSuggestions || showKeyboardSuggestions
    
    IconButton(
        onClick = onClick,
        modifier = modifier
            .padding(4.dp)
            .clip(CircleShape)
            .size(40.dp)
            .background(
                if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) 
                else Color.Transparent
            )
    ) {
        Icon(
            imageVector = Icons.Default.AutoFixHigh,
            contentDescription = "Enhance Message",
            tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * Quick reply suggestion bar that displays AI-generated quick replies.
 */
@Composable
fun QuickReplySuggestionBar(
    suggestions: List<String>,
    onSuggestionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        items(suggestions) { suggestion ->
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onSuggestionSelected(suggestion) },
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 2.dp
            ) {
                Text(
                    text = suggestion,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * In-IME Compose overlay that allows users to enhance their messages with AI.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageEnhancementOverlay(
    originalMessage: String,
    onDismiss: () -> Unit,
    onMessageEnhanced: (String) -> Unit,
    enhancerService: MessageEnhancerService,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    
    // State for the selected parameters
    var recipientType by remember { mutableStateOf(MessageEnhancerService.RecipientType.FRIEND.name) }
    var tone by remember { mutableStateOf(MessageEnhancerService.MessageTone.FRIENDLY.name) }
    var intent by remember { mutableStateOf(MessageEnhancerService.MessageIntent.NEUTRAL.name) }
    
    // Enhanced message state
    var enhancedMessage by remember { mutableStateOf("") }
    var isEnhancing by remember { mutableStateOf(false) }
    var showEnhancedMessage by remember { mutableStateOf(false) }
    
    // State for the saved style dialog
    var showSaveStyleDialog by remember { mutableStateOf(false) }
    var styleName by remember { mutableStateOf("") }
    
    // Drop-down expanded states
    var recipientDropdownExpanded by remember { mutableStateOf(false) }
    var toneDropdownExpanded by remember { mutableStateOf(false) }
    var intentDropdownExpanded by remember { mutableStateOf(false) }
    
    // Use a Surface component instead of AlertDialog to avoid window token issues
    Surface(
        modifier = modifier
            .fillMaxWidth(0.95f)
            .padding(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Enhance Your Message",
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
            
            // Original message display
            Text(
                text = "Original Message:",
                style = MaterialTheme.typography.labelLarge
            )
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = originalMessage,
                    modifier = Modifier.padding(12.dp)
                )
            }
            
            // Enhancement parameters
            Text(
                text = "Customize Enhancement:",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )

            // Recipient type dropdown (replaces ExposedDropdownMenuBox)
            NeuboardDropdownMenu(
                items = MessageEnhancerService.RecipientType.values().toList(),
                expanded = recipientDropdownExpanded,
                selectedIndex = MessageEnhancerService.RecipientType.values().indexOfFirst { it.name == recipientType },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                labelProvider = { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } },
                onExpandRequest = { recipientDropdownExpanded = true },
                onDismissRequest = { recipientDropdownExpanded = false },
                onSelectItem = {
                    recipientType = MessageEnhancerService.RecipientType.values()[it].name
                    recipientDropdownExpanded = false
                }
            )

            // Tone dropdown (replaces ExposedDropdownMenuBox)
            NeuboardDropdownMenu(
                items = MessageEnhancerService.MessageTone.values().toList(),
                expanded = toneDropdownExpanded,
                selectedIndex = MessageEnhancerService.MessageTone.values().indexOfFirst { it.name == tone },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                labelProvider = { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } },
                onExpandRequest = { toneDropdownExpanded = true },
                onDismissRequest = { toneDropdownExpanded = false },
                onSelectItem = {
                    tone = MessageEnhancerService.MessageTone.values()[it].name
                    toneDropdownExpanded = false
                }
            )

            // Intent dropdown (replaces ExposedDropdownMenuBox)
            NeuboardDropdownMenu(
                items = MessageEnhancerService.MessageIntent.values().toList(),
                expanded = intentDropdownExpanded,
                selectedIndex = MessageEnhancerService.MessageIntent.values().indexOfFirst { it.name == intent },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                labelProvider = { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } },
                onExpandRequest = { intentDropdownExpanded = true },
                onDismissRequest = { intentDropdownExpanded = false },
                onSelectItem = {
                    intent = MessageEnhancerService.MessageIntent.values()[it].name
                    intentDropdownExpanded = false
                }
            )
            
            // Enhance button
            Button(
                onClick = {
                    coroutineScope.launch {
                        isEnhancing = true
                        val params = MessageEnhancerService.EnhancementParams(
                            recipientType = recipientType,
                            tone = tone,
                            intent = intent,
                            originalMessage = originalMessage
                        )
                        enhancedMessage = enhancerService.enhanceMessage(params)
                        isEnhancing = false
                        showEnhancedMessage = true
                    }
                },
                enabled = !isEnhancing,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                if (isEnhancing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Enhance with AI")
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { showSaveStyleDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Save Style",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save Style")
                }
            }
            
            // Enhanced message display
            AnimatedVisibility(
                visible = showEnhancedMessage,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Divider(modifier = Modifier.padding(vertical = 16.dp))
                    
                    Text(
                        text = "Enhanced Message:",
                        style = MaterialTheme.typography.labelLarge
                    )
                    
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = enhancedMessage,
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = {
                                onMessageEnhanced(enhancedMessage)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Use Enhanced Message",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Use This")
                        }
                        
                        Row {
                            OutlinedButton(
                                onClick = {
                                    coroutineScope.launch {
                                        isEnhancing = true
                                        val params = MessageEnhancerService.EnhancementParams(
                                            recipientType = recipientType,
                                            tone = tone,
                                            intent = intent,
                                            originalMessage = originalMessage
                                        )
                                        enhancedMessage = enhancerService.enhanceMessage(params)
                                        isEnhancing = false
                                    }
                                },
                                enabled = !isEnhancing
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Regenerate",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Regenerate")
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            OutlinedButton(onClick = onDismiss) {
                                Text("Cancel")
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Save style dialog - use Surface instead of AlertDialog to avoid window token issues
    if (showSaveStyleDialog) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = { showSaveStyleDialog = false })
                .background(Color.Black.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .padding(16.dp)
                    // This prevents the outer Box click from dismissing the dialog
                    .clickable(onClick = {}), 
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Title
                    Text(
                        text = "Save Style",
                        style = MaterialTheme.typography.titleLarge
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Content
                    Text("Save current enhancement settings as a style preset")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = styleName,
                        onValueChange = { styleName = it },
                        label = { Text("Style Name") },
                        placeholder = { Text("e.g., 'Professional Email to Boss'") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showSaveStyleDialog = false }) {
                            Text("Cancel")
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Button(
                            onClick = {
                                if (styleName.isNotBlank()) {
                                    val params = MessageEnhancerService.EnhancementParams(
                                        recipientType = recipientType,
                                        tone = tone,
                                        intent = intent
                                    )
                                    val style = MessageEnhancerService.SavedStyle(
                                        name = styleName,
                                        params = params
                                    )
                                    enhancerService.saveStyle(style)
                                    showSaveStyleDialog = false
                                    styleName = ""
                                }
                            },
                            enabled = styleName.isNotBlank()
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Component to display saved style presets.
 */
@Composable
fun SavedStylesBar(
    styles: List<MessageEnhancerService.SavedStyle>,
    onStyleSelected: (MessageEnhancerService.SavedStyle) -> Unit,
    onStyleRemoved: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (styles.isEmpty()) return
    
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        items(styles) { style ->
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Row(
                    modifier = Modifier
                        .padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = style.name,
                        modifier = Modifier
                            .clickable { onStyleSelected(style) }
                            .weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    IconButton(
                        onClick = { onStyleRemoved(style.name) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove Style",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
