package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.data.model.Comment
import com.example.data.model.Post
import com.example.data.model.Report
import com.example.data.model.UserProfile
import com.example.data.repository.AxomRepository
import com.example.ui.AdminScreen
import com.example.ui.AuthScreen
import com.example.ui.CommentsSheet
import com.example.ui.CreatePostScreen
import com.example.ui.ExploreScreen
import com.example.ui.HomeScreen
import com.example.ui.NotificationsScreen
import com.example.ui.ProfileScreen
import com.example.ui.signOut
import com.example.ui.theme.AxomBlue
import com.example.ui.theme.MyApplicationTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

enum class Screen(val title: String, val assamese: String, val icon: ImageVector, val outlinedIcon: ImageVector) {
    HOME("Home", "ঘৰ", Icons.Filled.Home, Icons.Outlined.Home),
    EXPLORE("Explore", "অন্বেষণ", Icons.Filled.Explore, Icons.Outlined.Explore),
    CREATE("Create", "পোষ্ট", Icons.Filled.AddCircle, Icons.Filled.AddCircle),
    NOTIFICATIONS("Alerts", "বিজ্ঞপ্তি", Icons.Filled.Notifications, Icons.Outlined.Notifications),
    PROFILE("Profile", "প্ৰ'ফাইল", Icons.Filled.Person, Icons.Outlined.Person)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                AppRoot()
            }
        }
    }
}

@Composable
fun AppRoot() {
    val auth = remember { FirebaseAuth.getInstance() }
    var currentUser by remember { mutableStateOf<FirebaseUser?>(auth.currentUser) }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { a ->
            currentUser = a.currentUser
        }
        auth.addAuthStateListener(listener)
        onDispose {
            auth.removeAuthStateListener(listener)
        }
    }

    if (currentUser == null) {
        AuthScreen(
            onAuthSuccess = {
                currentUser = auth.currentUser
            }
        )
    } else {
        MainApp(currentUser = currentUser!!)
    }
}

@Composable
fun MainApp(currentUser: FirebaseUser) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { AxomRepository(context) }
    val credentialManager = remember { CredentialManager.create(context) }

    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    var isInAdminScreen by remember { mutableStateOf(false) }

    // Live State
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    var reports by remember { mutableStateOf<List<Report>>(emptyList()) }
    var isAdmin by remember { mutableStateOf(false) }
    val likedPostIds = remember { mutableStateListOf<String>() }

    // Comments Sheet state
    var activeCommentPost by remember { mutableStateOf<Post?>(null) }
    var activeComments by remember { mutableStateOf<List<Comment>>(emptyList()) }
    var isSubmittingComment by remember { mutableStateOf(false) }

    // Realtime listeners
    LaunchedEffect(currentUser.uid) {
        // Observe feed posts
        launch {
            repository.observePosts().collectLatest { fetched ->
                posts = fetched
            }
        }

        // Observe user profile
        launch {
            repository.observeUserProfile(currentUser.uid).collectLatest { profile ->
                if (profile == null) {
                    // Initialize default profile if first time
                    val defaultProfile = UserProfile(
                        userId = currentUser.uid,
                        displayName = currentUser.displayName ?: "Axomian",
                        username = (currentUser.displayName?.replace(" ", "_")?.lowercase() ?: "user_${currentUser.uid.take(5)}"),
                        avatarUrl = currentUser.photoUrl?.toString(),
                        location = "Assam, India"
                    )
                    repository.saveUserProfile(defaultProfile)
                } else {
                    userProfile = profile
                }
            }
        }

        // Check Admin privilege
        launch {
            isAdmin = repository.isUserAdmin()
            if (isAdmin) {
                repository.observeReports().collectLatest { fetchedReports ->
                    reports = fetchedReports
                }
            }
        }
    }

    // Active Comments listener
    LaunchedEffect(activeCommentPost?.id) {
        val postId = activeCommentPost?.id ?: return@LaunchedEffect
        repository.observeComments(postId).collectLatest { fetchedComments ->
            activeComments = fetchedComments
        }
    }

    // Back handling
    if (isInAdminScreen) {
        BackHandler {
            isInAdminScreen = false
        }
    } else if (currentScreen != Screen.HOME) {
        BackHandler {
            currentScreen = Screen.HOME
        }
    }

    Scaffold(
        bottomBar = {
            if (!isInAdminScreen) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    Screen.values().forEach { screen ->
                        val isSelected = currentScreen == screen
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentScreen = screen },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) screen.icon else screen.outlinedIcon,
                                    contentDescription = screen.title
                                )
                            },
                            label = {
                                Text(
                                    text = screen.assamese,
                                    fontSize = 11.sp
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = AxomBlue,
                                selectedTextColor = AxomBlue,
                                indicatorColor = AxomBlue.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.testTag("nav_tab_${screen.name.lowercase()}")
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isInAdminScreen) {
                AdminScreen(
                    reports = reports,
                    onBack = { isInAdminScreen = false },
                    onUpdateReportStatus = { rId, status ->
                        repository.updateReportStatus(rId, status)
                    },
                    onDeleteReportedPost = { pId ->
                        repository.deletePost(pId)
                    }
                )
            } else {
                Crossfade(targetState = currentScreen, label = "screen_transition") { screen ->
                    when (screen) {
                        Screen.HOME -> HomeScreen(
                            posts = posts,
                            currentUserId = currentUser.uid,
                            likedPostIds = likedPostIds.toSet(),
                            onLikeClick = { post ->
                                coroutineScope.launch {
                                    val res = repository.toggleLike(post.id)
                                    if (res.isSuccess) {
                                        val liked = res.getOrThrow()
                                        if (liked) likedPostIds.add(post.id) else likedPostIds.remove(post.id)
                                    }
                                }
                            },
                            onCommentClick = { post ->
                                activeCommentPost = post
                            },
                            onDeletePost = { post ->
                                coroutineScope.launch {
                                    repository.deletePost(post.id)
                                }
                            },
                            onSubmitReport = { type, targetId, reason ->
                                repository.submitReport(type, targetId, reason)
                            },
                            onNavigateToCreate = { currentScreen = Screen.CREATE }
                        )

                        Screen.EXPLORE -> ExploreScreen(
                            posts = posts,
                            currentUserId = currentUser.uid,
                            likedPostIds = likedPostIds.toSet(),
                            onLikeClick = { post ->
                                coroutineScope.launch {
                                    val res = repository.toggleLike(post.id)
                                    if (res.isSuccess) {
                                        val liked = res.getOrThrow()
                                        if (liked) likedPostIds.add(post.id) else likedPostIds.remove(post.id)
                                    }
                                }
                            },
                            onCommentClick = { post ->
                                activeCommentPost = post
                            },
                            onDeleteClick = { post ->
                                coroutineScope.launch {
                                    repository.deletePost(post.id)
                                }
                            },
                            onReportClick = { post ->
                                coroutineScope.launch {
                                    repository.submitReport("post", post.id, "Flagged from Explore")
                                }
                            }
                        )

                        Screen.CREATE -> CreatePostScreen(
                            userDisplayName = userProfile?.displayName ?: (currentUser.displayName ?: "Member"),
                            userAvatarUrl = userProfile?.avatarUrl ?: currentUser.photoUrl?.toString(),
                            onPostCreated = {
                                currentScreen = Screen.HOME
                            },
                            onSubmitPost = { content, mediaUrl, mediaType, tag ->
                                repository.createPost(content, mediaUrl, mediaType, tag)
                            }
                        )

                        Screen.NOTIFICATIONS -> NotificationsScreen()

                        Screen.PROFILE -> ProfileScreen(
                            userProfile = userProfile,
                            currentUserEmail = currentUser.email,
                            isAdmin = isAdmin,
                            userPosts = posts.filter { it.authorId == currentUser.uid },
                            likedPostIds = likedPostIds.toSet(),
                            onSaveProfile = { updated ->
                                repository.saveUserProfile(updated)
                            },
                            onSignOut = {
                                signOut(
                                    context = context,
                                    credentialManager = credentialManager,
                                    onSignOutComplete = {
                                        // auth state listener transitions automatically to AuthScreen
                                    },
                                    scope = coroutineScope
                                )
                            },
                            onNavigateToAdmin = {
                                isInAdminScreen = true
                            },
                            onLikeClick = { post ->
                                coroutineScope.launch {
                                    val res = repository.toggleLike(post.id)
                                    if (res.isSuccess) {
                                        val liked = res.getOrThrow()
                                        if (liked) likedPostIds.add(post.id) else likedPostIds.remove(post.id)
                                    }
                                }
                            },
                            onCommentClick = { post ->
                                activeCommentPost = post
                            },
                            onDeletePost = { post ->
                                coroutineScope.launch {
                                    repository.deletePost(post.id)
                                }
                            }
                        )
                    }
                }
            }

            // Comments Bottom Sheet
            activeCommentPost?.let { post ->
                CommentsSheet(
                    post = post,
                    comments = activeComments,
                    currentUserId = currentUser.uid,
                    onDismiss = { activeCommentPost = null },
                    onAddComment = { text ->
                        isSubmittingComment = true
                        coroutineScope.launch {
                            repository.addComment(post.id, text)
                            isSubmittingComment = false
                        }
                    },
                    onDeleteComment = { commentId ->
                        coroutineScope.launch {
                            repository.deleteComment(post.id, commentId)
                        }
                    },
                    isSubmitting = isSubmittingComment
                )
            }
        }
    }
}
