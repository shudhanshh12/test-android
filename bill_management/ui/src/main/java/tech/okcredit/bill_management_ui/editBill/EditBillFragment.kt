package tech.okcredit.bill_management_ui.editBill

import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.camera.camera_preview_images.CameraImagesPreview
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.joda.time.DateTime
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.bill_management_ui.R
import tech.okcredit.bill_management_ui.bill_camera.BillCameraActivity
import tech.okcredit.bill_management_ui.databinding.BillImageDetailFragmentBinding
import tech.okcredit.bill_management_ui.editBill.EditBillContract.*
import tech.okcredit.bills.BILL_INTENT_EXTRAS
import tech.okcredit.camera_contract.CapturedImage
import tech.okcredit.sdk.analytics.BillTracker
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class EditBillFragment :
    BaseFragment<State, ViewEvent, Intent>(
        "BillImageDetailScreen",
        R.layout.bill_image_detail_fragment
    ),
    CameraImagesPreview.PreviewInteractor {

    val EDIT_BILL_REQUEST_CODE = 1
    var positionSet = false
    private val binding: BillImageDetailFragmentBinding by viewLifecycleScoped(BillImageDetailFragmentBinding::bind)
    private val deleteBillDoc: PublishSubject<String> = PublishSubject.create()
    private val deleteBill: PublishSubject<String> = PublishSubject.create()

    @Inject
    internal lateinit var billTracker: Lazy<BillTracker>

    override fun loadIntent(): UserIntent {
        return Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            deleteBillDoc.distinctUntilChanged().throttleFirst(300, TimeUnit.MILLISECONDS).map {
                Intent.DeleteBillDoc(it)
            },
            deleteBill.distinctUntilChanged().throttleFirst(300, TimeUnit.MILLISECONDS).map {
                Intent.DeleteBill
            }
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EDIT_BILL_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data?.getSerializableExtra("addedImages") != null) {
                    val list = data.getSerializableExtra("addedImages") as ArrayList<CapturedImage>
                    viewLifecycleOwner.lifecycleScope.launchWhenResumed {
                        pushIntent(Intent.NewImages(list))
                    }
                }
            }
        }
    }

    override fun render(state: State) {
        state.imageList?.let {
            if (it.isNotEmpty()) {
                binding.cameraPreview.setImages(it)
            }
        }
        state.initialPosition?.let {
            if (!positionSet) {
                positionSet = true
                binding.cameraPreview.setActiveItem(it)
            }
        }

        if (isDeleteVisible(state)) {
            binding.delete.visible()
        } else {
            binding.delete.gone()
        }
    }

    // TODO: Hack for V1 Release. Should show delete button and handle navigation for V2.
    private fun isDeleteVisible(state: State): Boolean {
        var noOfNonDeletedItems = 0
        state.localBill?.localBillDocList?.map {
            if (it.deletedAt == null) {
                noOfNonDeletedItems++
            }
        }
        return noOfNonDeletedItems > 1
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.cameraPreview.setListener(this)
        binding.delete.setOnClickListener {
            val deletedImage = binding.cameraPreview.onImageDeleted()
            getCurrentState().localBill?.localBillDocList?.forEach {
                if (it.url == deletedImage?.file?.toString()?.replace("https:/", "https://")) {
                    if (getCurrentState().localBill?.localBillDocList?.size == 1) {
                        deleteBill.onNext(getCurrentState().localBill?.id!!)
                    } else {
                        deleteBillDoc.onNext(it.billDocId)
                    }
                }
            }
        }
        binding.back.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            ViewEvent.GoBack -> requireActivity().onBackPressed()
            is ViewEvent.EditBillEvent -> billTracker.get().trackAddBillSuccess(
                size = event.count,
                date = DateTime(getCurrentState().localBill?.billDate?.toLong()),
                note = getCurrentState().localBill?.note,
                billId = getCurrentState().localBill?.id,
                defaultDateChange = false,
                label = "Updated",
                flow = "Edit Bill",
            )
        }
    }

    override fun onLastImageDeletion() {
    }

    override fun onFirstItemLeftScrolled() {
    }

    override fun onCameraClicked() {
        billTracker.get().trackAddMoreBillManagement(
            flow = "Edit Bill",
            billId = getCurrentState().localBill?.id
        )
        startActivityForResult(
            BillCameraActivity.createIntent(
                requireContext(), "Edit bill",
                "Customer",
                "aonr",
                "Add Screen",
                "123",
                "123",
                1,
                BILL_INTENT_EXTRAS.EDIT_FLOW,
                0
            ),
            EDIT_BILL_REQUEST_CODE
        )
    }
}
