package com.example.c001apk.util

import android.annotation.SuppressLint
import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.WindowManager

/**
 * Created by hcc on 17/06/28.
 *
 *
 * Utils for getting display metrics such as screen size, dp, sp and px
 */
object DisplayUtil {
    private val DEBUG_TAG = DisplayUtil::class.java.simpleName
    private var appContext: Context? = null
    private var screenWidth = 0
    private var screenHeight = 0
    private var statusBarHeight = 0

    /**
     * If want to use the more convenient method like [.getScreenWidth],
     * [.getScreenHeight] and [.getStatusBarHeight], should call this
     * with application context before. it's good to initialize in the application's
     * onCreate call back.
     *
     * @param appContext must be application context for best practise
     */
    @Synchronized
    fun init(appContext: Context?) {
        DisplayUtil.appContext = appContext
        val size = getScreenSize(appContext)
        screenWidth = size[0]
        screenHeight = size[1]
        statusBarHeight = getStatusBarHeight(appContext)
    }

    /**
     * This method is guaranteed to get the proper screen width and height.
     *
     * @param context any activity or application context
     * @return the size with width and height pixels of screen
     */
    @Synchronized
    fun getScreenSize(context: Context?): IntArray {
        val name = Context.WINDOW_SERVICE
        val service = context!!.getSystemService(name)
        val wm = service as WindowManager
        val display = wm.defaultDisplay
        val dm = DisplayMetrics()
        display.getMetrics(dm)
        screenWidth = dm.widthPixels
        screenHeight = dm.heightPixels
        val size = IntArray(2)
        size[0] = screenWidth
        size[1] = screenHeight
        //        Log.d("screenWidth of size[0]", String.valueOf(size[0]));
//        Log.d("screenHeight of size[1]", String.valueOf(size[1]));
        return size
    }

    private const val DIMEN_CLASS_NAME = "com.android.internal.R\$dimen"
    private const val SB_HEIGHT_FIELD_NAME = "status_bar_height"

    /**
     * Get the proper system status bar's height using reflection.
     *
     * @param context any activity or application context
     * @return the height in pixel of system status bar
     */
    @SuppressLint("PrivateApi")
    @Synchronized
    fun getStatusBarHeight(context: Context?): Int {
        try {
            val theClass = Class.forName(DIMEN_CLASS_NAME)
            val classObj = theClass.newInstance()
            val field = theClass.getField(SB_HEIGHT_FIELD_NAME)
            val idObj = field[classObj]
            val idStr = idObj.toString()
            val id = idStr.toInt()
            val resources = context!!.resources
            statusBarHeight = resources.getDimensionPixelSize(id)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        //        Log.d("statusBarHeight", String.valueOf(statusBarHeight));
        return statusBarHeight
    }

    /**
     * Get the user device's screen width, the correctness is not guaranteed,
     * because it's depend on the calling sequence.
     * WARNING: Should calling [.init] or [.getScreenSize] before,
     * or you may get 0 as result.
     *
     * @return the pixel width of screen, return 0 if the call order is wrong
     */
    @Synchronized
    fun getScreenWidth(): Int {
        if (screenWidth == 0 && appContext != null) {
            val size = getScreenSize(appContext)
            screenWidth = size[0]
            screenHeight = size[1]
        }
        return screenWidth
    }

    /**
     * Get the user device's screen height, the correctness is not guaranteed,
     * because it's depend on the calling sequence.
     * WARNING: Should calling [.init] or [.getScreenSize] before,
     * or you may get 0 as result.
     *
     * @return the pixel height of screen, return 0 if the call order is wrong
     */
    @Synchronized
    fun getScreenHeight(): Int {
        if (screenHeight == 0 && appContext != null) {
            val size = getScreenSize(appContext)
            screenWidth = size[0]
            screenHeight = size[1]
        }
        return screenHeight
    }

    @get:Synchronized
    val screenRate: Float
        get() {
            if ((screenWidth == 0 || screenHeight == 0) && appContext != null) {
                val size = getScreenSize(appContext)
                screenWidth = size[0]
                screenHeight = size[1]
            }
            return screenHeight.toFloat() / screenWidth.toFloat()
        }

    /**
     * Get the system status bar's height, the correctness is not guaranteed,
     * because it's depend on the calling sequence.
     * WARNING: Should calling [.init] or [.getStatusBarHeight] before,
     * or you may get 0 as result.
     *
     * @return the pixel height of system status bar, return 0 if the call order is wrong
     */
    @Synchronized
    fun getStatusBarHeight(): Int {
        if (statusBarHeight == 0 && appContext != null) {
            statusBarHeight = getStatusBarHeight(appContext)
        }
        return statusBarHeight
    }

    private const val WARNING_MSG0 = "Should call init(Context appContext) before this!"
    private const val WARNING_MSG1 = "The result from this method is WRONG basically!"

    /**
     * Another way to convert dp(density-independent pixels) unit to equivalent px(pixels),
     * depending on device [DisplayMetrics], using [TypedValue].
     * This method is more recommended compared to [.convertDpToPx].
     *
     * @param dp      value in dp unit which will be converted into px
     * @param context context to get resources and device specific display metrics
     * @return a float value to represent px equivalent to dp
     */
    @Synchronized
    fun dpToPx(dp: Float, context: Context?): Float {
        val resources = context!!.resources
        val metrics = resources.displayMetrics
        val unit = TypedValue.COMPLEX_UNIT_DIP
        return TypedValue.applyDimension(unit, dp, metrics)
    }

    /**
     * Calling this instead of [.dpToPx] for convenience, ONLY
     * IF you have [.init] with the application context before.
     * WARNING: Will return the original value passed in by @param dp when calling
     * sequence is wrong!
     *
     * @param dp value in dp unit which will be converted into px
     * @return a float value to represent px equivalent to dp
     */
    @JvmStatic
    @Synchronized
    fun dpToPx(dp: Float): Float {
        return if (appContext != null) {
            dpToPx(dp, appContext)
        } else {
            Log.w("$DEBUG_TAG.dpToPx()", WARNING_MSG0)
            Log.w("$DEBUG_TAG.dpToPx()", WARNING_MSG1)
            dp
        }
    }

    /**
     * Convert dp(density-independent pixels) unit to equivalent px(pixels), depending on
     * device densityDpi of [DisplayMetrics].
     *
     * @param dp      value in dp unit which will be converted into px
     * @param context context to get resources and device specific display metrics
     * @return a float value to represent px equivalent to dp
     */
    @Synchronized
    fun convertDpToPx(dp: Float, context: Context?): Float {
        val resources = context!!.resources
        val metrics = resources.displayMetrics
        val densityDpi = metrics.densityDpi
        return dp * (densityDpi / 160f)
    }

    /**
     * Calling this instead of [.convertDpToPx] for convenience,
     * ONLY IF you have [.init] with the application context before.
     * WARNING: Will return the original value passed in by @param dp when calling
     * sequence is wrong!
     *
     * @param dp value in dp unit which will be converted into px
     * @return a float value to represent px equivalent to dp
     */
    @Synchronized
    fun convertDpToPx(dp: Float): Float {
        return if (appContext != null) {
            convertDpToPx(dp, appContext)
        } else {
            Log.w("$DEBUG_TAG.convertDpToPixel()", WARNING_MSG0)
            Log.w("$DEBUG_TAG.convertDpToPixel()", WARNING_MSG1)
            dp
        }
    }

    /**
     * Convert px(pixels) unit to equivalent dp(density-independent pixels), depending on
     * device densityDpi of [DisplayMetrics].
     *
     * @param px      value in px unit which will be converted into dp
     * @param context context to get resources and device specific display metrics
     * @return a float value to represent dp equivalent to px
     */
    @Synchronized
    fun convertPxToDp(px: Float, context: Context?): Float {
        val resources = context!!.resources
        val metrics = resources.displayMetrics
        val densityDpi = metrics.densityDpi
        return px / (densityDpi / 160f)
    }

    /**
     * Calling this instead of [.convertPxToDp] for convenience,
     * ONLY IF you have [.init] with the application context before.
     * WARNING: Will return the original value passed in by @param px when calling
     * sequence is wrong!
     *
     * @param px value in px unit which will be converted into dp
     * @return a float value to represent dp equivalent to px
     */
    @Synchronized
    fun convertPxToDp(px: Float): Float {
        return if (appContext != null) {
            convertPxToDp(px, appContext)
        } else {
            Log.w("$DEBUG_TAG.convertPixelsToDp()", WARNING_MSG0)
            Log.w("$DEBUG_TAG.convertPixelsToDp()", WARNING_MSG1)
            px
        }
    }

    /**
     * Convert sp(scale-independent pixels) unit to equivalent px(pixels), depending on
     * device scaledDensity of [DisplayMetrics].
     *
     * @param sp      value in sp unit which will be converted into px
     * @param context context to get resources and device specific display metrics
     * @return a float value to represent px equivalent to sp
     */
    @Synchronized
    fun convertSpToPx(sp: Float, context: Context?): Float {
        val resources = context!!.resources
        val metrics = resources.displayMetrics
        val fontScale = metrics.scaledDensity
        return sp * fontScale
    }

    /**
     * Calling this instead of [.convertSpToPx] for convenience,
     * ONLY IF you have [.init] with the application context before.
     * WARNING: Will return the original value passed in by @param px when calling
     * sequence is wrong!
     *
     * @param sp value in sp unit which will be converted into px
     * @return a float value to represent px equivalent to sp
     */
    @Synchronized
    fun convertSpToPx(sp: Float): Float {
        return if (appContext != null) {
            convertSpToPx(sp, appContext)
        } else {
            Log.w("$DEBUG_TAG.spToPx()", WARNING_MSG0)
            Log.w("$DEBUG_TAG.spToPx()", WARNING_MSG1)
            sp
        }
    }

    /**
     * Convert px(pixels) unit to equivalent sp(scale-independent pixels), depending on
     * device scaledDensity of [DisplayMetrics].
     *
     * @param px      value in px unit which will be converted into sp
     * @param context context to get resources and device specific display metrics
     * @return a float value to represent sp equivalent to px
     */
    @Synchronized
    fun convertPxToSp(px: Float, context: Context?): Float {
        val resources = context!!.resources
        val metrics = resources.displayMetrics
        val fontScale = metrics.scaledDensity
        return px / fontScale
    }

    /**
     * Calling this instead of [.convertPxToSp] for convenience,
     * ONLY IF you have [.init] with the application context before.
     * WARNING: Will return the original value passed in by @param px when calling
     * sequence is wrong!
     *
     * @param px value in px unit which will be converted into sp
     * @return a float value to represent sp equivalent to px
     */
    @Synchronized
    fun convertPxToSp(px: Float): Float {
        return if (appContext != null) {
            convertPxToSp(px, appContext)
        } else {
            Log.w("$DEBUG_TAG.pxToSp()", WARNING_MSG0)
            Log.w("$DEBUG_TAG.pxToSp()", WARNING_MSG1)
            px
        }
    }
}