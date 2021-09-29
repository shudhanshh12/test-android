package in.okcredit.backend._offline.server.internal;

import com.google.gson.annotations.SerializedName;

public final class RegisterRequest {
    @SerializedName("method")
    private final String method = "otp";

    @SerializedName("mobile")
    private String mobile;

    @SerializedName("name")
    private String name;

    @SerializedName("password")
    private String password;

    @SerializedName("verification_token")
    private String token;

    @SerializedName("lang")
    private String lang;

    public RegisterRequest(String mobile, String name, String password, String token, String lang) {
        this.mobile = mobile;
        this.name = name;
        this.password = password;
        this.token = token;
        this.lang = lang;
    }
}
