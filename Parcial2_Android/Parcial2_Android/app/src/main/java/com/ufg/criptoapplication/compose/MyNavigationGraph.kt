package com.ufg.criptoapplication.compose

import Parcial2Screen
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.State
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.*
import androidx.compose.material.icons.filled.PieChart
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.composable


//Interface
interface MyDestination {
    val icon: ImageVector
    val route: String
}

fun NavGraphBuilder.mainGraph(navController: NavHostController) {

    getParcial2Destination()
}

fun NavHostController.navigateSingleTopTo(route: String) =
    this.navigate(route) {
        // Pop up to the start destination of the graph to
        // avoid building up a large stack of destinations
        // on the back stack as users select items
        popUpTo(
            this@navigateSingleTopTo.graph.findStartDestination().id
        ) {
            saveState = true
        }
        // Avoid multiple copies of the same destination when
        // reselecting the same item
        launchSingleTop = true
        // Restore state when reselecting a previously selected item
        restoreState = true
    }


object Parcial2 : MyDestination {
    override val icon = Icons.Filled.PieChart
    override val route = "Parcial2"
}

// Adds destination screen to `this` NavGraphBuilder
fun NavGraphBuilder.getParcial2Destination(
    // Navigation events are exposed to the caller to be handled at a higher level
    onNavigateToList: (conversationId: String) -> Unit= {}
) {
    composable(Parcial2.route){ backStackEntry ->
        //val viewModel = hiltViewModel<UserViewModel>()
        Parcial2Screen()

    }
}