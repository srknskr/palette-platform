package com.palette.mobile.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.palette.mobile.android.ui.navigation.PaletteNavHost
import com.palette.mobile.android.ui.theme.PaletteTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PaletteTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PaletteNavHost()
                }
            }
        }
    }
}
