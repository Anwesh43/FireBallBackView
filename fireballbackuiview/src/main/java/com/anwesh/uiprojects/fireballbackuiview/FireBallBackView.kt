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
val strokeFactor : Float = 2.9f
val sizeFactor : Float = 0.02f
val scGap : Float = 0.02f
val squareColor : Int = Color.parseColor("#3F51B5")
val ballColor : Int = Color.parseColor("#673AB7")
val delay : Long = 20
val backColor : Int = Color.parseColor("#BDBDBD")
val rFactor : Float = 4.8f


fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.sin(this * Math.PI).toFloat()
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawFireBallBack(scale : Float, size : Float, w : Float, paint : Paint) {
    val sf : Float = scale.sinify()
    val sf1 : Float = sf.divideScale(0, 2)
    val sf2 : Float = sf.divideScale(1, 2)
    val r : Float = size / rFactor
    paint.style = Paint.Style.FILL
    paint.color = squareColor
    drawRect(RectF(-size, -size + size * sf1, size, size - size * sf1), paint)
    paint.style = Paint.Style.STROKE
    drawRect(RectF(-size, -size, size, size), paint)
    paint.style = Paint.Style.FILL
    paint.color = ballColor
    drawCircle(size + (w - 2 * size) * sf2, 0f, r, paint)
}

fun Canvas.drawFBBNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = h / (nodes + 1)
    val size : Float = gap / sizeFactor
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    paint.strokeCap = Paint.Cap.ROUND
    save()
    translate(0f, gap * (i + 1))
    drawFireBallBack(scale, size, w, paint)
    restore()
}

class FireBallBackView(ctx : Context) : View(ctx) {

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class FBBNode(var i : Int, val state : State = State()) {

        private var next : FBBNode? = null
        private var prev : FBBNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = FBBNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawFBBNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : FBBNode {
            var curr : FBBNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class FireBallBack(var i : Int, val state : State = State()) {

        private val root : FBBNode = FBBNode(0)
        private var curr : FBBNode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : FireBallBackView) {

        private val animator : Animator = Animator(view)
        private val fbb : FireBallBack = FireBallBack(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(backColor)
            fbb.draw(canvas, paint)
            animator.animate {
                fbb.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            fbb.startUpdating {
                animator.start()
            }
        }
    }
}