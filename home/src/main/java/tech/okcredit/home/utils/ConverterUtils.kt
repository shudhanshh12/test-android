package tech.okcredit.home.utils

import com.google.common.base.Converter
import tech.okcredit.home.ui.homesearch.HomeConstants

var SORT_CONVERTER = object : Converter<String, Int>() {
    override fun doForward(a: String): Int {

        return when (a) {
            HomeConstants.SORT_BY_AMOUNT -> HomeConstants.SORT_TYPE_ABS_BALANCE
            HomeConstants.SORT_BY_NAME -> HomeConstants.SORT_TYPE_NAME
            HomeConstants.SORT_BY_LATEST -> HomeConstants.SORT_TYPE_LAST_PAYMENT
            else -> HomeConstants.SORT_TYPE_LAST_PAYMENT
        }
    }

    override fun doBackward(b: Int): String {

        return when (b) {
            HomeConstants.SORT_TYPE_ABS_BALANCE -> HomeConstants.SORT_BY_AMOUNT
            HomeConstants.SORT_TYPE_NAME -> HomeConstants.SORT_BY_NAME
            HomeConstants.SORT_TYPE_LAST_PAYMENT -> HomeConstants.SORT_BY_LATEST
            else -> HomeConstants.SORT_BY_LATEST
        }
    }
}
