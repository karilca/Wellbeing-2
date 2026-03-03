package com.gymflow.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.gymflow.app.navigation.NavGraph
import com.gymflow.app.ui.theme.GymFlowTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GymFlowTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    NavGraph()
                }
            }
        }
    }
}