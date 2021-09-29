package in.okcredit.backend._offline.server.internal;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ListDuesInfoResponse {

    @SerializedName("dues_info")
    public List<DueInfo> dueInfo;

    public List<DueInfo> getDueInfo() {
        return dueInfo;
    }

    public void setDueInfo(List<DueInfo> dueInfo) {
        this.dueInfo = dueInfo;
    }
}
