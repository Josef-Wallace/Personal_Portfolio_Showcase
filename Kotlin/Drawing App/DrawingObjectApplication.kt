package com.example.a418drawingapp

import android.app.Application
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class DrawingObjectApplication : Application() {

    private val scope = CoroutineScope(SupervisorJob())

    private val db by lazy {Room.databaseBuilder(
        applicationContext,
        DrawingObjectDatabase::class.java,
        "drawing_database"
    ).build()}

    val drawingObjectRepository by lazy { DrawingObjectRepository(scope, db.drawingObjectDao()) }
}