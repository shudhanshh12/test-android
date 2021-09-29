package in.okcredit.backend._offline.server.internal;

import com.google.gson.annotations.SerializedName;

public class Profile_ {

    @SerializedName("display_name")
    private String displayName;

    @SerializedName("profile_image")
    private String profileImage;

    @SerializedName("address")
    private String address;

    @SerializedName("lang")
    private String lang;

    @SerializedName("is_live_sales")
    private Boolean isLiveSales;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public Boolean getIsLiveSales() {
        return isLiveSales;
    }

    public void setIsLiveSales(Boolean isLiveSales) {
        this.isLiveSales = isLiveSales;
    }
}
