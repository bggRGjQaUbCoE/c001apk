package com.example.c001apk.view;

import android.database.DataSetObserver;

class LinearAdapterLayoutObserver extends DataSetObserver {
    final LinearAdapterLayout linearAdapterLayout;

    LinearAdapterLayoutObserver(LinearAdapterLayout linearAdapterLayout) {
        this.linearAdapterLayout = linearAdapterLayout;
    }

    @Override
    public void onChanged() {
        LinearAdapterLayout.updateChildView1(this.linearAdapterLayout);
    }
}
