package com.example.a418drawingapp

//The sheer amount of imports is a little nuts
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {

    private val customComposable = CustomComposables()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel:DrawingViewModel by viewModels{
            DrawingViewModelFactory((application as DrawingObjectApplication).drawingObjectRepository)
        }

        viewModel.passApplicationRef(application as DrawingObjectApplication)
        setContent {
            Screens(viewModel, customComposable)
        }
    }
}

//Controls the logic of which screen to display
@Composable
fun Screens(viewModel: DrawingViewModel, customComposable: CustomComposables){

    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "Thumbnail") {
        composable("Thumbnail") {
            customComposable.Thumbnail(
                onClickCall = {
                    viewModel.switchToMenuScreen()
                    navController.navigate("MenuScreen")
                }
            )
        }

        composable("MenuScreen") {
            customComposable.MenuScreen(
                onClickCall = {
                    viewModel.openNewDrawingPopup()
                },
                viewModel = viewModel
            )
        }

        //Not working bc of DrawingItem onClick being in ViewModel
        composable("InfoPopUp") {
            customComposable.DrawingInfoPopup(
                onClose = {
                    viewModel.closeDrawingInfoPopup()
                },
                onOpen = {
                    viewModel.switchToDrawScreen()
                    navController.navigate("DrawScreen")
                },
                onShare = { /*TODO*/ },
                onDelete = {
                    viewModel.deleteDrawing()
                },
                viewModel = viewModel
            )
        }

        composable("DrawScreen") {
            customComposable.CanvasScreen(
                currentColor = Color.White,
                onColorButton = {
                    viewModel.openCanvasColorSelectorPopup()
                },
                onMenuButton = {
                    viewModel.openCanvasMenuPopup()
                },
                viewModel = viewModel
            )
        }
    }

    /***
     * PLEASE READ!!! These if-statements are NOT Jetpack Navigation compliant obviously,
     * but we are using them to simulate temporary popups instead of replacing the
     * entire screen. TLDR this is intentionally not Jetpack Navigation
     */

    //New Drawing Popup
    if (viewModel.popUpState == 1){
        customComposable.NewPopup(
            onCreate = {
                viewModel.createNewDrawing()
            },
            onCancel = {
                viewModel.closeNewDrawingPopup()
            },
            viewModel = viewModel)
    }

    //Drawing Info
    if (viewModel.popUpState == 2){
        customComposable.DrawingInfoPopup(
            onClose = {
                viewModel.closeDrawingInfoPopup()
            },
            onOpen = {
                viewModel.switchToDrawScreen()
                navController.navigate("DrawScreen")
            },
            onShare = {

            },
            onDelete = {
                viewModel.deleteDrawing()
            },
            viewModel = viewModel
        )
    }

    if (viewModel.popUpState == 4){
        customComposable.CanvasColorPickerPopup(
            viewModel = viewModel
        )
    }

    //Draw Screen
    if (viewModel.appState == 2){
        customComposable.CanvasScreen(
            currentColor = Color.White,
            onColorButton = {
                viewModel.openCanvasColorSelectorPopup()
            },
            onMenuButton = {
                viewModel.openCanvasMenuPopup()
            },
            viewModel = viewModel)
    }

    //Canvas Menu
    if (viewModel.popUpState == 3){
        customComposable.CanvasMenuPopup(
            onShare = {

            },
            onSave = {
                viewModel.switchToMenuScreen()
                navController.navigate("MenuScreen")
            },
            onClose = {
                viewModel.closeCanvasMenuPopup()
            }
        )
    }
    if (viewModel.popUpState == 4){
        customComposable.CanvasColorPickerPopup(
            viewModel = viewModel
        )
    }

    Log.e("VIEWMODEL VARS", "PopUp state: " + viewModel.popUpState)

/*
    //Thumbnail
    if (viewModel.appState == 0) {
        customComposable.Thumbnail() {
            viewModel.switchToMenuScreen()
        }
    }
    //Menu
    if (viewModel.appState == 1) {
        customComposable.MenuScreen({viewModel.openNewDrawingPopup()}, viewModel)
    }
    //Draw Screen
    if (viewModel.appState == 2){
        customComposable.CanvasScreen(Color.White,{viewModel.openCanvasColorSelectorPopup()}, {viewModel.openCanvasMenuPopup()}, viewModel)
    }

    //Drawing Info
    if (viewModel.popupState == 2){
        customComposable.DrawingInfoPopup(
            {viewModel.closeDrawingInfoPopup()},
            {viewModel.switchToDrawScreen()},
            {},
            {viewModel.deleteDrawing()},
            viewModel
        )
    }
    //Canvas Menu
    if (viewModel.popupState == 3){
        customComposable.CanvasMenuPopup(
            {},
            {viewModel.switchToMenuScreen()},
            {viewModel.closeCanvasMenuPopup()}
        )
    }

    if (viewModel.popupState == 4){
        customComposable.CanvasColorPickerPopup(viewModel)
    }
    */

}