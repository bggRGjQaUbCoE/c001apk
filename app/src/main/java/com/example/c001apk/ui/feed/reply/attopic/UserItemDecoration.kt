package com.example.c001apk.ui.feed.reply.attopic

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.util.dp
import com.google.android.material.color.MaterialColors

class UserItemDecoration(context: Context) : RecyclerView.ItemDecoration() {


    /**
     * 顶部的大小
     */
    private var topHeight = 30.dp

    /**
     * 顶部背景画笔
     */
    private var topPaint: Paint = Paint()

    /**
     *顶部文字画笔
     */
    private var topTextPaint = Paint()

    /**
     * 顶部文字大小
     */
    private var topTextSize = 15.dp

    /**
     * 顶部文字边距
     */
    private var topTextPadding = 15.dp
    private var topTextRect: Rect = Rect()


    /**
     * 分组头部的大小
     */
    private var headHeight = 30.dp

    /**
     * 分组头部背景画笔
     */
    private var headPaint: Paint = Paint()

    /**
     *分组头部文字画笔
     */
    private var headTextPaint = Paint()

    /**
     * 分组头部文字大小
     */
    private var headTextSize = 15.dp

    /**
     * 分组头部文字边距
     */
    private var headTextPadding = 15.dp
    private var headTextRect: Rect = Rect()


    /**
     * 分隔线画笔
     */
    private var mPaint: Paint = Paint()


    private val tagColor = MaterialColors.getColor(
        context,
        com.google.android.material.R.attr.colorSecondaryContainer,
        0
    )
    private val textColor = MaterialColors.getColor(
        context,
        com.google.android.material.R.attr.colorOnSecondaryContainer,
        0
    )
    private val dividerColor: Int = MaterialColors.getColor(
        context,
        com.google.android.material.R.attr.colorSurfaceVariant,
        0
    )

    /**
     * 初始化画笔
     */
    init {
        topPaint.isAntiAlias = true
        // topPaint.strokeWidth = 1f
        topPaint.style = Paint.Style.FILL
        topPaint.color = tagColor

        topTextPaint.color = textColor
        topTextPaint.style = Paint.Style.FILL
        topTextPaint.isAntiAlias = true
        topTextPaint.textSize = topTextSize.toFloat()

        headPaint.isAntiAlias = true
        // headPaint.strokeWidth = 1f
        headPaint.style = Paint.Style.FILL
        headPaint.color = tagColor

        headTextPaint.color = textColor
        headTextPaint.style = Paint.Style.FILL
        headTextPaint.isAntiAlias = true
        headTextPaint.textSize = headTextSize.toFloat()

        mPaint.isAntiAlias = true
        mPaint.strokeWidth = 1f
        mPaint.style = Paint.Style.FILL
        mPaint.color = dividerColor
    }

    /**
     * onDraw先绘制，然后在轮到item,最后是onDrawOver
     * 绘制分组的头部
     */
    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
        val adapter = (parent.adapter as ConcatAdapter).adapters[1] as AtUserAdapter
        val count = parent.childCount
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight

        //遍历所有子view
        for (i in 0 until count) {
            val view = parent.getChildAt(i)
            val childPosition = parent.getChildAdapterPosition(view)

            if (childPosition != -1) {
                //在paddingTop范围内绘制
                if (view.top - headHeight >= parent.paddingTop) {
                    //如果是分组的头部
                    if (adapter.isGroupHead(childPosition)) {
                        val groupName = adapter.getGroupName(childPosition)

                        //绘制头部的背景
                        val rect = Rect(left, view.top - headHeight, right, view.top)
                        c.drawRect(rect, headPaint)

                        //绘制头部文字
                        headTextPaint.getTextBounds(
                            groupName,
                            0,
                            groupName.length,
                            headTextRect
                        )
                        c.drawText(
                            groupName,
                            (left + headTextPadding).toFloat(),
                            (view.top - (headHeight - headTextRect.height()) / 2).toFloat(),
                            headTextPaint
                        )
                    }
                    //如果不是头部，就绘制分隔线
                    else {
                        val rect = Rect(left, view.top - 1, right, view.top)
                        c.drawRect(rect, mPaint)
                    }
                }
            }

        }
    }

    /**
     * 绘制吸顶效果
     */
    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        val adapter = (parent.adapter as ConcatAdapter).adapters[1] as AtUserAdapter
        val layoutManager = parent.layoutManager as LinearLayoutManager
        //只考虑LinearLayoutManager
        //找到RecyclerView第一个显示的view的position
        val position = layoutManager.findFirstVisibleItemPosition()
        //通过viewHolder获取itemView
        val childView = parent.findViewHolderForAdapterPosition(position)?.itemView

        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight
        val top = parent.paddingTop

        childView?.let {
            //如果第一个可见itemView的下一个是组的头部，就把吸顶的顶上去
            if (adapter.isGroupHead(position + 1)) {
                //绘制吸顶头部的背景,bottom会随着上滑越来越小
                val bottom = topHeight.coerceAtMost(childView.bottom - top)
                val rect = Rect(left, top, right, top + bottom)
                c.drawRect(rect, topPaint)

                //绘制吸顶的头部文字
                val groupName = adapter.getGroupName(position)
                topTextPaint.getTextBounds(groupName, 0, groupName.length, topTextRect)

                //将超出的挡住裁掉
                val clipRect = Rect(left, top + bottom, right, top)
                c.clipRect(clipRect)

                c.drawText(
                    groupName,
                    (left + topTextPadding).toFloat(),
                    (top + bottom - (topHeight - topTextRect.height()) / 2).toFloat(),
                    topTextPaint
                )
            }
            //如果第一个可见itemView的下一个不是组的头部，就直接绘制吸顶头部
            else {
                //绘制吸顶头部的背景
                val rect = Rect(left, top, right, top + topHeight)
                c.drawRect(rect, topPaint)

                //绘制吸顶的头部文字
                val groupName = adapter.getGroupName(position)
                topTextPaint.getTextBounds(groupName, 0, groupName.length, topTextRect)

                c.drawText(
                    groupName,
                    (left + topTextPadding).toFloat(),
                    (top + topHeight - (topHeight - topTextRect.height()) / 2).toFloat(),
                    topTextPaint
                )
            }
        }
    }

    /**
     * 设置itemView偏移大小
     */
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        val adapter = (parent.adapter as ConcatAdapter).adapters[1] as AtUserAdapter
        //RecyclerView的LayoutParams，是有viewHolder的，所以可以通过View 获取LayoutParams,再拿到ViewHolder
        //获取当前view对应的position
        val position = parent.getChildAdapterPosition(view)

        if (position != -1) {
            //判断分组头
            if (adapter.isGroupHead(position)) {
                outRect.set(0, headHeight, 0, 0)
            }
            //分隔线
            else {
                outRect.set(0, 1, 0, 0)
            }
        }
    }

}