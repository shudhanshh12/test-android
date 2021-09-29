package `in`.okcredit.cashback.datasource.local

import `in`.okcredit.cashback.datasource.remote.apiClient.CashbackMessageDetailsDto
import `in`.okcredit.shared.utils.CommonUtils.currentDateTime
import com.google.gson.Gson
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.rx2.rxCompletable
import java.lang.RuntimeException
import javax.inject.Inject

class CashbackLocalCacheSourceImpl @Inject constructor(
    private val cashbackPreferences: Lazy<CashbackPreferences>,
) : CashbackLocalCacheSource {

    private val gson = Gson()

    override fun setCashbackMessageDetailsCache(cashbackMessageDetailsDto: CashbackMessageDetailsDto): Completable {
        return cashbackPreferences.get().setCashbackMessageDetails(gson.toJson(cashbackMessageDetailsDto))
            .andThen(
                cashbackPreferences.get().setCashbackMessageDetailsTimestamp(currentDateTime().millis)
            )
    }

    override fun getCachedCashbackMessageDetails(): Single<CashbackMessageDetailsDto> {
        return cashbackPreferences.get().getCashbackMessageDetails()
            .map { serialisedObject ->
                if (serialisedObject.isEmpty()) {
                    throw RuntimeException(CASHBACK_DETAILS_MESSAGE_NOT_FOUND)
                } else {
                    return@map gson.fromJson(serialisedObject, CashbackMessageDetailsDto::class.java)
                }
            }
            .firstOrError()
    }

    override fun getCachedCashbackMessageDetailsTimestamp(): Observable<Long> {
        return cashbackPreferences.get().getCashbackMessageDetailsTimestamp()
    }

    override fun invalidateCache(): Completable {
        return cashbackPreferences.get().invalidatePreferenceValues()
    }

    override fun clear(): Completable {
        return rxCompletable { cashbackPreferences.get().clear() }
    }

    companion object {
        const val CASHBACK_DETAILS_MESSAGE_NOT_FOUND = "cashback_details_message_not_found"
    }
}
