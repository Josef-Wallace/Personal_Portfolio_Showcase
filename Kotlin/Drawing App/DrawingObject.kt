package com.example.a418drawingapp

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Transient

@Entity(tableName = "drawing_objects")
data class DrawingObject(var filename: String) {
    @Transient
    @PrimaryKey
        (autoGenerate = true)
    var id: Int = 0

}