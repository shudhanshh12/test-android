package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.backend.contract.Features
import `in`.okcredit.collection.contract.CheckLiveSalesActive
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.ab.AbRepository
import javax.inject.Inject

class CheckLiveSalesActiveImpl @Inject constructor(
    private val ab: Lazy<AbRepository>,
) : CheckLiveSalesActive {
    override fun execute(): Observable<Boolean> {
        return ab.get().isFeatureEnabled(Features.LIVESALES)
    }
}
