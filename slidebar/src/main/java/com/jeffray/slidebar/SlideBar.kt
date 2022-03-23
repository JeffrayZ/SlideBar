package com.jeffray.slidebar

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.ssa.buddy.view.slidebar.Content

open class SlideBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val mPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
    private val data = mutableListOf<Content>()

    // 每一个字母的宽度
    private var itemWidth = 0f

    // 每一个字母的高度
    private var itemHeight = 0f

    // 当前选择的提示宽度
    private var tipWidth = 0f

    // 当前选择的提示高度
    private var tipHeight = 0f
    var itemNormalTextColor = Color.BLACK
        set(value) {
            field = value
            invalidate()
        }
    var itemTouchedTextColor = Color.RED
        set(value) {
            field = value
            invalidate()
        }
    private var touchIndex: Int = -1
    private var callBack: ((Int, String) -> Unit)? = null
    private var bgTipDrawableId: Int = -1
    var tipTextColor = Color.WHITE
        set(value) {
            field = value
            invalidate()
        }
    var tipTextSize = 20f
        set(value) {
            field = value
            invalidate()
        }
    var itemNormalTextSize = 12f
        set(value) {
            field = value
            invalidate()
        }
    var itemTouchedTextSize = 16f
        set(value) {
            field = value
            invalidate()
        }
    private var fontMetrics = mPaint.fontMetrics
    private var textWidth: Float = 0f
    private var textHeight: Float = 0f

    // 当前绘制的选择提示背景
    private var currentBitmap: Bitmap? = null

    // 用于绘制选择提示的区域
    val dstRectF = RectF(0f, 0f, 0f, 0f)

    init {
        val ta =
            context.obtainStyledAttributes(attrs, R.styleable.SlideBar, defStyleAttr, 0)
        try {
            bgTipDrawableId = ta.getResourceId(R.styleable.SlideBar_tip_icon, -1)
            itemNormalTextColor = ta.getColor(
                R.styleable.SlideBar_item_normal_text_color,
                Color.parseColor("#e6000000")
            )
            itemTouchedTextColor = ta.getColor(
                R.styleable.SlideBar_item_touched_text_color,
                Color.parseColor("#EF4C4F")
            )
            itemNormalTextSize = ta.getDimension(R.styleable.SlideBar_item_normal_text_size, 12f)
            itemTouchedTextSize =
                ta.getDimension(R.styleable.SlideBar_item_touched_text_size, 16f)
            tipTextColor = ta.getColor(R.styleable.SlideBar_tip_text_color, Color.WHITE)
            tipTextSize = ta.getDimension(R.styleable.SlideBar_tip_text_size, 20f)
            tipWidth = ta.getDimension(R.styleable.SlideBar_tip_width, 0f)
            tipHeight = ta.getDimension(R.styleable.SlideBar_tip_height, 0f)
        } finally {
            ta.recycle()
        }
        if (bgTipDrawableId != -1) {
            val originalBitmap = BitmapFactory.decodeResource(resources, bgTipDrawableId)
            currentBitmap = Bitmap.createScaledBitmap(
                originalBitmap,
                tipWidth.toInt(), tipHeight.toInt(), true
            )
        }
    }

    fun setItems(items: List<String>) {
        data.clear()
        items.forEach {
            val letter = Content(it, false)
            data.add(letter)
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (data.size == 0) {
            return
        }
        // 计算每一个item的宽高
        itemWidth = width - tipWidth
        itemHeight = (height - tipHeight) / data.size
        // 遍历绘制
        data.forEachIndexed { index, item ->
            if (item.touched) {
                mPaint.textSize = itemTouchedTextSize
                mPaint.color = itemTouchedTextColor
            } else {
                mPaint.textSize = itemNormalTextSize
                mPaint.color = itemNormalTextColor
            }
            fontMetrics = mPaint.fontMetrics
            textHeight = -(fontMetrics.ascent + fontMetrics.descent)
            textWidth = mPaint.measureText(item.text.uppercase())
            val left = tipWidth
            val top = index * itemHeight + tipHeight / 2f
            val wordX: Float = (itemWidth / 2f - textWidth / 2f) + left
            val wordY: Float = itemHeight / 2f + textHeight / 2f + top
            canvas?.drawText(
                item.text.uppercase(),
                wordX,
                wordY,
                mPaint
            )
            // 绘制touche提示
            if (item.touched && currentBitmap != null) {
                mPaint.color = tipTextColor
                mPaint.textSize = tipTextSize
                fontMetrics = mPaint.fontMetrics
                textHeight = -(fontMetrics.ascent + fontMetrics.descent)
                textWidth = mPaint.measureText(item.text.uppercase())
                // 提示绘制的区域
                val bgLeft: Float = 0f
                val bgTop: Float = top + itemHeight / 2f - tipHeight / 2f
                val bgBottom: Float = bgTop + tipHeight
                val bgRight: Float = tipWidth
                dstRectF.set(bgLeft, bgTop, bgRight, bgBottom)
                // 绘制选择提示的背景
                canvas?.drawBitmap(currentBitmap!!, null, dstRectF, mPaint)
                val wordX1: Float = tipWidth / 2f - textWidth / 2f
                val wordY1: Float = tipHeight / 2f + textHeight / 2f + bgTop
                // 绘制选择提示的文字，默认是在提示背景的最中间
                canvas?.drawText(
                    item.text.uppercase(),
                    wordX1,
                    wordY1,
                    mPaint
                )
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val downX = event.x
                val downY = event.y
                val calculateTouchY = downY - tipHeight / 2f
                // 重置item的状态
                if (downX < tipWidth || calculateTouchY < 0 || downY > (height - tipHeight / 2f)) {
                    resetAll()
                    return super.onTouchEvent(event)
                }
                // 获得我们按下的是哪个item
                var index: Int = (calculateTouchY / itemHeight).toInt()
                if (index == data.size) {
                    index -= 1
                }
                if (index != touchIndex) {
                    if (touchIndex != -1) {
                        data[touchIndex].touched = false
                    }
                    touchIndex = index
                }

                data[touchIndex].touched = true
                invalidate()
                callBack?.invoke(touchIndex, data[touchIndex].text)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                resetAll()
            }
        }
        return true
    }

    fun setOnItemSelectCallback(callBack: ((Int, String) -> Unit)) {
        this.callBack = callBack
    }

    private fun resetAll() {
        data.forEach {
            it.touched = false
        }
        invalidate()
    }

    companion object {
        private const val TAG = "SlideBar"
    }
}