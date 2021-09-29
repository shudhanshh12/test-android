package `in`.okcredit.frontend.ui.live_sales._di

import `in`.okcredit.frontend.ui.live_sales.views.TransactionView
import com.google.common.base.Preconditions

object LiveSaleInjector {

    private var injector: Injector? = null
    interface Injector {
        fun inject(ctransactionView: TransactionView)
    }

    @JvmStatic
    fun setInjector(injector: Injector) {
        this.injector = injector
    }

    @JvmStatic
    fun inject(transactionView: TransactionView) {
        Preconditions.checkState(injector != null, "fileupload injector not initialized")
        injector?.inject(transactionView)
    }
}
