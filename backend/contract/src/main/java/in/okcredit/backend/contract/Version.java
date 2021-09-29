package in.okcredit.backend.contract;

import com.google.gson.annotations.SerializedName;
import kotlin.Deprecated;

@Deprecated(message = "Please Use Firebase Remote Config")
public final class Version {
    @SerializedName("version")
    private final int version;

    @SerializedName("intro_video")
    private final String introVideo;

    @SerializedName("help_number")
    private final String helpNumber;

    @SerializedName("otp_number")
    private final String otpNumber;

    @SerializedName("setup_collection_video")
    private final String setupCollectionVideo;

    @SerializedName("sc_intro_video")
    private final String scTutorialVideo;

    @SerializedName("sc_education_video_1")
    private final String scEducationVideo1;

    @SerializedName("sc_education_video_2")
    private final String scEducationVideo2;

    @SerializedName("lp_education_video_1")
    private final String lpEducationVideo1;

    @SerializedName("ca_education_video_1")
    private final String caEducationVideo1;

    @SerializedName("supplier_learn_more_web_link")
    private final String supplierLearnMoreWebLink;

    @SerializedName("common_ledger_seller_video")
    private final String commonLedgerSellerVideo;

    @SerializedName("common_ledger_buyer_video")
    private final String commonLedgerBuyerVideo;

    public Version(
            int version,
            String introVideo,
            String helpNumber,
            String setupCollectionVideo,
            String otpNumber,
            String scTutorialVideo,
            String scEducationVideo1,
            String scEducationVideo2,
            String lpEducationVideo1,
            String caEducationVideo1,
            String supplierLearnMoreWebLink,
            String commonLedgerSellerVideo,
            String commonLedgerBuyerVideo) {
        this.version = version;
        this.introVideo = introVideo;
        this.helpNumber = helpNumber;
        this.otpNumber = otpNumber;
        this.setupCollectionVideo = setupCollectionVideo;
        this.scTutorialVideo = scTutorialVideo;
        this.scEducationVideo1 = scEducationVideo1;
        this.scEducationVideo2 = scEducationVideo2;
        this.lpEducationVideo1 = lpEducationVideo1;
        this.caEducationVideo1 = caEducationVideo1;
        this.supplierLearnMoreWebLink = supplierLearnMoreWebLink;
        this.commonLedgerSellerVideo = commonLedgerSellerVideo;
        this.commonLedgerBuyerVideo = commonLedgerBuyerVideo;
    }

    public int getVersion() {
        return version;
    }

    public String getScEducationVideo1() {
        return scEducationVideo1;
    }

    public String getScEducationVideo2() {
        return scEducationVideo2;
    }

    public String getCaEducationVideo1() {
        return caEducationVideo1;
    }

    public String getCommonLedgerSellerVideo() {
        return commonLedgerSellerVideo;
    }

    public String getCommonLedgerBuyerVideo() {
        return commonLedgerBuyerVideo;
    }

    public String getHelpNumber() {
        return helpNumber;
    }

    public String getSetupCollectionVideo() {
        return setupCollectionVideo;
    }

    public String getOtpNumber() {
        return otpNumber;
    }

    public String getSupplierLearnMoreWebLink() {
        return supplierLearnMoreWebLink;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Version version = (Version) o;

        if (introVideo != null
                ? !introVideo.equals(version.introVideo)
                : version.introVideo != null) return false;
        if (setupCollectionVideo != null
                ? !setupCollectionVideo.equals(version.setupCollectionVideo)
                : version.setupCollectionVideo != null) return false;
        if (commonLedgerBuyerVideo != null
                ? !commonLedgerBuyerVideo.equals(version.commonLedgerBuyerVideo)
                : version.commonLedgerBuyerVideo != null) return false;
        if (commonLedgerSellerVideo != null
                ? !commonLedgerSellerVideo.equals(version.commonLedgerSellerVideo)
                : version.commonLedgerSellerVideo != null) return false;
        return scTutorialVideo != null
                ? scTutorialVideo.equals(version.scTutorialVideo)
                : version.scTutorialVideo == null;
    }

    @Override
    public int hashCode() {
        int result = introVideo != null ? introVideo.hashCode() : 0;
        result = 31 * result + (setupCollectionVideo != null ? setupCollectionVideo.hashCode() : 0);
        result = 31 * result + (scTutorialVideo != null ? scTutorialVideo.hashCode() : 0);
        result =
                31 * result
                        + (commonLedgerSellerVideo != null
                                ? commonLedgerSellerVideo.hashCode()
                                : 0);
        result =
                31 * result
                        + (commonLedgerBuyerVideo != null ? commonLedgerBuyerVideo.hashCode() : 0);
        return result;
    }
}
