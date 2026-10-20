package com.example.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.AxomBlue
import com.example.ui.theme.AxomGold
import com.example.ui.theme.AxomPurple
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    userDisplayName: String,
    userAvatarUrl: String?,
    onPostCreated: () -> Unit,
    onSubmitPost: suspend (content: String, mediaUrl: String?, mediaType: String?, tag: String?) -> Result<String>
) {
    var content by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf("Bihu") }
    var mediaUrl by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showUrlDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    val tags = listOf(
        "Bihu" to "বিহু",
        "AssamCulture" to "সংস্কৃতি",
        "AssamFood" to "অসমীয়া খাদ্য",
        "Nature" to "প্ৰকৃতি",
        "Kaziranga" to "কাজিৰঙা",
        "Majuli" to "মাজুলী",
        "Music" to "সংগীত",
        "Community" to "সমাজ"
    )

    // Curated cultural photography presets for quick high-quality sharing
    val sampleImages = listOf(
        "https://images.unsplash.com/photo-1598327105666-5b89351aff97?w=800" to "Kaziranga Landscape",
        "https://images.unsplash.com/photo-1544816155-12df9643f363?w=800" to "Assam Tea Estate",
        "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800" to "River Brahmaputra",
        "https://images.unsplash.com/photo-1582510003544-4d00b7f74220?w=800" to "Bihu Dance Celebration"
    )

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            mediaUrl = uri.toString()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "নতুন পোষ্ট কৰক",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Create a Post",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            if (content.isNotBlank()) {
                                isSubmitting = true
                                errorMessage = null
                                coroutineScope.launch {
                                    val result = onSubmitPost(
                                        content.trim(),
                                        mediaUrl.ifBlank { null },
                                        if (mediaUrl.isNotBlank()) "image" else "none",
                                        selectedTag
                                    )
                                    isSubmitting = false
                                    if (result.isSuccess) {
                                        content = ""
                                        mediaUrl = ""
                                        selectedImageUri = null
                                        onPostCreated()
                                    } else {
                                        errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Failed to post"
                                    }
                                }
                            }
                        },
                        enabled = content.isNotBlank() && !isSubmitting,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AxomBlue),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("publish_post_button")
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Publish")
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // User header
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!userAvatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = userAvatarUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(AxomBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userDisplayName.take(1),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = userDisplayName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Posting to AxomConnect Feed",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Text input area
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                placeholder = {
                    Text("মনৰ কথা ব্যক্ত কৰক... Share stories, Bihu wishes, questions, or updates with the community...")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .testTag("post_content_input"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AxomBlue,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Category tag picker
            Text(
                text = "বিষয় বাছি লওক (Select Category):",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tags.forEach { (tagId, label) ->
                    val isSelected = selectedTag == tagId
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedTag = tagId },
                        label = { Text("$label (#$tagId)") },
                        leadingIcon = if (isSelected) {
                            { Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AxomBlue.copy(alpha = 0.15f),
                            selectedLabelColor = AxomBlue
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Media attachment options
            Text(
                text = "ছবি সংযোগ কৰক (Attach Media):",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Select Photo", fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = { showUrlDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Image, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Image URL", fontSize = 13.sp)
                }
            }

            // Quick cultural sample images
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "বা নমুনা ছবি বাছি লওক (Or choose a scenic photo):",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                sampleImages.forEach { (url, name) ->
                    val isSelected = mediaUrl == url
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) AxomBlue else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { mediaUrl = url }
                    ) {
                        AsyncImage(
                            model = url,
                            contentDescription = name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            // Media Preview if attached
            if (mediaUrl.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(14.dp))
                ) {
                    AsyncImage(
                        model = mediaUrl,
                        contentDescription = "Selected media preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = {
                            mediaUrl = ""
                            selectedImageUri = null
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            .size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove media",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }

    if (showUrlDialog) {
        var tempUrl by remember { mutableStateOf(mediaUrl) }
        AlertDialog(
            onDismissRequest = { showUrlDialog = false },
            title = { Text("Enter Image URL") },
            text = {
                OutlinedTextField(
                    value = tempUrl,
                    onValueChange = { tempUrl = it },
                    label = { Text("https://...") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        mediaUrl = tempUrl.trim()
                        showUrlDialog = false
                    }
                ) {
                    Text("Attach")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUrlDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
