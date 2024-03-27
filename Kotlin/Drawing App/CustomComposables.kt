package com.example.a418drawingapp

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.skydoves.colorpicker.compose.AlphaSlider
import com.github.skydoves.colorpicker.compose.AlphaTile
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController

/**
 * This class serves to contain most of the composable code to keep the main activity clean.
 */
class CustomComposables {
    @Composable
    fun TextButton(buttonText: String, onClickCall: () -> Unit){
        OutlinedButton(
            //On click event
            onClick = {
                onClickCall()
            },
            //Inner Color
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color(0xff161616),
            ),
            //Border Color
            border = BorderStroke(2.dp, Color(0xffE25F00)),
            modifier = Modifier
                .padding(8.dp)
        ){
            Text(
                text = buttonText,
                color = Color(0xffE25F00),
            )
        }
    }

    @Composable
    fun Thumbnail(onClickCall: () -> Unit){
        Image(
            painter = painterResource(id = R.drawable.inkpot_logo),
            contentDescription = "Logo",
            modifier = Modifier
                .fillMaxSize()
                .clickable { onClickCall() }
        )
    }

    @Composable
    fun MenuScreen(onClickCall: () -> Unit, viewModel: DrawingViewModel){
        Image(
            painter = painterResource(R.drawable.inkpot_background),
            contentDescription = "background",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .fillMaxSize()
        )

        //Organization
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
        ){
            //First row of column
            OutlinedButton(
                //On click event
                onClick = {onClickCall()},
                modifier = Modifier
                    .padding(20.dp),
                //Inner Color
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color(0xff161616),
                ),
                //Border Color
                border = BorderStroke(2.dp, Color(0xffE25F00))
            ){
                Text(
                    text = "New Drawing",
                    color = Color(0xffE25F00),
                )
            }

            val list by viewModel.drawings.collectAsState(listOf())

            //Second row of column
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
            ){
                for(drawing in list){
                    item{
                        DrawingItem(drawing.filename, viewModel.getBitmap(drawing.filename)){
                            viewModel.openDrawingInfoPopup(drawing.filename)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun NewPopup(onCreate: () -> Unit, onCancel: () -> Unit, viewModel: DrawingViewModel){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .background(Color(0xff101010), RoundedCornerShape(24.dp))
                .border(4.dp, Color(0xffe25f00), RoundedCornerShape(24.dp))
        ){
            Text(
                text = "Drawing Name:",
                color = Color(0xffe25f00),
                fontSize = 24.sp,
                modifier = Modifier
                    .padding(8.dp)
            )
            TextField(
                value = viewModel.textBoxText,
                singleLine = true,
                onValueChange = {text ->
                    viewModel.getName(text)
                },
                modifier = Modifier
                    .border(4.dp, Color(0xffe25f00), RoundedCornerShape(2.dp))
                    .testTag("AddName")
            )
            Row(
                modifier = Modifier
                    .padding(4.dp)
            ){
                TextButton("Create", onCreate)
                TextButton("Cancel", onCancel)
            }
        }
    }

    @Composable
    fun DrawingInfoPopup(onClose: () -> Unit, onOpen: () -> Unit, onShare: () -> Unit, onDelete: () -> Unit, viewModel: DrawingViewModel){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .background(Color(0xff101010), RoundedCornerShape(24.dp))
                .border(4.dp, Color(0xffe25f00), RoundedCornerShape(24.dp))
        ){
            TextButton("X", onClose)
            Text(
                text = viewModel.selectedFile,
                color = Color(0xffe25f00),
                fontSize = 24.sp,
                modifier = Modifier
                    .padding(4.dp)
            )
            Image(
                bitmap = viewModel.getBitmap(viewModel.selectedFile).asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(400.dp, 400.dp)
                    .offset(0.dp, 4.dp)
                    .padding(12.dp)
                    .border(4.dp, Color(0xffe25f00))
                    .background(Color.White)
            )
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
            ){
                TextButton("Open", onOpen)
                TextButton("Share", onShare)
                TextButton("Delete", onDelete)
            }
        }

    }

    @Composable
    fun DrawingItem(name: String, bitmap: Bitmap, onClickCall: () -> Unit){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp, 3.dp)
                .border(4.dp, Color(0xffe25f00))
                .background(Color(0xff101010))
                .clickable() { onClickCall() }
        ){
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Image",
                modifier = Modifier
                    .background(Color.White)
                    .size(56.dp, 56.dp)
            )
            Text(
                text = name,
                fontSize = 24.sp,
                color = Color(0xFFE25F00),
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(10.dp)
            )
        }
    }

    @Composable
    fun CanvasScreen(currentColor: Color, onColorButton: () -> Unit, onMenuButton: () -> Unit, viewModel: DrawingViewModel){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xff101010))
                .border(4.dp, Color(0xffe25f00))
        ){
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(2.dp)
                    .background(Color(0xff161616))
                    .border(4.dp, Color(0xffe25f00))
            ){
                Button(
                    onClick = {onColorButton()},
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = viewModel.currentColor,
                    ),
                    modifier = Modifier
                        .padding(12.dp)
                        .border(2.dp, Color(0xffe25f00), shape = CircleShape)
                        .size(64.dp, 64.dp)
                ){}
                LazyRow(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .size(228.dp, 90.dp)
                ){
                    items(viewModel.colorHistory.asReversed()){ currentColor ->
                        ColorHistoryButton(currentColor, viewModel)
                    }
                }
                Button(
                    onClick = {onMenuButton()},
                    contentPadding = PaddingValues(4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xff101010),
                    ),
                    modifier = Modifier
                        .padding(12.dp)
                        .size(64.dp, 64.dp)
                        .offset(0.dp, 0.dp)
                        .border(2.dp, Color(0xffe25f00), shape = CircleShape)
                        .testTag("HamburgerButton")
                ){
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = null,
                        tint = Color(0xffe25f00),
                        modifier = Modifier
                            .size(64.dp)
                    )
                }
            }

            Canvas(
                modifier = Modifier
                    .testTag("DrawingCanvas")
                    .padding(20.dp)
                    .size(360.dp)
                    .background(Color.White)
                    .border(3.dp, Color(0xffe25f00))
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            viewModel.editDrawing(change, dragAmount)
                        }
                    },
            ){
                drawImage(image = viewModel.currentBitmap.asImageBitmap(), topLeft = Offset.Zero)
            }

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(4.dp, Color(0xffe25f00))
            ){
                Column(
                    modifier = Modifier
                        .padding(8.dp)
                ){
                    FixedTextButton("Pen", 1, {viewModel.setTool(1)}, viewModel)
                    FixedTextButton("Shape", -1, {viewModel.setMenuState(1)}, viewModel)
                }
                Column(
                    modifier = Modifier
                        .padding(8.dp)
                ){
                    FixedTextButton("Erase", 2, {viewModel.setTool(2)}, viewModel)
                    FixedTextButton("Size", -1, {viewModel.setMenuState(2)}, viewModel)
                }
                Column(
                    modifier = Modifier
                        .padding(8.dp)
                ){
                    FixedTextButton("Line", 3, {viewModel.setTool(3)}, viewModel)
                    FixedTextButton("Fill",4, {viewModel.setTool(4)}, viewModel)
                }
            }

            if (viewModel.canvasMenuState == 1){
                ShapeSelectionMenu(viewModel)
            }
            if (viewModel.canvasMenuState == 2){
                SizeSelectionMenu(viewModel)
            }

        }
    }

    @Composable
    fun IconButton(icon: ImageVector, id: Int, onClickCall: () -> Unit){
        Button(
            onClick = {onClickCall()},
            contentPadding = PaddingValues(4.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color(0xff101010),
            ),
            modifier = Modifier
                .padding(12.dp)
                .size(84.dp, 64.dp)
                .border(2.dp, Color(0xffe25f00), shape = CircleShape)
        ){
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xffe25f00),
                modifier = Modifier
                    .size(64.dp)
            )
        }
    }

    @Composable
    fun FixedTextButton(buttonText: String, id: Int, onClickCall: () -> Unit, viewModel: DrawingViewModel){
        if (id == viewModel.currentTool){
            OutlinedButton(
                //On click event
                onClick = {onClickCall()},
                contentPadding = PaddingValues(0.dp),
                //Inner Color
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color(0xff303030),
                ),
                //Border Color
                border = BorderStroke(2.dp, Color(0xffE25F00)),
                modifier = Modifier
                    .padding(12.dp)
                    .size(84.dp, 64.dp)
            ){
                Text(
                    text = buttonText,
                    color = Color(0xffE25F00),
                )
            }
        }
        else{
            OutlinedButton(
                //On click event
                onClick = {onClickCall()},
                contentPadding = PaddingValues(0.dp),
                //Inner Color
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color(0xff101010),
                ),
                //Border Color
                border = BorderStroke(2.dp, Color(0xffE25F00)),
                modifier = Modifier
                    .padding(12.dp)
                    .size(84.dp, 64.dp)
            ){
                Text(
                    text = buttonText,
                    color = Color(0xffE25F00),
                )
            }
        }
    }

    @Composable
    fun ThinFixedTextButton(buttonText: String, id: Int, onClickCall: () -> Unit, viewModel: DrawingViewModel){
        if (id == viewModel.currentTool){
            OutlinedButton(
                //On click event
                onClick = {onClickCall()},
                contentPadding = PaddingValues(0.dp),
                //Inner Color
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color(0xff303030),
                ),
                //Border Color
                border = BorderStroke(2.dp, Color(0xffE25F00)),
                modifier = Modifier
                    .padding(12.dp)
                    .size(84.dp, 64.dp)
            ){
                Text(
                    text = buttonText,
                    color = Color(0xffE25F00),
                )
            }
        }
        else{
            OutlinedButton(
                //On click event
                onClick = {onClickCall()},
                contentPadding = PaddingValues(0.dp),
                //Inner Color
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color(0xff101010),
                ),
                //Border Color
                border = BorderStroke(2.dp, Color(0xffE25F00)),
                modifier = Modifier
                    .padding(8.dp, 12.dp)
                    .size(40.dp, 64.dp)
            ){
                Text(
                    text = buttonText,
                    color = Color(0xffE25F00),
                )
            }
        }
    }

    @Composable
    fun CanvasMenuPopup(onShare: () -> Unit, onSave: () -> Unit, onClose: () -> Unit){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(Color(0xff101010), RoundedCornerShape(24.dp))
                .border(4.dp, Color(0xffe25f00), RoundedCornerShape(24.dp))
                .fillMaxWidth()
                .padding(8.dp)
        ){
            TextButton("Share", onShare)
            TextButton("Save & Return", onSave)
            TextButton("Close Menu", onClose)
        }
    }

    @Composable
    fun CanvasColorPickerPopup(viewModel: DrawingViewModel){
        val controller = rememberColorPickerController()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(30.dp)
                .background(Color(0xff101010))
                .border(4.dp, Color(0xffe25f00))
        ){
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ){
                AlphaTile(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                        .height(60.dp)
                        .border(2.dp, Color(0xffe25f00), shape = RoundedCornerShape(4.dp))
                        .clip(RoundedCornerShape(6.dp)),
                    controller = controller
                )
            }
            HsvColorPicker(
                modifier = Modifier
                    .height(350.dp)
                    .padding(10.dp)
                    .border(4.dp, Color(0xffe25f00), shape = CircleShape)
                    .fillMaxWidth(),
                controller = controller,
                onColorChanged = {}
            )
            AlphaSlider(
                modifier = Modifier
                    .padding(10.dp)
                    .height(35.dp)
                    .border(2.dp, Color(0xffe25f00), shape = RoundedCornerShape(4.dp))
                    .fillMaxWidth(),
                controller = controller,
                tileOddColor = Color.White,
                tileEvenColor = Color.Gray
            )
            BrightnessSlider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .border(2.dp, Color(0xffe25f00), shape = RoundedCornerShape(4.dp))
                    .height(35.dp),
                controller = controller
            )
            TextButton("Confirm Color", {viewModel.closeCanvasColorSelectorPopup(controller)})
        }

    }

    @Composable
    fun ShapeSelectionMenu(viewModel: DrawingViewModel){
        Row(){
            FixedTextButton("Square", -1, {viewModel.setStrokeShape(1)}, viewModel)
            FixedTextButton("Circle", -1, {viewModel.setStrokeShape(0)}, viewModel)
        }
    }

    @Composable
    fun SizeSelectionMenu(viewModel: DrawingViewModel){
        Row(){
            ThinFixedTextButton("5", -1, {viewModel.setStrokeWidth(5f)}, viewModel)
            ThinFixedTextButton("10", -1, {viewModel.setStrokeWidth(10f)}, viewModel)
            ThinFixedTextButton("15", -1, {viewModel.setStrokeWidth(15f)}, viewModel)
            ThinFixedTextButton("20", -1, {viewModel.setStrokeWidth(20f)}, viewModel)
            ThinFixedTextButton("30", -1, {viewModel.setStrokeWidth(30f)}, viewModel)
            ThinFixedTextButton("40", -1, {viewModel.setStrokeWidth(40f)}, viewModel)
            ThinFixedTextButton("50", -1, {viewModel.setStrokeWidth(50f)}, viewModel)
        }
    }

    @Composable
    fun ColorHistoryButton(color: Color, viewModel: DrawingViewModel){
        Button(
            onClick = {viewModel.setColor(color)},
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = color
            ),
            modifier = Modifier
                .padding(4.dp)
                .size(48.dp, 48.dp)
                .border(2.dp, Color(0xffe25f00), shape = CircleShape)

        ){}
    }

}