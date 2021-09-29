package tech.okcredit.secure_keys

object KeyProvider {

    external fun getMixpanelStagingToken(): String

    external fun getMixpanelAlphaToken(): String

    external fun getMixpanelProdToken(): String

    external fun getBugfenderToken(): String

    external fun getAppsFlyerKey(): String

    external fun getCleverTapAccountIdStaging(): String

    external fun getCleverTapTokenStaging(): String

    external fun getCleverTapAccountIdAlpha(): String

    external fun getCleverTapTokenAlpha(): String

    external fun getCleverTapAccountIdProd(): String

    external fun getCleverTapTokenProd(): String
}
