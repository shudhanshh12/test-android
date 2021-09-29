package `in`.okcredit.sales_ui.ui.view_sale

import `in`.okcredit.analytics.Event
import `in`.okcredit.analytics.PropertiesMap
import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.sales_ui.R
import `in`.okcredit.sales_ui.databinding.SalesDetailScreenBinding
import `in`.okcredit.sales_ui.dialogs.DeleteSaleBottomSheetDialog
import `in`.okcredit.sales_ui.utils.SalesUtil
import `in`.okcredit.shared.base.BaseScreen
import `in`.okcredit.shared.base.UserIntent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment.findNavController
import com.google.android.material.snackbar.Snackbar
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import tech.okcredit.android.base.extensions.snackbar
import tech.okcredit.android.base.utils.DateTimeUtils
import tech.okcredit.app_contract.LegacyNavigator
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.roundToInt

class SalesDetailFragment :
    BaseScreen<SalesDetailContract.State>("SalesDetailScreen"),
    SalesDetailContract.Navigator,
    DeleteSaleBottomSheetDialog.DeleteDialogListener {

    @Inject
    internal lateinit var legacyNavigator: LegacyNavigator

    @Inject
    internal lateinit var tracker: Tracker

    lateinit var binding: SalesDetailScreenBinding

    private val showDeleteDialog: PublishSubject<Unit> = PublishSubject.create()
    private val deleteSale: PublishSubject<String> = PublishSubject.create()
    private var alert: Snackbar? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = SalesDetailScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }
        binding.delete.setOnClickListener {
            if (isStateInitialized() && getCurrentState().sale != null) {
                tracker.trackEvents(
                    eventName = Event.DELETE_CASH_SALE_CLICK,
                    screen = PropertyValue.CASH_SALE_TX,
                    propertiesMap = PropertiesMap.create()
                        .add("Tx_id", getCurrentState().sale!!.id)
                        .add("Amount", getCurrentState().sale!!.amount)
                )
                showDeleteDialog.onNext(Unit)
            }
        }
    }

    override fun loadIntent(): UserIntent {
        return SalesDetailContract.Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            showDeleteDialog.throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    SalesDetailContract.Intent.ShowDeleteDialog
                },
            deleteSale.throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    SalesDetailContract.Intent.DeleteSale(it)
                }
        )
    }

    override fun render(state: SalesDetailContract.State) {
        state.sale?.let {
            binding.notes.text = it.note
            binding.date.text = getString(R.string.added_on_date, DateTimeUtils.formatLong(it.saleDate))
            binding.amount.text = SalesUtil.currencyDisplayFormat(it.amount)
            if (it.amount.minus(it.amount.roundToInt()) == 0.0) {
                binding.amount.text = DecimalFormat("##,##,##0").format(it.amount)
            } else {
                binding.amount.text = String.format("%.2f", DecimalFormat("##,##,###.##").format(it.amount).toDouble())
            }
            if (it.buyerName != null) {
                binding.billingName.text = it.buyerName
                binding.billingName.visibility = View.VISIBLE
                binding.billingNameImg.visibility = View.VISIBLE
                binding.contactDetails.visibility = View.VISIBLE
                if (it.buyerMobile != null) {
                    binding.billingMobile.text = it.buyerMobile
                    binding.billingMobile.visibility = View.VISIBLE
                    binding.billingMobileImg.visibility = View.VISIBLE
                } else {
                    binding.billingMobile.visibility = View.GONE
                }
            } else {
                binding.billingName.visibility = View.GONE
                binding.billingNameImg.visibility = View.GONE
                binding.billingMobile.visibility = View.GONE
                binding.billingMobileImg.visibility = View.GONE
                binding.contactDetails.visibility = View.GONE
            }
        }
        if (state.canShowAlert) {
            alert = when {
                state.alert.isNotEmpty() -> view?.snackbar(state.alert, Snackbar.LENGTH_INDEFINITE)
                else -> view?.snackbar(getString(R.string.err_default), Snackbar.LENGTH_INDEFINITE)
            }
            if (alert?.isShown?.not()!!) {
                alert?.show()
            }
        } else {
            alert?.dismiss()
        }
    }

    override fun gotoLogin() {
        activity?.runOnUiThread {
            legacyNavigator.goToLoginScreenForAuthFailure(requireActivity())
        }
    }

    override fun showDeleteDialog(saleId: String) {
        val deleteFragment = DeleteSaleBottomSheetDialog.newInstance(saleId)
        deleteFragment.setListener(this)
        deleteFragment.show(childFragmentManager, DeleteSaleBottomSheetDialog.TAG)
        deleteFragment.isCancelable = false
    }

    override fun onDeleted() {
        if (isStateInitialized()) {
            tracker.trackEvents(
                eventName = Event.CASH_SALE_DELETED,
                screen = PropertyValue.CASH_SALE_TX,
                propertiesMap = PropertiesMap.create()
                    .add("Tx id", getCurrentState().sale!!.id)
            )
            activity?.runOnUiThread {
                findNavController(this).popBackStack()
            }
        }
    }

    override fun onDelete(saleId: String) {
        deleteSale.onNext(saleId)
    }

    override fun onCancel() {
        tracker.trackEvents(eventName = Event.DELETE_CASH_SALE_CANCELLED, screen = PropertyValue.CASH_SALE_TX)
    }
}
