package com.example.trackpoints.presentation.main

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.trackpoints.R
import com.example.trackpoints.core.ui.LoadingOverlay
import com.example.trackpoints.core.ui.NotificationsDialog
import com.example.trackpoints.data.model.CommissionStatus
import com.example.trackpoints.data.model.UserRole
import com.example.trackpoints.navigation.main.AdminBottomBarScreen
import com.example.trackpoints.navigation.main.AdminBottomBarScreenSaver
import com.example.trackpoints.navigation.main.ClientBottomBarScreen
import com.example.trackpoints.navigation.main.ClientBottomBarScreenSaver
import com.example.trackpoints.navigation.main.FreelancerBottomBarScreen
import com.example.trackpoints.navigation.main.FreelancerBottomBarScreenSaver
import com.example.trackpoints.navigation.main.adminBottomBarItems
import com.example.trackpoints.navigation.main.clientBottomBarItems
import com.example.trackpoints.navigation.main.freelancerBottomBarItems
import com.example.trackpoints.presentation.admin.UserRequestsScreen
import com.example.trackpoints.presentation.client.FreelancersScreen
import com.example.trackpoints.presentation.freelancer.CommissionRequestsDialog
import com.example.trackpoints.presentation.shared.HomeScreen
import com.example.trackpoints.presentation.shared.MessagesScreen
import com.example.trackpoints.presentation.shared.PointsScreen
import com.example.trackpoints.presentation.shared.ProfileScreenTab
import com.example.trackpoints.presentation.shared.ProjectsScreen
import com.example.trackpoints.ui.theme.AppFonts
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun MainScreens(
    viewModel: MainViewModel,
    onNavigateRequest: () -> Unit,
    onNavigateChat: () -> Unit,
    onNavigateProfile: () -> Unit,
    onLogout: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.startPolling()
        viewModel.checkDeadlines()
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                MainEffect.NavigateProfile -> onNavigateProfile()
                MainEffect.NavigateBack -> {}
                MainEffect.NavigateLogout -> onLogout()
                MainEffect.NavigateChat -> onNavigateChat()
                MainEffect.NavigateRequest -> onNavigateRequest()
            }
        }
    }
    Box(Modifier.fillMaxSize()) {
        val role = state.currentUser?.role

        if (role == UserRole.ADMIN) {
            AdminContent(
                state = state,
                onIntent = viewModel::handleIntent,
                onLogout = onLogout
            )
        } else if (role == UserRole.CLIENT) {
            ClientContent(
                state = state,
                onIntent = viewModel::handleIntent,
            )
        } else {
            FreelancerContent(
                state = state,
                onIntent = viewModel::handleIntent,
                onLogout = onLogout
            )
        }

        if (state.shouldShowNotifications) {
            NotificationsDialog(
                state,
                { viewModel.handleIntent(MainIntent.ShouldShowNotificationsChanged(false)) })
        }

        if (state.shouldShowCommissionRequests) {
            CommissionRequestsDialog(
                state,
                onIntent = viewModel::handleIntent,
                { viewModel.handleIntent(MainIntent.ShouldShowCommissionRequestsChanged(false)) })
        }

        if (state.isLoading) {
            LoadingOverlay()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreelancerContent(
    state: MainState,
    onIntent: (MainIntent) -> Unit,
    onLogout: () -> Unit
) {
    val backStack = rememberNavBackStack(FreelancerBottomBarScreen.Home)
    var currentBottomBarScreen: FreelancerBottomBarScreen by rememberSaveable(
        stateSaver = FreelancerBottomBarScreenSaver,
    ) { mutableStateOf(FreelancerBottomBarScreen.Home) }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFEF3E2)
                ),
                modifier = Modifier
                    .dropShadow(
                        shape = RoundedCornerShape(0.dp), shadow = Shadow(
                            radius = 4.dp,
                            color = Color.Black.copy(0.25f),
                            offset = DpOffset(x = 0.dp, y = (4).dp)
                        )
                    ),
                title = {},
                navigationIcon = {
                    Image(
                        painter = painterResource(R.drawable.trackpoints_whole_logo),
                        contentDescription = "TrackPay logo",
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .width(185.dp)
                            .padding(top = 15.dp, bottom = 15.dp, start = 15.dp)
                    )
                },
                actions = {
                    Row {
                        Box(Modifier.padding(top = 15.dp, bottom = 15.dp, end = 15.dp)) {
                            var hasClickedNotifications by remember { mutableStateOf(false) }

                            Icon(
                                painter = painterResource(R.drawable.notification_icon),
                                contentDescription = "Notifications",
                                modifier = Modifier
                                    .clickable {
                                        onIntent(MainIntent.ShouldShowNotificationsChanged(true))
                                        onIntent(MainIntent.MarkCurrentNotificationsAsRead)
                                    }
                            )
                            val hasNotifications = state.notifications.any { !it.isRead }
                            if (hasNotifications)
                                Box(
                                    Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFA812F))
                                        .align(Alignment.TopEnd)
                                )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFFFEF3E2),
                modifier = Modifier
                    .drawWithContent {
                        drawContent()
                        drawLine(
                            color = Color(0xFFD7D7D7),
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = 1.dp.toPx(),
                        )
                    }
                    .dropShadow(
                        shape = RoundedCornerShape(0.dp), shadow = Shadow(
                            radius = 4.dp,
                            color = Color.Black.copy(0.25f),
                            offset = DpOffset(x = 0.dp, y = (-4).dp)
                        )
                    ),
            ) {
                freelancerBottomBarItems.forEach { destination ->
                    NavigationBarItem(
                        modifier = Modifier,
                        selected = currentBottomBarScreen == destination,
                        icon = {
                            val selected = currentBottomBarScreen == destination
                            Column(
                                Modifier
                                    .width(60.dp)
                                    .padding(vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val hasNewMessages =
                                    state.inbox.any { it.unreadCount > 0 }
                                val hasNewProjectRequests =
                                    state.commissions.any { it.status == CommissionStatus.PENDING }
                                if ((destination.title == "Messages" && hasNewMessages) || destination.title == "Projects" && hasNewProjectRequests) {
                                    BadgedBox(
                                        badge = {
                                            Badge(
                                                modifier = Modifier.size(5.dp),
                                                containerColor = Color(0xFFDD0303),
                                            )
                                        },
                                    ) {
                                        Icon(
                                            modifier = Modifier.size(38.dp),
                                            painter = painterResource(destination.icon),
                                            contentDescription = "$destination icon",
                                        )
                                    }
                                } else {
                                    Icon(
                                        modifier = Modifier.size(38.dp),
                                        painter = painterResource(destination.icon),
                                        contentDescription = "$destination icon",
                                    )
                                }
                                Text(
                                    destination.title,
                                    style = TextStyle(
                                        fontFamily = AppFonts.roboto,
                                        fontWeight = FontWeight.Light,
                                        fontSize = 10.sp,
                                    ),
                                )
                            }
                        },
                        onClick = {
                            if (backStack.lastOrNull() != destination) {
                                if (backStack.lastOrNull() in freelancerBottomBarItems) {
                                    backStack.removeAt(backStack.lastIndex)
                                }
                                backStack.add(destination)
                                currentBottomBarScreen = destination
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color(0xFFFA812F),
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.Black,
                            unselectedIconColor = Color.Black,
                            unselectedTextColor = Color.Black,
                            disabledIconColor = Color.Unspecified,
                            disabledTextColor = Color.Unspecified,
                        ),
                    )
                }
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFD3AD))
                .padding(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
                entryProvider = entryProvider {
                    entry<FreelancerBottomBarScreen.Home> {
                        HomeScreen(
                            state, onIntent,
                            onNavigateProjects = {
                                backStack.add(FreelancerBottomBarScreen.Projects)
                                currentBottomBarScreen = FreelancerBottomBarScreen.Projects
                            },
                            onNavigateMessages = {
                                backStack.add(FreelancerBottomBarScreen.Messages)
                                currentBottomBarScreen = FreelancerBottomBarScreen.Messages
                                onIntent(MainIntent.StartChat(it))
                            })
                    }
                    entry<FreelancerBottomBarScreen.Projects> {
                        ProjectsScreen(state, onIntent, onNavigateMessages = {
                            backStack.add(FreelancerBottomBarScreen.Messages)
                            currentBottomBarScreen = FreelancerBottomBarScreen.Messages
                            onIntent(MainIntent.StartChat(it))
                        })
                    }
                    entry<FreelancerBottomBarScreen.Profile> {
                        ProfileScreenTab(state, onIntent, onLogout)
                    }
                    entry<FreelancerBottomBarScreen.Points> {
                        PointsScreen(state, onIntent)
                    }
                    entry<FreelancerBottomBarScreen.Messages> {
                        MessagesScreen(state, onIntent)
                    }
                },
                transitionSpec = {
                    // Slide in from right when navigating forward
                    slideInHorizontally(initialOffsetX = { it }) togetherWith slideOutHorizontally(
                        targetOffsetX = { -it })
                },
                popTransitionSpec = {
                    // Slide in from left when navigating back
                    slideInHorizontally(initialOffsetX = { -it }) togetherWith slideOutHorizontally(
                        targetOffsetX = { it })
                },
                predictivePopTransitionSpec = {
                    // Slide in from left when navigating back
                    slideInHorizontally(initialOffsetX = { -it }) togetherWith slideOutHorizontally(
                        targetOffsetX = { it })
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientContent(
    state: MainState,
    onIntent: (MainIntent) -> Unit,
) {
    val backStack = rememberNavBackStack(ClientBottomBarScreen.Home)
    var currentBottomBarScreen: ClientBottomBarScreen by rememberSaveable(
        stateSaver = ClientBottomBarScreenSaver,
    ) { mutableStateOf(ClientBottomBarScreen.Home) }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFEF3E2)
                ),
                modifier = Modifier
                    .dropShadow(
                        shape = RoundedCornerShape(0.dp), shadow = Shadow(
                            radius = 4.dp,
                            color = Color.Black.copy(0.25f),
                            offset = DpOffset(x = 0.dp, y = (4).dp)
                        )
                    ),
                title = {},
                navigationIcon = {
                    Image(
                        painter = painterResource(R.drawable.trackpoints_whole_logo),
                        contentDescription = "TrackPay logo",
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .width(185.dp)
                            .padding(top = 15.dp, bottom = 15.dp, start = 15.dp)
                    )
                },
                actions = {
                    Row {
                        Icon(
                            painter = painterResource(R.drawable.profile_icon),
                            contentDescription = "Profile",
                            modifier = Modifier
                                .padding(top = 15.dp, bottom = 15.dp, end = 15.dp)
                                .clickable { onIntent(MainIntent.ProfileClicked) }
                        )
                        Spacer(Modifier.width(5.dp))
                        Box(Modifier.padding(top = 15.dp, bottom = 15.dp, end = 15.dp)) {
                            var hasClickedNotifications by remember { mutableStateOf(false) }

                            Icon(
                                painter = painterResource(R.drawable.notification_icon),
                                contentDescription = "Notifications",
                                modifier = Modifier
                                    .clickable {
                                        onIntent(MainIntent.ShouldShowNotificationsChanged(true))
                                        onIntent(MainIntent.MarkCurrentNotificationsAsRead)
                                    }
                            )
                            val hasNotifications = state.notifications.any { !it.isRead }
                            if (hasNotifications)
                                Box(
                                    Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFA812F))
                                        .align(Alignment.TopEnd)
                                )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFFFEF3E2),
                modifier = Modifier
                    .drawWithContent {
                        drawContent()
                        drawLine(
                            color = Color(0xFFD7D7D7),
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = 1.dp.toPx(),
                        )
                    }
                    .dropShadow(
                        shape = RoundedCornerShape(0.dp), shadow = Shadow(
                            radius = 4.dp,
                            color = Color.Black.copy(0.25f),
                            offset = DpOffset(x = 0.dp, y = (-4).dp)
                        )
                    ),
            ) {
                clientBottomBarItems.forEach { destination ->
                    NavigationBarItem(
                        modifier = Modifier,
                        selected = currentBottomBarScreen == destination,
                        icon = {
                            val selected = currentBottomBarScreen == destination
                            Column(
                                Modifier
                                    .width(60.dp)
                                    .padding(vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val hasNewMessages = state.inbox.any { it.unreadCount > 0 }
                                if (destination.title == "Messages" && hasNewMessages) {
                                    BadgedBox(
                                        badge = {
                                            Badge(
                                                modifier = Modifier.size(5.dp),
                                                containerColor = Color(0xFFDD0303),
                                            )
                                        },
                                    ) {
                                        Icon(
                                            modifier = Modifier.size(38.dp),
                                            painter = painterResource(destination.icon),
                                            contentDescription = "$destination icon",
                                        )
                                    }
                                } else {
                                    Icon(
                                        modifier = Modifier.size(38.dp),
                                        painter = painterResource(destination.icon),
                                        contentDescription = "$destination icon",
                                    )
                                }
                                Text(
                                    destination.title,
                                    style = TextStyle(
                                        fontFamily = AppFonts.roboto,
                                        fontWeight = FontWeight.Light,
                                        fontSize = 10.sp,
                                    ),
                                )
                            }
                        },
                        onClick = {
                            if (backStack.lastOrNull() != destination) {
                                if (backStack.lastOrNull() in clientBottomBarItems) {
                                    backStack.removeAt(backStack.lastIndex)
                                }
                                backStack.add(destination)
                                currentBottomBarScreen = destination
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color(0xFFFA812F),
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.Black,
                            unselectedIconColor = Color.Black,
                            unselectedTextColor = Color.Black,
                            disabledIconColor = Color.Unspecified,
                            disabledTextColor = Color.Unspecified,
                        ),
                    )
                }
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFD3AD))
                .padding(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
                entryProvider = entryProvider {
                    entry<ClientBottomBarScreen.Home> {
                        HomeScreen(
                            state, onIntent,
                            onNavigateProjects = {
                                backStack.add(ClientBottomBarScreen.Projects)
                                currentBottomBarScreen = ClientBottomBarScreen.Projects
                            },
                            onNavigateMessages = {
                                backStack.add(ClientBottomBarScreen.Messages)
                                currentBottomBarScreen = ClientBottomBarScreen.Messages
                                onIntent(MainIntent.StartChat(it))
                            })
                    }
                    entry<ClientBottomBarScreen.Projects> {
                        ProjectsScreen(state, onIntent, onNavigateMessages = {
                            backStack.add(ClientBottomBarScreen.Messages)
                            currentBottomBarScreen = ClientBottomBarScreen.Messages
                            onIntent(MainIntent.StartChat(it))
                        })
                    }
                    entry<ClientBottomBarScreen.Freelancers> {
                        FreelancersScreen(state, onIntent)
                    }
                    entry<ClientBottomBarScreen.Points> {
                        PointsScreen(state, onIntent)
                    }
                    entry<ClientBottomBarScreen.Messages> {
                        MessagesScreen(state, onIntent)
                    }
                },
                transitionSpec = {
                    // Slide in from right when navigating forward
                    slideInHorizontally(initialOffsetX = { it }) togetherWith slideOutHorizontally(
                        targetOffsetX = { -it })
                },
                popTransitionSpec = {
                    // Slide in from left when navigating back
                    slideInHorizontally(initialOffsetX = { -it }) togetherWith slideOutHorizontally(
                        targetOffsetX = { it })
                },
                predictivePopTransitionSpec = {
                    // Slide in from left when navigating back
                    slideInHorizontally(initialOffsetX = { -it }) togetherWith slideOutHorizontally(
                        targetOffsetX = { it })
                },
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminContent(
    state: MainState,
    onIntent: (MainIntent) -> Unit,
    onLogout: () -> Unit
) {
    val backStack = rememberNavBackStack(AdminBottomBarScreen.Home)
    var currentBottomBarScreen: AdminBottomBarScreen by rememberSaveable(
        stateSaver = AdminBottomBarScreenSaver,
    ) { mutableStateOf(AdminBottomBarScreen.Home) }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFEF3E2)
                ),
                modifier = Modifier
                    .dropShadow(
                        shape = RoundedCornerShape(0.dp), shadow = Shadow(
                            radius = 4.dp,
                            color = Color.Black.copy(0.25f),
                            offset = DpOffset(x = 0.dp, y = (4).dp)
                        )
                    ),
                title = {},
                navigationIcon = {
                    Image(
                        painter = painterResource(R.drawable.trackpoints_whole_logo),
                        contentDescription = "TrackPay logo",
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .width(185.dp)
                            .padding(top = 15.dp, bottom = 15.dp, start = 15.dp)
                    )
                },
                actions = {
                    Row {
                        Box(Modifier.padding(top = 15.dp, bottom = 15.dp, end = 15.dp)) {
                            var hasClickedNotifications by remember { mutableStateOf(false) }

                            Icon(
                                painter = painterResource(R.drawable.notification_icon),
                                contentDescription = "Notifications",
                                modifier = Modifier
                                    .clickable {
                                        onIntent(MainIntent.ShouldShowNotificationsChanged(true))
                                        onIntent(MainIntent.MarkCurrentNotificationsAsRead)
                                    }
                            )
                            val hasNotifications = state.notifications.any { !it.isRead }
                            if (hasNotifications)
                                Box(
                                    Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFA812F))
                                        .align(Alignment.TopEnd)
                                )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFFFEF3E2),
                modifier = Modifier
                    .drawWithContent {
                        drawContent()
                        drawLine(
                            color = Color(0xFFD7D7D7),
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = 1.dp.toPx(),
                        )
                    }
                    .dropShadow(
                        shape = RoundedCornerShape(0.dp), shadow = Shadow(
                            radius = 4.dp,
                            color = Color.Black.copy(0.25f),
                            offset = DpOffset(x = 0.dp, y = (-4).dp)
                        )
                    ),
            ) {
                adminBottomBarItems.forEach { destination ->
                    NavigationBarItem(
                        modifier = Modifier,
                        selected = currentBottomBarScreen == destination,
                        icon = {
                            Column(
                                Modifier
                                    .width(60.dp)
                                    .padding(vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val hasNewRequests =
                                    state.users.any { !it.isApproved && !it.isRejected }
                                if (destination.title == "Requests" && hasNewRequests) {
                                    BadgedBox(
                                        badge = {
                                            Badge(
                                                modifier = Modifier.size(5.dp),
                                                containerColor = Color(0xFFDD0303),
                                            )
                                        },
                                    ) {
                                        Icon(
                                            modifier = Modifier.size(38.dp),
                                            painter = painterResource(destination.icon),
                                            contentDescription = "$destination icon",
                                        )
                                    }
                                } else {
                                    Icon(
                                        modifier = Modifier.size(38.dp),
                                        painter = painterResource(destination.icon),
                                        contentDescription = "$destination icon",
                                    )
                                }
                                Text(
                                    destination.title,
                                    style = TextStyle(
                                        fontFamily = AppFonts.roboto,
                                        fontWeight = FontWeight.Light,
                                        fontSize = 10.sp,
                                    ),
                                )
                            }
//                            }
                        },
                        onClick = {
                            if (backStack.lastOrNull() != destination) {
                                if (backStack.lastOrNull() in adminBottomBarItems) {
                                    backStack.removeAt(backStack.lastIndex)
                                }
                                backStack.add(destination)
                                currentBottomBarScreen = destination
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color(0xFFFA812F),
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.Black,
                            unselectedIconColor = Color.Black,
                            unselectedTextColor = Color.Black,
                            disabledIconColor = Color.Unspecified,
                            disabledTextColor = Color.Unspecified,
                        ),
                    )
                }
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFD3AD))
                .padding(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
                entryProvider = entryProvider {
                    entry<AdminBottomBarScreen.Home> {
                        HomeScreen(
                            state, onIntent, {}, {},
                            {
                                backStack.add(AdminBottomBarScreen.Requests)
                                currentBottomBarScreen = AdminBottomBarScreen.Requests
                            })
                    }
                    entry<AdminBottomBarScreen.Profile> {
                        ProfileScreenTab(state, onIntent, onLogout)
                    }
                    entry<AdminBottomBarScreen.Requests> {
                        UserRequestsScreen(state, onIntent)
                    }
                },
                transitionSpec = {
                    // Slide in from right when navigating forward
                    slideInHorizontally(initialOffsetX = { it }) togetherWith slideOutHorizontally(
                        targetOffsetX = { -it })
                },
                popTransitionSpec = {
                    // Slide in from left when navigating back
                    slideInHorizontally(initialOffsetX = { -it }) togetherWith slideOutHorizontally(
                        targetOffsetX = { it })
                },
                predictivePopTransitionSpec = {
                    // Slide in from left when navigating back
                    slideInHorizontally(initialOffsetX = { -it }) togetherWith slideOutHorizontally(
                        targetOffsetX = { it })
                },
            )
        }
    }
}
