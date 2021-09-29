package `in`.okcredit.shared.usecase

import io.reactivex.Observable
import tech.okcredit.android.base.utils.ThreadUtils
import java.net.InetAddress
import javax.inject.Inject

class IsInternetAvailable @Inject constructor() {
    fun execute(): Observable<Boolean> {
        return Observable.fromCallable {
            try {
                val ipAddr: InetAddress = InetAddress.getByName("google.com")
                !ipAddr.equals("")
            } catch (e: Exception) {
                false
            }
        }.subscribeOn(ThreadUtils.api())
    }
}
