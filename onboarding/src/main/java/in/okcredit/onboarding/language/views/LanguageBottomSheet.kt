package `in`.okcredit.onboarding.language.views

import `in`.okcredit.analytics.Tracker
import `in`.okcredit.onboarding.R
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics
import `in`.okcredit.onboarding.contract.autolang.LanguageSelectedListener
import `in`.okcredit.onboarding.databinding.AutoLangBottomSheetBinding
import `in`.okcredit.onboarding.language.usecase.GetSortedLanguages
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.Lazy
import dagger.android.support.AndroidSupportInjection
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.android.base.language.LocaleManager
import javax.inject.Inject

class LanguageBottomSheet : BottomSheetDialogFragment() {

    @Inject
    lateinit var localeManager: Lazy<LocaleManager>

    @Inject
    lateinit var getSortedLanguages: Lazy<GetSortedLanguages>

    @Inject
    lateinit var onboardingAnalytics: Lazy<OnboardingAnalytics>

    private var hasChangedLanguage = false

    @Inject
    lateinit var tracker: Lazy<Tracker>

    private val binding: AutoLangBottomSheetBinding by viewLifecycleScoped(AutoLangBottomSheetBinding::bind)
    private var listener: LanguageSelectedListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return AutoLangBottomSheetBinding.inflate(layoutInflater).root
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onboardingAnalytics.get().trackLanguageBottomSheetShown()
        loadLanguageData()
    }

    override fun onDismiss(dialog: DialogInterface) {
        onboardingAnalytics.get().trackLanguageBottomSheetDismissed(hasChangedLanguage)
        super.onDismiss(dialog)
        listener?.onDismissed()
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    fun setListener(listener: LanguageSelectedListener) {
        this.listener = listener
    }

    private fun loadLanguageData() = lifecycleScope.launchWhenCreated {
        binding.languageLoadingProgress.visible()

        val currentLanguageCode = localeManager.get().getLanguage()
        val languageList = getSortedLanguages.get().execute(currentLanguageCode)

        with(binding.rvLanguage) {
            val itemSpacing = resources.getDimension(R.dimen._16dp).toInt()
            addItemDecoration(GridItemSpacingDecoration(itemSpacing))
            adapter = LanguageAdapter(currentLanguageCode, languageList) { language ->
                hasChangedLanguage = true
                tracker.get().setLangSuperProperty(language.languageCode)
                listener?.onSelected(language, this@LanguageBottomSheet)
            }
        }

        binding.languageLoadingProgress.gone()
    }

    companion object {
        val TAG: String = LanguageBottomSheet::class.java.simpleName
    }
}
