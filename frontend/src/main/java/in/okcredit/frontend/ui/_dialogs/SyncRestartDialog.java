package in.okcredit.frontend.ui._dialogs;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AlertDialog;
import in.okcredit.frontend.R;

public final class SyncRestartDialog {
    public interface Listener {
        void isSyncRestart();
    }

    public static AlertDialog show(Activity activity, Listener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setCancelable(true);

        LayoutInflater layoutInflater = activity.getLayoutInflater();
        View dialogView = layoutInflater.inflate(R.layout.alert_dialog_sync_restart, null);
        builder.setView(dialogView);

        Button no = dialogView.findViewById(R.id.no);
        Button yes = dialogView.findViewById(R.id.yes);

        AlertDialog alertDialog = builder.create();
        yes.setOnClickListener(
                v -> {
                    if (listener != null) listener.isSyncRestart();
                });

        no.setOnClickListener(v -> alertDialog.dismiss());
        alertDialog.show();

        return alertDialog;
    }

    private SyncRestartDialog() {}
}