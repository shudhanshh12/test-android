package tech.okcredit.android.base.utils;

import com.google.android.gms.common.util.Strings;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;

import org.joda.time.DateTime;

public final class GsonUtil {
    public static Gson getGson() {
        return new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(String.class, new TypeAdapter_String())
                .registerTypeAdapter(DateTime.class, new TypeAdapter_DateTime())
                .create();
    }

    public static class TypeAdapter_String
            implements JsonSerializer<String>, JsonDeserializer<String> {
        @Override
        public String deserialize(
                JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return Strings.emptyToNull(json.getAsString());
        }

        @Override
        public JsonElement serialize(String src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src);
        }
    }

    public static class TypeAdapter_DateTime
            implements JsonSerializer<DateTime>, JsonDeserializer<DateTime> {
        @Override
        public JsonElement serialize(
                DateTime src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(DateTimeMapper.toEpoch(src));
        }

        @Override
        public DateTime deserialize(
                JsonElement jsonElement, Type type, JsonDeserializationContext context)
                throws JsonParseException {
            long epoch = jsonElement.getAsLong();
            return DateTimeMapper.fromEpoch(epoch);
        }
    }

    private GsonUtil() {}

}
