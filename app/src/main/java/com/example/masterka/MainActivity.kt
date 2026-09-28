package com.example.masterka

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.masterka.ui.MasterkaNavGraph
import com.example.masterka.ui.theme.MasterkaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MasterkaTheme {
                MasterkaNavGraph()
            }
        }
    }
}