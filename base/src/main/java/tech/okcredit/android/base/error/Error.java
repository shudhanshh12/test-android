package tech.okcredit.android.base.error;

import com.google.gson.annotations.SerializedName;
import java.io.IOException;
import java.io.Serializable;
import kotlin.Deprecated;
import retrofit2.Response;
import tech.okcredit.android.base.utils.GsonUtil;

@Deprecated(message = "Use ApiError class instead")
public final class Error extends IOException implements Serializable {
    @SerializedName("code")
    private int code;

    @SerializedName("error")
    private String error;

    public Error(int code, String error) {
        this.code = code;
        this.error = error;
    }

    public int getCode() {
        return code;
    }

    public String getError() {
        return error;
    }

    @Override
    public String getMessage() {
        if (error == null || error.isEmpty())
            return String.format("HTTP request failed (code=%d)", code);
        else return error;
    }

    public static Error parse(Response httpResponse) {
        if (httpResponse.isSuccessful()) return new Error(0, "");

        try {
            return GsonUtil.getGson().fromJson(httpResponse.errorBody().charStream(), Error.class);
        } catch (Exception e) {
            return new Error(httpResponse.code(), null);
        }
    }
}
