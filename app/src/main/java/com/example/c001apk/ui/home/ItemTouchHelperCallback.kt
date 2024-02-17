package com.example.c001apk.ui.home

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayoutManager

class ItemTouchHelperCallback(onItemTouchCallbackListener: OnItemTouchCallbackListener?) :
    ItemTouchHelper.Callback() {
    private val l: OnItemTouchCallbackListener

    //当前item可拖拽状态
    private var currentPositionLongPressEnabled = true

    //整个列表的可操作状态 (总开关)
    private val isLongPressEnabled: Boolean
    private val isItemSwipeEnabled: Boolean

    init {
        if (onItemTouchCallbackListener == null) {
            throw NullPointerException("OnItemTouchCallbackListener is null")
        }
        l = onItemTouchCallbackListener
        isLongPressEnabled = l.isLongPressDragEnabled
        isItemSwipeEnabled = l.isItemViewSwipeEnabled
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val position = viewHolder.bindingAdapterPosition
        if (isLongPressEnabled) currentPositionLongPressEnabled =
            l.currentPositionLongPressEnabled(position)
        val layoutManager = recyclerView.layoutManager
        if (layoutManager is GridLayoutManager || layoutManager is FlexboxLayoutManager) { // GridLayoutManager
            // flag如果值是0，相当于这个功能被关闭
            val dragFlag =
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT or ItemTouchHelper.UP or ItemTouchHelper.DOWN
            val swipeFlag = 0
            // create make
            return makeMovementFlags(dragFlag, swipeFlag)
        } else if (layoutManager is LinearLayoutManager) { // linearLayoutManager
            val orientation = layoutManager.orientation
            var dragFlag = 0
            var swipeFlag = 0
            // 为了方便理解，相当于分为横着的ListView和竖着的ListView
            if (orientation == LinearLayoutManager.HORIZONTAL) { // 如果是横向的布局
                swipeFlag = ItemTouchHelper.UP or ItemTouchHelper.DOWN
                dragFlag = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            } else if (orientation == LinearLayoutManager.VERTICAL) { // 如果是竖向的布局，相当于ListView
                dragFlag = ItemTouchHelper.UP or ItemTouchHelper.DOWN
                swipeFlag = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            }
            return makeMovementFlags(dragFlag, swipeFlag)
        }
        return 0
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        val fromPosition = viewHolder.bindingAdapterPosition
        val targetPosition = target.bindingAdapterPosition
        //如果fromPosition or targetPosition不可操作则直接返回false 不进行数据交换回调
        return (l.currentPositionLongPressEnabled(fromPosition)
                && l.currentPositionLongPressEnabled(targetPosition)
                && l.onMove(fromPosition, targetPosition))
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        l.onSwiped(viewHolder.bindingAdapterPosition)
    }

    override fun onMoved(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        fromPos: Int,
        target: RecyclerView.ViewHolder,
        toPos: Int,
        x: Int,
        y: Int
    ) {
        super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y)
        l.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y)
    }

    override fun isLongPressDragEnabled(): Boolean {
        return isLongPressEnabled && currentPositionLongPressEnabled
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return isItemSwipeEnabled
    }

    interface OnItemTouchCallbackListener {
        /**
         * @param position 当前位置是否可长按拖拽
         * @return 默认true 可拖拽
         */
        fun currentPositionLongPressEnabled(position: Int): Boolean {
            return true
        }

        val isLongPressDragEnabled: Boolean
            /**
             * 所有item是否可拖拽
             *
             * @return 默认true 可拖拽
             */
            get() = true
        val isItemViewSwipeEnabled: Boolean
            /**
             * 所有item是否可滑动删除
             *
             * @return 默认true 可删除
             */
            get() = true

        /**
         * @param position 滑动删除后位置
         */
        fun onSwiped(position: Int) {}

        /**
         * @param fromPosition   开始位置
         * @param targetPosition 结束位置
         * @return 是否处理
         */
        fun onMove(fromPosition: Int, targetPosition: Int): Boolean
        fun onMoved(
            recyclerView: RecyclerView?,
            viewHolder: RecyclerView.ViewHolder?,
            fromPos: Int,
            target: RecyclerView.ViewHolder?,
            toPos: Int,
            x: Int,
            y: Int
        ) {
        }
    }
}