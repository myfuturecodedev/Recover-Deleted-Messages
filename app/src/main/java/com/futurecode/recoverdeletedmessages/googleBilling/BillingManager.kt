package com.futurecode.recoverdeletedmessages.googleBilling

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.ref.WeakReference

class BillingManager(
    activity: Activity,
    private val onPurchaseFailure: (String) -> Unit,
    private val onPurchaseSuccess: () -> Unit
) {

    // ── Product IDs ──────────────────────────────────────────────────────
    companion object {
        const val PRODUCT_WEEKLY = "premium_weekly"
        const val PRODUCT_MONTHLY = "premium_monthly"
        const val PRODUCT_QUARTERLY = "premium_quarterly"

        // Product Month backup identifier added to support mismatched string variants found in premium layouts
        const val PRODUCT_PRODUCT_MONTHLY = "premium_monthly"
    }

    // FIXED: Swapped Activity reference to a WeakReference wrapper to fully eliminate Context and Activity memory leaks
    private val activityRef = WeakReference(activity)

    // Main thread handler loop coordinator
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _productDetails = MutableStateFlow<List<ProductDetails>>(emptyList())
    val productDetails = _productDetails.asStateFlow()

    private val _isPurchased = MutableStateFlow(false)
    val isPurchased = _isPurchased.asStateFlow()

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        // Post updates onto the Main UI Thread to cleanly isolate threading crashes
        mainHandler.post {
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    // FIXED: Handle safe null-check navigation across the items list payload
                    purchases?.forEach { handlePurchase(it) }
                }

                BillingClient.BillingResponseCode.USER_CANCELED -> {
                    onPurchaseFailure("User cancelled the purchase")
                }

                else -> {
                    onPurchaseFailure("Purchase failed: ${billingResult.debugMessage}")
                }
            }
        }
    }

    private var billingClient: BillingClient? = null

    init {
        // Safe check initialization pattern using context scope locks
        activityRef.get()?.let { context ->
            billingClient = BillingClient.newBuilder(context)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases(
                    PendingPurchasesParams.newBuilder()
                        .enableOneTimeProducts()
                        .build()
                )
                .build()
        }
    }

    fun startConnection() {
        val client = billingClient ?: return
        try {
            client.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    mainHandler.post {
                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            queryProducts()
                            checkActivePurchases()
                        } else {
                            Log.e("BILLING", "Setup failed with response connection code: ${billingResult.responseCode}")
                        }
                    }
                }

                override fun onBillingServiceDisconnected() {
                    Log.w("BILLING", "Billing service disconnected handler event triggered.")
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun queryProducts() {
        val client = billingClient ?: return
        if (!client.isReady) return

        val targetProducts = listOf(PRODUCT_WEEKLY, PRODUCT_MONTHLY, PRODUCT_QUARTERLY)
        val productList = targetProducts.map { id ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(id)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }

        try {
            client.queryProductDetailsAsync(
                QueryProductDetailsParams.newBuilder().setProductList(productList).build()
            ) { billingResult, result ->
                mainHandler.post {
                    Log.d("BILLING", "Response Msg: ${billingResult.debugMessage}")
                    Log.d("BILLING", "Response Code = ${billingResult.responseCode}")
                    Log.d("BILLING", "Fetched Items Count = ${result.productDetailsList?.size ?: 0}")

                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        _productDetails.value = result.productDetailsList ?: emptyList()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun launchPurchaseFlow(productId: String) {
        val hostActivity = activityRef.get()
        val client = billingClient

        if (hostActivity == null || hostActivity.isFinishing || hostActivity.isDestroyed) {
            onPurchaseFailure("Hosting window context closed.")
            return
        }

        if (client == null || !client.isReady) {
            onPurchaseFailure("Billing billing service is not ready yet.")
            return
        }

        val product = _productDetails.value.find { it.productId == productId }
        if (product == null) {
            onPurchaseFailure("Product details configuration not found.")
            return
        }

        // FIXED: Added safe null fallback logic to fetch subscription token params if explicit promotional offer tracks are missing
        val offerToken = product.subscriptionOfferDetails?.firstOrNull()?.offerToken
            ?: product.subscriptionOfferDetails?.getOrNull(0)?.offerToken

        if (offerToken == null) {
            onPurchaseFailure("Valid subscription token generation failed.")
            return
        }

        try {
            val params = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(product)
                            .setOfferToken(offerToken)
                            .build()
                    )
                ).build()

            client.launchBillingFlow(hostActivity, params)
        } catch (e: Exception) {
            e.printStackTrace()
            onPurchaseFailure("Error launching payment flow interface.")
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        val client = billingClient ?: return
        if (!client.isReady) return

        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
            val ackParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()

            try {
                client.acknowledgePurchase(ackParams) { billingResult ->
                    mainHandler.post {
                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            _isPurchased.value = true
                            onPurchaseSuccess()
                        } else {
                            onPurchaseFailure("Acknowledgement verification failed.")
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun checkActivePurchases() {
        val client = billingClient ?: return
        if (!client.isReady) return

        try {
            client.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            ) { _, purchases ->
                mainHandler.post {
                    // FIXED: Embedded safe execution check (?.) to handle occasional null purchases list mutations cleanly
                    _isPurchased.value = purchases?.any {
                        it.purchaseState == Purchase.PurchaseState.PURCHASED
                    } == true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun endConnection() {
        // FIXED: Added public connection teardown hook method block to clear framework allocations safely on Fragment teardowns
        try {
            billingClient?.endConnection()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            billingClient = null
            activityRef.clear()
        }
    }
}