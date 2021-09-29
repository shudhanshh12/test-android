package `in`.okcredit.payment.utils

import `in`.okcredit.payment.BuildConfig.*
import `in`.okcredit.payment.server.internal.PaymentApiMessages
import android.text.TextUtils
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

object JuspayPayloadUtils {
    private const val INIT_ACTION = "initiate"
    private const val PROCESS_ACTION = "paymentPage"
    private const val PROCESS_ACTION_QUICK_PAY = "quickPay"

    private fun getJuspayRequiredPayload(
        requestId: String,
        payload: JSONObject,
        service: String,
    ): JSONObject {
        val paymentsPayload = JSONObject()
        try {
            paymentsPayload.apply {
                put("requestId", requestId)
                put("service", service)
                put("payload", payload)
                put("betaAssets", JUSPAY_IS_BETA_ASSETS)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return paymentsPayload
    }

    private fun generateRequestId(): String {
        val uuid =
            UUID.randomUUID().toString().split("-".toRegex()).toTypedArray()
        for (i in uuid.indices) {
            if (i % 2 != 0) {
                uuid[i] = uuid[i].toUpperCase(Locale.ROOT)
            }
        }
        return TextUtils.join("-", uuid)
    }

    fun generateJuspayInitiatePayload(
        signaturePayload: String,
        signature: String,
        boldFont: Int,
        regularFont: Int,
        service: String,
    ): JSONObject {
        val initiatePayload = JSONObject()
        try {
            initiatePayload.apply {
                put("action", INIT_ACTION)
                put("service", service)
                put("clientId", JUSPAY_CLIENT_ID)
                put("merchantKeyId", JUSPAY_MERCHANT_KEY_ID)
                put("signaturePayload", signaturePayload)
                put("signature", signature)
                put("environment", JUSPAY_ENV)
                val merchantFontsJsonObj = JSONObject()
                merchantFontsJsonObj.apply {
                    put(
                        "bold",
                        JSONObject().apply {
                            put("name", "Noto-sans-bold")
                            put("resId", boldFont)
                        }
                    )
                    put(
                        "regular",
                        JSONObject().apply {
                            put("name", "Noto-sans-regular")
                            put("resId", regularFont)
                        }
                    )
                    put(
                        "semiBold",
                        JSONObject().apply {
                            put("name", "Noto-sans-semiBold")
                            put("resId", boldFont)
                        }
                    )
                }
                put("merchantFonts", merchantFontsJsonObj)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return getJuspayRequiredPayload(generateRequestId(), initiatePayload, service)
    }

    fun generateProcessPayload(
        getJuspayProcessResponse: PaymentApiMessages.GetJuspayAttributesResponse,
        quickPayEnabled: Boolean = false,
    ): JSONObject {
        val processPayload = JSONObject()
        try {
            val juspayProcessJsonObject = JSONObject(getJuspayProcessResponse.signaturePayload)
            val arrayAsList = arrayListOf<String>()
            getJuspayProcessResponse.endUrls.forEach {
                arrayAsList.add(it)
            }
            val endUrlArr = ArrayList(arrayAsList)
            val endUrls = JSONArray(endUrlArr)

            processPayload.apply {
                put("action", if (quickPayEnabled) PROCESS_ACTION_QUICK_PAY else PROCESS_ACTION)
                put("merchantId", JUSPAY_MERCHANT_ID)
                put("clientId", JUSPAY_CLIENT_ID)
                put("orderId", juspayProcessJsonObject.getString("order_id"))
                put("amount", juspayProcessJsonObject.getString("amount"))
                put("customerId", juspayProcessJsonObject.getString("customer_id"))
                put("customerMobile", juspayProcessJsonObject.getString("customer_phone"))
                put("endUrls", endUrls)
                put("merchantKeyId", JUSPAY_MERCHANT_KEY_ID)
                put("orderDetails", getJuspayProcessResponse.signaturePayload)
                put("signature", getJuspayProcessResponse.signature)
                put(
                    "language",
                    if (getJuspayProcessResponse.language.isNotEmpty()) getJuspayProcessResponse.language else "english"
                )
                put("environment", JUSPAY_ENV)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return getJuspayRequiredPayload(generateRequestId(), processPayload, JUSPAY_SERVICE)
    }

    fun constructPrefetchPayload(service: String): JSONObject {
        val preFetchPayload = JSONObject()
        val services = JSONArray()
        services.put(service)
        val innerPayload = JSONObject()
        try {
            innerPayload.put("clientId", JUSPAY_CLIENT_ID)
            preFetchPayload.put("services", services)
            preFetchPayload.put("betaAssets", JUSPAY_IS_BETA_ASSETS)
            preFetchPayload.put("payload", innerPayload)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return preFetchPayload
    }

    fun constructPspManagementPayload(signature: String, signaturePayload: String): JSONObject {
        val payload = JSONObject()
        val innerPayload = JSONObject()
        innerPayload.apply {
            put("action", "Management")
            put("merchantKeyId", JUSPAY_MERCHANT_KEY_ID)
            put("signaturePayload", signaturePayload)
            put("signature", signature)
        }

        payload.apply {
            put("requestId", generateRequestId())
            put("service", JUSPAY_SERVICE_UPI_PSP)
            put("payload", innerPayload)
        }
        return payload
    }

    fun constructPspIntentPayload(intentData: String, signature: String, signaturePayload: String): JSONObject {
        val payload = JSONObject()
        val innerPayload = JSONObject()
        innerPayload.apply {
            put("action", "IncomingIntent")
            put("merchantKeyId", JUSPAY_MERCHANT_KEY_ID)
            put("signaturePayload", signaturePayload)
            put("signature", signature)
            put("intentData", intentData)
        }

        payload.apply {
            put("requestId", generateRequestId())
            put("service", JUSPAY_SERVICE_UPI_PSP)
            put("payload", innerPayload)
        }
        return payload
    }

    fun constructPspApproveCollectRequestPayload(
        gatewayTxnId: String,
        gatewayRefId: String,
        signature: String,
        signaturePayload: String,
    ): JSONObject {
        val payload = JSONObject()
        val innerPayload = JSONObject()
        innerPayload.apply {
            put("action", "ApproveCollect")
            put("merchantKeyId", JUSPAY_MERCHANT_KEY_ID)
            put("signaturePayload", signaturePayload)
            put("signature", signature)
            put("gatewayTransactionId", gatewayTxnId)
            put("gatewayReferenceId", gatewayRefId)
        }

        payload.apply {
            put("requestId", generateRequestId())
            put("service", JUSPAY_SERVICE_UPI_PSP)
            put("payload", innerPayload)
        }
        return payload
    }
}
