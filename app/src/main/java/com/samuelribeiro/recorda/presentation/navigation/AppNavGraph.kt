package com.samuelribeiro.recorda.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.samuelribeiro.recorda.presentation.ui.mindmap.MindMapViewModel
import com.samuelribeiro.recorda.presentation.ui.review.ReviewViewModel
import com.samuelribeiro.recorda.presentation.ui.stats.StatsViewModel
import com.samuelribeiro.recorda.presentation.ui.study.StudyViewModel
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
                onNavigateToMindMap = { topicId -> navController.navigate(AppRoute.mindMap(topicId)) },
                onNavigateToStudy = { topicId -> navController.navigate(AppRoute.study(topicId)) },
                onNavigateToStats = { topicId -> navController.navigate(AppRoute.stats(topicId)) },
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
        composable(
            route = AppRoute.MIND_MAP,
            arguments = listOf(navArgument("topicId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val topicId = checkNotNull(backStackEntry.arguments?.getString("topicId"))
            val viewModel = getViewModel<MindMapViewModel, MindMapViewModel.ViewModelFactory> { factory ->
                factory.create(topicId)
            }
            MindMapSessionEntry.content(viewModel) { navController.popBackStack() }
        }
        composable(
            route = AppRoute.STUDY,
            arguments = listOf(navArgument("topicId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val topicId = checkNotNull(backStackEntry.arguments?.getString("topicId"))
            val viewModel = getViewModel<StudyViewModel, StudyViewModel.ViewModelFactory> { factory ->
                factory.create(topicId)
            }
            StudySessionEntry.content(viewModel) { navController.popBackStack() }
        }
        composable(
            route = AppRoute.STATS,
            arguments = listOf(navArgument("topicId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val topicId = checkNotNull(backStackEntry.arguments?.getString("topicId"))
            val viewModel = getViewModel<StatsViewModel, StatsViewModel.ViewModelFactory> { factory ->
                factory.create(topicId)
            }
            StatsSessionEntry.content(viewModel) { navController.popBackStack() }
        }
    }
}
