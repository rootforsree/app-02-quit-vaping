package com.sreecharan.quitvaping.di

import android.app.Application
import android.util.Log
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.sreecharan.quitvaping.BuildConfig
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TemplateApp : Application() {
    override fun onCreate() {
        super.onCreate()
        configureRevenueCat()
    }

    /**
     * Configures RevenueCat on the main thread at app start. The public API
     * key comes from the REVENUECAT_API_KEY build env var (BuildConfig).
     * When empty (local dev), billing stays disabled and BillingRepository
     * reports [EntitlementState.Unavailable]: free tier only.
     */
    private fun configureRevenueCat() {
        val apiKey = BuildConfig.REVENUECAT_API_KEY
        if (apiKey.isBlank()) {
            Log.w(TAG, "REVENUECAT_API_KEY is empty: billing disabled (free tier only).")
            return
        }
        Purchases.configure(PurchasesConfiguration.Builder(this, apiKey).build())
        Log.i(TAG, "RevenueCat configured.")
    }

    companion object {
        private const val TAG = "Billing"
    }
}
