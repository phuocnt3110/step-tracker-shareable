package com.nphstudio.appname

import android.app.Application
import com.nphlab.sdk.ads.NphSdk
import com.nphlab.sdk.config.ConfigSource

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NphSdk.init(
            context = this,
            apiKey = "nph_test_valid",
            configSource = ConfigSource.FIREBASE,
            enableDebug = BuildConfig.DEBUG
        )
    }
}
