package in.okcredit.backend.analytics.mixpanel;

import com.google.common.base.Strings;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import in.okcredit.analytics.AnalyticsHelper;
import in.okcredit.analytics.EventProperties;
import tech.okcredit.android.base.language.LocaleManager;
import tech.okcredit.android.base.utils.DateTimeUtils;

/** Created by harsh on 10/01/18. */
public class MixpanelAnalyticsProvider implements AnalyticsHelper {
    private MixpanelAPI mixpanelAPI;

    public MixpanelAnalyticsProvider(MixpanelAPI mixpanelAPI) {
        this.mixpanelAPI = mixpanelAPI;
    }

    @Override
    public void setUserProperty(String key, String value) {

        if (Strings.isNullOrEmpty(value)) {
            value =
                    DateTimeUtils.currentDateTime()
                            .toString(
                                    "YYYY-MM-dd hh:mm:ss",
                                    LocaleManager.Companion.getEnglishLocale());
        }

        mixpanelAPI.getPeople().set(key, value);
    }

    @Override
    public void track(String eventName, EventProperties props) {

        if (props != null) {
            mixpanelAPI.track(eventName, props.getProperties());
        } else {
            mixpanelAPI.track(eventName);
        }
    }
}
