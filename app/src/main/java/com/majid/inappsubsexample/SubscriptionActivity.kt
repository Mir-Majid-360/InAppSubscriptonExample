package com.majid.inappsubsexample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.AcknowledgePurchaseResponseListener
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ConsumeResponseListener
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.majid.inappsubsexample.databinding.ActivitySubscriptionBinding
import java.io.IOException
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class SubscriptionActivity : AppCompatActivity() {

    lateinit var itemArrayList: ArrayList<ItemDs>
    var isSuccess = false
    var productId = 0

    private var billingClient: BillingClient? = null

    lateinit var binding: ActivitySubscriptionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubscriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.subscriptionRV.layoutManager = LinearLayoutManager(this)
        binding.subscriptionRV.hasFixedSize()
        itemArrayList = arrayListOf<ItemDs>()


        billingClient = BillingClient.newBuilder(this)
            .setListener(purchaseUpdateListener)
            .enablePendingPurchases()
            .build()
        showList()


    }

    private val purchaseUpdateListener = PurchasesUpdatedListener { billingResult, purchases ->

        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {

            for (purchase in purchases) {
                //handle purchase
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            Toast.makeText(this, "ALREADY SUBSCRIBED", Toast.LENGTH_SHORT).show()
            isSuccess = true
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED) {
            Toast.makeText(this, "FEATURE NOT SUPPORTED", Toast.LENGTH_SHORT).show()
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Toast.makeText(this, "USER CANCELED", Toast.LENGTH_SHORT).show()
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.BILLING_UNAVAILABLE) {
            Toast.makeText(this, "BILLING UNAVAILABLE", Toast.LENGTH_SHORT).show()
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.NETWORK_ERROR) {
            Toast.makeText(this, "NETWORK ERROR", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "ERROR ${billingResult.debugMessage}", Toast.LENGTH_SHORT).show()

        }
    }

    fun handlePurchase(purchase: Purchase){
        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        val listener = ConsumeResponseListener{billingResult, s ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK){

            }

        }
        billingClient!!.consumeAsync(consumeParams,listener)
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED){
            if (!verifyValidSignature(purchase.originalJson,purchase.signature)){
                Toast.makeText(this,"Invalid Purchase",Toast.LENGTH_SHORT).show()
                return
            }
            if (!purchase.isAcknowledged){
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient!!.acknowledgePurchase(acknowledgePurchaseParams,acknowledgePurchaseResponseListener)
                isSuccess = true
            }else{
                Toast.makeText(this,"Already Subscribed",Toast.LENGTH_SHORT).show()

            }
        }else if (purchase.purchaseState == Purchase.PurchaseState.UNSPECIFIED_STATE){
            Toast.makeText(this,"UNSPECIFIED STATE",Toast.LENGTH_SHORT).show()


        }else if (purchase.purchaseState == Purchase.PurchaseState.PENDING){
            Toast.makeText(this,"PENDING",Toast.LENGTH_SHORT).show()


        }

    }
    var acknowledgePurchaseResponseListener = AcknowledgePurchaseResponseListener { billingResult ->

        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK){
            isSuccess = true
        }
    }

    private fun verifyValidSignature(signedData : String,signature : String):Boolean{
        return try {
            val security = Security()
            val base64Key = ""
            security.verifyPurchase(base64Key,signedData,signature)
        }catch (e: IOException){
            false
        }

    }

    private fun showList() {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {

            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                val executorService = Executors.newSingleThreadExecutor()
                executorService.execute {

                    val productList = listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId("")
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build()

                    )
                    val params = QueryProductDetailsParams.newBuilder()
                        .setProductList(productList)
                    billingClient!!.queryProductDetailsAsync(params.build()) { billingResult, productDetailsList ->
                        for (productDetails in productDetailsList) {
                            if (productDetails.subscriptionOfferDetails != null) {
                                for (i in 0 until productDetails.subscriptionOfferDetails!!.size) {
                                    var subName: String = productDetails.name
                                    var index: Int = i
                                    var phases = ""
                                    var formattedPrice: String =
                                        productDetails.subscriptionOfferDetails?.get(i)?.pricingPhases?.pricingPhaseList?.get(
                                            0
                                        )?.formattedPrice.toString()

                                    var billingPeriod: String =
                                        productDetails.subscriptionOfferDetails?.get(i)?.pricingPhases?.pricingPhaseList?.get(
                                            0
                                        )?.billingPeriod.toString()

                                    var recurrenceMode: String =
                                        productDetails.subscriptionOfferDetails?.get(i)?.pricingPhases?.pricingPhaseList?.get(
                                            0
                                        )?.recurrenceMode.toString()

                                    if (recurrenceMode.equals("2")) {
                                        when (billingPeriod) {
                                            "P1W" -> billingPeriod = " For 1 Week"
                                            "P1M" -> billingPeriod = " For 1 Month"
                                            "P1Y" -> billingPeriod = " For 1 Year"

                                        }
                                    } else {

                                        when (billingPeriod) {
                                            "P1W" -> billingPeriod = "/Week"
                                            "P1M" -> billingPeriod = "/Month"
                                            "P1Y" -> billingPeriod = "/Year"

                                        }
                                    }

                                    phases = "$formattedPrice$billingPeriod"
                                    for (j in 0 until (productDetails.subscriptionOfferDetails!![i]?.pricingPhases?.pricingPhaseList?.size!!)) {

                                        if (j > 0) {

                                            var period: String =
                                                productDetails.subscriptionOfferDetails?.get(j)?.pricingPhases?.pricingPhaseList?.get(
                                                    0
                                                )?.billingPeriod.toString()
                                            var price: String =
                                                productDetails.subscriptionOfferDetails?.get(j)?.pricingPhases?.pricingPhaseList?.get(
                                                    0
                                                )?.formattedPrice.toString()
                                            when (period) {
                                                "P1W" -> period = "/Week"
                                                "P1M" -> period = "/Month"
                                                "P1Y" -> period = "/Year"

                                            }
                                            subName += "\n" + productDetails.subscriptionOfferDetails?.get(
                                                i
                                            )?.offerId.toString()
                                            phases += "\n$price$period"
                                        }
                                    }
                                    val item = ItemDs(subName, phases, index)
                                    itemArrayList.add(item)

                                }
                            }
                        }
                    }
                }
                runOnUiThread {
                    Thread.sleep(1000)
                    var adapter = AdapterSubscription(itemArrayList)
                    binding.subscriptionRV.adapter = adapter
                    adapter.setOnItemClickListener(object : AdapterSubscription.IListener {
                        override fun onItemClick(position: Int) {
                            val cItem = itemArrayList.get(position)
                            productId = cItem.planIndex
                            subscribeProduct()
                        }

                    })
                }
            }

        })
    }

    fun subscribeProduct() {

        billingClient!!.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {

            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {

                    val productList = listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId("")
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build()

                    )
                    val params = QueryProductDetailsParams.newBuilder()
                        .setProductList(productList)
                    billingClient!!.queryProductDetailsAsync(params.build()) { billingResult, productDetailsList ->
                        for (productDetails in productDetailsList) {

                            val offerToken =
                                productDetails.subscriptionOfferDetails?.get(productId)?.offerToken
                            val productDetailsParamsList = listOf(
                                offerToken?.let {
                                    BillingFlowParams.ProductDetailsParams.newBuilder()
                                        .setProductDetails(productDetails)
                                        .setOfferToken(it)
                                        .build()
                                }
                            )
                            val billingFlowParams = BillingFlowParams.newBuilder()
                                .setProductDetailsParamsList(productDetailsParamsList)
                                .build()
                            val billingResult = billingClient!!.launchBillingFlow(
                                this@SubscriptionActivity,
                                billingFlowParams
                            )

                        }

                    }
                }
            }

        })
    }
}