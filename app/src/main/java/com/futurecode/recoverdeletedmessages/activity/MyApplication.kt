package com.futurecode.recoverdeletedmessages.activity

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import com.facebook.ads.AudienceNetworkAds
import com.futurecode.recoverdeletedmessages.ads.app_open_ad.AppOpenHelperNew
import com.futurecode.recoverdeletedmessages.database.AppDatabase
import com.futurecode.recoverdeletedmessages.database.MessageRepository
import com.futurecode.recoverdeletedmessages.utils.JsonReadUtils
import com.futurecode.recoverdeletedmessages.utils.NetworkMonitor
import com.futurecode.recoverdeletedmessages.utils.PrefManager
import com.google.android.gms.ads.MobileAds
import java.util.Locale
import kotlin.collections.get

class MyApplication : Application() {

    lateinit var prefManager: PrefManager
    // Use nullable to avoid lateinit exceptions if ads are off
    var appOpenHelper: AppOpenHelperNew? = null
    private lateinit var networkMonitor: NetworkMonitor
    private var currentActivity: Activity? = null

    // =========================================================================
    // ADDED LAZY INITIALIZERS: Thread-safe injection models
    // =========================================================================
    private val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { MessageRepository(database.messageRecoveryDao()) }
    // =========================================================================

    override fun attachBaseContext(base: Context) {
        // Safe locale initialization for Application context
        val lang = PrefManager.get(base).selectedLanguage
        val locale = Locale(lang)
        Locale.setDefault(locale)

        val configuration = Configuration(base.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        // Using createConfigurationContext is the safe way for attachBaseContext
        super.attachBaseContext(base.createConfigurationContext(configuration))
    }


    override fun onCreate() {
        super.onCreate()
        app = this
        prefManager = PrefManager.get(this)

        Log.d("TAG", "prefManagerOffffff: ${prefManager.adsOff}")

        // 1. Setup Activity Tracker FIRST
        // This is required because NetworkMonitor needs getCurrentActivity() to show the dialog
        setupActivityTracker()


        // 2. Initialize and Start Network Monitoring
        networkMonitor = NetworkMonitor(this)
        networkMonitor.startMonitoring()

        // 3. General Initializations
        JsonReadUtils.fetchJsonData(this)
        initializeADS()


    }

    private fun setupActivityTracker() {
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityStarted(activity: Activity) {
                currentActivity = activity
            }

            override fun onActivityStopped(activity: Activity) {
                if (currentActivity == activity) {
                    currentActivity = null
                }
            }

            override fun onActivityCreated(p0: Activity, p1: Bundle?) {}
            override fun onActivityResumed(p0: Activity) {}
            override fun onActivityPaused(p0: Activity) {}
            override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {}
            override fun onActivityDestroyed(p0: Activity) {}
        })
    }

    fun getCurrentActivity(): Activity? = currentActivity

    private fun initializeADS() {
        AudienceNetworkAds.initialize(this)
        MobileAds.initialize(this) {

        }

        // Setup Helper only if ads are enabled
        if (!prefManager.adsOff) {
//        if (prefManager.adsOff) {
            appOpenHelper = AppOpenHelperNew(this)
//            appOpenHelper?.fetchAd()
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        if (::networkMonitor.isInitialized) {
            networkMonitor.stopMonitoring()
        }
    }

    companion object {
        lateinit var app: MyApplication

        fun setLocale(context: Context): Context {
            val prefManager = PrefManager.get(context)
            val languageCode = prefManager.selectedLanguage ?: "en"
            val locale = Locale(languageCode)
            Locale.setDefault(locale)

            val config = Configuration(context.resources.configuration)
            config.setLocale(locale)
            config.setLayoutDirection(locale)

            // This is the key: Update the context immediately
            context.resources.updateConfiguration(config, context.resources.displayMetrics)

            // Return a localized context for attachBaseContext
            return context.createConfigurationContext(config)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val languageCode = app.prefManager.selectedLanguage ?: "en"
        val locale = Locale(languageCode)
        newConfig.setLocale(locale)
        Locale.setDefault(locale)
    }
}