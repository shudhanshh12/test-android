package `in`.okcredit.merchant.contract

sealed class UpdateBusinessRequest() {
    data class UpdateBusinessName(
        val businessName: String
    ) : UpdateBusinessRequest()

    data class UpdateBusinessNameOnboarding(
        val businessName: String
    ) : UpdateBusinessRequest()

    data class UpdateName(
        val personName: String
    ) : UpdateBusinessRequest()

    data class UpdateProfileImage(
        val profileImage: String
    ) : UpdateBusinessRequest()

    data class UpdateCategory(
        val categoryId: String?,
        val categoryName: String?
    ) : UpdateBusinessRequest()

    data class UpdateEmail(
        val email: String
    ) : UpdateBusinessRequest()

    data class UpdateAddress(
        val address: String,
        val latitude: Double,
        val longitude: Double
    ) : UpdateBusinessRequest()

    data class UpdateAbout(
        val about: String
    ) : UpdateBusinessRequest()

    data class UpdateBusinessType(
        val businessTypeId: String
    ) : UpdateBusinessRequest()
}
