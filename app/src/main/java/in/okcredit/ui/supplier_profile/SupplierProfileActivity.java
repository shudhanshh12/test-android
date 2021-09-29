package in.okcredit.ui.supplier_profile;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.transition.TransitionManager;

import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.Lazy;
import in.okcredit.R;
import in.okcredit.analytics.AnalyticsEvents;
import in.okcredit.analytics.PropertyValue;
import in.okcredit.analytics.Tracker;
import in.okcredit.databinding.SupplierProfileActivityBinding;
import in.okcredit.fileupload._id.GlideApp;
import in.okcredit.fileupload.usecase.IImageLoader;
import in.okcredit.frontend.ui.MainActivity;
import in.okcredit.merchant.customer_ui.ui.dialogs.BlockRelationShipDialogFragment;
import in.okcredit.merchant.customer_ui.ui.dialogs.CyclicAccountDialog;
import in.okcredit.merchant.suppliercredit.Supplier;
import in.okcredit.merchant.suppliercredit.server.internal.common.SupplierCreditServerErrors;
import in.okcredit.shared.utils.ScreenName;
import in.okcredit.ui._base_v2.BaseActivity;
import in.okcredit.ui._dialog.NetworkErrorDialog;
import in.okcredit.ui._dialog.SmsLanguageDialog;
import in.okcredit.ui._dialog.TxSmsDialog;
import in.okcredit.ui._dialog.VerifiedDialog;
import in.okcredit.ui.customer_profile.BottomSheetMediaFragment;
import in.okcredit.ui.delete_txn.supplier.supplier.DeleteSupplierActivity;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import tech.okcredit.android.base.animation.AnimationUtils;
import tech.okcredit.android.base.language.LocaleManager;
import tech.okcredit.android.base.utils.keyboardUtils.KeyboardVisibilityEvent;
import tech.okcredit.app_contract.LegacyNavigator;
import tech.okcredit.base.permission.IPermissionListener;
import tech.okcredit.base.permission.Permission;
import tech.okcredit.bill_management_ui.BillActivity;
import tech.okcredit.contract.AppLock;
import tech.okcredit.contract.AppLockTracker;
import tech.okcredit.contract.OnSetPinClickListener;
import tech.okcredit.contract.OnUpdatePinClickListener;
import tech.okcredit.home.ui.activity.HomeActivity;
import tech.okcredit.userSupport.SupportRepository;
import zendesk.belvedere.Belvedere;
import zendesk.belvedere.Callback;
import zendesk.belvedere.MediaResult;

import static tech.okcredit.contract.AppLockTrackerKt.SET_PIN;
import static tech.okcredit.contract.Constants.IS_AUTHENTICATED;
import static tech.okcredit.contract.Constants.SECURITY_PIN_CHANGED;
import static tech.okcredit.contract.Constants.SECURITY_PIN_SET;

public class SupplierProfileActivity extends BaseActivity
        implements SupplierProfileContract.View,
        BottomSheetMediaFragment.OnBottomSheetFragmentListener,
        OnUpdatePinClickListener,
        OnSetPinClickListener {
    public static final String EXTRA_SUPPLIER_ID = "supplier_id", IS_EDIT_MOBILE = "is_edit_mobile";
    private boolean isCameraImage;
    private String name;

    public static Intent startingIntent(
            Context context, @NonNull String supplierId, Boolean isEditMobile) {
        Intent intent = new Intent(context, SupplierProfileActivity.class);
        intent.putExtra(EXTRA_SUPPLIER_ID, supplierId);
        intent.putExtra(IS_EDIT_MOBILE, isEditMobile);
        return intent;
    }

    public static final int DELETE_SUPPLIER_PROFILE = 1041;
    @Inject
    SupplierProfileContract.Presenter viewModel;
    @Inject
    Tracker tracker;
    @Inject
    IImageLoader imageLoader;
    @Inject
    Lazy<AppLock> appLock;
    @Inject
    Lazy<AppLockTracker> appLockTracker;
    @Inject
    Lazy<SupportRepository> userSupport;
    @Inject
    Lazy<LegacyNavigator> legacyNavigator;

    private boolean isSetNewPin;
    private BottomSheetMediaFragment bottomSheet;
    private String mMobile = "";
    private String supplierId = "";
    private boolean isRegistered;
    private String profileUrl;
    private AlertDialog alertDialog;
    private SupplierProfileActivityBinding supplierProfileBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supplierProfileBinding = SupplierProfileActivityBinding.inflate(getLayoutInflater());
        setContentView(supplierProfileBinding.getRoot());

        showActionBar(true);
        setTitle(R.string.account_profile);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        bottomSheet = BottomSheetMediaFragment.newInstance();
        supplierProfileBinding.phoneContainer.inputPhone.setHint(getString(R.string.mobile));
        supplierProfileBinding.phoneContainer.phoneButton.setImageResource(R.drawable.ic_check);
        supplierProfileBinding.phoneContainer.phoneButton.show();
        supplierProfileBinding.phoneContainer.importContactBottomContainer.setVisibility(View.GONE);
        supplierProfileBinding.contextualHelp.setScreenNameValue(
                ScreenName.SupplierProfile.getValue(),
                tracker,
                userSupport.get(),
                legacyNavigator.get());

        supplierProfileBinding.switchSms.setOnClickListener(
                v -> {
                    if (isRegistered) {
                        showNotAllowedDialog();
                    } else {
                        viewModel.txSmsSwitchChanged(
                                supplierProfileBinding.switchSms.isChecked(), false);
                    }
                });

        clickListeners();
        setepKeyBoard();
        textChangedListener();
        textActionListener();

        supplierProfileBinding.rootView.setTracker(performanceTracker);
    }

    private void textActionListener() {
        supplierProfileBinding.phoneContainer.inputPhone.setOnEditorActionListener(
                (v, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        viewModel.saveMobile(
                                supplierProfileBinding
                                        .phoneContainer
                                        .inputPhone
                                        .getText()
                                        .toString());
                        return true;
                    }
                    return false;
                });

        supplierProfileBinding.nameContainer.inputName.setOnEditorActionListener(
                (v, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        onSubmitNameClicked();
                        return true;
                    }
                    return false;
                });

        supplierProfileBinding.addressContainer.inputAddress.setOnEditorActionListener(
                (v, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        onSubmitAddressClicked();
                        return true;
                    }
                    return false;
                });
    }

    private void setepKeyBoard() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        KeyboardVisibilityEvent.setEventListener(
                (this),
                isOpen -> {
                    if (!isOpen) {
                        supplierProfileBinding.nameContainer.inputName.clearFocus();
                        supplierProfileBinding.phoneContainer.inputPhone.clearFocus();
                        supplierProfileBinding.addressContainer.inputAddress.clearFocus();
                        supplierProfileBinding.phoneContainer.getRoot().setVisibility(View.GONE);
                        supplierProfileBinding.nameContainer.getRoot().setVisibility(View.GONE);
                        supplierProfileBinding.addressContainer.getRoot().setVisibility(View.GONE);

                        AnimationUtils.fadeOut(supplierProfileBinding.dimLayout);
                        Disposable subscribe =
                                Completable.timer(
                                        500,
                                        TimeUnit.MILLISECONDS,
                                        AndroidSchedulers.mainThread())
                                        .subscribe(
                                                () ->
                                                        supplierProfileBinding.dimLayout
                                                                .setVisibility(View.GONE));
                    } else {
                        if (supplierProfileBinding.nameContainer.getRoot().getVisibility()
                                == View.VISIBLE
                                || supplierProfileBinding.phoneContainer.getRoot().getVisibility()
                                == View.VISIBLE
                                || supplierProfileBinding.addressContainer.getRoot().getVisibility()
                                == View.VISIBLE) {
                            supplierProfileBinding.dimLayout.setVisibility(View.VISIBLE);
                            AnimationUtils.fadeIn(supplierProfileBinding.dimLayout);
                        }
                    }
                });
    }

    private void textChangedListener() {
        supplierProfileBinding.nameContainer.inputName.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(
                            CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        String text = editable.toString();

                        if (text.length() == 0
                                || text.length() == 1
                                || text.length() == 29
                                || text.length() == 30
                                || text.length() == 31) {
                            TransitionManager.beginDelayedTransition(
                                    supplierProfileBinding.nameContainer.getRoot());
                        }

                        supplierProfileBinding.nameContainer.errorName.setVisibility(View.GONE);
                        supplierProfileBinding.nameContainer.counterName.setVisibility(View.GONE);

                        if (text.length() > 0) {
                            supplierProfileBinding.nameContainer.nameButton.show();
                            supplierProfileBinding.nameContainer.buttonContainerName.setVisibility(
                                    View.VISIBLE);
                        } else {
                            supplierProfileBinding.nameContainer.nameButton.hide();
                            supplierProfileBinding.nameContainer.buttonContainerName.setVisibility(
                                    View.GONE);
                        }

                        if (text.length() >= 500) {
                            supplierProfileBinding.nameContainer.errorName.setVisibility(
                                    View.VISIBLE);
                            supplierProfileBinding.nameContainer.errorName.setText(
                                    getString(R.string.name_should_not));

                            supplierProfileBinding.nameContainer.counterName.setVisibility(
                                    View.VISIBLE);
                            supplierProfileBinding.nameContainer.counterName.setText(
                                    String.format("%d/500", text.length()));

                            supplierProfileBinding.nameContainer.nameButton.hide();
                            supplierProfileBinding.nameContainer.buttonContainerName.setVisibility(
                                    View.GONE);

                        } else {
                            supplierProfileBinding.nameContainer.nameButton.show();
                            supplierProfileBinding.nameContainer.buttonContainerName.setVisibility(
                                    View.VISIBLE);

                            supplierProfileBinding.nameContainer.errorName.setVisibility(View.GONE);
                            supplierProfileBinding.nameContainer.counterName.setVisibility(
                                    View.GONE);
                        }
                    }
                });

        supplierProfileBinding.addressContainer.inputAddress.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(
                            CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        String text = editable.toString();

                        if (text.length() == 0
                                || text.length() == 1
                                || text.length() == 149
                                || text.length() == 150
                                || text.length() == 151) {
                            TransitionManager.beginDelayedTransition(
                                    supplierProfileBinding.addressContainer.getRoot());
                        }

                        if (text.length() > 0) {
                            supplierProfileBinding.addressContainer.addressButton.show();
                            supplierProfileBinding.addressContainer.buttonContainerAddress
                                    .setVisibility(View.VISIBLE);
                        } else {
                            supplierProfileBinding.addressContainer.addressButton.hide();
                            supplierProfileBinding.addressContainer.buttonContainerAddress
                                    .setVisibility(View.GONE);
                        }
                        if (text.length() == 150) {
                            supplierProfileBinding.addressContainer.errorAddress.setVisibility(
                                    View.VISIBLE);
                            supplierProfileBinding.addressContainer.errorAddress.setText(
                                    getString(R.string.address_should_not));

                            supplierProfileBinding.addressContainer.counterAddress.setVisibility(
                                    View.VISIBLE);
                            supplierProfileBinding.addressContainer.counterAddress.setText(
                                    String.format("%d/150", text.length()));
                        } else {
                            supplierProfileBinding.addressContainer.errorAddress.setVisibility(
                                    View.GONE);
                            supplierProfileBinding.addressContainer.counterAddress.setVisibility(
                                    View.GONE);
                        }
                    }
                });

        supplierProfileBinding.phoneContainer.inputPhone.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(
                            CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        String text = editable.toString();

                        if (text.length() == 0
                                || text.length() == 1
                                || text.length() == 10
                                || text.length() == 11) {
                            TransitionManager.beginDelayedTransition(
                                    supplierProfileBinding.phoneContainer.getRoot());
                        }

                        if (text.length() > 10) {
                            supplierProfileBinding.phoneContainer.errorPhone.setText(
                                    getString(R.string.invalid_phone));
                            supplierProfileBinding.phoneContainer.errorPhone.setVisibility(
                                    View.VISIBLE);

                            supplierProfileBinding.phoneContainer.counterPhone.setText(
                                    String.format("%d/10", text.length()));
                            supplierProfileBinding.phoneContainer.counterPhone.setVisibility(
                                    View.VISIBLE);
                        } else {
                            supplierProfileBinding.phoneContainer.errorPhone.setVisibility(
                                    View.GONE);
                            supplierProfileBinding.phoneContainer.counterPhone.setVisibility(
                                    View.GONE);
                        }
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
    protected void onDestroy() {
        super.onDestroy();

        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        if (KeyboardVisibilityEvent.isKeyboardVisible(this)) {
            KeyboardVisibilityEvent.hideKeyboard((this));
        } else if (supplierProfileBinding.nameContainer.getRoot().getVisibility() == View.VISIBLE
                || supplierProfileBinding.phoneContainer.getRoot().getVisibility() == View.VISIBLE
                || supplierProfileBinding.addressContainer.getRoot().getVisibility()
                == View.VISIBLE) {
            supplierProfileBinding.nameContainer.getRoot().setVisibility(View.GONE);
            supplierProfileBinding.phoneContainer.getRoot().setVisibility(View.GONE);
            supplierProfileBinding.addressContainer.getRoot().setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DELETE_SUPPLIER_PROFILE) {
            if (data != null) {
                boolean isAuthenticated = data.getBooleanExtra(IS_AUTHENTICATED, false);
                if (isAuthenticated) {
                    gotoDeleteScreen(supplierId);
                }else if (isSetNewPin){
                    appLockTracker
                            .get()
                            .trackEvents(SECURITY_PIN_SET, "Supplier Profile Screen", null);
                }else{
                    appLockTracker
                            .get()
                            .trackEvents(SECURITY_PIN_CHANGED, "Supplier Profile Screen", null);
                }
            }
        } else {
            Belvedere.from(this)
                    .getFilesFromActivityOnResult(
                            requestCode,
                            resultCode,
                            data,
                            new Callback<List<MediaResult>>() {
                                @Override
                                public void success(List<MediaResult> result) {
                                    if (result != null && result.size() > 0) {
                                        viewModel.setProfileImage(
                                                isCameraImage, result.get(0).getFile());
                                    }
                                }
                            });
        }
    }

    @Override
    public void displayProfileImageFile(File image) {
        if (image == null || !image.exists() || image.length() == 0) {
            return;
        }
        GlideApp.with(this)
                .load(Uri.fromFile(image))
                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                .into(supplierProfileBinding.profileImage);
    }

    @Override
    public void setProfileImageLocal(File localFile) {
        if (localFile == null || !localFile.exists() || localFile.length() == 0) {
            return;
        }
        GlideApp.with(this)
                .load(localFile)
                .placeholder(ContextCompat.getDrawable(this, R.drawable.ic_account_125dp))
                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                .into(supplierProfileBinding.profileImage);
    }

    @Override
    public void setProfileImageRemote(String url) {
        GlideApp.with(this)
                .load(url)
                .placeholder(ContextCompat.getDrawable(this, R.drawable.ic_account_125dp))
                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                .into(supplierProfileBinding.profileImage);
    }

    @Override
    public void showSmsLangPopup(String language, String customerId) {
        new SmsLanguageDialog()
                .show(
                        this,
                        language,
                        updatedLang -> {
                            viewModel.updateSupplierTxAlertLanguage(language, updatedLang);
                        });
    }

    @Override
    public void showSmsInitialPopup(boolean isChecked) {
        supplierProfileBinding.switchSms.setChecked(!isChecked);
        new TxSmsDialog()
                .show(
                        this,
                        isChecked,
                        () -> {
                            viewModel.txSmsSwitchChanged(isChecked, true);
                        });
    }

    @Override
    public void setSupplierPref(String lang, boolean alertEnable) {

        supplierProfileBinding.switchSms.setChecked(alertEnable);

        switch (lang) {
            case LocaleManager.LANGUAGE_ENGLISH:
                supplierProfileBinding.smsLang.setText(getString(R.string.language_english));
                break;

            case LocaleManager.LANGUAGE_HINDI:
                supplierProfileBinding.smsLang.setText(getString(R.string.language_hindi));
                break;

            case LocaleManager.LANGUAGE_HINGLISH:
                supplierProfileBinding.smsLang.setText(getString(R.string.hinglish));
                break;

            case LocaleManager.LANGUAGE_MALAYALAM:
                supplierProfileBinding.smsLang.setText(getString(R.string.language_malayalam));
                break;

            case LocaleManager.LANGUAGE_PUNJABI:
                supplierProfileBinding.smsLang.setText(getString(R.string.language_punjabi));
                break;

            case LocaleManager.LANGUAGE_MARATHI:
                supplierProfileBinding.smsLang.setText(getString(R.string.language_marathi));
                break;

            case LocaleManager.LANGUAGE_TAMIL:
                supplierProfileBinding.smsLang.setText(getString(R.string.language_tamil));
                break;

            case LocaleManager.LANGUAGE_TELUGU:
                supplierProfileBinding.smsLang.setText(getString(R.string.language_telugu));
                break;

            case LocaleManager.LANGUAGE_BENGALI:
                supplierProfileBinding.smsLang.setText(getString(R.string.language_bengali));
                break;

            case LocaleManager.LANGUAGE_GUJARATI:
                supplierProfileBinding.smsLang.setText(getString(R.string.language_gujarati));
                break;

            case LocaleManager.LANGUAGE_KANNADA:
                supplierProfileBinding.smsLang.setText(getString(R.string.language_kannada));
                break;
        }
    }

    /**
     * ************************************************************** Listeners
     * **************************************************************
     */
    public void clickListeners() {
        supplierProfileBinding.camera.setOnClickListener(v -> onCameraClicked());
        supplierProfileBinding.smsLangBox.setOnClickListener(v -> smsLanguageClicked());
        supplierProfileBinding.deleteContainer.setOnClickListener(v -> onDeleteClicked());

        supplierProfileBinding.nameBox.setOnClickListener(v -> onNameEditClicked());
        supplierProfileBinding.callContainer.setOnClickListener(v -> onPhoneEditClicked());
        supplierProfileBinding.blockContainer.setOnClickListener(v -> onBlockClicked());

        supplierProfileBinding.addressBox.setOnClickListener(v -> onAddressEditClicked());
        supplierProfileBinding.nameContainer.nameButton.setOnClickListener(
                v -> onSubmitNameClicked());
        supplierProfileBinding.addressContainer.addressButton.setOnClickListener(
                v -> onSubmitAddressClicked());

        supplierProfileBinding.phoneContainer.phoneButton.setOnClickListener(
                v -> onSubmitPhoneClicked());

        supplierProfileBinding.switchBox.setOnClickListener(v -> switchBoxClicked());
        supplierProfileBinding.dimLayout.setOnClickListener(v -> dimLayoutClicked());
        supplierProfileBinding.llbill.setOnClickListener(v -> onBillLayoutClicked());
    }

    private void onBillLayoutClicked() {
        startActivity(BillActivity.Companion.getIntent(this, supplierId, "Supplier", name));
    }

    public void smsLanguageClicked() {
        if (isRegistered) {
            showNotAllowedDialog();
        } else {
            tracker.trackSelectProfileV1(
                    PropertyValue.SUPPLIER, "", PropertyValue.SMS_LANG, supplierId);
            viewModel.onSmsLanguageClicked();
        }
    }

    public void onDeleteClicked() {
        viewModel.onDeleteClicked();
    }

    public void onCameraClicked() {
        if (bottomSheet != null && !bottomSheet.isAdded()) {
            bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
        }
    }

    public void onNameEditClicked() {
        viewModel.onEditNameClicked();
        tracker.trackSelectProfileV1(
                PropertyValue.SUPPLIER, "", PropertyValue.NAME, supplierId);
        supplierProfileBinding.nameContainer.getRoot().setVisibility(View.VISIBLE);
        supplierProfileBinding.phoneContainer.getRoot().setVisibility(View.GONE);
        supplierProfileBinding.addressContainer.getRoot().setVisibility(View.GONE);
        KeyboardVisibilityEvent.showKeyboard(
                this,
                supplierProfileBinding.nameContainer.inputName,
                supplierProfileBinding.rootView);
    }

    public void onPhoneEditClicked() {
        if (isRegistered) {
            showNotAllowedDialog();
        } else {
            tracker.trackSelectProfileV1(
                    PropertyValue.SUPPLIER, "", PropertyValue.MOBILE, supplierId);
            viewModel.onEditMobileClicked();
        }
    }

    public void onBlockClicked() {
        viewModel.onBlockClicked();
    }

    public void onAddressEditClicked() {
        if (isRegistered) {
            showNotAllowedDialog();

        } else {
            viewModel.onEditAddressClicked();
            tracker.trackSelectProfileV1(
                    PropertyValue.SUPPLIER, "", PropertyValue.ADDRESS, supplierId);
            supplierProfileBinding.nameContainer.getRoot().setVisibility(View.GONE);
            supplierProfileBinding.phoneContainer.getRoot().setVisibility(View.GONE);
            supplierProfileBinding.addressContainer.getRoot().setVisibility(View.VISIBLE);
            KeyboardVisibilityEvent.showKeyboard(
                    this,
                    supplierProfileBinding.addressContainer.inputAddress,
                    supplierProfileBinding.rootView);
        }
    }

    public void onSubmitNameClicked() {
        viewModel.saveName(supplierProfileBinding.nameContainer.inputName.getText().toString());
    }

    public void onSubmitAddressClicked() {
        viewModel.saveAddress(
                supplierProfileBinding.addressContainer.inputAddress.getText().toString());
    }

    public void onSubmitPhoneClicked() {
        viewModel.saveMobile(supplierProfileBinding.phoneContainer.inputPhone.getText().toString());
    }

    public void switchBoxClicked() {
        if (isRegistered) {
            showNotAllowedDialog();
        }
    }

    public void dimLayoutClicked() {
        KeyboardVisibilityEvent.hideKeyboard(this);
    }

    /**
     * ************************************************************** MVP methods
     * **************************************************************
     */
    @Override
    public void showError() {
        Toast.makeText(this, R.string.err_default, Toast.LENGTH_SHORT).show();
        finish();
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
                            public void onCancel() {
                            }
                        });
    }

    @Override
    public void setName(String supplierName) {
        if (supplierName != null) {
            name = supplierName;
            supplierProfileBinding.name.setText(supplierName);
            supplierProfileBinding.nameContainer.inputName.setText(supplierName);
            supplierProfileBinding.nameContainer.inputName.setSelection(supplierName.length());
        }
    }

    @Override
    public void setMobile(String mob) {
        if (mob != null) {
            mMobile = mob;
            supplierProfileBinding.mobile.setText(mob);
            supplierProfileBinding.phoneContainer.inputPhone.setText(mob);
            supplierProfileBinding.phoneContainer.inputPhone.setSelection(mob.length());
            supplierProfileBinding.mobile.setTextColor(
                    ContextCompat.getColor(this, R.color.grey800));
        } else {
            supplierProfileBinding.mobile.setText(getString(R.string.custpr_add_mobile));
            supplierProfileBinding.phoneContainer.inputPhone.setText("");
            supplierProfileBinding.mobile.setTextColor(
                    ContextCompat.getColor(this, R.color.grey600));

            supplierProfileBinding.callContainer.setOnClickListener(v -> onPhoneEditClicked());
        }
    }

    @Override
    public void setAddress(String addr) {
        if (addr != null) {
            supplierProfileBinding.address.setText(addr);
            supplierProfileBinding.addressContainer.inputAddress.setText(addr);
            supplierProfileBinding.addressContainer.inputAddress.setSelection(addr.length());
            supplierProfileBinding.address.setTextColor(
                    ContextCompat.getColor(this, R.color.grey800));

        } else {
            supplierProfileBinding.address.setText(getString(R.string.custpr_add_address));
            supplierProfileBinding.addressContainer.inputAddress.setText("");
            supplierProfileBinding.address.setTextColor(
                    ContextCompat.getColor(this, R.color.grey600));

            supplierProfileBinding.addressBox.setOnClickListener(v -> onAddressEditClicked());
        }
    }

    @Override
    public void showNameLoading() {
        supplierProfileBinding.nameContainer.nameButton.hide();
        supplierProfileBinding.nameContainer.editNameLoader.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideNameLoading() {
        supplierProfileBinding.nameContainer.nameButton.show();
        supplierProfileBinding.nameContainer.editNameLoader.setVisibility(View.GONE);
        KeyboardVisibilityEvent.hideKeyboard(this, supplierProfileBinding.nameContainer.inputName);
        supplierProfileBinding.nameContainer.getRoot().setVisibility(View.GONE);
    }

    @Override
    public void displayInvalidNameError() {
        supplierProfileBinding.nameContainer.errorName.setVisibility(View.VISIBLE);
        supplierProfileBinding.nameContainer.errorName.setText(getString(R.string.invalid_name));
    }

    @Override
    public void showAddressLoading() {
        supplierProfileBinding.addressContainer.addressButton.hide();
        supplierProfileBinding.addressContainer.editAddressLoader.setVisibility(View.VISIBLE);
    }

    @Override
    public void showUpdatePinDialog() {
        appLock.get().showUpdatePin(getSupportFragmentManager(), this, DELETE_SUPPLIER_PROFILE, AnalyticsEvents.SUPPLIER_PROFILE_SCREEN);
    }

    @Override
    public void hideAddressLoading() {
        supplierProfileBinding.addressContainer.addressButton.show();
        supplierProfileBinding.addressContainer.editAddressLoader.setVisibility(View.GONE);
        KeyboardVisibilityEvent.hideKeyboard(
                this, supplierProfileBinding.addressContainer.inputAddress);
        supplierProfileBinding.addressContainer.getRoot().setVisibility(View.GONE);
    }

    @Override
    public void displayInvalidAddressError() {
        supplierProfileBinding.addressContainer.errorAddress.setVisibility(View.VISIBLE);
        supplierProfileBinding.addressContainer.errorAddress.setText(
                getString(R.string.invalid_address));
    }

    @Override
    public void displayInvalidMobileError() {
        supplierProfileBinding.phoneContainer.errorPhone.setVisibility(View.VISIBLE);
        supplierProfileBinding.phoneContainer.errorPhone.setText(
                getString(R.string.invalid_mobile));
    }

    @Override
    public void showMobileLoading() {
        supplierProfileBinding.phoneContainer.phoneButton.hide();
        supplierProfileBinding.phoneContainer.loader.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideMobileLoading() {
        supplierProfileBinding.phoneContainer.phoneButton.show();
        supplierProfileBinding.phoneContainer.loader.setVisibility(View.GONE);
        KeyboardVisibilityEvent.hideKeyboard(
                this, supplierProfileBinding.phoneContainer.inputPhone);
        supplierProfileBinding.phoneContainer.getRoot().setVisibility(View.GONE);
    }

    @Override
    public void showActiveCyclicAccount(SupplierCreditServerErrors.Error info) {
        tracker.trackViewRelationship(
                PropertyValue.FALSE,
                PropertyValue.SUPPLIER,
                PropertyValue.FALSE,
                info.getId(),
                null,
                isRegistered,
                null
        );
        CyclicAccountDialog.Companion.showCustomerConflict(
                this,
                info,
                () -> {
                    startActivity(
                            MainActivity.startingIntent(
                                    this, info.getId(), MainActivity.CUSTOMER_SCREEN));
                });
    }

    @Override
    public void onMobileConflict(Supplier supplier) {

        supplierProfileBinding.phoneContainer.errorPhone.setVisibility(View.VISIBLE);
        if (supplier == null) {
            supplierProfileBinding.phoneContainer.errorPhone.setText(
                    getString(R.string.err_mobile_conflict_default));
        } else {
            supplierProfileBinding.phoneContainer.errorPhone.setText(
                    getString(R.string.err_mobile_conflict, supplier.getName()));
        }
        supplierProfileBinding.phoneContainer.phoneButton.show();
        supplierProfileBinding.phoneContainer.loader.setVisibility(View.GONE);
    }

    @Override
    public void gotoDeleteScreen(String supplierId) {
        startActivity(DeleteSupplierActivity.startingIntent(this, supplierId));
    }

    @Override
    public void gotoLogin() {
        super.gotoLogin();
    }

    @Override
    public void openCamera() {
        Permission.INSTANCE.requestStorageAndCameraPermission(
                this,
                new IPermissionListener() {
                    @Override
                    public void onPermissionGrantedFirstTime() {
                        tracker.trackRuntimePermission(
                                PropertyValue.SUPPLIER, PropertyValue.STORAGE, true);
                    }

                    @Override
                    public void onPermissionGranted() {
                        Belvedere.from(SupplierProfileActivity.this)
                                .camera()
                                .open(SupplierProfileActivity.this);
                    }

                    @Override
                    public void onPermissionDenied() {
                        tracker.trackRuntimePermission(
                                PropertyValue.SUPPLIER, PropertyValue.STORAGE, false);
                    }
                });
    }

    @Override
    public void openGallery() {
        Permission.INSTANCE.requestStoragePermission(
                this,
                new IPermissionListener() {
                    @Override
                    public void onPermissionGrantedFirstTime() {
                        tracker.trackRuntimePermission(
                                PropertyValue.SUPPLIER, PropertyValue.STORAGE, true);
                    }

                    @Override
                    public void onPermissionGranted() {
                        Belvedere.from(SupplierProfileActivity.this)
                                .document()
                                .contentType("image/*")
                                .allowMultiple(false)
                                .open(SupplierProfileActivity.this);
                    }

                    @Override
                    public void onPermissionDenied() {
                        tracker.trackRuntimePermission(
                                PropertyValue.SUPPLIER, PropertyValue.STORAGE, false);
                    }
                });
    }

    @Override
    public void showMobileEditBox() {
        supplierProfileBinding.nameContainer.getRoot().setVisibility(View.GONE);
        supplierProfileBinding.phoneContainer.getRoot().setVisibility(View.VISIBLE);
        supplierProfileBinding.addressContainer.getRoot().setVisibility(View.GONE);
        KeyboardVisibilityEvent.showKeyboard(
                this,
                supplierProfileBinding.phoneContainer.inputPhone,
                supplierProfileBinding.rootView);
    }

    @Override
    public void onClickCamera() {
        isCameraImage = true;
        tracker.trackSelectProfileV1(
                PropertyValue.SUPPLIER, "", PropertyValue.CAMERA, supplierId);
        viewModel.onCameraClicked();
        bottomSheet.dismiss();
    }

    @Override
    public void onClickGallery() {
        isCameraImage = false;
        tracker.trackSelectProfileV1(
                PropertyValue.SUPPLIER, "", PropertyValue.GALLERY, supplierId);
        viewModel.onGalleryClicked();
        bottomSheet.dismiss();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
    }

    @Override
    public void goToResetPasswordScreen(String mobile) {
        appLock.get().showSetNewPin(getSupportFragmentManager(), this, DELETE_SUPPLIER_PROFILE, AnalyticsEvents.SUPPLIER_PROFILE_SCREEN);
    }

    @Override
    public void showDeletePasswordLoader() {
        supplierProfileBinding.deleteContainer.setEnabled(false);
    }

    @Override
    public void hideDeletePasswordLoader() {
        supplierProfileBinding.deleteContainer.setEnabled(true);
    }

    @Override
    public void setSupplier(Supplier supplier) {
        supplierId = supplier.getId();
        isRegistered = supplier.getRegistered();
        profileUrl = supplier.getProfileImage();

        if (supplier.getMobile() == null || supplier.getMobile().equals("")) {
            supplierProfileBinding.llCommunication.setVisibility(View.GONE);
        } else {
            supplierProfileBinding.llCommunication.setVisibility(View.VISIBLE);
        }

        if (isRegistered) {

            // callContainer.setEnabled(false);
            // addressBox.setEnabled(false);
            supplierProfileBinding.switchBox.setClickable(true);
            supplierProfileBinding.switchSms.setClickable(false);
            supplierProfileBinding.switchSms.setEnabled(false);
            supplierProfileBinding.personalsIcon.setVisibility(View.VISIBLE);

            supplierProfileBinding.ivReminder.setColorFilter(
                    getResources().getColor(R.color.grey800));
            supplierProfileBinding.tvReminder.setTextColor(
                    ContextCompat.getColor(this, R.color.grey800));

            supplierProfileBinding.callIcon.setColorFilter(
                    getResources().getColor(R.color.grey800));
            supplierProfileBinding.mobile.setTextColor(
                    ContextCompat.getColor(this, R.color.grey800));

            supplierProfileBinding.homeIcon.setColorFilter(
                    getResources().getColor(R.color.grey800));
            supplierProfileBinding.address.setTextColor(
                    ContextCompat.getColor(this, R.color.grey800));

            supplierProfileBinding.smsIcon.setColorFilter(getResources().getColor(R.color.grey800));
            supplierProfileBinding.smsText.setTextColor(
                    ContextCompat.getColor(this, R.color.grey800));

            supplierProfileBinding.globeIcon.setColorFilter(
                    getResources().getColor(R.color.grey800));
            supplierProfileBinding.smsLangText.setTextColor(
                    ContextCompat.getColor(this, R.color.grey800));
            supplierProfileBinding.smsLang.setTextColor(
                    ContextCompat.getColor(this, R.color.grey800));

            supplierProfileBinding.smsText.setText(getString(R.string.transaction_notification));
            supplierProfileBinding.smsLangText.setText(getString(R.string.notification_language));
        }
        supplierProfileBinding.blockContainer.setVisibility(
                isRegistered ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showTransactionRestrictionDeletion(Supplier supplier) {
        Toast.makeText(
                this,
                getString(
                        R.string.common_ledger_transaction_restriction, supplier.getName()),
                Toast.LENGTH_SHORT)
                .show();
    }

    @Override
    public void showBlockRelationShipDialog(
            String type, String name, String number, String profileImg) {
        BlockRelationShipDialogFragment fragment =
                BlockRelationShipDialogFragment.Companion.newInstance(
                        BlockRelationShipDialogFragment.Companion.getSCREEN_SUPPLIER(),
                        type,
                        name,
                        profileImg,
                        number);
        fragment.show(getSupportFragmentManager(), fragment.getTag());
        fragment.setListener(
                new BlockRelationShipDialogFragment.Listener() {
                    @Override
                    public void onAction(String action) {
                        viewModel.blockTransaction(
                                action.equals(getString(R.string.block))
                                        ? Supplier.BLOCKED
                                        : Supplier.ACTIVE);
                    }
                });
    }

    @Override
    public void setBlockField(boolean isBlocked) {
        if (isBlocked) {
            supplierProfileBinding.blockImg.setImageResource(R.drawable.ic_unblock);
            supplierProfileBinding.blockText.setText(getResources().getString(R.string.unblock));
        } else {
            supplierProfileBinding.blockImg.setImageResource(R.drawable.ic_block);
            supplierProfileBinding.blockText.setText(getResources().getString(R.string.block));
        }
        supplierProfileBinding.blockContainer.setVisibility(
                isRegistered ? View.VISIBLE : View.GONE);
    }

    @Override
    public void goToSupplierScreen() {
        setResult(MainActivity.SUPPLIER_PROFILE_ACTIVITY_RESULT_CODE);
        finish();
    }

    @Override
    public void setUnseenAndTotalBillsCount(Integer total, Integer newBill) {
        if (newBill != 0) {
            supplierProfileBinding.newBillCount.setText(total.toString());
            supplierProfileBinding.totalBillCount.setText(R.string.new_bill_text);
        } else {
            supplierProfileBinding.newBillCount.setVisibility(View.GONE);
            supplierProfileBinding.totalBillCount.setText(total.toString());
        }
    }

    @Override
    public void isBillFeatureEnabled(Boolean canShow) {
        if (canShow) {
            supplierProfileBinding.llbill.setVisibility(View.VISIBLE);
        } else {
            supplierProfileBinding.llbill.setVisibility(View.GONE);
        }
    }

    @Override
    public void isNewBillFeatureEnabled(Boolean canShow) {
        if (canShow) {
            supplierProfileBinding.labelText.setVisibility(View.VISIBLE);
        } else {
            supplierProfileBinding.labelText.setVisibility(View.GONE);
        }
    }

    @Override
    public void goToHomeWithClearStack() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finishAffinity();
    }

    private void showNotAllowedDialog() {
        alertDialog = VerifiedDialog.Companion.show(this, profileUrl);
    }

    @Override
    public void onSetPinClicked(int requestCode) {
        isSetNewPin = true;
        startActivityForResult(
                appLock.get()
                        .appLock(
                                getString(R.string.changepin_screen_deeplink),
                                this,
                                "Supplier Profile Screen",
                                SET_PIN),
                DELETE_SUPPLIER_PROFILE);
    }

    @Override
    public void onUpdateDialogDismissed() {
    }

    @Override
    public void onDismissed() {
    }

    @Override
    public void onSetNewPinClicked(int requestCode) {
        isSetNewPin = false;
        startActivityForResult(
                appLock.get()
                        .appLock(
                                getString(R.string.changepin_screen_deeplink),
                                this,
                                "Supplier Profile Screen",
                                SET_PIN),
                DELETE_SUPPLIER_PROFILE);
    }
}
