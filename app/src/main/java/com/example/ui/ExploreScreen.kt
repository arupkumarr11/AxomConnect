package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Post
import com.example.ui.theme.AxomBlue
import com.example.ui.theme.AxomGold
import com.example.ui.theme.AxomPurple

@Composable
fun ExploreScreen(
    posts: List<Post>,
    currentUserId: String?,
    likedPostIds: Set<String>,
    onLikeClick: (Post) -> Unit,
    onCommentClick: (Post) -> Unit,
    onDeleteClick: (Post) -> Unit,
    onReportClick: (Post) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTagFilter by remember { mutableStateOf<String?>(null) }

    val trendingTopics = listOf(
        "Bihu" to "ৰঙালী আৰু ভোগালী বিহু",
        "AssamCulture" to "অসমৰ লোক-সংস্কৃতি",
        "Kaziranga" to "এশিঙীয়া গঁড় আৰু কাজিৰঙা",
        "Majuli" to "বিশ্বৰ বৃহত্তম নদীদ্বীপ মাজুলী",
        "AssamFood" to "মাছৰ টেঙা, খাৰ আৰু পিঠা",
        "Music" to "জ্যোতিপ্ৰসাদ, ভূপেন্দ্ৰ সংগীত"
    )

    val filteredPosts = remember(posts, searchQuery, selectedTagFilter) {
        posts.filter { post ->
            val matchesQuery = searchQuery.isBlank() ||
                post.content.contains(searchQuery, ignoreCase = true) ||
                post.authorName.contains(searchQuery, ignoreCase = true) ||
                (post.tag?.contains(searchQuery, ignoreCase = true) == true)

            val matchesTag = selectedTagFilter == null || post.tag.equals(selectedTagFilter, ignoreCase = true)

            matchesQuery && matchesTag
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Search Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("সন্ধান কৰক (Search topics, people, culture)...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = AxomBlue)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("explore_search_field"),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AxomBlue,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Trending tags chip row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = selectedTagFilter == null,
                        onClick = { selectedTagFilter = null },
                        label = { Text("সকলো (All)") }
                    )
                    trendingTopics.forEach { (tag, label) ->
                        val isSelected = selectedTagFilter == tag
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedTagFilter = if (isSelected) null else tag
                            },
                            label = { Text("#$tag") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AxomBlue.copy(alpha = 0.15f),
                                selectedLabelColor = AxomBlue
                            )
                        )
                    }
                }
            }
        }

        // Feed of filtered posts
        if (filteredPosts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "কোনো পোষ্ট পোৱা নগল",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "No posts found for this search or topic. Try another keyword or create a post!",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(filteredPosts, key = { it.id }) { post ->
                    PostCard(
                        post = post,
                        currentUserId = currentUserId,
                        isLiked = likedPostIds.contains(post.id),
                        onLikeClick = { onLikeClick(post) },
                        onCommentClick = { onCommentClick(post) },
                        onDeleteClick = if (post.authorId == currentUserId) { { onDeleteClick(post) } } else null,
                        onReportClick = { onReportClick(post) }
                    )
                }
            }
        }
    }
}
