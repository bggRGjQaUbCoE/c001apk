package com.example.c001apk.util

import android.app.Activity

object ActivityCollector {

    private var activities = mutableListOf<Activity>()

    fun addActivity(activity: Activity) {
        activities.add(activity)
        // Log.i("ActivityCollector", "addActivity: ${activity.javaClass.name}")
    }

    fun removeActivity(activity: Activity) {
        activities.remove(activity)
    }

    fun finishOneActivity(activityName: String) {
        activities.forEach { activity ->
            val name = activity.javaClass.name
            if (name == activityName) {
                if (activity.isFinishing) {
                    activities.remove(activity)
                } else {
                    activity.finish()
                }
            }
        }
    }

    fun recreateActivity(activityName: String) {
        activities.forEach { activity ->
            val name = activity.javaClass.name
            if (name == activityName) {
                activity.recreate()
            }
        }
    }

    fun finishOtherActivity(activityName: String) {
        activities.forEach { activity ->
            val name = activity.javaClass.name //activity的类名
            if (name != activityName) {
                if (activity.isFinishing) {
                    activities.remove(activity)
                } else {
                    activity.finish()
                }
            }
        }
    }

    fun finishAll() {
        activities.forEach { activity ->
            if (!activity.isFinishing) {
                activity.finish()
            }
        }
        activities.clear()
    }

}
