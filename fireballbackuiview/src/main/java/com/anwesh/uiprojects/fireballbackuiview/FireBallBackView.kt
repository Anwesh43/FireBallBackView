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
val strokeFactor : Float = 90f
val sizeFactor : Float = 2.9f
val parts : Int = 3
val scGap : Float = 0.02f / parts
val squareColor : Int = Color.parseColor("#3F51B5")
val ballColor : Int = Color.parseColor("#673AB7")
val delay : Long = 10
val backColor : Int = Color.parseColor("#BDBDBD")
val rFactor : Float = 4.8f


fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawFireBallBack(scale : Float, size : Float, w : Float, paint : Paint) {
    val sf : Float = scale.sinify()
    val sf2 : Float = sf.divideScale(1, parts)
    val r : Float = size / rFactor
    for (j in 0..1) {
        val sfi : Float = sf.divideScale(j * 2, parts)
        paint.color = squareColor
        save()
        translate((w - 2 * size) * j, 0f)
        paint.style = Paint.Style.FILL
        val y : Float = size * sfi * j + size * (1 - sfi) * (1 - j)
        drawRect(RectF(0f, -y, 2 * size, y), paint)
        paint.style = Paint.Style.STROKE
        drawRect(RectF(0f, -size, 2 * size, size), paint)
        restore()
    }
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

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
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
        private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun render(canvas : Canvas) {
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

    companion object {

        fun create(activity : Activity) : FireBallBackView {
            val view : FireBallBackView = FireBallBackView(activity)
            activity.setContentView(view)
            return view
        }
    }
}