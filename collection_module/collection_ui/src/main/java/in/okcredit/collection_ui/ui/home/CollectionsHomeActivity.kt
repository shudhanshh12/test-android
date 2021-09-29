package `in`.okcredit.collection_ui.ui.home

import `in`.okcredit.collection.contract.MerchantDestinationListener
import `in`.okcredit.collection_ui.R
import `in`.okcredit.collection_ui.databinding.ActivityCollectionsHomeBinding
import `in`.okcredit.collection_ui.ui.home.CollectionsHomeActivityContract.State
import `in`.okcredit.collection_ui.ui.home.CollectionsHomeActivityContract.ViewEvent
import `in`.okcredit.collection_ui.ui.home.adoption.CollectionAdoptionV2Fragment
import `in`.okcredit.collection_ui.ui.home.adoption.CollectionAdoptionV2Fragment.Companion.ARG_REFERRAL_MERCHANT_ID
import `in`.okcredit.collection_ui.ui.home.merchant_qr.QrCodeFragment
import `in`.okcredit.shared.base.BaseActivity
import `in`.okcredit.shared.base.UserIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import io.reactivex.Observable
import tech.okcredit.android.base.extensions.replaceFragment
import tech.okcredit.android.base.extensions.viewLifecycleScoped

class CollectionsHomeActivity :
    BaseActivity<State, ViewEvent, CollectionsHomeActivityContract.Intent>(
        "CollectionsHomeActivity"
    ),
    MerchantDestinationListener {

    private val binding: ActivityCollectionsHomeBinding by viewLifecycleScoped(ActivityCollectionsHomeBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        lifecycleScope.launchWhenResumed {
            supportFragmentManager.findFragmentById(R.id.fragmentHolder)?.onActivityResult(
                requestCode,
                resultCode,
                data
            )
        }
    }

    override fun onAccountAddedSuccessfully(eta: Long) {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentHolder)
        if (currentFragment != null && currentFragment is QrCodeFragment) {
            currentFragment.onMerchantDestinationAdded()
        } else if (currentFragment != null && currentFragment is CollectionAdoptionV2Fragment) {
            moveToMerchantQr()
        }
    }

    override fun onCancelled() {}

    companion object {

        @JvmStatic
        fun getIntent(context: Context) = Intent(context, CollectionsHomeActivity::class.java)
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.ambArray()
    }

    override fun render(state: State) {
        binding.progressBar.isVisible = state.loading
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            is ViewEvent.CollectionAdoption -> moveToCollectionAdoption(event.referralMerchantId)
            ViewEvent.QrScreen -> moveToMerchantQr()
        }
    }

    private fun moveToMerchantQr() {
        val fragment = QrCodeFragment()
        supportFragmentManager.replaceFragment(fragment, addToBackStack = false, holder = R.id.fragmentHolder)
    }

    private fun moveToCollectionAdoption(referralMerchantId: String?) {
        val fragment = CollectionAdoptionV2Fragment().apply {
            arguments = bundleOf(ARG_REFERRAL_MERCHANT_ID to referralMerchantId)
        }
        supportFragmentManager.replaceFragment(fragment, addToBackStack = false, holder = R.id.fragmentHolder)
    }

    override fun loadIntent(): UserIntent {
        return CollectionsHomeActivityContract.Intent.Load
    }
}
