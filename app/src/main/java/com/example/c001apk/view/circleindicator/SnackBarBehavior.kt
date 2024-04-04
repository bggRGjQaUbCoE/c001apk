package com.example.c001apk.view.circleindicator

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.snackbar.Snackbar.SnackbarLayout
import kotlin.math.min

class SnackBarBehavior : CoordinatorLayout.Behavior<BaseCircleIndicator> {
    constructor()
    constructor(context: Context?, attributeSet: AttributeSet?) : super(context, attributeSet)

    @SuppressLint("RestrictedApi")
    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: BaseCircleIndicator, dependency: View
    ): Boolean {
        return dependency is SnackbarLayout
    }

    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: BaseCircleIndicator, dependency: View
    ): Boolean {
        val translationY = getTranslationYForSnackBar(parent, child)
        child.translationY = translationY
        return true
    }

    @SuppressLint("RestrictedApi")
    private fun getTranslationYForSnackBar(
        parent: CoordinatorLayout,
        ci: BaseCircleIndicator
    ): Float {
        var minOffset = 0f
        val dependencies = parent.getDependencies(ci)
        var i = 0
        val z = dependencies.size
        while (i < z) {
            val view = dependencies[i]
            if (view is SnackbarLayout && parent.doViewsOverlap(ci, view)) {
                minOffset = min(
                    minOffset.toDouble(),
                    (view.getTranslationY() - view.getHeight()).toDouble()
                )
                    .toFloat()
            }
            i++
        }
        return minOffset
    }
}