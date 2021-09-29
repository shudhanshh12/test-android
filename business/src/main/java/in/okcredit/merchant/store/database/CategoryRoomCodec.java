package in.okcredit.merchant.store.database;

import androidx.room.TypeConverter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public final class CategoryRoomCodec {
    @TypeConverter
    public static BusinessCategory fromEpoch(String categoryStr) {
        Type categoryObj = new TypeToken<BusinessCategory>() {}.getType();
        return new Gson().fromJson(categoryStr, categoryObj);
    }

    @TypeConverter
    public static String toEpoch(BusinessCategory category) {
        Gson gson = new Gson();
        String json = gson.toJson(category);
        return json;
    }

    private CategoryRoomCodec() {}
}
