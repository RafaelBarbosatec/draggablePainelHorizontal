package com.example.draggerpainel

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import kotlinx.android.synthetic.main.layout_dragable.view.*


class OuterLayout(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {

    companion object {
        private const val  WIDGHT_CLOSED = 60f
        private const val  MAX_PADDING_TOP = 200f
        private const val  MAX_RADIUS_CORNER = 50f
    }

    private var _weightMeasured = 0
    private var _positionXClosed = 0f
    private var _status:StatusCreditBar = StatusCreditBar.CLOSE
    var statusListern:((StatusCreditBar)->Unit)? = null
    private var innerStatusListern:((StatusCreditBar)->Unit)? = null

    init {
        initView(context)
    }

    private fun initView(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.layout_dragable, this, true)
        this.post {
            this.confMensure()
            _weightMeasured = this.measuredWidth
            this.translationX = _weightMeasured - WIDGHT_CLOSED
            this.setPadding(0,MAX_PADDING_TOP.toInt(),0,0)
            _positionXClosed = _weightMeasured - WIDGHT_CLOSED
            view_draggable.setOnTouchListener { _, motionEvent ->
                myOnTouchEvent(motionEvent)
            }

            innerStatusListern = {
                //Obtem o status (CLOSE,OPEN,DRAGGABLE)
            }
        }
    }

    private fun myOnTouchEvent(event: MotionEvent?): Boolean {
        val xCurrentTouch = event?.rawX
        event?.let {
            when(it.action){
                MotionEvent.ACTION_MOVE -> {
                    if(xCurrentTouch?:0f < (_positionXClosed)) {
                        this.translationX = xCurrentTouch ?: 0f
                        calculateAndSetPaddingTop(xCurrentTouch?:0f)
                        changeCornerBG(xCurrentTouch?:0f)
                        setStatus(StatusCreditBar.DRAGGABLE)
                    }
                }
                MotionEvent.ACTION_UP -> {

                    if((xCurrentTouch?:0f) > _weightMeasured/2){
                        closeScreen()
                    }else{
                        openScreen()
                    }
                }
            }
        }

        return true
    }

    private fun openScreen() {
        val animation = createAnimationAxisX(0f)
        animation.interpolator = DecelerateInterpolator()
        animation.start()
    }

    private fun closeScreen() {
        val animation = createAnimationAxisX(_positionXClosed)
        animation.interpolator = LinearInterpolator()
        animation.start()
    }

    private fun createAnimationAxisX(toX: Float): ObjectAnimator {
        val anim = ObjectAnimator
            .ofFloat(
                this,
                "translationX",
                toX
            )
        anim.addUpdateListener { value ->
            calculateAndSetPaddingTop(value.animatedValue as Float)
            changeCornerBG(value.animatedValue as Float)
            when(value.animatedValue){
                0f -> setStatus(StatusCreditBar.OPEN)
                _positionXClosed -> setStatus(StatusCreditBar.CLOSE)
            }
        }
        anim.duration = 200
        return anim
    }

    private fun calculateAndSetPaddingTop(currentX:Float){
        val paddingTop =  (MAX_PADDING_TOP * currentX)/_weightMeasured
        this.setPadding(0,paddingTop.toInt(),0,0)
    }

    private fun changeCornerBG(currentX: Float) {
        val value =  (MAX_RADIUS_CORNER * currentX)/_weightMeasured
        val shape = GradientDrawable()
        shape.shape = GradientDrawable.RECTANGLE
        shape.cornerRadii = floatArrayOf(value,value,0f,0f,0f,0f,0f,0f)
        shape.setColor(Color.parseColor("#1EBBA4"))
        view_background.setBackgroundDrawable(shape)
    }

    private fun setStatus(status: StatusCreditBar) {
        if(_status != status){
            _status = status
            statusListern?.invoke(_status)
            innerStatusListern?.invoke(_status)
        }
    }

}

enum class StatusCreditBar{
    OPEN,
    DRAGGABLE,
    CLOSE
}

fun View.confMensure(){
    val matchParentMeasureSpec = View.MeasureSpec.makeMeasureSpec((this.parent as View).width, View.MeasureSpec.EXACTLY)
    val wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    this.measure(matchParentMeasureSpec, wrapContentMeasureSpec)
}