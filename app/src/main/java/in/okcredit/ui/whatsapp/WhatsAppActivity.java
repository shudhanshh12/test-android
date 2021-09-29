package in.okcredit.ui.whatsapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import in.okcredit.R;
import in.okcredit.databinding.WhatsappActivityBinding;
import in.okcredit.navigation.NavigationActivity;
import in.okcredit.ui._base_v2.BaseActivity;
import in.okcredit.ui._dialog.NetworkErrorDialog;
import javax.inject.Inject;

public class WhatsAppActivity extends BaseActivity implements WhatsAppContract.View {

    public static final String ARG_MOBILE = "mobile";
    public static final String ARG_IS_WHATSAPP = "is_whatsapp";

    public static Intent startingIntent(Context context, Boolean isFromHelp) {
        Intent intent = new Intent(context, WhatsAppActivity.class);
        intent.putExtra(ARG_IS_WHATSAPP, isFromHelp);
        return intent;
    }

    @Inject WhatsAppContract.Presenter viewModel;

    private WhatsappActivityBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = WhatsappActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.yes.setOnClickListener(v -> viewModel.onWhatsAppEnableClicked());

        binding.no.setOnClickListener(v -> viewModel.onWhatsAppDisableClicked());
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.attachView(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        viewModel.detachView();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void showError() {

        Toast.makeText(this, R.string.err_default, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showNoInternetMessage() {
        new NetworkErrorDialog()
                .show(
                        this,
                        new NetworkErrorDialog.Listener() {
                            @Override
                            public void onNetworkOk() {
                                viewModel.onInternetRestored();
                            }

                            @Override
                            public void onCancel() {}
                        });
    }

    @Override
    public void setMobile(String mobile) {
        binding.description.setText(getString(R.string.whatsapp_page_description, mobile));
    }

    @Override
    public void goToHomeScreen() {
        NavigationActivity.navigateToHomeScreen(this);
        finishAffinity();
    }

    @Override
    public void hideDisableButton() {
        binding.no.setVisibility(View.GONE);
    }

    @Override
    public void goToHelpScreen() {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.WRITE_CONTACTS)
                .withListener(
                        new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse response) {
                                viewModel.onWhatsappUsClicked(true);
                            }

                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse response) {
                                viewModel.onWhatsappUsClicked(false);
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(
                                    PermissionRequest permission, PermissionToken token) {
                                if (token != null) {
                                    token.continuePermissionRequest();
                                }
                            }
                        })
                .check();
    }

    @Override
    public void openWhatsapp(String helpNumber) {
        Uri uri =
                Uri.parse("whatsapp://send")
                        .buildUpon()
                        .appendQueryParameter("text", getString(R.string.help_whatsapp_msg))
                        .appendQueryParameter("phone", ("91" + helpNumber))
                        .build();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.whatsapp_not_installed, Toast.LENGTH_SHORT).show();
        }
    }
}
