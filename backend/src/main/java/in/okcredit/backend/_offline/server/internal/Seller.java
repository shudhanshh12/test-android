package in.okcredit.backend._offline.server.internal;

import com.google.gson.annotations.SerializedName;

public class Seller {

    @SerializedName("type")
    private Integer type;

    @SerializedName("mobile")
    private String mobile;

    @SerializedName("merchant_id")
    private String merchantId;

    @SerializedName("registered")
    private Boolean registered;

    @SerializedName("account_deleted")
    private Boolean accountDeleted;

    @SerializedName("last_delete_time")
    private String lastDeleteTime;

    @SerializedName("profile")
    private Profile profile;

    @SerializedName("preferences")
    private Preferences preferences;

    @SerializedName("txn_start_time")
    private String txnStartTime;

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public Boolean getRegistered() {
        return registered;
    }

    public void setRegistered(Boolean registered) {
        this.registered = registered;
    }

    public Boolean getAccountDeleted() {
        return accountDeleted;
    }

    public void setAccountDeleted(Boolean accountDeleted) {
        this.accountDeleted = accountDeleted;
    }

    public String getLastDeleteTime() {
        return lastDeleteTime;
    }

    public void setLastDeleteTime(String lastDeleteTime) {
        this.lastDeleteTime = lastDeleteTime;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public Preferences getPreferences() {
        return preferences;
    }

    public void setPreferences(Preferences preferences) {
        this.preferences = preferences;
    }

    public String getTxnStartTime() {
        return txnStartTime;
    }

    public void setTxnStartTime(String txnStartTime) {
        this.txnStartTime = txnStartTime;
    }
}
