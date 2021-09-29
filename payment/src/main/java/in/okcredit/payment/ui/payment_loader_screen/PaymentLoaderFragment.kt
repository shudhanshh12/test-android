package `in`.okcredit.payment.ui.payment_loader_screen

import `in`.okcredit.payment.databinding.PaymentLoaderFragmentBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import tech.okcredit.android.base.extensions.viewLifecycleScoped

class PaymentLoaderFragment : Fragment() {

    internal val binding: PaymentLoaderFragmentBinding by viewLifecycleScoped(
        PaymentLoaderFragmentBinding::bind
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return PaymentLoaderFragmentBinding.inflate(layoutInflater).root
    }
}
