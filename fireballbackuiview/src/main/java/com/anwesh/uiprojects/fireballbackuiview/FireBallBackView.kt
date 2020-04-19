package com.anwesh.uiprojects.fireballbackuiview

/**
 * Created by anweshmishra on 20/04/20.
 */

import android.view.View
import android.view.MotionEvent
import android.content.Context
import android.app.Activity
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF

val nodes : Int = 5
val lines : Int = 4
val strokeFactor : Float = 2.9f
val sizeFactor : Float = 0.02f
val scGap : Float = 0.02f
val squareColor : Int = Color.parseColor("#3F51B5")
val ballColor : Int = Color.parseColor("#673AB7")
val delay : Long = 20
val backColor : Int = Color.parseColor("#BDBDBD")


fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.sin(this * Math.PI).toFloat()
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()
