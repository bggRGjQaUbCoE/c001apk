package com.example.c001apk.util

import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.github.megatronking.stringfog.annotation.StringFogIgnore
import java.lang.reflect.Method

@StringFogIgnore
object RecyclerView {
    val checkForGaps: Method =
        StaggeredGridLayoutManager::class.java.getDeclaredMethod("checkForGaps")
    val markItemDecorInsetsDirty: Method =
        androidx.recyclerview.widget.RecyclerView::class.java.getDeclaredMethod("markItemDecorInsetsDirty")
}