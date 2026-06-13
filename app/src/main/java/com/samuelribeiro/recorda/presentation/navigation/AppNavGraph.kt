package com.samuelribeiro.recorda.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.samuelribeiro.recorda.presentation.ui.activerecall.ActiveRecallViewModel
import com.samuelribeiro.recorda.presentation.ui.content.ContentViewModel
import com.samuelribeiro.recorda.presentation.ui.mindmap.MindMapViewModel
import com.samuelribeiro.recorda.presentation.ui.review.ReviewViewModel
import com.samuelribeiro.recorda.presentation.ui.stats.StatsViewModel
import com.samuelribeiro.recorda.presentation.ui.topic.TopicUiState
import com.samuelribeiro.recorda.presentation.ui.topic.TopicViewModel
import com.samuelribeiro.recorda.presentation.ui.topic.composables.TopicScreen
import com.samuelribeiro.recorda.presentation.ui.topichub.TopicHubViewModel
import com.samuelribeiro.recorda.presentation.ui.topichub.composables.TopicHubScreen
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
                onNavigateToTopicHub = { topicId -> navController.navigate(AppRoute.topicHub(topicId)) },
            )
        }
        composable(
            route = AppRoute.TOPIC_HUB,
            arguments = listOf(navArgument("topicId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val topicId = checkNotNull(backStackEntry.arguments?.getString("topicId"))
            val viewModel = getViewModel<TopicHubViewModel, TopicHubViewModel.ViewModelFactory> { factory ->
                factory.create(topicId)
            }
            TopicHubScreen(
                viewModel = viewModel,
                onNavigateToContent = { navController.navigate(AppRoute.content(topicId)) },
                onNavigateToActiveRecall = { navController.navigate(AppRoute.activeRecall(topicId)) },
                onNavigateToMindMap = { navController.navigate(AppRoute.mindMap(topicId)) },
                onNavigateToReview = { navController.navigate(AppRoute.review(topicId)) },
                onNavigateToStats = { navController.navigate(AppRoute.stats(topicId)) },
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable(
            route = AppRoute.CONTENT,
            arguments = listOf(navArgument("topicId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val topicId = checkNotNull(backStackEntry.arguments?.getString("topicId"))
            val viewModel = getViewModel<ContentViewModel, ContentViewModel.ViewModelFactory> { factory ->
                factory.create(topicId)
            }
            ContentSessionEntry.content(viewModel) { navController.popBackStack() }
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
            route = AppRoute.ACTIVE_RECALL,
            arguments = listOf(navArgument("topicId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val topicId = checkNotNull(backStackEntry.arguments?.getString("topicId"))
            val viewModel = getViewModel<ActiveRecallViewModel, ActiveRecallViewModel.ViewModelFactory> { factory ->
                factory.create(topicId)
            }
            ActiveRecallSessionEntry.content(viewModel) { navController.popBackStack() }
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
