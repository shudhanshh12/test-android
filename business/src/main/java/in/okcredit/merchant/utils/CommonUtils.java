package in.okcredit.merchant.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.MediaStore;

public final class CommonUtils {

    public static boolean hasCamera(Context context) {
        final Intent mockIntent = new Intent();
        mockIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);

        // ask the system if there is a camera (front, back)
        final PackageManager packageManager = context.getPackageManager();
        mockIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        final boolean hasCamera =
                packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
                        || packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT);

        final boolean hasCameraApp = isIntentResolvable(mockIntent, context);

        return (hasCamera && hasCameraApp);
    }

    private static boolean isIntentResolvable(Intent intent, Context context) {
        return intent != null && intent.resolveActivity(context.getPackageManager()) != null;
    }
}
