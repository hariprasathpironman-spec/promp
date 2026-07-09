package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PromptEntity
import com.example.ui.PromptViewModel
import com.example.ui.theme.GlassyCard
import com.example.ui.theme.LiquidGlassCircleButton
import com.example.ui.theme.GlassyButton
import com.example.ui.theme.liquidPressEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeepNotesScreen(
    viewModel: PromptViewModel,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val keepNotes by viewModel.keepNotesState.collectAsState()
    val focusManager = LocalFocusManager.current
    
    // Google Keep dialog forms
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var noteTitleInput by remember { mutableStateOf("") }
    var noteBodyInput by remember { mutableStateOf("") }
    var selectedNoteBgColor by remember { mutableStateOf("#FFF9C4") } // Default Keep yellow

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 75.dp) // Margin for bottom bar
    ) {
        // Keep Hub Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Keep Notes Notebook",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDark) Color.White else Color(0xFF1E1B4B)
                )
                Text(
                    text = "Pin, color code, and export key prompts or workflows",
                    fontSize = 12.sp,
                    color = if (isDark) Color(0xA9FFFFFF) else Color(0xFF6B7280)
                )
            }
            com.example.ui.theme.ThemeToggleButton(viewModel = viewModel, isDark = isDark)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- GOOGLE KEEP HUB VIEW ---
        Box(modifier = Modifier.weight(1f)) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Keep Toolbar actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "My Pinned & Saved Notes",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF1E1B4B)
                    )
                    
                    LiquidGlassCircleButton(
                        onClick = {
                            noteTitleInput = ""
                            noteBodyInput = ""
                            selectedNoteBgColor = "#FFF9C4"
                            showAddNoteDialog = true
                        },
                        icon = Icons.Default.Add,
                        contentDescription = "Add Note",
                        diameter = 38.dp,
                        tint = Color(0xFF007AFF),
                        isDark = isDark
                    )
                }

                if (keepNotes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Text("📝", fontSize = 42.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Your Keep pinboard is empty",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color(0xFF1E1B4B)
                            )
                            Text(
                                text = "Create quick sticky notes, pin important instructions, color code priorities, or export them.",
                                fontSize = 11.sp,
                                color = if (isDark) Color(0x8DFFFFFF) else Color(0xFF6B7280),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        val pinned = keepNotes.filter { it.isPinnedToKeep }
                        val unpinned = keepNotes.filter { !it.isPinnedToKeep }

                        if (pinned.isNotEmpty()) {
                            item {
                                Text(
                                    text = "PINNED NOTES",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color(0x88FFFFFF) else Color(0xFF6B7280),
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }
                            items(pinned) { note ->
                                KeepNoteCard(note = note, viewModel = viewModel, isDark = isDark)
                            }
                            item { Spacer(modifier = Modifier.height(10.dp)) }
                        }

                        if (unpinned.isNotEmpty()) {
                            item {
                                Text(
                                    text = "OTHERS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color(0x88FFFFFF) else Color(0xFF6B7280),
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }
                            items(unpinned) { note ->
                                KeepNoteCard(note = note, viewModel = viewModel, isDark = isDark)
                            }
                        }
                    }
                }
            }

            // Floating Export/Drive loading overlays if active
            if (viewModel.isDocsExporting) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E1E24) else Color.White),
                        modifier = Modifier.fillMaxWidth(0.85f).padding(16.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            CircularProgressIndicator(color = Color(0xFFDB4437))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Google Docs Cloud Export",
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color(0xFF1E1B4B)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Provisioning connection and generating file structure...",
                                fontSize = 12.sp,
                                color = if (isDark) Color(0x8DFFFFFF) else Color(0xFF6B7280),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal dialog to add a Keep Note
    if (showAddNoteDialog) {
        AlertDialog(
            onDismissRequest = { showAddNoteDialog = false },
            title = {
                Text(
                    text = "Add Sticky Note to Keep",
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color(0xFF1E1B4B)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = noteTitleInput,
                        onValueChange = { noteTitleInput = it },
                        label = { Text("Note Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = noteBodyInput,
                        onValueChange = { noteBodyInput = it },
                        label = { Text("Note Content / Prompt Text") },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp)
                    )

                    // Keep Pastel Color selection dots
                    Text("Select Keep Theme Color:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val colorsOfKeep = listOf(
                            "#FFF9C4", // Yellow
                            "#FFCDD2", // Pastel Red/Pink
                            "#C8E6C9", // Pastel Green
                            "#BBDEFB", // Pastel Blue
                            "#E1BEE7", // Pastel Purple
                            "#B2DFDB"  // Pastel Teal
                        )
                        colorsOfKeep.forEach { colorString ->
                            val isSelectedColor = selectedNoteBgColor == colorString
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(colorString)))
                                    .border(
                                        width = if (isSelectedColor) 2.dp else 0.dp,
                                        color = if (isDark) Color.White else Color.Black,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedNoteBgColor = colorString }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (noteBodyInput.isBlank()) {
                            viewModel.showToast("Note content cannot be empty!")
                            return@Button
                        }
                        viewModel.saveToGoogleKeep(
                            raw = noteTitleInput.ifBlank { "Untitled Sticky Note" },
                            optimized = noteBodyInput,
                            model = "Google Keep Notebook",
                            category = "Productivity",
                            color = selectedNoteBgColor,
                            isPinned = false
                        )
                        showAddNoteDialog = false
                    }
                ) {
                    Text("Save to Keep")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddNoteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Individual Google Keep Sticky Card Component
@Composable
fun KeepNoteCard(
    note: PromptEntity,
    viewModel: PromptViewModel,
    isDark: Boolean
) {
    val clipboardManager = LocalClipboardManager.current
    var isExpandedColorPicker by remember { mutableStateOf(false) }

    val noteColor = remember(note.keepColorHex) {
        try {
            Color(android.graphics.Color.parseColor(note.keepColorHex))
        } catch (e: Exception) {
            Color(0xFFFFF9C4)
        }
    }

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(note.id) {
        isVisible = true
    }
    val itemScale by animateFloatAsState(if(isVisible) 1f else 0.8f, spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow), label = "note_scale")
    val itemAlpha by animateFloatAsState(if(isVisible) 1f else 0f, tween(400), label = "note_alpha")

    Card(
        colors = CardDefaults.cardColors(containerColor = noteColor),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .scale(itemScale)
            .alpha(itemAlpha)
            .border(
                1.dp,
                if (isDark) Color.White.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.15f),
                RoundedCornerShape(14.dp)
            )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Title Header with Pin Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.rawIdea,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.togglePinKeepNote(note) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pin Note",
                            tint = if (note.isPinnedToKeep) Color(0xFFF57C00) else Color.Black.copy(alpha = 0.45f),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.deleteKeepNote(note.id) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Note",
                            tint = Color.Black.copy(alpha = 0.45f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Body
            Text(
                text = note.optimizedPrompt,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontFamily = FontFamily.Monospace,
                color = Color.Black.copy(alpha = 0.85f),
                maxLines = 8,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Note Bottom Accessories Controls: Color picker, Docs sync, Copy link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Color dots toggle & copy actions
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { isExpandedColorPicker = !isExpandedColorPicker },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(Icons.Default.Palette, "Color Palette", tint = Color.Black.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                    }

                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(note.optimizedPrompt))
                            viewModel.showToast("Copied Keep Note Prompt content")
                        },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, "Copy Note", tint = Color.Black.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                    }

                    IconButton(
                        onClick = {
                            viewModel.exportToGoogleDocs(note.rawIdea, note.optimizedPrompt)
                        },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(Icons.Default.CloudUpload, "Upload Google Docs", tint = Color.Black.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                    }
                }

                // Small capsule showing category metadata
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.08f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = note.category,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black.copy(alpha = 0.65f)
                    )
                }
            }

            // Expanded color selector toolbar
            AnimatedVisibility(
                visible = isExpandedColorPicker,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .background(Color.Black.copy(alpha = 0.04f))
                        .padding(6.dp)
                ) {
                    Text("Sticky theme palette:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black.copy(alpha = 0.6f))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    ) {
                        val colorsOfKeep = listOf(
                            "#FFF9C4", // Yellow
                            "#FFCDD2", // Pastel Red/Pink
                            "#C8E6C9", // Pastel Green
                            "#BBDEFB", // Pastel Blue
                            "#E1BEE7", // Pastel Purple
                            "#B2DFDB"  // Pastel Teal
                        )
                        colorsOfKeep.forEach { colorString ->
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(colorString)))
                                    .border(
                                        width = if (note.keepColorHex == colorString) 2.dp else 1.dp,
                                        color = if (note.keepColorHex == colorString) Color.Black else Color.Black.copy(alpha = 0.12f),
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        viewModel.updateKeepNoteColor(note, colorString)
                                        isExpandedColorPicker = false
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}
