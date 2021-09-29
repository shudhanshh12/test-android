package `in`.okcredit.payment.di

import `in`.okcredit.payment.ui.juspay.juspayWorkerFragment.JuspayWorkerFragment
import `in`.okcredit.payment.ui.juspay.juspayWorkerFragment.di.JuspayWorkerModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class JuspayPspActivityModule {

    @ContributesAndroidInjector(modules = [JuspayWorkerModule::class])
    abstract fun juspayPaymentFragmentWorker(): JuspayWorkerFragment
}
