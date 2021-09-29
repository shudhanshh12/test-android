@file:JvmName("MobileUtils")
@file:JvmMultifileClass

package tech.okcredit.android.base.mobile

class InvalidMobile(mobile: String? = null) : IllegalArgumentException("$mobile is not a valid mobile")

fun mustParseMobile(mobile: String?): String = parseMobile(mobile) ?: throw InvalidMobile(mobile)

fun parseMobile(mobile: String?): String? {
    if (mobile == null) return null
    var mobile_ = mobile.filter { it.isDigit() }
    mobile_ = mobile_.trimStart('0') // remove trailing 0s
    if (mobile_.length == 12) mobile_ = mobile_.substring(2) // remove "91" if the mobile is 12 digit
    if (mobile_.length != 10) return null
    return when (mobile_[0]) {
        '9', '8', '7', '6', '5' -> mobile_
        else -> null
    }
}

private fun Char.isDigit(): Boolean =
    (this == '0' || this == '1' || this == '2' || this == '3' || this == '4' || this == '5' || this == '6' || this == '7' || this == '8' || this == '9')

fun isValidMobile(mobile: String?): Boolean = parseMobile(mobile) != null
