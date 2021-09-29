package tech.okcredit.help.help_main

import com.airbnb.epoxy.AsyncEpoxyController
import tech.okcredit.help.BuildConfig
import tech.okcredit.help.help_main.views.helpMainItem
import tech.okcredit.help.help_main.views.helpSectionView
import timber.log.Timber
import javax.inject.Inject

class HelpController @Inject
constructor(private val helpFragment: HelpFragment) : AsyncEpoxyController() {
    private lateinit var state: HelpContract.State

    init {
        isDebugLoggingEnabled = BuildConfig.DEBUG
    }

    fun setState(state: HelpContract.State) {
        this.state = state
        requestModelBuild()
    }

    override fun buildModels() {
        Timber.d("$state")

        if (state.help.isNullOrEmpty().not()) {
            state.help?.forEachIndexed { index, helpV2 ->
                helpMainItem {
                    id(helpV2.id)
                    helpSectionIcon(helpV2.icon)
                    helpSectionId(helpV2.id)
                    helpText(helpV2.title)
                    isExpanded(helpV2.id == state.expandedId)
                    onExpandIconClick(helpFragment)
                }
                if (helpV2.id == state.expandedId) {
                    helpV2.help_items?.forEachIndexed { index, helpItemV2 ->
                        Timber.d("<<<<HelpItem ${index + 1 == helpV2.help_items?.size}")
                        helpSectionView {
                            id(helpItemV2.id)
                            helpSectionItemText(helpItemV2.title)
                            isFinalItem(index + 1 == helpV2.help_items?.size)
                            itemId(helpItemV2.id)
                            sectionClick(helpFragment)
                        }
                    }
                }
            }
        }

//        if(state.help?.isNullOrEmpty()?.not()){
//            state.help?.sections?.forEach {
//                helpMainItem {
//                    id(it.id)
//                    helpSectionIcon(it.iconUrl)
//                    helpSectionId(it.id)
//                    helpText(it.title)
//                    isExpanded(if(it.id == state.expandedId) true else false)
//                    onExpandIconClick(helpScreen)
//                }
//                if(it.id == state.expandedId){
//                    it.helpItems.forEachIndexed { index, helpItem ->
//                        helpSectionView {
//                            id(helpItem.id)
//                            helpSectionItemText(helpItem.title)
//                            itemId(helpItem.id)
//                            sectionClick(helpScreen)
//                        }
//                    }
//                }
//            }
//        }
    }
}
