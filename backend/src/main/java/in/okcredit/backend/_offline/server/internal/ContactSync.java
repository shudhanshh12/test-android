package in.okcredit.backend._offline.server.internal;

import com.google.gson.annotations.SerializedName;

public class ContactSync {
    @SerializedName("account_id")
    private String accountId;

    @SerializedName("display_name")
    private String displayName;

    public ContactSync(String accountId, String displayName) {
        this.accountId = accountId;
        this.displayName = displayName;
    }
}
