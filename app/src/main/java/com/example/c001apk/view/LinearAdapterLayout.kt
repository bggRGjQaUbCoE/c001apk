package com.example.c001apk.view

import android.content.Context
import android.database.DataSetObserver
import android.util.AttributeSet
import android.view.View
import android.widget.BaseAdapter
import android.widget.LinearLayout

class LinearAdapterLayout(context: Context?, attributeSet: AttributeSet?) :
    LinearLayout(context, attributeSet) {

    private var mObserver: DataSetObserver? = null
    var adapter: BaseAdapter? = null
        set(value) {
            if (field === value) {
                return
            }
            if (mObserver == null) {
                mObserver = LinearAdapterLayoutObserver(this)
            }
            val baseAdapter2 = field
            baseAdapter2?.unregisterDataSetObserver(mObserver)
            field = value
            val baseAdapter3 = field
            baseAdapter3?.registerDataSetObserver(mObserver)
            updateChildView()
        }

    fun notifyDataSetChange() {
        updateChildView()
    }

    fun updateChildView() {
        adapter?.let { adapter ->
            val arrayList = ArrayList<View?>()
            for (i in 0 until adapter.count) {
                arrayList.add(adapter.getView(i, getChildAt(i), this))
            }
            removeAllViewsInLayout()
            for (i2 in arrayList.indices) {
                var layoutParams = (arrayList[i2] as View).layoutParams
                if (layoutParams == null) {
                    layoutParams = generateDefaultLayoutParams()
                }
                addViewInLayout(arrayList[i2] as View, i2, layoutParams)
            }
            requestLayout()
            invalidate()
        } ?: removeAllViews()
    }

    internal class LinearAdapterLayoutObserver(private val linearAdapterLayout: LinearAdapterLayout) :
        DataSetObserver() {
        override fun onChanged() {
            updateChildView1(linearAdapterLayout)
        }
    }

    companion object {
        fun updateChildView1(linearAdapterLayout: LinearAdapterLayout) {
            linearAdapterLayout.updateChildView()
        }
    }
}
