package tech.okcredit.help.contextual_help

import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import `in`.okcredit.shared.utils.ScreenName
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import tech.okcredit.android.base.extensions.isEqual
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.help.R
import tech.okcredit.userSupport.ContextualHelp
import tech.okcredit.userSupport.SupportRepository

/**
 * A composite view to display add transaction buttons.
 **/

class ContextualHelpMenuView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private var tracker: Tracker? = null

    private var legacyNavigator: LegacyNavigator? = null

    private val contextualHelpIds by lazy { mutableListOf<String>() }

    lateinit var screenName: String

    private val compositeDisposable = CompositeDisposable()

    var visibilitySubject: PublishSubject<Boolean> = PublishSubject.create()

    private val helpIcon: ImageView

    init {
        View.inflate(context, R.layout.widget_contextual_help_view, this)
        helpIcon = this.findViewById(R.id.help_icon)
        helpIcon.setOnClickListener {
            setContextualHelpClick()
        }
    }

    fun initDependencies(
        screenName: String,
        tracker: Tracker,
        legacyNavigator: LegacyNavigator,
    ) {
        this.screenName = screenName
        this.tracker = tracker
        this.legacyNavigator = legacyNavigator
    }

    fun setContextualHelpIds(helpIds: List<String>) {
        if (helpIds.isEqual(contextualHelpIds)) return

        contextualHelpIds.clear()
        contextualHelpIds.addAll(helpIds)
        setContextualHelpVisibility()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        disposeObserver()
    }

    private fun disposeObserver() {
        if (compositeDisposable.isDisposed.not())
            compositeDisposable.dispose()
    }

    fun setContextualHelpClick() {
        if (contextualHelpIds.isNotEmpty()) {
            tracker?.trackViewHelpTopic_v2(contextualHelpIds[0], screenName, PropertyValue.EXPAND)
            legacyNavigator?.goToHelpV2Screen(context, contextualHelpIds, screenName)
        }
    }

    private fun setContextualHelpVisibility() {
        if (contextualHelpIds.isNullOrEmpty()) {
            visibilitySubject.onNext(false)
            helpIcon.visibility = View.GONE
        } else {
            visibilitySubject.onNext(true)
            helpIcon.visibility = View.VISIBLE
        }
    }

    @Deprecated("Use GetContextualHelpIds in view model")
    fun setScreenNameValue(
        screenName: String,
        tracker: Tracker,
        userSupport: SupportRepository,
        legacyNavigator: LegacyNavigator,
    ) {
        this.screenName = screenName
        this.tracker = tracker
        this.legacyNavigator = legacyNavigator
        var displayType: List<String> = emptyList()
        when (screenName) {
            ScreenName.CustomerScreen.value ->
                displayType = ContextualHelp.CUSTOMER.value

            ScreenName.TxnDetailsScreen.value ->
                displayType = ContextualHelp.TRANSACTION.value

            ScreenName.RewardsScreen.value ->
                displayType = ContextualHelp.REWARD.value

            ScreenName.AccountScreen.value ->
                displayType = ContextualHelp.ACCOUNT.value

            ScreenName.MerchantScreen.value ->
                displayType = ContextualHelp.MERCHANT.value

            ScreenName.SecurityScreen.value ->
                displayType = ContextualHelp.SECURITY.value

            ScreenName.Collection.value ->
                displayType = ContextualHelp.COLLECTION.value

            ScreenName.CustomerProfile.value ->
                displayType = ContextualHelp.CUSTOMER_PROFILE.value

            ScreenName.SupplierScreen.value ->
                displayType = ContextualHelp.SUPPLIER.value

            ScreenName.SupplierProfile.value ->
                displayType = ContextualHelp.SUPPLIER_PROFILE.value
            ScreenName.SupplierTxnDetailsScreen.value ->
                displayType = ContextualHelp.SUPPLIER_TRANSACTION.value
            ScreenName.LanguageScreen.value ->
                displayType = ContextualHelp.LANGUAGE.value
            ScreenName.ShareOkCreditScreen.value ->
                displayType = ContextualHelp.SHARE_OKC.value
        }

        compositeDisposable.add(
            UseCase.wrapObservable(
                userSupport.getContextualHelpIds(displayType)
            )
                .subscribeOn(ThreadUtils.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (it is Result.Success) {
                        setContextualHelpIds(it.value)
                    }
                    setContextualHelpVisibility()
                }
        )
    }
}
