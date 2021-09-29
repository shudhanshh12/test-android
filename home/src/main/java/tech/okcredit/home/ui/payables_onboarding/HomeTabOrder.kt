package tech.okcredit.home.ui.payables_onboarding

import tech.okcredit.home.ui.homesearch.HomeConstants.HomeTab
import java.io.Serializable

data class HomeTabOrderList(
    val list: List<HomeTab>
) : Serializable
