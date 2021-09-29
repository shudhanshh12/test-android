package in.okcredit.backend.analytics.clevertap;

import com.clevertap.android.sdk.CleverTapAPI;
import com.google.common.base.Strings;
import dagger.Lazy;
import in.okcredit.analytics.AnalyticsHelper;
import in.okcredit.analytics.EventProperties;
import java.util.HashMap;
import java.util.Iterator;
import org.json.JSONException;
import tech.okcredit.android.base.language.LocaleManager;
import tech.okcredit.android.base.utils.DateTimeUtils;

/** Created by harsh on 10/01/18. */
public class ClevertapAnalyticsProvider implements AnalyticsHelper {
    private CleverTapAPI cleverTapAPI;
    private Lazy<LocaleManager> localeManager;

    public ClevertapAnalyticsProvider(
            CleverTapAPI cleverTapAPI, Lazy<LocaleManager> localeManager) {
        this.cleverTapAPI = cleverTapAPI;
        this.localeManager = localeManager;
    }

    @Override
    public void setUserProperty(String key, String value) {
        if (cleverTapAPI == null) {
            return;
        }

        if (Strings.isNullOrEmpty(value)) {
            value =
                    DateTimeUtils.currentDateTime()
                            .toString(
                                    "YYYY-MM-dd hh:mm:ss",
                                    LocaleManager.Companion.getEnglishLocale());
        }

        HashMap<String, Object> profileUpdate = new HashMap<String, Object>();
        profileUpdate.put(key, value);

        cleverTapAPI.pushProfile(profileUpdate);
    }

    @Override
    public void track(String eventName, EventProperties props) {
        if (cleverTapAPI == null) {
            return;
        }

        HashMap<String, Object> properties = new HashMap<String, Object>();

        if (props != null) {
            Iterator<String> keys = props.getProperties().keys();
            while (keys.hasNext()) {
                String key = keys.next();
                try {
                    properties.put(key, props.getProperties().get(key));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            properties.put("lang", localeManager.get().getLanguage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        cleverTapAPI.pushEvent(eventName, properties);
    }
}
