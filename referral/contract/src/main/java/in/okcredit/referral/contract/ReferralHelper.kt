package `in`.okcredit.referral.contract

object ReferralHelper {
    const val REFERRAL_LINK = "https://get.okcredit.in"
    const val DEFAULT_REFERRAL_PRICE = 5000L
    const val DEFAULT_REFERRAL_MAX_PRICE = 25000L
    const val DEFAULT_STATUS = 0
    const val REFERRAL_IMAGE_URL = "https://s3.ap-south-1.amazonaws.com/okcredit.app-assets/referral_v2_new.jpg"

    const val LOCAL_FILE_NAME = "referral.jpg"
    const val LOCAL_FOLDER_NAME = "share"

    private const val APK_DIRECTORY = "apk"
    const val LOCAL_APK_PATH = "$APK_DIRECTORY/local/OkCredit-Udhar-Bahi-Khata-l.apk"
    const val REMOTE_APK_PATH = "$APK_DIRECTORY/remote/OkCredit-Udhar-Bahi-Khata-r.apk"
}
