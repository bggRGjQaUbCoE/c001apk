package net.mikaelzero.mojito.view.sketch.core.cache;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.text.format.Formatter;
import android.util.DisplayMetrics;

import androidx.annotation.NonNull;

import net.mikaelzero.mojito.view.sketch.core.SLog;

public class MemorySizeCalculator {
    private static final int BYTES_PER_ARGB_8888_PIXEL = 4;
    private static final int MEMORY_CACHE_TARGET_SCREENS = 3;
    private static final int BITMAP_POOL_TARGET_SCREENS = 3;
    private static final float MAX_SIZE_MULTIPLIER = 0.4f;
    private static final float LOW_MEMORY_MAX_SIZE_MULTIPLIER = 0.33f;
    private static final String NAME = "MemorySizeCalculator";

    private final int bitmapPoolSize;
    private final int memoryCacheSize;

    public MemorySizeCalculator(@NonNull Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();

        int maxSize = calculateMaxSize(activityManager);
        int screenSize = calculateScreenSize(displayMetrics);

        bitmapPoolSize = calculatePoolSize(screenSize, maxSize, BITMAP_POOL_TARGET_SCREENS);
        memoryCacheSize = calculateCacheSize(screenSize, maxSize, MEMORY_CACHE_TARGET_SCREENS);

        logSizes(context, activityManager, maxSize, memoryCacheSize, bitmapPoolSize);
    }
    
    private int calculatePoolSize(int screenSize, int maxSize, int targetScreens) {
        int targetSize = screenSize * targetScreens;
        return isSizeWithinMax(targetSize, maxSize) ? targetSize : calculatePartOfMaxSize(maxSize, targetScreens);
    }

    private int calculateCacheSize(int screenSize, int maxSize, int targetScreens) {
        int targetSize = screenSize * targetScreens;
        return isSizeWithinMax(targetSize, maxSize) ? targetSize : calculatePartOfMaxSize(maxSize, targetScreens);
    }

    private boolean isSizeWithinMax(int targetSize, int maxSize) {
        return targetSize <= maxSize;
    }

    private int calculatePartOfMaxSize(int maxSize, int targetScreens) {
        return Math.round((float) maxSize / (MEMORY_CACHE_TARGET_SCREENS + BITMAP_POOL_TARGET_SCREENS)) * targetScreens;
    }

    private int calculateScreenSize(DisplayMetrics displayMetrics) {
        return displayMetrics.widthPixels * displayMetrics.heightPixels * BYTES_PER_ARGB_8888_PIXEL;
    }
    
    private int calculateMaxSize(ActivityManager activityManager) {
        int memoryClassBytes = activityManager != null ? activityManager.getMemoryClass() * 1024 * 1024 : 0;
        return Math.round(memoryClassBytes * (isLowMemoryDevice(activityManager) ? LOW_MEMORY_MAX_SIZE_MULTIPLIER : MAX_SIZE_MULTIPLIER));
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static boolean isLowMemoryDevice(ActivityManager activityManager) {
        return activityManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && activityManager.isLowRamDevice();
    }

    private void logSizes(Context context, ActivityManager activityManager, int maxSize, int memoryCacheSize, int bitmapPoolSize) {
        if (SLog.isLoggable(SLog.LEVEL_DEBUG | SLog.TYPE_CACHE)) {
            SLog.d(NAME, String.format("Calculated memory cache size: %s pool size: %s memory class limited? %b " +
                            "max size: %s memoryClass: %d isLowMemoryDevice: %b",
                    toMb(context, memoryCacheSize), toMb(context, bitmapPoolSize),
                    memoryCacheSize + bitmapPoolSize > maxSize, toMb(context, maxSize),
                    activityManager != null ? activityManager.getMemoryClass() : -1, isLowMemoryDevice(activityManager)));
        }
    }

    private static String toMb(Context context, int bytes) {
        return Formatter.formatFileSize(context, bytes);
    }

    public int getMemoryCacheSize() {
        return memoryCacheSize;
    }

    public int getBitmapPoolSize() {
        return bitmapPoolSize;
    }
}
