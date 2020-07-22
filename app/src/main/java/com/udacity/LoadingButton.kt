package com.udacity


import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.annotation.StringRes
import java.util.*
import kotlin.properties.Delegates


class LoadingButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val loadingRectangle = Rect()
    private lateinit var progressCircle: ValueAnimator

    private var status = 0
    private var loadingState = 0
    private val cornerRadius = 4.dpToPx().toFloat()

    private var defaultTextColor = Color.WHITE
    private var primaryBackgroundColor = context.getColor(R.color.colorPrimary)
    private var secondaryBackgroundColor = context.getColor(R.color.colorPrimaryDark)

    private val cornerPath = Path()
    private val progressRectangle = RectF()
    private var progressCircleColor = context.getColor(R.color.colorAccent)


    enum class State(@StringRes private val textId: Int) {
        LOADING(R.string.downloading),
        COMPLETE(R.string.download);

        fun getTextId(): Int {
            return textId
        }
    }

    private var btnState: State by Delegates.observable(State.COMPLETE) { _, old, new ->
        textDisplayed = context.getString(new.getTextId()).toUpperCase(Locale.ENGLISH)

        when (btnState) {
            State.COMPLETE -> {
                progressCircle.cancel()
                Toast.makeText(context, "Download Completed!", Toast.LENGTH_SHORT).show()
            }
            State.LOADING -> {
                if (old != State.LOADING) {
                    progressCircle = ValueAnimator.ofInt(0, 360).setDuration(1000).apply {
                        addUpdateListener {
                            status = it.animatedValue as Int
                            invalidate()
                        }
                        addListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator?) {
                                super.onAnimationEnd(animation)
                                this@LoadingButton.btnState = State.COMPLETE
                            }

                            override fun onAnimationCancel(animation: Animator?) {
                                super.onAnimationCancel(animation)
                                status = 0
                                loadingState = 0
                            }

                            override fun onAnimationRepeat(animation: Animator?) {
                                super.onAnimationRepeat(animation)
                                loadingState = loadingState xor 1
                            }
                        })
                        interpolator = LinearInterpolator()
                        repeatCount = ValueAnimator.INFINITE
                        repeatMode = ValueAnimator.RESTART
                        start()
                    }
                }
            }
        }

        requestLayout()
        invalidate()
    }


    init {
        setPadding(32.dpToPx(), 16.dpToPx(), 32.dpToPx(), 16.dpToPx())

        val typedArray = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.LoadingButton,
            defStyleAttr,
            0
        )

        with(typedArray) {
            defaultTextColor = getColor(
                R.styleable.LoadingButton_textColor,
                defaultTextColor
            )
            primaryBackgroundColor = getColor(
                R.styleable.LoadingButton_primaryBackgroundColor,
                primaryBackgroundColor
            )
            secondaryBackgroundColor = getColor(
                R.styleable.LoadingButton_secondaryBackgroundColor,
                secondaryBackgroundColor
            )
            progressCircleColor = getColor(
                R.styleable.LoadingButton_circularProgressColor,
                progressCircleColor
            )
        }

        typedArray.recycle()
    }

    private val textRectangle = Rect()
    private var textDisplayed = context.getString(btnState.getTextId()).toUpperCase(Locale.ENGLISH)
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            it.save()
            it.clipPath(cornerPath)
            it.drawColor(primaryBackgroundColor)

            paint.getTextBounds(textDisplayed, 0, textDisplayed.length, textRectangle)
            val textX = width / 2f - textRectangle.width() / 2f
            val textY = height / 2f + textRectangle.height() / 2f - textRectangle.bottom

            var textOffset = 0
            if (btnState == State.LOADING) {
                paint.color = secondaryBackgroundColor
                if (loadingState == 0) {
                    loadingRectangle.set(0, 0, width * status / 360, height)
                } else {
                    loadingRectangle.set(width * status / 360, 0, width, height)
                }
                it.drawRect(loadingRectangle, paint)

                paint.style = Paint.Style.FILL
                paint.color = progressCircleColor
                val circleStartX = width / 2f + textRectangle.width() / 2f
                val circleStartY = height / 2f - 20
                progressRectangle.set(circleStartX, circleStartY, circleStartX + 40, circleStartY + 40)
                if (loadingState == 0) {
                    it.drawArc(progressRectangle, 0f, status.toFloat(), true, paint)
                } else {
                    it.drawArc(
                        progressRectangle,
                        status.toFloat(),
                        360f - status.toFloat(),
                        true,
                        paint
                    )
                }
                textOffset = 35
            }

            paint.color = defaultTextColor
            it.drawText(textDisplayed, textX - textOffset, textY, paint)
            it.restore()
        }
    }

    private val paint = Paint().apply {
        isAntiAlias = true
        textAlignment = TEXT_ALIGNMENT_CENTER
        textSize = 16.spToPx().toFloat()
        typeface = Typeface.DEFAULT_BOLD
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minW = paddingLeft + paddingRight + suggestedMinimumWidth
        val minH = paddingTop + paddingBottom + suggestedMinimumHeight
        val w = resolveSizeAndState(minW, widthMeasureSpec, 1)
        val h = resolveSizeAndState(minH, heightMeasureSpec, 1)
        setMeasuredDimension(w, h)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        cornerPath.reset()
        cornerPath.addRoundRect(
            0f,
            0f,
            w.toFloat(),
            h.toFloat(),
            cornerRadius,
            cornerRadius,
            Path.Direction.CW
        )
        cornerPath.close()
    }

    override fun getSuggestedMinimumWidth(): Int {
        paint.getTextBounds(textDisplayed, 0, textDisplayed.length, textRectangle)
        return textRectangle.width() - textRectangle.left + if (btnState == State.LOADING) 70 else 0
    }

    override fun getSuggestedMinimumHeight(): Int {
        paint.getTextBounds(textDisplayed, 0, textDisplayed.length, textRectangle)
        return textRectangle.height()
    }

    fun setState(buttonState: State) {
        this.btnState = buttonState
    }
}

fun Number.dpToPx(): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    ).toInt()
}


fun Number.spToPx(): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    ).toInt()
}
