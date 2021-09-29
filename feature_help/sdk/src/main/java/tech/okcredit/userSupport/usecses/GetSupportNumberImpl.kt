package tech.okcredit.userSupport.usecses

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Lazy
import dagger.Reusable
import tech.okcredit.feature_help.contract.GetSupportNumber
import tech.okcredit.feature_help.contract.HelpConstants.FRC_SUPPORT_NUMBER_KEY
import javax.inject.Inject

@Reusable
class GetSupportNumberImpl @Inject constructor(val firebaseRemoteConfig: Lazy<FirebaseRemoteConfig>) :
    GetSupportNumber {

    override val supportNumber: String = firebaseRemoteConfig.get().getString(FRC_SUPPORT_NUMBER_KEY)
}
