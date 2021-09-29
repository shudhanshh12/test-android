package `in`.okcredit.collection_ui.ui.inventory.add_item_dialog

import `in`.okcredit.collection.contract.InventoryItem
import `in`.okcredit.collection_ui.R
import `in`.okcredit.collection_ui.analytics.InventoryEventTracker
import `in`.okcredit.collection_ui.databinding.BottomSheetDialogAddBillinItemBinding
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.Lazy
import dagger.android.support.AndroidSupportInjection
import tech.okcredit.android.base.edit_text.DecimalDigitsInputFilter
import tech.okcredit.android.base.extensions.capitalizeWords
import tech.okcredit.android.base.extensions.shortToast
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import javax.inject.Inject

class AddInventoryItemBottomSheetDialog : BottomSheetDialogFragment() {

    private var listener: AddBillDialogListener? = null

    private var billingItem = InventoryItem()

    interface AddBillDialogListener {
        fun onSubmitAddBillItem(billItem: InventoryItem)
    }

    private val binding: BottomSheetDialogAddBillinItemBinding by viewLifecycleScoped(
        BottomSheetDialogAddBillinItemBinding::bind
    )

    @Inject
    lateinit var inventoryEventTracker: Lazy<InventoryEventTracker>

    private var isNameEditable: Boolean = false
    private var screen: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogThemeWithKeyboard)
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return BottomSheetDialogAddBillinItemBinding.inflate(inflater, container, false).root
    }

    fun setListener(listener: AddBillDialogListener) {
        this.listener = listener
    }

    override fun onDetach() {
        listener = null
        super.onDetach()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setClickListener()
        setExtra()
    }

    private fun setExtra() {
        arguments?.let {
            it.getString(ARG_BILLING_ITEM)?.let {
                if (it.isNotEmpty()) {
                    binding.editTextName.setText(it.capitalizeWords())
                    try {
                        binding.editTextName.setSelection(it.length)
                    } catch (ex: Exception) {
                    }

                    billingItem.item = it.lowercase()
                }
            }
            it.getInt(ARG_BILLING_QTY).let {
                if (it > 0) {
                    billingItem.quantity = it
                    binding.editTextQuantity.setText(it.toString())
                }
            }
            it.getLong(ARG_BILLING_PRICE).let {
                if (it > 0L) {
                    billingItem.price = it
                    binding.editTextRate.setText(it.toDouble().div(100).toString())
                }
            }
            it.getBoolean(ARG_IS_NAME_EDITABLE).let {
                binding.editTextName.isEnabled = it
                isNameEditable = it
                if (it) {
                    binding.editTextName.requestFocus()
                } else {
                    binding.editTextRate.requestFocus()
                }
            }

            it.getString(ARG_SCREEN)?.let {
                if (it.isNotEmpty()) {
                    screen = it
                }
            }
        }
    }

    private fun setClickListener() {
        binding.apply {

            editTextRate.filters = arrayOf(DecimalDigitsInputFilter(7, 2))

            editTextQuantity.addTextChangedListener {
                if (!it.isNullOrEmpty())
                    billingItem.quantity = it.toString().toInt()
            }
            textPlus.setOnClickListener {
                billingItem.quantity += 1
                editTextQuantity.setText(billingItem.quantity.toString())
            }

            textMinus.setOnClickListener {
                if (billingItem.quantity > 0) {
                    billingItem.quantity -= 1
                    editTextQuantity.setText(billingItem.quantity.toString())
                } else {
                    shortToast(R.string.inventory_quantity_already_min)
                }
            }

            editTextRate.addTextChangedListener {
                if (it.isNullOrEmpty().not()) {
                    billingItem.price = it.toString().toDouble().times(100).toLong()
                }
            }

            editTextName.addTextChangedListener {
                if (!it.isNullOrEmpty())
                    billingItem.item = it.toString().lowercase()
            }

            buttonSubmit.setOnClickListener {
                if (billingItem.quantity > 0 && billingItem.item.isNotEmpty() && billingItem.price > 0L) {
                    if (isNameEditable) {
                        inventoryEventTracker.get().trackBillingNewItemSaved(
                            screen = screen,
                            name = billingItem.item,
                            rate = billingItem.price.toString(),
                            quantity = billingItem.quantity.toString(),
                        )
                    } else {
                        inventoryEventTracker.get().trackBillingNewItemEdited(
                            screen = screen,
                            name = billingItem.item,
                            rate = billingItem.price.toString(),
                            quantity = billingItem.quantity.toString(),
                        )
                    }

                    listener?.onSubmitAddBillItem(billingItem)
                    dismissAllowingStateLoss()
                } else {
                    shortToast(R.string.inventory_fill_details_correctly)
                }
            }
        }
    }

    companion object {
        const val ARG_BILLING_ITEM = "item"
        const val ARG_BILLING_QTY = "qty"
        const val ARG_BILLING_PRICE = "price"
        const val ARG_IS_NAME_EDITABLE = "is_name_editable"
        const val ARG_SCREEN = "screen"
        const val TAG = "AddInventoryItemBottomSheetDialog"
        fun newInstance(
            item: String = "",
            quantity: Int = 0,
            price: Long = 0L,
            isNameEditable: Boolean = true,
            screen: String,
        ): AddInventoryItemBottomSheetDialog {
            val frag = AddInventoryItemBottomSheetDialog()
            val bundle = Bundle()
            bundle.putString(ARG_BILLING_ITEM, item)
            bundle.putInt(ARG_BILLING_QTY, quantity)
            bundle.putLong(ARG_BILLING_PRICE, price)
            bundle.putBoolean(ARG_IS_NAME_EDITABLE, isNameEditable)
            bundle.putString(ARG_SCREEN, screen)
            frag.arguments = bundle
            return frag
        }
    }
}
