package com.example.c001apk.view;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;

import java.util.ArrayList;

public class LinearAdapterLayout extends LinearLayout {
    private BaseAdapter mAdapter;
    private DataSetObserver mObserver;

    public LinearAdapterLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public static void updateChildView1(LinearAdapterLayout linearAdapterLayout) {
        linearAdapterLayout.updateChildView();
    }

    public BaseAdapter getAdapter() {
        return this.mAdapter;
    }

    public void setAdapter(BaseAdapter baseAdapter) {
        if (this.mAdapter == baseAdapter) {
            return;
        }
        if (this.mObserver == null) {
            this.mObserver = new LinearAdapterLayoutObserver(this);
        }
        BaseAdapter baseAdapter2 = this.mAdapter;
        if (baseAdapter2 != null) {
            baseAdapter2.unregisterDataSetObserver(this.mObserver);
        }
        this.mAdapter = baseAdapter;
        BaseAdapter baseAdapter3 = this.mAdapter;
        if (baseAdapter3 != null) {
            baseAdapter3.registerDataSetObserver(this.mObserver);
        }
        updateChildView();
    }

    public void notifyDataSetChange() {
        updateChildView();
    }

    public void updateChildView() {
        if (this.mAdapter == null) {
            removeAllViews();
            return;
        }
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < this.mAdapter.getCount(); i++) {
            arrayList.add(this.mAdapter.getView(i, getChildAt(i), this));
        }
        removeAllViewsInLayout();
        for (int i2 = 0; i2 < arrayList.size(); i2++) {
            ViewGroup.LayoutParams layoutParams = ((View) arrayList.get(i2)).getLayoutParams();
            if (layoutParams == null) {
                layoutParams = generateDefaultLayoutParams();
            }
            addViewInLayout((View) arrayList.get(i2), i2, layoutParams);
        }
        requestLayout();
        invalidate();
    }


}

