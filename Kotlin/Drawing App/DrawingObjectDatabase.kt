package com.example.a418drawingapp

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow


@Database(entities = [DrawingObject::class], version = 1, exportSchema = false)

abstract class DrawingObjectDatabase : RoomDatabase() {

    abstract fun drawingObjectDao(): DrawingObjectDAO

    companion object {
        @Volatile
        private var INSTANCE: DrawingObjectDatabase? = null
    }


    @Dao
    interface DrawingObjectDAO {

        @Insert
        suspend fun addDrawing(drawingObject: DrawingObject)

        @Query("SELECT * from drawing_objects WHERE filename = :filename")
        fun getDrawing(filename: String) : List<DrawingObject>

        @Query("DELETE FROM drawing_objects WHERE filename = :filename")
        suspend fun deleteDrawing(filename: String)

        //Query to retrieve all drawings from database in natural order
        @Query("SELECT * from drawing_objects ORDER BY id ASC")
        fun allDrawings() : Flow<List<DrawingObject>>


    }

}