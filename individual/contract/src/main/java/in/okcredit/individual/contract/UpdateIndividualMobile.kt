package `in`.okcredit.individual.contract

interface UpdateIndividualMobile {
    suspend fun execute(
        mobile: String,
        currentMobileOtpToken: String,
        newMobileOtpToken: String,
        individualId: String,
        businessId: String,
    )
}
