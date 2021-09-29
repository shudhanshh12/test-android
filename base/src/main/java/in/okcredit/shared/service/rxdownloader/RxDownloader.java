package in.okcredit.shared.service.rxdownloader;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LongSparseArray;

import io.reactivex.Single;
import io.reactivex.subjects.SingleSubject;

import java.io.File;

import javax.inject.Inject;

import timber.log.Timber;

public class RxDownloader  {

    private static final String DEFAULT_MIME_TYPE = "*/*";

    private final Context context;
    LongSparseArray<SingleSubject<String>> subjectMap = new LongSparseArray<>();
    private DownloadManager downloadManager;
    private final DownloadStatusReceiver downloadStatusReceiver;

    private DownloadStatusReceiver.Listener listener= new DownloadStatusReceiver.Listener() {
        @Override
        public void onSuccess(long id, String downloadedUri) {
            if (!subjectMap.containsKey(id)) return;
            SingleSubject<String> singleSubject = subjectMap.get(id);
            singleSubject.onSuccess(downloadedUri);
            subjectMap.remove(id);
        }

        @Override
        public void onError(long id, Throwable throwable) {
            if (!subjectMap.containsKey(id)) return;
            SingleSubject<String> singleSubject = subjectMap.get(id);
            singleSubject.onError(throwable);
            subjectMap.remove(id);
        }
    };

    @Inject
    public RxDownloader(@NonNull Context context) {
        this.context = context.getApplicationContext();
        downloadStatusReceiver = new DownloadStatusReceiver(getDownloadManager());
        downloadStatusReceiver.setListener(listener);
        IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        context.registerReceiver(downloadStatusReceiver, intentFilter);
    }

    @NonNull
    private DownloadManager getDownloadManager() {
        if (downloadManager == null) {
            downloadManager = (DownloadManager) context.getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
        }
        if (downloadManager == null) {
            throw new RuntimeException("Can't get DownloadManager from system service");
        }
        return downloadManager;
    }

    public Single<String> download(
            @NonNull String url, @NonNull String filename, boolean showCompletedNotification) {
        return download(url, filename, DEFAULT_MIME_TYPE, showCompletedNotification);
    }

    public Single<String> download(
            @NonNull String url,
            @NonNull String filename,
            @NonNull String mimeType,
            boolean showCompletedNotification) {
        Timber.d("<<<<RxDownloader download");
        return download(
                createRequest(url, filename, null, mimeType, true, showCompletedNotification));
    }

    public Single<String> download(
            @NonNull String url,
            @NonNull String filename,
            @NonNull String destinationPath,
            @NonNull String mimeType,
            boolean showCompletedNotification) {
        return download(
                createRequest(
                        url, filename, destinationPath, mimeType, true, showCompletedNotification));
    }

    public Single<String> downloadInFilesDir(
            @NonNull String url,
            @NonNull String filename,
            @NonNull String destinationPath,
            @NonNull String mimeType,
            boolean showCompletedNotification) {
        return download(
                createRequest(
                        url,
                        filename,
                        destinationPath,
                        mimeType,
                        false,
                        showCompletedNotification));
    }

    public Single<String> download(DownloadManager.Request request) {
        long downloadId = getDownloadManager().enqueue(request);

        SingleSubject<String> singleSubject = SingleSubject.create();
        Timber.d("<<<<RxDownloader downloadId=%s", downloadId);
        subjectMap.put(downloadId, singleSubject);

        return singleSubject;
    }

    private DownloadManager.Request createRequest(
            @NonNull String url,
            @NonNull String filename,
            @Nullable String destinationPath,
            @NonNull String mimeType,
            boolean inPublicDir,
            boolean showCompletedNotification) {

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDescription(filename);
        request.setMimeType(mimeType);

        if (destinationPath == null) {
            destinationPath = Environment.DIRECTORY_DOWNLOADS;
        }

        File destinationFolder =
                inPublicDir
                        ? Environment.getExternalStoragePublicDirectory(destinationPath)
                        : new File(context.getFilesDir(), destinationPath);

        createFolderIfNeeded(destinationFolder);
        removeDuplicateFileIfExist(destinationFolder, filename);

        if (inPublicDir) {
            request.setDestinationInExternalPublicDir(destinationPath, filename);
        } else {
            request.setDestinationInExternalFilesDir(context, destinationPath, filename);
        }

        request.setNotificationVisibility(
                showCompletedNotification
                        ? DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                        : DownloadManager.Request.VISIBILITY_VISIBLE);

        return request;
    }

    private void createFolderIfNeeded(@NonNull File folder) {
        if (!folder.exists() && !folder.mkdirs()) {
            throw new RuntimeException("Can't create directory");
        }
    }

    private void removeDuplicateFileIfExist(@NonNull File folder, @NonNull String fileName) {
        File file = new File(folder, fileName);
        if (file.exists() && !file.delete()) {
            throw new RuntimeException("Can't delete file");
        }
    }

    static class DownloadStatusReceiver extends BroadcastReceiver {

        private final DownloadManager downloadManager;
        private Listener listener;

        DownloadStatusReceiver(@NonNull DownloadManager downloadManager) {
            this.downloadManager = downloadManager;
        }

        void setListener(Listener listener) {
            this.listener = listener;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Timber.d("<<<<RxDownloader onReceive");
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0L);
            if (listener == null) {
                return;
            }

            try {
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(id);
                Cursor cursor = downloadManager.query(query);

                if (!cursor.moveToFirst()) {
                    cursor.close();
                    downloadManager.remove(id);
                    listener.onError(id, new IllegalStateException("Cursor empty, this shouldn't happened"));
                    return;
                }

                int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                if (DownloadManager.STATUS_SUCCESSFUL != cursor.getInt(statusIndex)) {
                    cursor.close();
                    downloadManager.remove(id);
                    listener.onError(id, new IllegalStateException("Download Failed"));
                    return;
                }

                int uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
                String downloadedPackageUriString = cursor.getString(uriIndex);
                cursor.close();

                listener.onSuccess(id, downloadedPackageUriString);
            } catch (Exception e) {
                listener.onError(id, new IllegalStateException("Download Failed"));
            }
        }

        interface Listener {
            void onSuccess(long id, String downloadedUri);

            void onError(long id, Throwable throwable);
        }
    }
}
