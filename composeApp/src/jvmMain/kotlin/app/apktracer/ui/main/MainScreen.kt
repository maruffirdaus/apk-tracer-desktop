package app.apktracer.ui.main

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.window.core.layout.WindowSizeClass
import app.apktracer.ui.navigation.AppDestination
import app.apktracer.ui.common.extension.toAppDestination
import io.github.composefluent.component.Icon
import io.github.composefluent.component.MenuItem
import io.github.composefluent.component.NavigationDefaults
import io.github.composefluent.component.NavigationDisplayMode
import io.github.composefluent.component.NavigationView
import io.github.composefluent.component.Text
import io.github.composefluent.component.rememberNavigationState
import io.github.composefluent.icons.Icons
import io.github.composefluent.icons.regular.ArrowSwap
import io.github.composefluent.icons.regular.DocumentSearch
import io.github.composefluent.icons.regular.Settings

@Composable
fun MainScreen(
    navHost: @Composable () -> Unit,
    navController: NavHostController
) {
    val currentBackStack = navController.currentBackStack.collectAsState()

    MainScreenContent(
        navHost = navHost,
        currentBackStack = currentBackStack.value,
        onBack = { navController.popBackStack() },
        onNavigate = { destination ->
            navController.navigate(destination)
        }
    )
}

@Composable
private fun MainScreenContent(
    navHost: @Composable () -> Unit,
    currentBackStack: List<NavBackStackEntry>,
    onBack: () -> Unit,
    onNavigate: (AppDestination) -> Unit
) {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val isAtLeastMediumBreakpoint =
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
    val isAtLeastExpandedBreakpoint =
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)

    NavigationView(
        menuItems = {
            item {
                MenuItem(
                    selected = currentBackStack.lastOrNull()
                        ?.toAppDestination() is AppDestination.TraceApks,
                    onClick = {
                        onNavigate(AppDestination.TraceApks)
                    },
                    text = {
                        Text("Trace APKs")
                    },
                    icon = {
                        Icon(Icons.Regular.DocumentSearch, "Trace APKs")
                    }
                )
            }

            item {
                MenuItem(
                    selected = currentBackStack.lastOrNull()
                        ?.toAppDestination() is AppDestination.ConvertApks,
                    onClick = {
                        onNavigate(AppDestination.ConvertApks)
                    },
                    text = {
                        Text("Convert to APKs")
                    },
                    icon = {
                        Icon(Icons.Regular.ArrowSwap, "Convert to APKs")
                    }
                )
            }
        },
        displayMode = if (isAtLeastExpandedBreakpoint) {
            NavigationDisplayMode.Left
        } else if (isAtLeastMediumBreakpoint) {
            NavigationDisplayMode.LeftCompact
        } else {
            NavigationDisplayMode.LeftCollapsed
        },
        state = rememberNavigationState(initialExpanded = isAtLeastExpandedBreakpoint),
        backButton = {
            NavigationDefaults.BackButton(
                onClick = onBack,
                disabled = currentBackStack.size < 3
            )
        },
        footerItems = {
            item {
                MenuItem(
                    selected = currentBackStack.lastOrNull()
                        ?.toAppDestination() is AppDestination.Settings,
                    onClick = {
                        onNavigate(AppDestination.Settings)
                    },
                    text = {
                        Text("Settings")
                    },
                    icon = {
                        Icon(Icons.Regular.Settings, "Settings")
                    }
                )
            }
        }
    ) {
        navHost()
    }
}