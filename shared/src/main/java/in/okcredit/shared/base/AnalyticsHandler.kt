package `in`.okcredit.shared.base

@Deprecated("Event should be handled by the the concrete viewModel")
interface AnalyticsHandler {
    fun handleUserIntent(intent: UserIntent)
}
