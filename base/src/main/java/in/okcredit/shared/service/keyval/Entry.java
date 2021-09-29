package in.okcredit.shared.service.keyval;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Deprecated
@Entity
public final class Entry {
    @PrimaryKey @NonNull private String key;
    private String value;

    @Ignore
    public Entry(@NonNull String key) {
        this(key, null);
    }

    public Entry(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
