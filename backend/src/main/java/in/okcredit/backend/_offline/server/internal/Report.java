package in.okcredit.backend._offline.server.internal;

import com.google.gson.annotations.SerializedName;

public final class Report {
    @SerializedName("report_url")
    public final String reportUrl;

    public Report(String reportUrl) {
        this.reportUrl = reportUrl;
    }
}
