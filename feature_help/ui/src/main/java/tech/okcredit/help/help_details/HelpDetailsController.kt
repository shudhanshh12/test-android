package tech.okcredit.help.help_details

import com.airbnb.epoxy.AsyncEpoxyController
import tech.okcredit.help.BuildConfig
import tech.okcredit.help.help_details.views.helpDetailsView
import tech.okcredit.help.help_details.views.likeDislikeView
import tech.okcredit.help.help_details.views.titleForHelpDetailView
import tech.okcredit.help.help_details.views.youtubeVideoView
import timber.log.Timber
import javax.inject.Inject

class HelpDetailsController @Inject
constructor(private val helpDetailsFragment: HelpDetailsFragment) : AsyncEpoxyController() {
    private lateinit var state: HelpDetailsContract.State

    init {
        isDebugLoggingEnabled = BuildConfig.DEBUG
    }

    fun setState(state: HelpDetailsContract.State) {
        this.state = state
        requestModelBuild()
    }

    override fun buildModels() {
        Timber.d("state=>: $state")
        if (state.helpItemV2 != null) {
            if (state.helpItemV2?.sub_title != null) {
                titleForHelpDetailView {
                    id("title_text")
                    titleForHelpDetail(state.helpItemV2!!.sub_title!!)
                }
            }
            if (state.helpItemV2?.instructions.isNullOrEmpty()?.not()) {
                state.helpItemV2?.instructions!!.forEachIndexed { index, helpInstruction ->
                    helpDetailsView {
                        id(helpInstruction.id)
                        detailText(helpInstruction.title)
                        indexText(index + 1)
                        itemImage(helpInstruction)
                    }
                }
                if (state.helpItemV2 != null && state.helpItemV2?.id?.isNotEmpty() == true && state.helpItemV2?.video_type?.isNotEmpty() == true) {
                    youtubeVideoView {
                        id(state.helpItemV2?.id)
                        helpItemValue(state.helpItemV2!!)
                        initYoutubePlayer(helpDetailsFragment)
                    }
                }
                likeDislikeView {
                    checkLikeOrDisLike(state.likeState)
                    id(state.helpItemV2?.sub_title)
                    helpItemId(state.helpItemV2!!.id)
                    onLikeClick(helpDetailsFragment)
                }
            }
        }
    }
}
