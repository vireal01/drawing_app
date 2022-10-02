package com.example.drawingapp

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorLong
import androidx.core.view.get
import androidx.core.view.size
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener


class MainActivity : AppCompatActivity() {

    private var drawingView: DrawingView? = null
    private var changeBrushSizeBtn: ImageButton? = null
    private var mImageButtonCurrentPaint: ImageButton? = null
    private var linearLayoutPaintColors: LinearLayout? = null
    private var chosenColorIB: ImageButton? = null
    private var setBackgroundImage: ImageButton? = null
    private var undoBtn: ImageButton? = null
    private var redoBtn: ImageButton? = null

    private val openGalleryLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result ->
        if( result.resultCode == RESULT_OK && result.data != null){
            val imageBackground: ImageView = findViewById(R.id.iv_background)
            imageBackground.setImageURI(result.data?.data)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        drawingView = findViewById(R.id.drawingView)
        drawingView?.setSizeOfBrush(20.toFloat())
        chosenColorIB = findViewById(R.id.ib_selected_color)
        linearLayoutPaintColors = findViewById(R.id.ll_paint_colors)
        undoBtn = findViewById(R.id.ib_undo)
        redoBtn = findViewById(R.id.ib_redo)

        mImageButtonCurrentPaint = linearLayoutPaintColors!![0] as ImageButton
        mImageButtonCurrentPaint!!.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.pallet_selected)
        )
        changeBrushSizeBtn = findViewById(R.id.ib_change_brush_size)
        changeBrushSizeBtn?.setOnClickListener{
            showBrushSizeChooserDialog()
        }
        setBackgroundImage = findViewById(R.id.ib_set_background_image)
        setBackgroundImage?.setOnClickListener{
            requestStoragePermission()
        }
        undoBtn?.setOnClickListener{ drawingView?.undoMPath() }
        redoBtn?.setOnClickListener{ drawingView?.redoMPath() }
    }

    private fun showBrushSizeChooserDialog(){
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)
        brushDialog.setTitle("Brush size: ")
        val smallBtn: ImageButton = brushDialog.findViewById(R.id.ib_small_brush)
        smallBtn.setOnClickListener{
            drawingView?.setSizeOfBrush(10.toFloat())
            brushDialog.dismiss()
        }
        val mediumBtn = brushDialog.findViewById<ImageButton>(R.id.ib_medium_brush)
        mediumBtn.setOnClickListener{
            drawingView?.setSizeOfBrush(20.toFloat())
            brushDialog.dismiss()
        }

        val largeBtn = brushDialog.findViewById<ImageButton>(R.id.ib_large_brush)
        largeBtn.setOnClickListener{
            drawingView?.setSizeOfBrush(30.toFloat())
            brushDialog.dismiss()
        }

        brushDialog.show()
    }

    fun selectColor(view: View) {
        println(view.toString())
        if(view !== mImageButtonCurrentPaint){
            val imageButton = view as ImageButton
            val colorTag = imageButton.tag.toString()
            val colorTagInt = Color.parseColor(colorTag)
            println(colorTag.replace("#", ""))
            chosenColorIB!!.setBackgroundColor(colorTagInt)
            drawingView?.setColor(colorTag)
            for ( i in 0 until  linearLayoutPaintColors!!.size) {
                val imgBtn = linearLayoutPaintColors!![i] as ImageButton
                imgBtn.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.pallet_normal)
                )
            }
            imageButton.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.pallet_selected)
            )
            mImageButtonCurrentPaint = imageButton
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun toggleColorPicker(view: View) {
        ColorPickerDialog.Builder(this)
            .setTitle("Pick a color")
            .setPreferenceName("MyColorPickerDialog")
            .setPositiveButton("Confirm",
                ColorEnvelopeListener { envelope, _ ->
                    val newColor = Integer.toHexString(Color.toArgb(envelope.color.toColorLong()))
                    drawingView?.setColor("#".plus(newColor))
                    chosenColorIB!!.setBackgroundColor(envelope.color)
                })
            .setNegativeButton("Cancel") { dialogInterface, _ -> dialogInterface.dismiss() }
            .attachAlphaSlideBar(false)
            .attachBrightnessSlideBar(true)
            .setBottomSpace(12)
            .show()
    }

    private val requestPermissions: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            permissions -> permissions.entries.forEach{
                val permissionName = it.key
                val isGranted = it.value
                if(isGranted){
                    Toast.makeText(applicationContext,"${it.key} Permission granted", Toast.LENGTH_LONG).show()
                    val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    openGalleryLauncher.launch(pickIntent)
                } else {
                    if(permissionName == Manifest.permission.READ_MEDIA_IMAGES) {
                        Toast.makeText(applicationContext,"${it.key} Permission denied", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

    private fun requestStoragePermission() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_IMAGES)) {
                showRationaleDialog("Drawing app", "The app needs to Access Your External Storage")
            } else {
                requestPermissions.launch(arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES
                ))
            }
    }

    private fun showRationaleDialog(title: String, message: String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("Ok") { dialog, _ ->
                dialog.dismiss()
            }
        builder.create().show()
    }
}