package com.example.a418drawingapp


import android.graphics.Bitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.lang.Exception

class DrawingObjectRepository(private val scope: CoroutineScope, private val dao: DrawingObjectDatabase.DrawingObjectDAO) {

    var allDrawings = dao.allDrawings()

    fun addDrawing(drawingObject: DrawingObject) {
        scope.launch {
            try{
                dao.addDrawing(drawingObject)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    fun deleteDrawing(filename: String) {
        scope.launch {
            try{
                dao.deleteDrawing(filename)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }


}