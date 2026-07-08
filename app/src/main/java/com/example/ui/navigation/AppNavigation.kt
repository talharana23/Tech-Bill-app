package com.example.ui.navigation

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.SaaSViewModel

@Composable
fun AppNavigation(
    viewModel: SaaSViewModel,
    modifier: Modifier = Modifier
) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val navController = rememberNavController()

    if (!isLoggedIn) {
        LoginScreen(viewModel = viewModel, modifier = modifier)
    } else {
        // Handle Android 13+ (API 33+) Runtime Notification Permission gracefully
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            // Let the VM or system know the result if needed
        }

        LaunchedEffect(Unit) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        Scaffold(
            bottomBar = {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                NavigationBar(
                    containerColor = DarkSurface,
                    modifier = Modifier.border(
                        width = 1.dp,
                        color = DarkBorder,
                        shape = RectangleShape
                    ),
                    tonalElevation = 8.dp
                ) {
                    val items = listOf(
                        NavigationItem(
                            title = "Dashboard",
                            route = Screen.Dashboard.route,
                            selectedIcon = Icons.Default.Dashboard,
                            unselectedIcon = Icons.Outlined.Dashboard
                        ),
                        NavigationItem(
                            title = "Invoices",
                            route = Screen.Invoices.route,
                            selectedIcon = Icons.Default.ReceiptLong,
                            unselectedIcon = Icons.Outlined.ReceiptLong
                        ),
                        NavigationItem(
                            title = "Online Orders",
                            route = Screen.OnlineOrders.route,
                            selectedIcon = Icons.Default.LocalShipping,
                            unselectedIcon = Icons.Outlined.LocalShipping
                        ),
                        NavigationItem(
                            title = "Profile",
                            route = Screen.Profile.route,
                            selectedIcon = Icons.Default.Person,
                            unselectedIcon = Icons.Outlined.Person
                        )
                    )

                    items.forEach { item ->
                        val isSelected = currentRoute == item.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title,
                                    tint = if (isSelected) AccentCyan else DarkTextSecondary
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    color = if (isSelected) AccentCyan else DarkTextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = AccentCyan.copy(alpha = 0.12f)
                            )
                        )
                    }
                }
            },
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route,
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkBgStart)
                    .padding(innerPadding)
            ) {
                composable(Screen.Dashboard.route) {
                    DashboardScreen(viewModel = viewModel)
                }
                composable(Screen.Invoices.route) {
                    InvoicesScreen(viewModel = viewModel)
                }
                composable(Screen.OnlineOrders.route) {
                    OnlineOrdersScreen(viewModel = viewModel)
                }
                composable(Screen.Profile.route) {
                    ProfileScreen(viewModel = viewModel)
                }
            }
        }
    }
}

private data class NavigationItem(
    val title: String,
    val route: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector
)
