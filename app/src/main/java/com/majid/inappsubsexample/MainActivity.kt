package com.majid.inappsubsexample

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.android.billingclient.api.*
import com.majid.inappsubsexample.databinding.ActivityMainBinding
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private var billingClient: BillingClient? = null
    var isSuccess = false
    var isRemoveAds = false
    var proName =""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        billingClient = BillingClient.newBuilder(this)
            .setListener(purchaseUpdateListener)
            .enablePendingPurchases()
            .build()
        queryPurchase()
    }

    private val purchaseUpdateListener = PurchasesUpdatedListener { billingResult, purchases ->

        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {

            for (purchase in purchases) {
                //handle purchase
//                handlePurchase(purchase)
            }
//        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
//            Toast.makeText(this, "ALREADY SUBSCRIBED", Toast.LENGTH_SHORT).show()
////            isSuccess = true
//        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED) {
//            Toast.makeText(this, "FEATURE NOT SUPPORTED", Toast.LENGTH_SHORT).show()
//        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
//            Toast.makeText(this, "USER CANCELED", Toast.LENGTH_SHORT).show()
//        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.BILLING_UNAVAILABLE) {
//            Toast.makeText(this, "BILLING UNAVAILABLE", Toast.LENGTH_SHORT).show()
//        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.NETWORK_ERROR) {
//            Toast.makeText(this, "NETWORK ERROR", Toast.LENGTH_SHORT).show()
//        } else {
//            Toast.makeText(this, "ERROR ${billingResult.debugMessage}", Toast.LENGTH_SHORT).show()
//
        }
    }


    fun btnClick(view: View) {
        startActivity(Intent(this, SubscriptionActivity::class.java))
    }

    fun queryPurchase() {
        billingClient!!.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {

            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                val executorService = Executors.newSingleThreadExecutor()
                executorService.execute {

                    billingClient!!.queryPurchasesAsync(
                        QueryPurchasesParams.newBuilder()
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build()
                    ) {billingResult,purchaseList->
                        for (purchases in purchaseList){

                            if (purchases != null && purchases.isAcknowledged){
                                isSuccess= true
                                isRemoveAds = true
                                for (i in 0 until  purchases.products.size){
                                    proName +=purchases.products[i].toString()
                                    var data = purchases.originalJson
                                }

                            }
                        }

                    }
                }

                runOnUiThread {
                    try {

                        Thread.sleep(1000)
                    } catch (e: InterruptedException) {

                        e.printStackTrace()
                    }
                    binding.tvStatus.text = proName
                }

            }

        })
    }

    override fun onDestroy() {
        super.onDestroy()
        if (billingClient != null){
            billingClient!!.endConnection() 
        }
    }
}