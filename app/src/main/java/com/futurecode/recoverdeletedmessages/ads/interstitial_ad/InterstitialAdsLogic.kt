package com.futurecode.recoverdeletedmessages.ads.interstitial_ad

import android.app.Activity
import com.futurecode.recoverdeletedmessages.activity.MyApplication
import com.futurecode.recoverdeletedmessages.utils.PrefManager
import com.futurecode.recoverdeletedmessages.utils.Utils


object InterstitialAdsLogic {
    private var adNetworkIndex = 0
    private var myPreferenceHelper: PrefManager = MyApplication.app.prefManager


    fun getCurrentAdNetwork(activity: Activity): String {
        try {
//            return "Custom"
            val adNetworks: List<String> = Utils.jsonToStringList(myPreferenceHelper.adFlow)


            adNetworkIndex = myPreferenceHelper.adNetworkIndex
            val currentAdNetwork = adNetworks[adNetworkIndex]

            // Increment the index for the next ad network
            adNetworkIndex = (adNetworkIndex + 1) % adNetworks.size
            myPreferenceHelper.adNetworkIndex = adNetworkIndex

            return currentAdNetwork
        } catch (e: Exception) {
            e.printStackTrace()
            return "Admob"
        }

    }

}