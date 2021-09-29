package in.okcredit.merchant.store.database;

import androidx.room.TypeConverter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public final class BusinessRoomCodec {
    @TypeConverter
    public static BusinessType fromEpoch(String businesStr) {
        Type businesObj = new TypeToken<BusinessType>() {}.getType();
        return new Gson().fromJson(businesStr, businesObj);
    }

    @TypeConverter
    public static String toEpoch(BusinessType business) {
        Gson gson = new Gson();
        String json = gson.toJson(business);
        return json;
    }

    private BusinessRoomCodec() {}
}
