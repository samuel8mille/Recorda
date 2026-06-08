package com.samuelribeiro.recorda

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.samuelribeiro.recorda.presentation.navigation.AppNavGraph
import com.samuelribeiro.recorda.ui.theme.RecordaTheme
import dagger.hilt.android.AndroidEntryPoint

abstract class BaseActivity : ComponentActivity()

@AndroidEntryPoint
class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RecordaTheme {
                val navController = rememberNavController()
                AppNavGraph(navController = navController)
            }
        }
    }
}
