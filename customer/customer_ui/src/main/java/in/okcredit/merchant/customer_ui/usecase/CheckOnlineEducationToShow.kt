package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.collection.contract.rxPreference.CollectionRxPreference
import dagger.Lazy
import io.reactivex.Observable
import kotlinx.coroutines.rx2.asObservable
import tech.okcredit.android.base.preferences.DefaultPreferences
import tech.okcredit.android.base.preferences.Scope
import javax.inject.Inject

class CheckOnlineEducationToShow @Inject constructor(
    private val rxSharedPreference: Lazy<DefaultPreferences>,
    private val collectionRepository: Lazy<CollectionRepository>,
) {

    fun execute(): Observable<Boolean> {

        return Observable.zip(
            onlineEducationShown(),
            onlineEducationDenied(),
            collectionRepository.get().isCollectionActivated(),
            { onlineEducationShown, onlineEducationDenied, isCollectionActivated ->
                onlineEducationShown.not() && onlineEducationDenied.not() && isCollectionActivated.not()
            }
        )
    }

    private fun onlineEducationShown() = rxSharedPreference.get()
        .getBoolean(CollectionRxPreference.IS_ONLINE_COLLECTION_EDUCATION_SHOWN, Scope.Individual)
        .asObservable()

    private fun onlineEducationDenied() = rxSharedPreference.get()
        .getBoolean(CollectionRxPreference.IS_ONLINE_COLLECTION_EDUCATION_DENIED, Scope.Individual)
        .asObservable()
}
