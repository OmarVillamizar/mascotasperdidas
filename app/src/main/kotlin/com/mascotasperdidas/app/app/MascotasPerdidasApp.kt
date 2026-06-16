package com.mascotasperdidas.app.app

import android.app.Application
import androidx.preference.PreferenceManager
import com.mascotasperdidas.app.data.firebase.ActivityProvider
import dagger.hilt.android.HiltAndroidApp
import org.osmdroid.config.Configuration
import javax.inject.Inject

@HiltAndroidApp
class MascotasPerdidasApp : Application() {

    @Inject
    lateinit var activityProvider: ActivityProvider

    override fun onCreate() {
        super.onCreate()
        // Track the current Activity so Firebase Phone Auth can attach its
        // reCAPTCHA flow (PhoneAuthOptions.setActivity requires a non-null Activity).
        activityProvider.register(this)
        Configuration.getInstance().load(
            applicationContext,
            PreferenceManager.getDefaultSharedPreferences(applicationContext),
        )
        Configuration.getInstance().userAgentValue = packageName
    }
}

