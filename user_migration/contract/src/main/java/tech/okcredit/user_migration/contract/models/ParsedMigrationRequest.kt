package tech.okcredit.user_migration.contract.models

import com.google.gson.annotations.SerializedName

data class ParsedMigrationRequest(
    @SerializedName("urls")
    val urls: List<String?>,
    @SerializedName("merchant_id")
    val merchantId: String,
)

data class FileUrlAndName(
    @SerializedName("url")
    val url: String,
    @SerializedName("file_name")
    val fileName: String
)
