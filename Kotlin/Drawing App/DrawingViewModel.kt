package com.example.a418drawingapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.skydoves.colorpicker.compose.ColorPickerController
import java.io.File
import java.io.FileOutputStream


data class DrawingData(var name: String, var bitmap: Bitmap)


class DrawingViewModel(private val repository: DrawingObjectRepository): ViewModel() {
    //Runtime Model
    var application: DrawingObjectApplication? = null
    var appState by mutableStateOf(0)
        private set
    var popUpState by mutableStateOf(0)
        private set
    var canvasMenuState by mutableStateOf(0)
        private set
    var textBoxText by mutableStateOf("")
        private set
    var selectedFile by mutableStateOf("")
        private set
    var drawings = repository.allDrawings
    var colorHistory by mutableStateOf(listOf<Color>())
        private set
    var currentBitmap by mutableStateOf(Bitmap.createBitmap(935, 935, Bitmap.Config.ARGB_8888))
        private set
    var currentColor by mutableStateOf(Color(0xff000000))
        private set
    var currentTool by mutableStateOf(1)
        private set
    var currentShape by mutableStateOf(0)
        private set
    var strokeSize by mutableStateOf(5f)
        private set



    //How tools edit the canvas
    fun editDrawing(change: PointerInputChange, dragAmount: Offset){
        if (currentTool == 1){
            drawLineAssist(change, dragAmount, currentColor, currentShape)
        }
        if (currentTool == 2){
            drawLineAssist(change, dragAmount, Color(0xffffffff), currentShape)
        }
    }

    private fun drawLineAssist(change: PointerInputChange, dragAmount: Offset, setColor: Color, shape: Int){
        currentBitmap.let { bitmap ->
            val canvas = android.graphics.Canvas()
            canvas.setBitmap(currentBitmap)
            val startX = change.position.x - dragAmount.x
            val startY = change.position.y - dragAmount.y
            val endX = change.position.x
            val endY = change.position.y
            canvas.drawLine(
                startX,
                startY,
                endX,
                endY,
                Paint().apply {
                    color = setColor.toArgb()
                    strokeWidth = strokeSize
                    if (shape == 0){
                        strokeCap = Paint.Cap.ROUND
                    }
                    if (shape == 1){
                        strokeCap = Paint.Cap.SQUARE
                    }
                })
            setNewCurrentBitmap(bitmap)
        }
    }

    //View model functions
    fun getName(name: String){
        if (name.length <= 22){
            textBoxText = name
        }
    }

    fun setColor(color: Color){
        currentColor = color
    }

    fun setMenuState(state: Int){
        canvasMenuState = state
    }

    fun setStrokeShape(shapeID: Int){
        currentShape = shapeID
    }

    fun setStrokeWidth(size: Float){
        strokeSize = size
    }

    fun setNewCurrentBitmap(newBitmap: Bitmap){
        currentBitmap = newBitmap.copy(Bitmap.Config.ARGB_8888, true)
    }

    fun openNewDrawingPopup(){
        if(popUpState < 1)
            popUpState = 1
    }

    fun openDrawingInfoPopup(filename: String){
        if(popUpState!=2)
            selectedFile = filename
        popUpState = 2
    }

    fun openCanvasMenuPopup(){
        if(popUpState < 3)
            popUpState = 3
    }

    fun openCanvasColorSelectorPopup(){
        popUpState = 4
    }

    fun closeDrawingInfoPopup(){
        popUpState = 0
    }

    fun closeCanvasMenuPopup(){
        popUpState = 0
    }

    fun closeNewDrawingPopup(){
        popUpState = 0
        textBoxText = ""
    }

    fun closeCanvasColorSelectorPopup(controller: ColorPickerController){
        popUpState = 0
        colorHistory = colorHistory + currentColor
        currentColor = controller.selectedColor.value
    }

    fun switchToMenuScreen(){
        val file = File(application!!.filesDir.toString() + "/"+ "$selectedFile.PNG")
        file.delete()

        val fOut = FileOutputStream(file)
        val bitmap = currentBitmap
        bitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut)
        fOut.flush()
        fOut.close()

        appState = 1
        popUpState = 0
    }

    fun switchToDrawScreen(){
        currentBitmap = getBitmap(selectedFile).copy(Bitmap.Config.ARGB_8888, true)
        appState = 2
        popUpState = 0
    }

    fun setTool(id: Int){
        currentTool = id
    }

    fun createNewDrawing(){

        //Add new filename to the database
        repository.addDrawing(DrawingObject(textBoxText))

        //Write the bitmap to a file
        val file = File(application!!.filesDir.toString(),
            "$textBoxText.PNG"
        )
        val fOut = FileOutputStream(file)
        val bitmap = Bitmap.createBitmap(935, 935, Bitmap.Config.ARGB_8888)
        bitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut)
        fOut.flush()
        fOut.close()

        drawings = repository.allDrawings
        closeNewDrawingPopup()

    }

    fun deleteDrawing(){
        val file = File(application!!.filesDir.toString() + "/"+ "$selectedFile.PNG")
        file.delete()

        repository.deleteDrawing(selectedFile)

        closeDrawingInfoPopup()
    }

    fun passApplicationRef(app: DrawingObjectApplication){
        application = app

    }

    fun getBitmap(filename: String):  Bitmap {
        try {
            return BitmapFactory.decodeFile(application!!.filesDir.toString() + "/" + filename + ".PNG")
        } catch (e: Exception){
            deleteDrawing()
        }

        return Bitmap.createBitmap(935, 935, Bitmap.Config.ARGB_8888)
    }


}

class DrawingViewModelFactory(private val repository: DrawingObjectRepository) :
    ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DrawingViewModel::class.java)) {
            return DrawingViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}