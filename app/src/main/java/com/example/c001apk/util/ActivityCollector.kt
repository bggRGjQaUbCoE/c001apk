package com.example.c001apk.util

import android.app.Activity

object ActivityCollector {

    private var activities = mutableListOf<Activity>()

    fun addActivity(activity: Activity) {
        activities.add(activity)
    }

    fun removeActivity(activity: Activity) {
        activities.remove(activity)
    }

    fun finishOneActivity(activityName: String) {
        for (activity in activities) {
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
        for (activity in activities) {
            val name = activity.javaClass.name
            if (name == activityName) {
                activity.recreate()
            }
        }
    }

    fun finishOtherActivity(activityName: String) {

        for (activity in activities) {
            val name = activity.javaClass.name //activity的类名
            if (name != activityName) {
                if(activity.isFinishing){
                    activities.remove(activity)
                }else{
                    activity.finish()
                }
            }
        }
    }

    fun finishAll() {
        for (activity in activities) {
            if (!activity.isFinishing) {
                activity.finish()
            }
        }
        activities.clear()
    }

}
