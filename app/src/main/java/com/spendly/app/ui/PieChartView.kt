package com.spendly.app.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

/**
 * Minimal donut chart drawn straight on Canvas — no third-party chart
 * library required, keeps the project dependency-light.
 */
class PieChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val palette = listOf(
        "#6C5CE7", "#00C2A8", "#FFB020", "#FF6B6B", "#4C6FFF", "#9AA0AC", "#2ECC71"
    ).map { Color.parseColor(it) }

    private var slices: List<Pair<String, Double>> = emptyList()

    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 42f }
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeWidth = 42f; color = Color.parseColor("#eceef3")
    }

    fun setData(byCategory: Map<String, Double>) {
        slices = byCategory.entries.sortedByDescending { it.value }.map { it.key to it.value }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val size = minOf(width, height).toFloat()
        if (size <= 0f) return

        val pad = arcPaint.strokeWidth
        val rect = RectF(pad, pad, size - pad, size - pad)

        canvas.drawArc(rect, 0f, 360f, false, bgPaint)

        val total = slices.sumOf { it.second }
        if (total <= 0.0) return

        var startAngle = -90f
        slices.forEachIndexed { index, (_, value) ->
            val sweep = (value / total * 360f).toFloat()
            arcPaint.color = palette[index % palette.size]
            canvas.drawArc(rect, startAngle, sweep, false, arcPaint)
            startAngle += sweep
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = MeasureSpec.getSize(widthMeasureSpec)
        setMeasuredDimension(size, size)
    }
}
