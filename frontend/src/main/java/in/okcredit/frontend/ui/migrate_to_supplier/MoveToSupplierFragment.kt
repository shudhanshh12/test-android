package `in`.okcredit.frontend.ui.migrate_to_supplier

import `in`.okcredit.analytics.Tracker
import `in`.okcredit.fileupload.usecase.IImageLoader
import `in`.okcredit.frontend.R
import `in`.okcredit.frontend.databinding.MoveToSupplierScreenBinding
import `in`.okcredit.frontend.ui._dialogs.RelationBottomSheeetDialog
import `in`.okcredit.shared.base.BaseScreen
import `in`.okcredit.shared.base.UserIntent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.snackbar.Snackbar
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import tech.okcredit.android.base.extensions.shortToast
import tech.okcredit.android.base.extensions.snackbar
import tech.okcredit.app_contract.LegacyNavigator
import javax.inject.Inject

class MoveToSupplierFragment :
    BaseScreen<MoveToSupplierContract.State>("MoveToSupplierScreen"),
    MoveToSupplierContract.Navigator,
    RelationBottomSheeetDialog.RelationBottomSheetDialogListener {

    private var fragment: RelationBottomSheeetDialog? = null
    private var isBackPressAllowed: Boolean = true

    @Inject
    internal lateinit var legacyNavigator: LegacyNavigator

    @Inject
    internal lateinit var imageLoader: IImageLoader

    @Inject
    internal lateinit var tracker: Tracker

    private val showConfirmDialogSubject: PublishSubject<Unit> = PublishSubject.create()
    private val migrateSubject: PublishSubject<Unit> = PublishSubject.create()

    private lateinit var binding: MoveToSupplierScreenBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = MoveToSupplierScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.moveToSupplier.setOnClickListener {
            tracker.trackAllowRelationShipMigration(
                "Customer",
                "Supplier",
                "Relationship Migration Screen",
                getCurrentState().customer?.id,
            )
            showConfirmDialogSubject.onNext(Unit)
        }
        binding.viewAccount.setOnClickListener {
            activity?.runOnUiThread {
                legacyNavigator.goToHome(requireActivity())
                activity?.finish()
            }
        }
        binding.home.setOnClickListener {
            activity?.runOnUiThread {
                gotoHomeScreen()
            }
        }
        binding.retry.setOnClickListener {
            tracker.trackRetryMigration(
                "Customer",
                "Supplier",
                "Relationship Migration Screen",
                getCurrentState().customer?.id,
            )

            activity?.runOnUiThread {
                migrateSubject.onNext(Unit)
            }
        }
        binding.rootView.setTracker(performanceTracker)
    }

    override fun loadIntent(): UserIntent {
        return MoveToSupplierContract.Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            showConfirmDialogSubject.map {
                MoveToSupplierContract.Intent.ShowConfirmDialog
            },
            migrateSubject.map {
                MoveToSupplierContract.Intent.Migrate
            }
        )
    }

    override fun render(state: MoveToSupplierContract.State) {

        if (state.networkError) {
            shortToast(getString(R.string.no_internet_msg))
        }
        if (state.error) {
            view?.snackbar("Failed", Snackbar.LENGTH_SHORT)
        }
        if (state.canShowLoaderScreen) {
            binding.loaderContainer.visibility = View.VISIBLE
            binding.loaderView.percent = state.progressValue.toFloat()
            binding.loadingValueTv.text = """${state.progressValue} %"""
        } else {
            binding.loaderContainer.visibility = View.GONE
        }
        if (state.canShowFailureScreen) {
            binding.failContainer.visibility = View.VISIBLE
        } else {
            binding.failContainer.visibility = View.GONE
        }
        if (state.canShowSuccessfulScreen) {
            binding.succesfulContainer.visibility = View.VISIBLE
        } else {
            binding.succesfulContainer.visibility = View.GONE
        }
        state.errorMessage?.let {
            if (it.isNotBlank()) {
                if (it == "account_migration_not_permitted") {
                    view?.snackbar(getString(R.string.migration_error), Snackbar.LENGTH_SHORT)?.show()
                } else if (it == "cyclic_account_exists") {
                    view?.snackbar(getString(R.string.cyclic_account_error), Snackbar.LENGTH_SHORT)?.show()
                }
            }
        }
    }

    override fun gotoLogin() {
        activity?.runOnUiThread {
            legacyNavigator.goToLoginScreenForAuthFailure(requireActivity())
        }
    }

    override fun gotoHomeScreen() {
        activity?.runOnUiThread {
            tracker.trackViewRelationshipV1(
                "true",
                "Customer",
                "false",
                "Relationship Migration Screen",
                getCurrentState().customer?.id,
                getCurrentState().commonLedger
            )

            legacyNavigator.goToHome(requireActivity())
            activity?.finish()
        }
    }

    override fun showConfirmDialog() {
        val customer = getCurrentState().customer
        if (customer != null) {
            fragment =
                requireActivity().supportFragmentManager.findFragmentByTag(RelationBottomSheeetDialog.TAG) as? RelationBottomSheeetDialog
            if (fragment == null) {
                fragment = RelationBottomSheeetDialog.newInstance(
                    title = getString(R.string.move_to_supplier),
                    description = getString(R.string.please_confirm_moving_customer),
                    name = customer.description,
                    number = customer.mobile,
                    profileImg = customer.profileImage,
                    imageLoader = imageLoader
                )
                fragment?.setListener(this)
                fragment?.show(requireActivity().supportFragmentManager, RelationBottomSheeetDialog.TAG)
            }
        }
    }

    override fun onConfirm(action: String) {
        tracker.trackConfirmRelationShipMigration(
            "Customer",
            "Supplier",
            "Relationship Migration Screen",
            getCurrentState().customer?.id,
        )
        isBackPressAllowed = false
        fragment?.dismiss()
        migrateSubject.onNext(Unit)
    }

    override fun onDismiss() {
        tracker.trackCancelRelationShipMigration(
            "Customer",
            "Supplier",
            "Relationship Migration Screen",
            getCurrentState().customer?.id,
        )
    }

    // we return true to stop back press
    override fun onBackPressed(): Boolean {
        if (isBackPressAllowed)
            return super.onBackPressed() else return true
    }
}
