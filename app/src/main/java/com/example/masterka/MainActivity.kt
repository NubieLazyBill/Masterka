package com.example.masterka

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.masterka.ui.MasterkaNavGraph
import com.example.masterka.ui.theme.MasterkaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MasterkaTheme {
                MasterkaNavGraph()
            }
        }
    }
}