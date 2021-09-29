package `in`.okcredit.frontend.utils

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import tech.okcredit.android.base.utils.ThreadUtils
import java.net.InetAddress

class Utils {

    companion object {
        const val EMPTY = ""

        fun sanitiseFilePathToURL(path: String): String {
            var res = path
            if (path.contains("https:/s3")) {
                res = path.replace("https:/s3", "https://s3")
            }
            return res
        }

        fun isInternetAvailable(): Observable<Boolean> {
            return Observable.fromCallable {
                try {
                    val ipAddr: InetAddress = InetAddress.getByName("google.com")
                    !ipAddr.equals("")
                } catch (e: Exception) {
                    false
                }
            }.subscribeOn(ThreadUtils.api()).observeOn(AndroidSchedulers.mainThread())
        }

        fun isValidIFSC(ifsc: String): Boolean {
            val regExpIFSC: String
            if (ifsc.isEmpty()) {
                return true
            } else if (ifsc.length <= 4) {
                regExpIFSC = "^[A-Z]{${ifsc.length}}$"
            } else if (ifsc.length == 5) {
                regExpIFSC = "^[A-Z]{4}[0]$"
            } else {
                regExpIFSC = "^[A-Z]{4}[0][A-Z0-9]{${ifsc.length - 5}}$"
            }

            return ifsc.matches(regExpIFSC.toRegex())
        }
    }
}
