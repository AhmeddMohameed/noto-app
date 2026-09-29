package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.navigation.NotoNavHost
import com.example.ui.theme.NotoTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as NotoApplication).container

        setContent {
            val themeMode by appContainer.userPreferencesRepository.themeMode.collectAsStateWithLifecycle()

            NotoTheme(themeMode = themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NotoNavHost(appContainer = appContainer)
                }
            }
        }
    }
}
