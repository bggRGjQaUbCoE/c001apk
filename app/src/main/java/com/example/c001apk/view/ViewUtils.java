package com.example.c001apk.view;

import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ViewUtils {

    public static View getChildRecyclerView(View view) {
        ArrayList<View> unvisited = new ArrayList<>();
        unvisited.add(view);

        while (!unvisited.isEmpty()) {
            View child = unvisited.remove(0);
            if (child instanceof RecyclerView) {
                return child;
            }
            if (!(child instanceof ViewGroup viewGroup)) {
                continue;
            }
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                unvisited.add(viewGroup.getChildAt(i));
            }
        }
        return null;
    }

}
