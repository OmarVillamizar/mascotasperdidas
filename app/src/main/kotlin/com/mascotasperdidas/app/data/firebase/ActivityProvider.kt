package com.mascotasperdidas.app.data.firebase

import android.app.Activity
import android.app.Application
import android.os.Bundle
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Keeps a reference to the currently resumed [Activity].
 *
 * Firebase Phone Auth (reCAPTCHA flow) requires a non-null Activity in
 * [com.google.firebase.auth.PhoneAuthOptions.Builder.setActivity]. The data
 * layer has no direct access to one, so the Application registers this provider
 * as an [Application.ActivityLifecycleCallbacks] listener at startup.
 */
@Singleton
class ActivityProvider @Inject constructor() : Application.ActivityLifecycleCallbacks {

    var current: Activity? = null
        private set

    fun register(application: Application) {
        application.registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityResumed(activity: Activity) {
        current = activity
    }

    override fun onActivityPaused(activity: Activity) {
        if (current === activity) current = null
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityStarted(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) {
        if (current === activity) current = null
    }
}
