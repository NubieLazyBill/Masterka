package com.example.masterka

import android.app.Application
import com.example.masterka.data.AppDatabase

class MasterkaApp : Application() {
    val db by lazy { AppDatabase.get(this) }
    val dao by lazy { db.storageDao() }
}