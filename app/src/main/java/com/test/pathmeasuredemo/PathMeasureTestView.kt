package com.test.pathmeasuredemo

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import kotlin.math.atan2


class PathMeasureTestView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)


    private var currentValue = 0f // 用于纪录当前的位置,取值范围[0,1]映射Path的整个长度


    private val pos: FloatArray = FloatArray(2) // 当前点的实际位置
    private val tan: FloatArray = FloatArray(2) // 当前点的tangent值,用于计算图片所需旋转的角度
    private var mBitmap: Bitmap? = null // 箭头图片
    private val mMatrix: Matrix = Matrix() // 矩阵,用于对图片进行一些操作
    private var options = BitmapFactory.Options()

    var path: Path = Path()
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 10f
        canvas?.translate((width / 2).toFloat(), (height / 2).toFloat())

//        test1(canvas)

//        test2(canvas)

//        test3(canvas)

//        test4(canvas)

//        test5(canvas)

        test6(canvas)
    }

    private fun test6(canvas: Canvas?) {
        options.inSampleSize = 2
        mBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_launcher_round, options)

        path.addCircle(0f, 0f, 200f, Path.Direction.CW) // 添加一个圆形
        val measure = PathMeasure(path, false) // 创建 PathMeasure
        currentValue += 0.005f // 计算当前的位置在总长度上的比例[0,1]
        if (currentValue >= 1) {
            currentValue = 0f
        }
        // 获取当前位置的坐标以及趋势的矩阵
        measure.getMatrix(
            measure.length * currentValue,
            mMatrix,
            PathMeasure.TANGENT_MATRIX_FLAG or PathMeasure.POSITION_MATRIX_FLAG
        )

        mMatrix.preTranslate(-mBitmap?.width!! / 2f, -mBitmap?.height!! / 2f)   // 将图片绘制中心调整到与当前点重合(注意:此处是前乘pre)

        canvas?.drawPath(path, paint)
        canvas?.drawBitmap(mBitmap!!, mMatrix, paint) // 绘制箭头
        invalidate()
    }

    private fun test5(canvas: Canvas?) {
        options.inSampleSize = 2
        mBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_launcher_round, options)

        path.addCircle(0f, 0f, 200f, Path.Direction.CW) // 添加一个圆形
        val measure = PathMeasure(path, false) // 创建 PathMeasure
        currentValue += 0.005f // 计算当前的位置在总长度上的比例[0,1]
        if (currentValue >= 1) {
            currentValue = 0f
        }
        measure.getPosTan(measure.length * currentValue, pos, tan) // 获取当前位置的坐标以及趋势

        mMatrix.reset()
        val degrees = (atan2(
            tan[1].toDouble(),
            tan[0].toDouble()
        ) * 180.0 / Math.PI).toFloat() // 计算图片旋转角度
        mMatrix.postRotate(
            degrees,
            mBitmap?.width?.div(2f) ?: 0f,
            mBitmap?.height?.div(2f) ?: 0f
        ) // 旋转图片
        mMatrix.postTranslate(
            pos[0] - mBitmap?.width?.div(2)!!,
            pos[1] - mBitmap?.height?.div(2)!!
        )  // 将图片绘制中心调整到与当前点重合

        canvas?.drawPath(path, paint)
        canvas?.drawBitmap(mBitmap!!, mMatrix, paint) // 绘制箭头
        invalidate()
    }

    private fun test4(canvas: Canvas?) {
        path.addRect(-100f, -100f, 100f, 100f, Path.Direction.CW) // 添加小矩形
        canvas?.drawPath(path, paint) // 绘制 Path
        path.addRect(-200f, -200f, 200f, 200f, Path.Direction.CW) // 添加大矩形
        canvas?.drawPath(path, paint) // 绘制 Path
        val measure = PathMeasure(path, false) // 将Path与PathMeasure关联
        val len1 = measure.length // 获得第一条路径的长度
        measure.nextContour() // 跳转到下一条路径
        val len2 = measure.length // 获得第二条路径的长度
        Log.i("PathMeasureTestView", "len1=$len1") // 输出两条路径的长度
        Log.i("PathMeasureTestView", "len2=$len2")
    }

    private fun test3(canvas: Canvas?) {
        path.addRect(-200f, -200f, 200f, 200f, Path.Direction.CW)

        val dst = Path() // 创建用于存储截取后内容的 Path

        dst.lineTo(-300f, -300f) // <--- 在 dst 中添加一条线段

        val measure = PathMeasure(path, false) // 将 Path 与 PathMeasure 关联

        measure.getSegment(200f, 600f, dst, true) // 截取一部分 并使用 moveTo 保持截取得到的 Path 第一个点的位置不变

        paint.color = Color.RED
        canvas?.drawPath(path, paint)

        paint.color = Color.BLACK
        canvas?.drawPath(dst, paint) // 绘制 Path
    }

    private fun test2(canvas: Canvas?) {
        path.addRect(-200f, -200f, 200f, 200f, Path.Direction.CW)
        // 创建用于存储截取后内容的 Path
        val dst = Path()
        // 将 Path 与 PathMeasure 关联
        val measure = PathMeasure(path, false)
        // 截取一部分存入dst中，并使用 moveTo 保持截取得到的 Path 第一个点的位置不变
        measure.getSegment(200f, 600f, dst, true)

        paint.color = Color.RED
        canvas?.drawPath(path, paint)

        paint.color = Color.BLACK
        canvas?.drawPath(dst, paint)
    }

    private fun test1(canvas: Canvas?) {
        path.lineTo(0f, 200f)
        path.lineTo(200f, 200f)
        path.lineTo(200f, 0f)

        val measure1 = PathMeasure(path, false)
        val measure2 = PathMeasure(path, true)

        Log.e("PathMeasureTestView", "forceClosed=false---->" + measure1.length);
        Log.e("PathMeasureTestView", "forceClosed=true----->" + measure2.length);


        paint.color = Color.RED
        canvas?.drawPath(path, paint)
    }
}