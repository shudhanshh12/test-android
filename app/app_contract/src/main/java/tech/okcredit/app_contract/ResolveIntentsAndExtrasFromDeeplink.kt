package tech.okcredit.app_contract

import android.content.Intent

interface ResolveIntentsAndExtrasFromDeeplink {

    fun execute(deepLinkUrl: String): List<Intent>
}
