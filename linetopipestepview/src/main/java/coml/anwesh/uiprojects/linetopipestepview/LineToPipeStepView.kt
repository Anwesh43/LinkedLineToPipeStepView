package coml.anwesh.uiprojects.linetopipestepview

/**
 * Created by anweshmishra on 16/12/18.
 */

import android.view.View
import android.view.MotionEvent
import android.content.Context
import android.app.Activity
import android.graphics.Color
import android.graphics.Canvas
import android.graphics.Paint

val nodes : Int = 5
val lines : Int = 4
val color : Int = Color.parseColor("#0D47A1")
val sizeFactor : Float = 2.5f
val strokeFactor : Int = 90
val scDiv : Double = 0.51
val scGap : Float = 0.05f

fun Int.getInverse() : Float = 1f / this

fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.getInverse(), Math.max(0f, this -  i * n.getInverse())) * n

fun Float.scaleFactor() : Float = Math.floor(this / scDiv).toFloat()

fun Float.mirrorValue(a : Int, b : Int) : Float = (1 - scaleFactor()) * a.getInverse() + scaleFactor() * b.getInverse()

fun Float.updateScale(dir : Float, a : Int, b : Int) : Float = dir * scGap * mirrorValue(a, b)

fun Canvas.drawLTPNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = w / (nodes + 1)
    val size : Float = gap / sizeFactor
    val sc1 : Float = scale.divideScale(0, 2)
    val sc2 : Float = scale.divideScale(1, 2)
    paint.color = color
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    paint.strokeCap = Paint.Cap.ROUND
    save()
    translate(gap * (i + 1), h/2)
    rotate(90f * sc2)
    for (j in 0..(lines - 1)) {
        val sc : Float = sc1.divideScale(j, lines)
        val ix : Float = 1f - 2 * (j / 2)
        val iy : Float = 1f - 2 * (j % 2)
        save()
        drawLine(0f, size * iy * sc, size * ix, size * iy * sc, paint)
        restore()
    }
    restore()
}

class LineToPipeStepView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var prevScale : Float = 0f, var dir : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scale.updateScale(dir, lines, 1)
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
                    Thread.sleep(50)
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

    data class LTPNode(var i : Int, val state : State = State()) {

        private var next : LTPNode? = null

        private var prev : LTPNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = LTPNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawLTPNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : LTPNode {
            var curr : LTPNode? = prev
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

    data class LineToPipeStep(var i : Int) {

        private val root : LTPNode = LTPNode(0)
        private var curr : LTPNode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : LineToPipeStepView) {

        private val animator : Animator = Animator(view)

        private val ltps : LineToPipeStep = LineToPipeStep(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#BDBDBD"))
            ltps.draw(canvas, paint)
            animator.animate {
                ltps.update {i, scl ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            ltps.startUpdating {
                animator.start()
            }
        }
    }

    companion object {
        fun create(activity: Activity) : LineToPipeStepView {
            val view : LineToPipeStepView = LineToPipeStepView(activity)
            activity.setContentView(view)
            return view
        }
    }
}