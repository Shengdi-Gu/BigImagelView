package com.example.bigpictureload

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.Scroller
import java.io.InputStream
import kotlin.math.min


/**
 * @Author gusd
 * @Date 2021/1/26
 * @Description
 */
private const val TAG = "BigImageView"

class BigImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), GestureDetector.OnGestureListener, View.OnTouchListener,
    GestureDetector.OnDoubleTapListener, ScaleGestureDetector.OnScaleGestureListener {
    private var mScaleGestureDetector: ScaleGestureDetector
    private var originalScale: Float = 0F
    private var mBitmap: Bitmap? = null
    private var mScale: Float = 0f
    private var tempScale: Float = 0F
    private var mViewHeight: Int = 0
    private var mViewWidth: Int = 0
    private lateinit var mDecoder: BitmapRegionDecoder
    private var mImageWidth: Int = 0
    private var mImageHeigh: Int = 0
    private val mRect = Rect()
    private val mOptions = BitmapFactory.Options()
    private val mGestureDetector = GestureDetector(context, this)
    private val mScroller = Scroller(context)

    init {
        setOnTouchListener(this)

        mScaleGestureDetector = ScaleGestureDetector(context, this)
    }


    public fun setImage(imageIs: InputStream) {
        mOptions.inJustDecodeBounds = true
        BitmapFactory.decodeStream(imageIs,null,mOptions)
        mImageWidth = mOptions.outWidth
        mImageHeigh = mOptions.outHeight
        mOptions.inJustDecodeBounds = false
        //开启复用
        mOptions.inMutable = true
        //设置图片格式
        mOptions.inPreferredConfig = Bitmap.Config.RGB_565

        try {
            mDecoder = BitmapRegionDecoder.newInstance(imageIs, false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mViewWidth = measuredWidth
        mViewHeight = measuredHeight

        //加入缩放因子
        mRect.top = 0
        mRect.left = 0
        mRect.right = min(mImageWidth, mViewHeight)
        mRect.bottom = min(mImageHeigh, mViewHeight)

        originalScale = (mViewWidth / mImageWidth).toFloat()
        mScale = originalScale

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        mOptions.inBitmap = mBitmap
        mBitmap = mDecoder.decodeRegion(mRect, mOptions)
        var matrix = Matrix()

        tempScale = mViewHeight / mRect.width().toFloat()
        matrix.setScale(tempScale, tempScale)
        mBitmap?.let {
            canvas?.drawBitmap(it, matrix, null)
        }
    }

    override fun onDown(e: MotionEvent?): Boolean {
        if (!mScroller.isFinished) {
            mScroller.forceFinished(true)
        }
        return true
    }

    override fun onShowPress(e: MotionEvent?) {
    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        return true
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        mRect.offset(distanceX.toInt(), distanceY.toInt())
        if (mRect.bottom > mImageHeigh) {
            mRect.bottom = mImageHeigh
            mRect.top = mImageHeigh - (mViewHeight / mScale).toInt()
        }

        if (mRect.top < 0) {
            mRect.top = 0
            mRect.bottom = (mViewHeight / mScale).toInt()
        }

        if (mRect.right > mImageWidth) {
            mRect.right = mImageWidth
            mRect.left = mImageWidth - (mViewWidth / mScale).toInt()
        }

        if (mRect.left < 0) {
            mRect.left = 0
            mRect.right = (mViewWidth / mScale).toInt()
        }
        invalidate()
        return false
    }

    override fun onLongPress(e: MotionEvent?) {
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        mScroller.fling(
            mRect.left,
            mRect.top,
            velocityX.toInt(),
            -velocityY.toInt(),
            0,
            mImageWidth - (mViewHeight / mScale).toInt(),
            0,
            mImageHeigh - (mViewHeight / mScale).toInt()
        )
        return false
    }

    override fun computeScroll() {
        super.computeScroll()
        if (mScroller.isFinished) {
            return
        }
        if (mScroller.computeScrollOffset()) {
            mRect.top = mScroller.currX
            mRect.bottom = mRect.top + (mViewHeight / mScale).toInt()
            invalidate()
        }
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        mGestureDetector.onTouchEvent(event)
        mScaleGestureDetector.onTouchEvent(event)
        return true
    }

    override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
        return false
    }

    override fun onDoubleTap(e: MotionEvent?): Boolean {
        //双击放大图片
        mScale = if (mScale < originalScale * 2) {
            originalScale * 2
        } else {
            originalScale
        }
        mRect.right = mRect.left + (mViewWidth / mScale).toInt()
        mRect.bottom = mRect.top + (mViewHeight / mScale).toInt()


        if (mRect.bottom > mImageHeigh) {
            mRect.bottom = mImageHeigh
            mRect.top = mImageHeigh - (mViewHeight / mScale).toInt()
        }

        if (mRect.top < 0) {
            mRect.top = 0
            mRect.bottom = (mViewHeight / mScale).toInt()
        }

        if (mRect.right > mImageWidth) {
            mRect.right = mImageWidth
            mRect.left = mImageWidth - (mViewWidth / mScale).toInt()
        }

        if (mRect.left < 0) {
            mRect.left = 0
            mRect.right = (mViewWidth / mScale).toInt()
        }
        invalidate()
        return false
    }

    override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
        return false
    }

    override fun onScale(detector: ScaleGestureDetector?): Boolean {
        var scale = mScale
        scale += (detector?.scaleFactor ?: mScale - 1)
        if (scale <= originalScale) {
            scale = originalScale
        } else if (scale > originalScale * 2) {
            scale = (originalScale * 2)
        }

        mRect.right = mRect.left + (mViewWidth / scale).toInt()
        mRect.bottom = mRect.bottom + (mViewHeight / scale).toInt()
        mScale = scale
        invalidate()
        return false
    }

    override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector?) {
    }

}