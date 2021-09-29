package in.okcredit.backend.analytics.firebase;

import android.os.Bundle;
import com.google.firebase.analytics.FirebaseAnalytics;
import in.okcredit.analytics.AnalyticsHelper;
import in.okcredit.analytics.EventProperties;
import in.okcredit.backend.BuildConfig;
import java.util.Iterator;

public class FirebaseAnalyticsProvider implements AnalyticsHelper {
    private FirebaseAnalytics firebaseAnalytics;

    public FirebaseAnalyticsProvider(FirebaseAnalytics firebaseAnalytics) {
        this.firebaseAnalytics = firebaseAnalytics;
    }

    @Override
    public void setUserProperty(String key, String value) {}

    @Override
    public void track(String eventName, EventProperties props) {
        if (BuildConfig.DEBUG || firebaseAnalytics == null) {
            return;
        }

        if (eventName != null) {
            eventName = eventName.replace(" ", "_");
            eventName = eventName.replaceAll("[(-+.^:,)]", "");
        }

        try {
            if (props != null) {
                Bundle bundle = new Bundle();

                Iterator<String> keys = props.getProperties().keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    try {
                        String value = props.getProperties().getString(key);
                        bundle.putString(key, value);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                firebaseAnalytics.logEvent(eventName, bundle);
            } else {
                Bundle params = new Bundle();
                firebaseAnalytics.logEvent(eventName, params);
            }
        } catch (Exception e) {

        }
    }
}
