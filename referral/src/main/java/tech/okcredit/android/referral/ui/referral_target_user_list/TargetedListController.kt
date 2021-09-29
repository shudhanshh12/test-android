package tech.okcredit.android.referral.ui.referral_target_user_list

import `in`.okcredit.referral.contract.models.TargetedUser
import com.airbnb.epoxy.AsyncEpoxyController
import dagger.Lazy
import tech.okcredit.android.referral.ui.referral_target_user_list.views.ConvertedTargetedUserItemView.ConvertedTargetedUserActionListener
import tech.okcredit.android.referral.ui.referral_target_user_list.views.UnconvertedTargetedUserItemView.UnconvertedTargetedUserActionListener
import tech.okcredit.android.referral.ui.referral_target_user_list.views.convertedTargetedUserItemView
import tech.okcredit.android.referral.ui.referral_target_user_list.views.unconvertedTargetedUserItemView
import javax.inject.Inject

class TargetedListController @Inject constructor(
    private val unconvertedTargetedUserActionListener: Lazy<UnconvertedTargetedUserActionListener>,
    private val convertedTargetedUserActionListener: Lazy<ConvertedTargetedUserActionListener>,
) : AsyncEpoxyController() {

    private var targetedUsers: List<TargetedUser>? = null

    fun setTargetedUserList(targetedUsers: List<TargetedUser>?) {
        this.targetedUsers = targetedUsers
        requestModelBuild()
    }

    override fun buildModels() {
        targetedUsers?.forEach {
            if (!it.converted) {
                unconvertedTargetedUserItemView {
                    id(modelCountBuiltSoFar)
                    listener(unconvertedTargetedUserActionListener.get())
                    targetedUserItem(it)
                }
            } else {
                convertedTargetedUserItemView {
                    id(modelCountBuiltSoFar)
                    listener(convertedTargetedUserActionListener.get())
                    targetedUserItem(it)
                }
            }
        }
    }
}
