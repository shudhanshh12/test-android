package in.okcredit.analytics;

import org.jetbrains.annotations.NonNls;
import org.json.JSONException;
import org.json.JSONObject;
import timber.log.Timber;

public class EventProperties {
    private JSONObject props = new JSONObject();

    @NonNls
    public EventProperties with(String key, String value) {
        try {
            props.put(key, value);
        } catch (JSONException e) {
            Timber.e(e, "failed to add event property");
        }
        return this;
    }

    @NonNls
    public EventProperties with(String key, int value) {
        try {
            props.put(key, value);
        } catch (JSONException e) {
            Timber.e(e, "failed to add event property");
        }
        return this;
    }

    @NonNls
    public EventProperties with(String key, double value) {
        try {
            props.put(key, value);
        } catch (JSONException e) {
            Timber.e(e, "failed to add event property");
        }
        return this;
    }

    @NonNls
    public EventProperties with(String key, boolean value) {
        try {
            props.put(key, value);
        } catch (JSONException e) {
            Timber.e(e, "failed to add event property");
        }
        return this;
    }

    public static EventProperties create() {
        return new EventProperties();
    }

    private EventProperties() {}

    public JSONObject getProperties() {
        return props;
    }
}
