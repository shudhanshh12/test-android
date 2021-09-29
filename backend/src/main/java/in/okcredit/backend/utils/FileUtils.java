package in.okcredit.backend.utils;

import android.content.Context;
import dagger.Reusable;
import io.reactivex.Completable;
import java.io.File;
import javax.inject.Inject;
import tech.okcredit.android.base.utils.ThreadUtils;

/** Created by harsh on 14/03/18. */
@Reusable
public class FileUtils {
    private Context context;

    @Inject
    public FileUtils(Context context) {
        this.context = context;
    }

    public void deleteReminderImages() {
        Completable.fromAction(
                        () -> {
                            deleteFiles(
                                    new File(context.getExternalFilesDir(null), "reminder_images"));
                        })
                .subscribeOn(ThreadUtils.INSTANCE.newThread())
                .subscribe(() -> {}, throwable -> {});
    }

    private void deleteFiles(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();

            if (files == null) {
                return;
            }

            for (File file : files) {
                file.delete();
            }
        }
    }
}
