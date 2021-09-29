package tech.okcredit.android

import `in`.okcredit.referral.contract.models.ReferralApiMessages
import `in`.okcredit.referral.contract.models.ShareContent
import `in`.okcredit.referral.contract.models.TargetedUser
import `in`.okcredit.shared.referral_views.model.ReferralTargetBanner

object TestData {

    val BUSINESS_ID = "business-id"
    val TARGETED_USERS = listOf(
        TargetedUser(
            "1",
            "Jerry seinfeld",
            "9123456789",
            "https://www.hotmodelsactress.com/actors/images/Jerry-Seinfeld.jpg",
            "Amazon Prime",
            true,
            8000
        ),
        TargetedUser(
            "2",
            "Elaine Benes",
            "9876543219",
            "https://encrypted-tbn0.gstatic.com/images?q=tbn%3AANd9GcTxEddWN3Wz1gGVyfevyq9MT8C9qTewxWolqg&usqp=CAU",
            "Amazon Prime",
            false,
            4000
        )
    )

    val TARGETED_CONTENT = ShareContent(
        "Kramer has invited you to install the app and earn money",
        "https://pmcdeadline2.files.wordpress.com/2019/09/seinfeld-1.jpg?w=681&h=383&crop=1"
    )
    val GENERIC_CONTENT = ShareContent(
        "Download OKCredit from this link and help me earn Rs.30",
        "https://pmcdeadline2.files.wordpress.com/2019/09/seinfeld-1.jpg?w=681&h=383&crop=1"
    )
    val SHARE_CONTENT_RESPONSE = ReferralApiMessages.GetShareContentResponse(TARGETED_CONTENT, GENERIC_CONTENT)

    val listOfReferralTargets = listOf(
        ReferralTargetBanner(
            id = "1234",
            referrerMerchantPrize = 2500L,
            referralMerchantPrize = 2500L,
            isActivated = listOf(),
            title = "add_transaction",
            description = "Get Rs. 15/- directly in your bank account.",
            icon = "Add first Transaction",
            deepLink = "https://okcredit.app/merchant/v1/home/add_customer",
            bannerPlace = listOf(1, 0),
            howDoesItWorks = ""
        )
    )

    val referralTarget = ReferralTargetBanner(
        id = "1234",
        referrerMerchantPrize = 2500L,
        referralMerchantPrize = 2500L,
        isActivated = listOf(),
        title = "add_transaction",
        description = "Get Rs. 15/- directly in your bank account.",
        icon = "Add first Transaction",
        deepLink = "https://okcredit.app/merchant/v1/home/add_customer",
        bannerPlace = listOf(1, 0),
        howDoesItWorks = ""
    )
}
