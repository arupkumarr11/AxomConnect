package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AxomBlue
import com.example.ui.theme.AxomCrimson
import com.example.ui.theme.AxomGold
import com.example.ui.theme.AxomPurple

data class NotificationItemData(
    val id: String,
    val title: String,
    val description: String,
    val timeAgo: String,
    val icon: ImageVector,
    val iconTint: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen() {
    val sampleNotifications = listOf(
        NotificationItemData(
            id = "1",
            title = "ৰঙালী বিহুৰ প্ৰস্তুতি (Bihu Festivities Ahead)",
            description = "Welcome to AxomConnect! Share your preparations, Dhol rhythms, and Bihu songs.",
            timeAgo = "10m ago",
            icon = Icons.Default.Celebration,
            iconTint = AxomCrimson
        ),
        NotificationItemData(
            id = "2",
            title = "নিয়মাৱলী আৰু নিৰাপত্তা (Community Guidelines)",
            description = "Respect fellow members. Zero tolerance for hate speech, harassment, or spam.",
            timeAgo = "1h ago",
            icon = Icons.Default.Info,
            iconTint = AxomBlue
        ),
        NotificationItemData(
            id = "3",
            title = "প্ৰ'ফাইল সম্পূৰ্ণ কৰক (Complete Your Profile)",
            description = "Add your district or city (Guwahati, Jorhat, Dibrugarh, etc.) to connect locally.",
            timeAgo = "3h ago",
            icon = Icons.Default.Groups,
            iconTint = AxomPurple
        ),
        NotificationItemData(
            id = "4",
            title = "অসমীয়া কৃষ্টি আৰু সাহিত্য চৰ্চা",
            description = "Post in Assamese or English. Literature and folk poetry discussions are welcomed!",
            timeAgo = "1d ago",
            icon = Icons.Default.Campaign,
            iconTint = AxomGold
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "বিজ্ঞপ্তি (Notifications)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Community updates & festival alerts",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .testTag("notifications_list"),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(sampleNotifications, key = { it.id }) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(item.iconTint.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                tint = item.iconTint,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = item.timeAgo,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.description,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
