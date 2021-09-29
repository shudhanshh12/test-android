package `in`.okcredit.merchant.usecase

import `in`.okcredit.merchant.contract.IsMultipleAccountEnabled
import dagger.Lazy
import dagger.Reusable
import io.reactivex.Observable
import tech.okcredit.android.ab.AbRepository
import javax.inject.Inject

@Reusable
class IsMultipleAccountEnabledImpl @Inject constructor(
    private val abRepository: Lazy<AbRepository>,
) : IsMultipleAccountEnabled {
    companion object {
        const val FEATURE = "multiple_accounts"
    }

    override fun execute(): Observable<Boolean> {
        return abRepository.get().isFeatureEnabled(FEATURE)
    }
}
