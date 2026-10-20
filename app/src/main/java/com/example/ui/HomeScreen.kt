package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Diversity1
import androidx.compose.material.icons.filled.Festival
import androidx.compose.material.icons.filled.NaturePeople
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Post
import com.example.ui.theme.AxomBlue
import com.example.ui.theme.AxomCrimson
import com.example.ui.theme.AxomGold
import com.example.ui.theme.AxomPurple
import kotlinx.coroutines.launch

data class CulturalStory(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    posts: List<Post>,
    currentUserId: String?,
    likedPostIds: Set<String>,
    onLikeClick: (Post) -> Unit,
    onCommentClick: (Post) -> Unit,
    onDeletePost: (Post) -> Unit,
    onSubmitReport: suspend (targetType: String, targetId: String, reason: String) -> Result<String>,
    onNavigateToCreate: () -> Unit
) {
    var reportingPost by remember { mutableStateOf<Post?>(null) }
    var reportReason by remember { mutableStateOf("") }
    var isReporting by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val culturalHighlights = listOf(
        CulturalStory("ৰঙালী বিহু", "Bihu Beats", Icons.Default.Festival, AxomCrimson),
        CulturalStory("চাহ বাগান", "Tea Estates", Icons.Default.NaturePeople, Color(0xFF16A34A)),
        CulturalStory("ব্ৰহ্মপুত্ৰ", "Mighty River", Icons.Default.Diversity1, AxomBlue),
        CulturalStory("সাহিত্য", "Culture & Art", Icons.Default.Celebration, AxomPurple),
        CulturalStory("খবৰ", "Local News", Icons.Default.Campaign, AxomGold)
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = AxomBlue,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "অ",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "AxomConnect",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = AxomBlue
                            )
                            Text(
                                text = "অসমীয়া সামাজিক মাধ্যম",
                                fontSize = 11.sp,
                                color = AxomCrimson,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Cultural Stories / Highlights row
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        culturalHighlights.forEach { story ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(68.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(story.color.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = story.icon,
                                        contentDescription = story.title,
                                        tint = story.color,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = story.title,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 0.5.dp)
                }
            }

            // Feed posts
            if (posts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "স্বাগতম! এতিয়ালৈকে কোনো পোষ্ট নাই।",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AxomBlue
                            )
                            Text(
                                text = "Welcome to AxomConnect! Be the very first to share an update, cultural greeting, or photo with the community.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 6.dp, bottom = 18.dp)
                            )
                            Button(
                                onClick = onNavigateToCreate,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AxomBlue)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Create Post (পোষ্ট কৰক)")
                            }
                        }
                    }
                }
            } else {
                items(posts, key = { it.id }) { post ->
                    PostCard(
                        post = post,
                        currentUserId = currentUserId,
                        isLiked = likedPostIds.contains(post.id),
                        onLikeClick = { onLikeClick(post) },
                        onCommentClick = { onCommentClick(post) },
                        onDeleteClick = if (post.authorId == currentUserId) { { onDeletePost(post) } } else null,
                        onReportClick = { reportingPost = post }
                    )
                }
            }
        }
    }

    // Report Post Dialog
    reportingPost?.let { post ->
        AlertDialog(
            onDismissRequest = { reportingPost = null },
            title = { Text("Report Post (অভিযোগ দাখিল কৰক)") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Why are you reporting this post by ${post.authorName}?",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = reportReason,
                        onValueChange = { reportReason = it },
                        placeholder = { Text("E.g. Inappropriate language, spam, harassment...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("report_reason_field"),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (reportReason.isNotBlank()) {
                            isReporting = true
                            coroutineScope.launch {
                                val res = onSubmitReport("post", post.id, reportReason.trim())
                                isReporting = false
                                reportingPost = null
                                reportReason = ""
                                if (res.isSuccess) {
                                    snackbarHostState.showSnackbar("Thank you. Report submitted for review.")
                                } else {
                                    snackbarHostState.showSnackbar("Failed to submit report.")
                                }
                            }
                        }
                    },
                    enabled = reportReason.isNotBlank() && !isReporting
                ) {
                    if (isReporting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                    } else {
                        Text("Submit Report")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { reportingPost = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
