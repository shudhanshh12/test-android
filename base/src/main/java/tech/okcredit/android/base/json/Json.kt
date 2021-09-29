@file:JvmName("JsonUtils")
@file:JvmMultifileClass

package tech.okcredit.android.base.json

import com.google.gson.*
import org.joda.time.DateTime
import tech.okcredit.android.base.datetime.fromEpoch
import tech.okcredit.android.base.datetime.toEpoch
import java.lang.reflect.Type

object GsonUtils {
    private val gson by lazy {
        GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(String::class.java, String_TypeAdapter)
            .registerTypeAdapter(DateTime::class.java, DateTime_TypeAdapter)
            .create()
    }

    private val gsonVanillaInstance by lazy {
        Gson()
    }

    fun gsonVanillaInstance(): Gson = gsonVanillaInstance

    fun gson(): Gson = gson
}

// json for any object
fun Any?.json(): String = GsonUtils.gson().toJson(this)

/****************************************************************
 * Type Adapters
 ****************************************************************/

// String
private object String_TypeAdapter : JsonSerializer<String>, JsonDeserializer<String> {
    override fun serialize(src: String?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(src)
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): String? {
        val s = json?.asString
        return if (s.isNullOrBlank()) null else s
    }
}

// DateTime
private object DateTime_TypeAdapter : JsonSerializer<DateTime>, JsonDeserializer<DateTime> {
    override fun serialize(src: DateTime?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(toEpoch(src))
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): DateTime? {
        return try {
            fromEpoch(json?.asLong!!)
        } catch (e: Exception) {
            null
        }
    }
}
