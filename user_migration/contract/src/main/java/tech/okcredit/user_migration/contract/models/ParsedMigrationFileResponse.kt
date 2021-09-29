package tech.okcredit.user_migration.contract.models

import com.google.gson.annotations.SerializedName

data class ParsedMigrationFileResponse(

    @SerializedName("data") val data: List<Data>,
    @SerializedName("file") val file: String,
    @SerializedName("file_object_id") val file_object_id: String
)
