package `in`.okcredit.onboarding.launcher

import `in`.okcredit.shared._base_v2.MVP

interface LauncherContract {
    interface Presenter : MVP.Presenter<View?>

    interface View : MVP.View {
        fun gotoHome()

        fun setupAppLock()

        fun goToLanguageSelectionScreen()

        fun goToEnterMobileScreen()

        fun authenticateViaNewAppLock()

        fun authenticateViaOldAppLOck()

        fun goToEnterBusinessName()
    }
}
