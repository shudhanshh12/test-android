package in.okcredit.backend._offline.server.internal;

import com.google.gson.annotations.SerializedName;

public final class AddCustomerRequest {
    @SerializedName("mobile")
    private final String mobile;

    @SerializedName("description")
    private final String desc;

    @SerializedName("reactivate")
    private final boolean reactivate;

    @SerializedName("profile_image")
    private final String profileImage;

    public AddCustomerRequest(String mobile, String desc, boolean reactivate, String profileImage) {
        this.mobile = mobile;
        this.desc = desc;
        this.reactivate = reactivate;
        this.profileImage = profileImage;
    }
}
