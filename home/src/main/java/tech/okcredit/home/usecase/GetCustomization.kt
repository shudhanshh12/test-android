package tech.okcredit.home.usecase

import `in`.okcredit.dynamicview.Targets
import `in`.okcredit.dynamicview.data.model.Customization
import `in`.okcredit.dynamicview.data.repository.DynamicViewRepositoryImpl
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.base.rxjava.SchedulerProvider
import tech.okcredit.android.base.utils.Optional
import tech.okcredit.android.base.utils.ofNullable
import javax.inject.Inject

class GetCustomization @Inject constructor(
    private val repository: Lazy<DynamicViewRepositoryImpl>,
    private val schedulerProvider: Lazy<SchedulerProvider>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun execute(target: Targets) = getCustomizationForTarget(target)

    private fun getCustomizationForTarget(target: Targets): Observable<Customization> {
        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            repository.get().getCustomizations(businessId).map {
                // TODO - move this condition to database query
                it.find { customization ->
                    customization.target == target.value
                }.ofNullable()
            }.filter { it is Optional.Present }
                .map { (it as Optional.Present<Customization>).`object` }
        }
    }

    private fun getFallbackCustomizationForTarget(target: Targets): Observable<Customization> {
        return Observable.just(repository.get().getFallbackCustomizations()).map {
            it.find { customization ->
                customization.target == target.value
            }.ofNullable()
        }.filter { it is Optional.Present }
            .map { (it as Optional.Present<Customization>).`object` }
            .subscribeOn(schedulerProvider.get().io())
    }

    fun getCustomizationOrFallbackForTarget(target: Targets, businessId: String): Observable<Customization> {
        return repository.get().getCustomizations(businessId)
            .flatMap {
                val customization = it.find { customization -> customization.target == target.value }
                if (customization != null) {
                    Observable.just(customization)
                } else {
                    getFallbackCustomizationForTarget(target)
                }
            }
    }
}
