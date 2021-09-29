package `in`.okcredit.supplier.usecase

import dagger.Lazy
import merchant.okcredit.supplier.contract.IsAccountChatEnabledForSupplier
import tech.okcredit.android.ab.AbRepository
import javax.inject.Inject

class IsAccountChatEnabledForSupplierImpl @Inject constructor(
    private val ab: Lazy<AbRepository>
) : IsAccountChatEnabledForSupplier {

    override fun execute() = ab.get().isFeatureEnabled("accounts_chat")
}
