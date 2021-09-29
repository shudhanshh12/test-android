package in.okcredit.ui.reset_pwd.otp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.Task;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import tech.okcredit.base.exceptions.ExceptionUtils;
import timber.log.Timber;

public class SmsHelper {
    private Activity activity;
    private BehaviorSubject<String> otp;
    private BroadcastReceiver smsReceiver;
    private SmsRetrieverClient smsRetrieverClient;

    public SmsHelper(Activity activity) {
        this.activity = activity;
        otp = BehaviorSubject.create();
        smsRetrieverClient = SmsRetriever.getClient(activity);
    }

    public Observable<String> otp() {
        return otp;
    }

    public void startListening() {
        smsReceiver =
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        if (SmsRetriever.SMS_RETRIEVED_ACTION.equals(intent.getAction())) {
                            Bundle extras = intent.getExtras();
                            Status status = (Status) extras.get(SmsRetriever.EXTRA_STATUS);
                            if (status == null) return;

                            switch (status.getStatusCode()) {
                                case CommonStatusCodes.SUCCESS:
                                    // Get SMS message contents
                                    String message =
                                            (String) extras.get(SmsRetriever.EXTRA_SMS_MESSAGE);
                                    if (message == null) return;

                                    message = message.trim();
                                    String o = message.substring(2, 8);
                                    Timber.e("otp=%s; len=%d", o, o.length());
                                    otp.onNext(o);

                                    break;

                                case CommonStatusCodes.TIMEOUT:
                                    Timber.e("Timeout, 5mins up");
                                    break;
                            }
                        }
                    }
                };

        activity.registerReceiver(smsReceiver, new IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION));

        Task<Void> task = smsRetrieverClient.startSmsRetriever();
        task.addOnSuccessListener(aVoid -> Timber.d("SMS receiver started"));
        task.addOnFailureListener(e -> Timber.e(e, "failed to start sms receiver"));
    }

    public void stopListening() {
        try {
            activity.unregisterReceiver(smsReceiver);
            smsReceiver = null;
        } catch (Exception e) {
            Timber.e(e, "failed to unregister sms receiver");
            ExceptionUtils.Companion.logException("Error: SmsHelper", e);
        }
    }
}
