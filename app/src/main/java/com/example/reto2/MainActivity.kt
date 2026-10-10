package com.example.reto2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.reto2.navigation.AppNavigation
import com.example.reto2.ui.theme.Reto2Theme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            Reto2Theme {
                AppNavigation()
            }
        }
    }
}