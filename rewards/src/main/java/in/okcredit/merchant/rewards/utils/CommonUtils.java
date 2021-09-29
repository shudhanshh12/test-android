package in.okcredit.merchant.rewards.utils;

import com.google.common.base.Converter;
import com.instacart.library.truetime.TrueTime;
import com.instacart.library.truetime.TrueTimeRx;
import java.util.ArrayList;
import java.util.List;
import org.joda.time.DateTime;
import timber.log.Timber;

public final class CommonUtils {
    public static <A, B> List<B> mapList(List<A> aList, Converter<A, B> mapper) {
        List<B> bList = new ArrayList<>(aList.size());
        for (A a : aList) {
            bList.add(mapper.convert(a));
        }
        return bList;
    }

    public static DateTime currentDateTime() {
        DateTime now;
        try {
            if (TrueTime.isInitialized()) {
                now = new DateTime(TrueTimeRx.now());
            } else {
                now = DateTime.now();
            }
        } catch (Exception e) {
            now = DateTime.now();
            Timber.i("TrueTime failed DeviceTime=%s, ServerTime=%s", DateTime.now(), now);
        }
        return now;
    }
}
