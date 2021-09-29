package `in`.okcredit.onboarding.language.views

import `in`.okcredit.onboarding.R
import `in`.okcredit.onboarding.contract.autolang.Language
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.onboarding_language_item.view.*

private typealias LanguageListener = (language: Language) -> Unit

class LanguageAdapter(
    private val currentLanguageCode: String,
    private val languageList: List<Language>,
    private val onLanguageSelected: LanguageListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.auto_lang_bottom_sheet_item, parent, false)
        return LanguageTitleViewHolder(view)
    }

    override fun getItemCount(): Int {
        return languageList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val languageView = holder as LanguageTitleViewHolder
        val language = languageList[position]
        val isSelected = language.languageCode == currentLanguageCode

        languageView.bindView(language, isSelected, onLanguageSelected)
    }

    class LanguageTitleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindView(
            language: Language,
            isSelected: Boolean,
            listener: LanguageListener
        ) = if (language.languageCode.isBlank()) {
            itemView.cvRoot.visibility = View.GONE
        } else {
            with(itemView.cvRoot) {
                visibility = View.VISIBLE
                setCardBackgroundColor(language.backgroundColor)
                setOnClickListener { listener(language) }
            }
            with(itemView.tvTitleLanguage) {
                text = language.languageTitle
                setTextColor(language.fontColor)
            }
            with(itemView.tvSubTitleLanguage) {
                text = language.languageSubTitle
                setTextColor(language.fontColor)
                isVisible = language.languageSubTitle.isNotBlank()
            }

            itemView.ivLangSelected.isVisible = isSelected
        }
    }
}
