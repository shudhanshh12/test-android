package `in`.okcredit.payment.di

import `in`.okcredit.payment.ui.juspay.juspayWorkerFragment.JuspayWorkerFragment
import `in`.okcredit.payment.ui.juspay.juspayWorkerFragment.di.JuspayWorkerModule
import `in`.okcredit.payment.ui.juspay.juspay_payment_bottom_sheet.PaymentEditAmountBottomSheet
import `in`.okcredit.payment.ui.juspay.juspay_payment_bottom_sheet.di.PaymentEditAmountModule
import `in`.okcredit.payment.ui.payment_blind_pay.PaymentBlindPayFragment
import `in`.okcredit.payment.ui.payment_blind_pay.di.PaymentBlindPayModule
import `in`.okcredit.payment.ui.payment_destination.PaymentDestinationDialog
import `in`.okcredit.payment.ui.payment_destination._di.PaymentDestinationModule
import `in`.okcredit.payment.ui.payment_error_screen.PaymentErrorFragment
import `in`.okcredit.payment.ui.payment_error_screen.di.PaymentErrorModule
import `in`.okcredit.payment.ui.payment_result.PaymentResultFragment
import `in`.okcredit.payment.ui.payment_result.di.PaymentResultModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class PaymentActivityModule {

    @ContributesAndroidInjector(modules = [PaymentEditAmountModule::class])
    abstract fun supplierEditAmountBottomSheet(): PaymentEditAmountBottomSheet

    @ContributesAndroidInjector(modules = [JuspayWorkerModule::class])
    abstract fun juspayPaymentFragmentWorker(): JuspayWorkerFragment

    @ContributesAndroidInjector(modules = [PaymentResultModule::class])
    abstract fun paymentResultModule(): PaymentResultFragment

    @ContributesAndroidInjector(modules = [PaymentErrorModule::class])
    abstract fun supplierErrorFragment(): PaymentErrorFragment

    @ContributesAndroidInjector(modules = [PaymentDestinationModule::class])
    abstract fun paymentDestinationDialog(): PaymentDestinationDialog

    @ContributesAndroidInjector(modules = [PaymentBlindPayModule::class])
    abstract fun paymentBlindPayFragment(): PaymentBlindPayFragment
}
