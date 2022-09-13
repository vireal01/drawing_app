package com.example.drawigapp

import android.app.Dialog
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
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
    private var choosenColorIB: ImageButton? = null

//    private var smallBtn: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawingView = findViewById(R.id.drawingView)
        drawingView?.setSizeOfBrush(20.toFloat())
        choosenColorIB = findViewById(R.id.ib_selected_color)

        linearLayoutPaintColors = findViewById<LinearLayout>(R.id.ll_paint_colors)

        mImageButtonCurrentPaint = linearLayoutPaintColors!![0] as ImageButton
        mImageButtonCurrentPaint!!.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.pallet_selected)
        )
        changeBrushSizeBtn = findViewById(R.id.ib_change_brush_size)
        changeBrushSizeBtn?.setOnClickListener{
            showBrushSizeChooserDialog()
        }
    }

    fun showBrushSizeChooserDialog(){
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
            choosenColorIB!!.setBackgroundColor(colorTagInt)
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
                    choosenColorIB!!.setBackgroundColor(envelope.color)
                })
            .setNegativeButton("Cancel") { dialogInterface, _ -> dialogInterface.dismiss() }
            .attachAlphaSlideBar(false)
            .attachBrightnessSlideBar(true)
            .setBottomSpace(12)
            .show()
    }
}