package in.okcredit.backend._offline.server.internal;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AllAccountsBuyerTxnAlertConfigResponse {
    @SerializedName("account_features")
    public List<AccountFeature> accountFeatures;

    public AllAccountsBuyerTxnAlertConfigResponse(List<AccountFeature> accountFeatures) {
        this.accountFeatures = accountFeatures;
    }
}
