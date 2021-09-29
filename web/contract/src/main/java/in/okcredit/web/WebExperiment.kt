package `in`.okcredit.web

interface Experiment {
    val nameKey: Int
    val prodUrl: String
    val stagingUrl: String
}

sealed class WebExperiment : Experiment {

    sealed class Experiment(val type: String) {
        object MONEY_TRANSFER : Experiment("money_transfer")
        object ONLINE_ORDER : Experiment("online_order")
        object INSURANCE : Experiment("insurance")
        object MOBILE_PREPAID_RECHARGE : Experiment("mobile_prepaid_recharge")
        object MOBILE_RECHARGE_SMS : Experiment("mobile_recharge_sms")
        object MONEY_TRANSFER_SMS : Experiment("money_transfer_sms")
        object LENDING_SME : Experiment("lending_sme")
        object KYC : Experiment("kyc")
        object KYC_SUPPLIER : Experiment("kyc_supplier")
        object BILLING : Experiment("billing")
    }

    data class MoneyTransfer(
        override val nameKey: Int = R.string.money_transfer,
        override val prodUrl: String = "https://experiments.okcredit.in/money_transfer",
        override val stagingUrl: String = "https://experiments.staging.okcredit.io/money_transfer",
    ) : WebExperiment()

    data class OnlineDelivery(
        override val nameKey: Int = R.string.home_delivery,
        override val prodUrl: String = "https://orders.okcredit.in/home",
        override val stagingUrl: String = "https://orders.staging.okcredit.in/home",
    ) : WebExperiment()

    data class Insurance(
        override val nameKey: Int = R.string.insurance,
        override val prodUrl: String = "https://insurance.okcredit.in/",
        override val stagingUrl: String = "https://insurance.staging.okcredit.in",
    ) : WebExperiment()

    data class MobilePrepaidRecharge(
        override val nameKey: Int = R.string.mobile_prepaid_recharge,
        override val prodUrl: String = "https://recharge.okcredit.in/prepaid",
        override val stagingUrl: String = "https://recharge.staging.okcredit.in/prepaid",
    ) : WebExperiment()

    data class UnknownExperiment(
        override val nameKey: Int = R.string.back,
        override val prodUrl: String = "",
        override val stagingUrl: String = "",
    ) : WebExperiment()

    data class MobileRechargeSMS(
        override val nameKey: Int = R.string.mobile_recharge_sms,
        override val prodUrl: String = "https://recharge.okcredit.in/notify?source=inapp",
        override val stagingUrl: String = "https://recharge.staging.okcredit.in/notify?source=inapp",
    ) : WebExperiment()

    data class MoneyTransferSMS(
        override val nameKey: Int = R.string.money_transfer_sms,
        override val prodUrl: String = "https://experiments.okcredit.in/notify?source=inapp",
        override val stagingUrl: String = "https://experiments.staging.okcredit.in/notify?source=inapp",
    ) : WebExperiment()

    data class LendingSme(
        override val nameKey: Int = R.string.lending_sme,
        override val prodUrl: String = "https://lending.okcredit.in/",
        override val stagingUrl: String = "https://lending.staging.okcredit.in/",
    ) : WebExperiment()

    data class Kyc(
        override val nameKey: Int = R.string.kyc,
        override val prodUrl: String = "https://wkyc.okcredit.in/?serviceName=collection&verify=pan,aadhaar",
        override val stagingUrl: String = "https://wkyc.staging.okcredit.in/?serviceName=collection&verify=pan,aadhaar",
    ) : WebExperiment()

    data class KycSupplier(
        override val nameKey: Int = R.string.kyc,
        override val prodUrl: String = "https://wkyc.okcredit.in/?serviceName=supplier_collection&verify=pan,aadhaar",
        override val stagingUrl: String = "https://wkyc.staging.okcredit.in/?serviceName=supplier_collection&verify=pan,aadhaar",
    ) : WebExperiment()

    data class Billing(
        override val nameKey: Int = R.string.billing,
        override val prodUrl: String = "https://account.staging.okcredit.in/bill",
        override val stagingUrl: String = "https://account.staging.okcredit.in/bill",
    ) : WebExperiment()

    companion object {
        @JvmStatic
        fun getExperiment(type: String?): WebExperiment {
            return when (type) {
                Experiment.MONEY_TRANSFER.type -> MoneyTransfer()
                Experiment.ONLINE_ORDER.type -> OnlineDelivery()
                Experiment.INSURANCE.type -> Insurance()
                Experiment.MOBILE_PREPAID_RECHARGE.type -> MobilePrepaidRecharge()
                Experiment.MOBILE_RECHARGE_SMS.type -> MobileRechargeSMS()
                Experiment.MONEY_TRANSFER_SMS.type -> MoneyTransferSMS()
                Experiment.LENDING_SME.type -> LendingSme()
                Experiment.KYC.type -> Kyc()
                Experiment.KYC_SUPPLIER.type -> KycSupplier()
                Experiment.BILLING.type -> Billing()
                else -> UnknownExperiment()
            }
        }

        const val WEBVIEW_LIBRARY_URL = "https://okcredit-42.firebaseapp.com/"
    }
}
