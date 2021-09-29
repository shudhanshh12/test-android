package in.okcredit.backend._offline.server.internal;

import com.google.gson.annotations.SerializedName;

public class MigrateAccountResponse {
    @SerializedName("account")
    private Account account;

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}
