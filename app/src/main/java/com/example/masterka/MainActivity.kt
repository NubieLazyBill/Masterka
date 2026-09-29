package com.example.masterka

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.example.masterka.data.AppTheme
import com.example.masterka.data.ThemePreferences
import com.example.masterka.ui.MasterkaNavGraph
import com.example.masterka.ui.theme.MasterkaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val appTheme by ThemePreferences
                .getTheme(context)
                .collectAsState(initial = AppTheme.WORKSHOP)

            MasterkaTheme(appTheme = appTheme) {
                MasterkaNavGraph()
            }
        }
    }
}