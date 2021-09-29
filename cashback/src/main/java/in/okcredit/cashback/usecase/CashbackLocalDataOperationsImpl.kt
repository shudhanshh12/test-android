package `in`.okcredit.cashback.usecase

import `in`.okcredit.cashback.contract.usecase.CashbackLocalDataOperations
import `in`.okcredit.cashback.repository.CashbackRepository
import dagger.Lazy
import javax.inject.Inject

class CashbackLocalDataOperationsImpl @Inject constructor(
    private val cashbackRepository: Lazy<CashbackRepository>,
) : CashbackLocalDataOperations {

    override fun executeInvalidateLocalData() = cashbackRepository.get().invalidateLocalData()

    override fun executeClearLocalData() = cashbackRepository.get().clearLocalData()
}
