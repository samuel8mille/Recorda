package com.samuelribeiro.recorda.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.samuelribeiro.recorda.presentation.ui.review.ReviewViewModel
import com.samuelribeiro.recorda.presentation.ui.topic.TopicUiState
import com.samuelribeiro.recorda.presentation.ui.topic.TopicViewModel
import com.samuelribeiro.recorda.presentation.ui.topic.composables.TopicScreen
import com.samuelribeiro.recorda.presentation.utils.getViewModel

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = AppRoute.TOPIC) {
        composable(AppRoute.TOPIC) {
            val viewModel = getViewModel<TopicViewModel, TopicViewModel.ViewModelFactory> { factory ->
                factory.create(TopicUiState())
            }
            TopicScreen(
                viewModel = viewModel,
                onNavigateToReview = { topicId -> navController.navigate(AppRoute.review(topicId)) },
            )
        }
        composable(
            route = AppRoute.REVIEW,
            arguments = listOf(navArgument("topicId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val topicId = checkNotNull(backStackEntry.arguments?.getString("topicId"))
            val viewModel = getViewModel<ReviewViewModel, ReviewViewModel.ViewModelFactory> { factory ->
                factory.create(topicId)
            }
            ReviewSessionEntry.content(viewModel) { navController.popBackStack() }
        }
    }
}
