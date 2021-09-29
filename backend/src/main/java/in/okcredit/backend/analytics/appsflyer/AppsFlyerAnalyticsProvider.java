package in.okcredit.backend.analytics.appsflyer;

import android.content.Context;
import com.appsflyer.AppsFlyerLib;
import com.appsflyer.AppsFlyerTrackingRequestListener;
import in.okcredit.analytics.AnalyticsHelper;
import in.okcredit.analytics.EventProperties;
import in.okcredit.analytics.appsflyer.AppsFlyerEventsConsumer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import org.json.JSONException;
import org.json.JSONObject;
import timber.log.Timber;

/** Created by harsh on 10/01/18. */
public class AppsFlyerAnalyticsProvider implements AnalyticsHelper {
    private AppsFlyerLib appsFlyerApi;
    private Context context;

    @Inject
    public AppsFlyerAnalyticsProvider(Context context, AppsFlyerLib appsFlyerApi) {
        this.appsFlyerApi = appsFlyerApi;
        this.context = context;
    }

    @Override
    public void setUserProperty(String key, String value) {}

    @Override
    public void track(String eventName, EventProperties props) {

        if (!AppsFlyerEventsConsumer.Companion.getAppsFlyerAnalyticsEvents().contains(eventName)) {
            return;
        }

        Map<String, Object> map = new ConcurrentHashMap<>();
        if (props != null) {

            try {
                map = toMap(props.getProperties());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            appsFlyerApi.trackEvent(
                    context,
                    eventName,
                    map,
                    new AppsFlyerTrackingRequestListener() {
                        @Override
                        public void onTrackingRequestSuccess() {
                            Timber.i("AppsFlyerAnalyticsProvider onTrackingRequestSuccess");
                        }

                        @Override
                        public void onTrackingRequestFailure(String s) {
                            Timber.i("AppsFlyerAnalyticsProvider onTrackingRequestFailure");
                        }
                    });

        } else {
            appsFlyerApi.trackEvent(context, eventName, new HashMap<>());
        }
    }

    public ConcurrentHashMap<String, Object> toMap(JSONObject object) throws JSONException {
        ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<String, Object>();

        for (Iterator<String> iter = object.keys(); iter.hasNext(); ) {
            String key = iter.next();
            String value = object.get(key).toString();
            map.put(key, value);
        }
        return map;
    }
}
