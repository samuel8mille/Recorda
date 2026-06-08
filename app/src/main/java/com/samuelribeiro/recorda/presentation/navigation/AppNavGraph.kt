package com.samuelribeiro.recorda.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
            TopicScreen(viewModel = viewModel)
        }
    }
}
