package in.okcredit.ui.customer_profile;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.google.common.base.Strings;
import com.google.firebase.perf.metrics.AddTrace;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.Lazy;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;
import in.okcredit.R;
import in.okcredit.analytics.PropertyValue;
import in.okcredit.analytics.Tracker;
import in.okcredit.backend.contract.Customer;
import in.okcredit.databinding.CustprActivityBinding;
import in.okcredit.fileupload.usecase.IImageLoader;
import in.okcredit.frontend.ui.MainActivity;
import in.okcredit.frontend.ui.SupplierActivity;
import in.okcredit.frontend.ui._dialogs.BottomSheetReminderPickerFragment;
import in.okcredit.merchant.customer_ui.analytics.CustomerEventTracker;
import in.okcredit.merchant.customer_ui.ui.add_customer_dialog.AddNumberDialogScreen;
import in.okcredit.merchant.customer_ui.ui.dialogs.BlockRelationShipDialogFragment;
import in.okcredit.merchant.customer_ui.ui.dialogs.CyclicAccountDialog;
import in.okcredit.merchant.suppliercredit.Supplier;
import in.okcredit.shared.utils.ScreenName;
import in.okcredit.ui._base_v2.BaseActivity;
import in.okcredit.ui._dialog.NetworkErrorDialog;
import in.okcredit.ui._dialog.SmsLanguageDialog;
import in.okcredit.ui._dialog.TxSmsDialog;
import in.okcredit.ui._dialog.VerifiedDialog;
import in.okcredit.ui.delete_customer.DeleteCustomerActivity;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import tech.okcredit.android.base.animation.AnimationUtils;
import tech.okcredit.android.base.language.LocaleManager;
import tech.okcredit.android.base.utils.keyboardUtils.KeyboardVisibilityEvent;
import tech.okcredit.app_contract.LegacyNavigator;
import tech.okcredit.base.Traces;
import tech.okcredit.base.permission.IPermissionListener;
import tech.okcredit.base.permission.Permission;
import tech.okcredit.bill_management_ui.BillActivity;
import tech.okcredit.userSupport.SupportRepository;
import timber.log.Timber;
import zendesk.belvedere.Belvedere;
import zendesk.belvedere.Callback;
import zendesk.belvedere.MediaResult;

public class CustomerProfileActivity extends BaseActivity
        implements CustomerProfileContract.View,
                BottomSheetMediaFragment.OnBottomSheetFragmentListener,
                BottomSheetReminderPickerFragment.OnBottomSheetFragmentListner,
                HasAndroidInjector {

    public static final String EXTRA_CUSTOMER_ID = "extra_customer_id";
    public static final String IS_EDIT_MOBILE = "is_edit_mobile";

    @Inject Lazy<Tracker> tracker;

    @Inject Lazy<IImageLoader> imageLoader;

    @Inject Lazy<CustomerProfileContract.Presenter> viewModel;

    @Inject Lazy<LegacyNavigator> legacyNavigator;

    @Inject DispatchingAndroidInjector<Object> dispatchingAndroidInjector;

    @Inject Lazy<SupportRepository> userSupport;

    CustprActivityBinding binding;
    @Inject DispatchingAndroidInjector<Object> androidInjector;
    private boolean isCameraImage;
    private boolean isRegistered;
    private String profileUrl;
    private String mMobile = "";
    private String customerId = "";
    private BottomSheetMediaFragment bottomSheet;
    private AlertDialog alertDialog;
    private boolean isInputFieldsSet;
    private String customerName;

    public static Intent startingIntent(
            Context context, @NonNull String customerId, Boolean isEditMobile) {
        Intent intent = new Intent(context, CustomerProfileActivity.class);
        intent.putExtra(EXTRA_CUSTOMER_ID, customerId);
        intent.putExtra(IS_EDIT_MOBILE, isEditMobile);
        return intent;
    }

    @Override
    @AddTrace(name = Traces.OnCreateCustomerProfile)
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = CustprActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        showActionBar(true);
        setTitle(R.string.account_profile);

        getWindow()
                .setBackgroundDrawable(
                        new ColorDrawable(ContextCompat.getColor(this, R.color.grey50)));

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        bottomSheet = BottomSheetMediaFragment.newInstance();
        binding.phoneContainer.inputPhone.setHint(getString(R.string.mobile));
        binding.phoneContainer.phoneButton.setImageResource(R.drawable.ic_check);
        binding.phoneContainer.phoneButton.show();
        binding.phoneContainer.importContactBottomContainer.setVisibility(View.GONE);
        binding.contextualHelp.setScreenNameValue(
                ScreenName.CustomerProfile.getValue(),
                tracker.get(),
                userSupport.get(),
                legacyNavigator.get());

        binding.switchSms.setOnClickListener(
                v -> {
                    if (isRegistered) {
                        showNotAllowedDialog();
                    } else {
                        viewModel.get().txSmsSwitchChanged(binding.switchSms.isChecked(), false);
                    }
                });

        binding.permissionSwitch.setOnClickListener(
                v -> {
                    tracker.get()
                            .trackSelectProfileV1(
                                    PropertyValue.CUSTOMER,
                                    "",
                                    PropertyValue.CUSTOMER_PERMISSION,
                                    customerId);
                    viewModel
                            .get()
                            .addTransactionPermissionSwitchChanged(
                                    binding.permissionSwitch.isChecked());
                });

        binding.migratelayout.setOnClickListener(
                v -> {
                    tracker.get()
                            .trackRelationShipMigrationStarted(
                                    PropertyValue.CUSTOMER,
                                    "Supplier",
                                    "Relationship Profile ",
                                    customerId);
                    legacyNavigator.get().goToMoveToSupplierScreen(this, customerId);
                });

        initListeners();
        setupKeyBoard();
        textChangedListener();
        textActionListener();

        binding.rootView.setTracker(performanceTracker);
    }

    private void textActionListener() {
        binding.phoneContainer.inputPhone.setOnEditorActionListener(
                (v, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        viewModel
                                .get()
                                .saveMobile(binding.phoneContainer.inputPhone.getText().toString());
                        return true;
                    }
                    return false;
                });

        binding.nameContainer.inputName.setOnEditorActionListener(
                (v, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        onSubmitNameClicked();
                        return true;
                    }
                    return false;
                });

        binding.addressContainer.inputAddress.setOnEditorActionListener(
                (v, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        onSubmitAddressClicked();
                        return true;
                    }
                    return false;
                });
    }

    private void setupKeyBoard() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        KeyboardVisibilityEvent.setEventListener(
                this,
                isOpen -> {
                    if (!isOpen) {
                        binding.nameContainer.inputName.clearFocus();
                        binding.phoneContainer.inputPhone.clearFocus();
                        binding.addressContainer.inputAddress.clearFocus();
                        binding.phoneContainer.getRoot().setVisibility(View.GONE);
                        binding.nameContainer.getRoot().setVisibility(View.GONE);
                        binding.addressContainer.getRoot().setVisibility(View.GONE);

                        AnimationUtils.fadeOut(binding.dimLayout);
                        Disposable subscribe =
                                Completable.timer(
                                                500,
                                                TimeUnit.MILLISECONDS,
                                                AndroidSchedulers.mainThread())
                                        .subscribe(
                                                () -> binding.dimLayout.setVisibility(View.GONE));
                    } else {
                        if (binding.nameContainer.getRoot().getVisibility() == View.VISIBLE
                                || binding.phoneContainer.getRoot().getVisibility() == View.VISIBLE
                                || binding.addressContainer.getRoot().getVisibility()
                                        == View.VISIBLE) {
                            binding.dimLayout.setVisibility(View.VISIBLE);
                            AnimationUtils.fadeIn(binding.dimLayout);
                        }
                    }
                });
    }

    private void textChangedListener() {
        binding.nameContainer.inputName.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(
                            CharSequence charSequence, int i, int i1, int i2) {}

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                    @Override
                    public void afterTextChanged(Editable editable) {
                        String text = editable.toString();

                        if (text.isEmpty()
                                || text.length() == 1
                                || text.length() == 29
                                || text.length() == 30
                                || text.length() == 31) {
                            TransitionManager.beginDelayedTransition(
                                    binding.nameContainer.getRoot());
                        }

                        binding.nameContainer.errorName.setVisibility(View.GONE);
                        binding.nameContainer.counterName.setVisibility(View.GONE);

                        if (!text.isEmpty()) {
                            binding.nameContainer.nameButton.show();
                            binding.nameContainer.buttonContainerName.setVisibility(View.VISIBLE);
                        } else {
                            binding.nameContainer.nameButton.hide();
                            binding.nameContainer.buttonContainerName.setVisibility(View.GONE);
                        }

                        if (text.length() >= 500) {
                            binding.nameContainer.errorName.setVisibility(View.VISIBLE);
                            binding.nameContainer.errorName.setText(
                                    getString(R.string.name_should_not));

                            binding.nameContainer.counterName.setVisibility(View.VISIBLE);
                            binding.nameContainer.counterName.setText(
                                    String.format("%d/500", text.length()));

                            binding.nameContainer.nameButton.hide();
                            binding.nameContainer.buttonContainerName.setVisibility(View.GONE);

                        } else {
                            binding.nameContainer.nameButton.show();
                            binding.nameContainer.buttonContainerName.setVisibility(View.VISIBLE);

                            binding.nameContainer.errorName.setVisibility(View.GONE);
                            binding.nameContainer.counterName.setVisibility(View.GONE);
                        }
                    }
                });

        binding.addressContainer.inputAddress.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(
                            CharSequence charSequence, int i, int i1, int i2) {}

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                    @Override
                    public void afterTextChanged(Editable editable) {
                        String text = editable.toString();

                        if (text.isEmpty()
                                || text.length() == 1
                                || text.length() == 149
                                || text.length() == 150
                                || text.length() == 151) {
                            TransitionManager.beginDelayedTransition(
                                    binding.addressContainer.getRoot());
                        }

                        if (text.length() > 0) {
                            binding.addressContainer.addressButton.show();
                            binding.addressContainer.buttonContainerAddress.setVisibility(
                                    View.VISIBLE);
                        } else {
                            binding.addressContainer.addressButton.hide();
                            binding.addressContainer.buttonContainerAddress.setVisibility(
                                    View.GONE);
                        }
                        if (text.length() == 150) {
                            binding.addressContainer.errorAddress.setVisibility(View.VISIBLE);
                            binding.addressContainer.errorAddress.setText(
                                    getString(R.string.address_should_not));

                            binding.addressContainer.counterAddress.setVisibility(View.VISIBLE);
                            binding.addressContainer.counterAddress.setText(
                                    String.format("%d/150", text.length()));
                        } else {
                            binding.addressContainer.errorAddress.setVisibility(View.GONE);
                            binding.addressContainer.counterAddress.setVisibility(View.GONE);
                        }
                    }
                });

        binding.phoneContainer.inputPhone.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(
                            CharSequence charSequence, int i, int i1, int i2) {}

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                    @Override
                    public void afterTextChanged(Editable editable) {
                        String text = editable.toString();

                        if (text.isEmpty()
                                || text.length() == 1
                                || text.length() == 10
                                || text.length() == 11) {
                            TransitionManager.beginDelayedTransition(
                                    binding.phoneContainer.getRoot());
                        }

                        if (text.length() > 10) {
                            binding.phoneContainer.errorPhone.setText(
                                    getString(R.string.invalid_phone));
                            binding.phoneContainer.errorPhone.setVisibility(View.VISIBLE);

                            binding.phoneContainer.counterPhone.setText(
                                    String.format("%d/10", text.length()));
                            binding.phoneContainer.counterPhone.setVisibility(View.VISIBLE);
                        } else {
                            binding.phoneContainer.errorPhone.setVisibility(View.GONE);
                            binding.phoneContainer.counterPhone.setVisibility(View.GONE);
                        }
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.get().attachView(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        viewModel.get().detachView();
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
        } else if (binding.nameContainer.getRoot().getVisibility() == View.VISIBLE
                || binding.phoneContainer.getRoot().getVisibility() == View.VISIBLE
                || binding.addressContainer.getRoot().getVisibility() == View.VISIBLE) {
            binding.nameContainer.getRoot().setVisibility(View.GONE);
            binding.phoneContainer.getRoot().setVisibility(View.GONE);
            binding.addressContainer.getRoot().setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Belvedere.from(this)
                .getFilesFromActivityOnResult(
                        requestCode,
                        resultCode,
                        data,
                        new Callback<List<MediaResult>>() {
                            @Override
                            public void success(List<MediaResult> result) {
                                if (result != null && !result.isEmpty()) {
                                    viewModel
                                            .get()
                                            .setProfileImage(
                                                    isCameraImage, result.get(0).getFile());
                                }
                            }
                        });
    }

    @Override
    public void displayProfileImageFile(File image) {

        if (image == null || !image.exists() || image.length() == 0) {
            return;
        }
        Glide.with(this)
                .load(Uri.fromFile(image))
                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                .into(binding.profileImage);
    }

    @Override
    public void setProfileImageLocal(File localFile) {
        if (localFile == null || !localFile.exists() || localFile.length() == 0) {
            return;
        }
        Glide.with(this)
                .load(localFile)
                .placeholder(ContextCompat.getDrawable(this, R.drawable.ic_account_125dp))
                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                .into(binding.profileImage);
    }

    @Override
    public void setProfileImageRemote(String url) {
        Glide.with(this)
                .load(url)
                .placeholder(ContextCompat.getDrawable(this, R.drawable.ic_account_125dp))
                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                .into(binding.profileImage);
    }

    @Override
    public void showSmsLangPopup(String language) {
        alertDialog = new SmsLanguageDialog().show(
                this, language, updatedLang -> viewModel.get().updateCustomerTxAlertLanguage(language, updatedLang)
        );
    }

    @Override
    public void showSmsInitialPopup(boolean isChecked) {
        binding.switchSms.setChecked(!isChecked);
        alertDialog =
                new TxSmsDialog()
                        .show(
                                this,
                                isChecked,
                                () -> {
                                    viewModel.get().txSmsSwitchChanged(isChecked, true);
                                });
    }

    @Override
    public void setCustomerPref(
            String lang, boolean alertEnable, String reminderMode, String mobile) {
        binding.switchSms.setChecked(alertEnable);

        switch (lang) {
            case LocaleManager.LANGUAGE_ENGLISH:
                binding.smsLang.setText(getString(R.string.language_english));
                break;

            case LocaleManager.LANGUAGE_HINDI:
                binding.smsLang.setText(getString(R.string.language_hindi));
                break;

            case LocaleManager.LANGUAGE_HINGLISH:
                binding.smsLang.setText(getString(R.string.hinglish));
                break;

            case LocaleManager.LANGUAGE_MALAYALAM:
                binding.smsLang.setText(getString(R.string.language_malayalam));
                break;

            case LocaleManager.LANGUAGE_PUNJABI:
                binding.smsLang.setText(getString(R.string.language_punjabi));
                break;

            case LocaleManager.LANGUAGE_MARATHI:
                binding.smsLang.setText(getString(R.string.language_marathi));
                break;

            case LocaleManager.LANGUAGE_TAMIL:
                binding.smsLang.setText(getString(R.string.language_tamil));
                break;

            case LocaleManager.LANGUAGE_TELUGU:
                binding.smsLang.setText(getString(R.string.language_telugu));
                break;

            case LocaleManager.LANGUAGE_BENGALI:
                binding.smsLang.setText(getString(R.string.language_bengali));
                break;

            case LocaleManager.LANGUAGE_GUJARATI:
                binding.smsLang.setText(getString(R.string.language_gujarati));
                break;

            case LocaleManager.LANGUAGE_KANNADA:
                binding.smsLang.setText(getString(R.string.language_kannada));
                break;
        }

        if (!Strings.isNullOrEmpty(reminderMode) && !Strings.isNullOrEmpty(mobile)) {
            binding.reminderContainer.setVisibility(View.VISIBLE);
            binding.dividerReminder.setVisibility(View.VISIBLE);
            if (reminderMode.equals("sms")) {
                binding.reminder.setText(R.string.sms);
            } else {
                binding.reminder.setText(R.string.whatsapp);
            }
        }
    }

    private void initListeners() {
        binding.smsLangBox.setOnClickListener(v -> smsLanguageClicked());
        binding.deleteContainer.setOnClickListener(v -> viewModel.get().onDeleteClicked());
        binding.camera.setOnClickListener(v -> onCameraClicked());
        binding.nameBox.setOnClickListener(v -> onNameEditClicked());
        binding.callContainer.setOnClickListener(v -> onPhoneEditClicked());
        binding.addressBox.setOnClickListener(v -> onAddressEditClicked());
        binding.nameContainer.nameButton.setOnClickListener(v -> onSubmitNameClicked());
        binding.addressContainer.addressButton.setOnClickListener(v -> onSubmitAddressClicked());
        binding.phoneContainer.phoneButton.setOnClickListener(v -> onSubmitPhoneClicked());
        binding.reminderContainer.setOnClickListener(v -> reminderContainerClicked());
        binding.switchBox.setOnClickListener(v -> switchBoxClicked());
        binding.dimLayout.setOnClickListener(v -> dimLayoutClicked());
        binding.blockContainer.setOnClickListener(v -> onBlockClicked());
        binding.llbill.setOnClickListener(v -> onBillClicked());
    }

    private void smsLanguageClicked() {
        if (isRegistered) {
            showNotAllowedDialog();
        } else {
            tracker.get()
                    .trackSelectProfileV1(
                            PropertyValue.CUSTOMER,
                            "",
                            PropertyValue.SMS_LANG,
                            customerId);
            viewModel.get().onSmsLanguageClicked();
        }
    }

    private void onCameraClicked() {
        if (bottomSheet != null && !bottomSheet.isAdded()) {
            bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
        }
    }

    private void onNameEditClicked() {
        runOnUiThread(
                () -> {
                    tracker.get()
                            .trackSelectProfileV1(
                                    PropertyValue.CUSTOMER,
                                    "",
                                    PropertyValue.NAME,
                                    customerId);
                    binding.nameContainer.getRoot().setVisibility(View.VISIBLE);
                    binding.phoneContainer.getRoot().setVisibility(View.GONE);
                    binding.addressContainer.getRoot().setVisibility(View.GONE);
                    binding.nameContainer.inputName.setText(binding.name.getText().toString());
                    KeyboardVisibilityEvent.showKeyboard(
                            this, binding.nameContainer.inputName, binding.rootView);
                });
    }

    private void onPhoneEditClicked() {
        if (isRegistered) {
            showNotAllowedDialog();
        } else {
            viewModel.get().onEditMobileClicked();
        }
    }

    private void onAddressEditClicked() {
        if (isRegistered) {
            showNotAllowedDialog();
        } else {
            tracker.get()
                    .trackSelectProfileV1(
                            PropertyValue.CUSTOMER, "", PropertyValue.ADDRESS, customerId);
            binding.nameContainer.getRoot().setVisibility(View.GONE);
            binding.phoneContainer.getRoot().setVisibility(View.GONE);
            binding.addressContainer.getRoot().setVisibility(View.VISIBLE);
            KeyboardVisibilityEvent.showKeyboard(
                    this, binding.addressContainer.inputAddress, binding.rootView);
        }
    }

    private void onSubmitNameClicked() {
        viewModel.get().saveName(binding.nameContainer.inputName.getText().toString().trim());
    }

    private void onSubmitAddressClicked() {
        viewModel.get().saveAddress(binding.addressContainer.inputAddress.getText().toString());
    }

    private void onSubmitPhoneClicked() {
        viewModel.get().saveMobile(binding.phoneContainer.inputPhone.getText().toString());
    }

    private void reminderContainerClicked() {
        tracker.get()
                .trackSelectProfileV1(
                        PropertyValue.CUSTOMER,
                        "",
                        PropertyValue.REMINDER_SETTING,
                        customerId);

        BottomSheetReminderPickerFragment bottomSheetReminderPickerFragment =
                BottomSheetReminderPickerFragment.Companion.newInstance();

        if (!bottomSheetReminderPickerFragment.isVisible()) {
            bottomSheetReminderPickerFragment.show(
                    getSupportFragmentManager(), bottomSheetReminderPickerFragment.getTag());
        }
    }

    private void switchBoxClicked() {
        if (isRegistered) {
            showNotAllowedDialog();
        }
    }

    private void dimLayoutClicked() {
        KeyboardVisibilityEvent.hideKeyboard(this);
    }

    private void onBlockClicked() {
        viewModel.get().onBlockClicked();
    }

    private void onBillClicked() {
        startActivity(BillActivity.Companion.getIntent(this, customerId, "Customer", customerName));
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
        alertDialog =
                new NetworkErrorDialog()
                        .show(
                                this,
                                new NetworkErrorDialog.Listener() {
                                    @Override
                                    public void onNetworkOk() {
                                        viewModel.get().onInternetRestored();
                                    }

                                    @Override
                                    public void onCancel() {}
                                });
    }

    @Override
    public void setInputFields(Customer customer) {
        if (!isInputFieldsSet) {
            isInputFieldsSet = true;
            if (!Strings.isNullOrEmpty(customer.getDescription())) {
                binding.nameContainer.inputName.setText(customer.getDescription());
                binding.nameContainer.inputName.setSelection(customer.getDescription().length());
            }

            if (!Strings.isNullOrEmpty(customer.getAddress())) {
                binding.addressContainer.inputAddress.setText(customer.getAddress());
                binding.addressContainer.inputAddress.setSelection(customer.getAddress().length());
            }
        }
    }

    @Override
    public void setName(String customerName) {
        if (customerName != null) {
            this.customerName = customerName;
            binding.name.setText(customerName);
        }
    }

    @Override
    public void setMobile(String mob) {
        if (mob != null) {
            mMobile = mob;
            binding.mobile.setText(mob);
            binding.mobile.setTextColor(ContextCompat.getColor(this, R.color.grey900));
        } else {
            binding.mobile.setText(getString(R.string.custpr_add_mobile));
            binding.mobile.setTextColor(ContextCompat.getColor(this, R.color.grey600));

            binding.callContainer.setOnClickListener(v -> onPhoneEditClicked());
        }
    }

    @Override
    public void setAddress(String addr) {
        if (addr != null) {
            binding.address.setText(addr);
            binding.address.setTextColor(ContextCompat.getColor(this, R.color.grey900));

        } else {
            binding.address.setText(getString(R.string.custpr_add_address));
            binding.address.setTextColor(ContextCompat.getColor(this, R.color.grey600));

            binding.addressBox.setOnClickListener(v -> onAddressEditClicked());
        }
    }

    @Override
    public void showNameLoading() {
        binding.nameContainer.nameButton.hide();
        binding.phoneContainer.loader.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideNameLoading() {
        binding.nameContainer.nameButton.show();
        binding.phoneContainer.loader.setVisibility(View.GONE);
        KeyboardVisibilityEvent.hideKeyboard(this, binding.nameContainer.inputName);
        binding.nameContainer.getRoot().setVisibility(View.GONE);
    }

    @Override
    public void displayInvalidNameError() {
        binding.nameContainer.errorName.setVisibility(View.VISIBLE);
        binding.nameContainer.errorName.setText(getString(R.string.invalid_name));
    }

    @Override
    public void showAddressLoading() {
        binding.addressContainer.addressButton.hide();
        binding.addressContainer.editAddressLoader.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideAddressLoading() {
        binding.addressContainer.addressButton.show();
        binding.addressContainer.editAddressLoader.setVisibility(View.GONE);
        KeyboardVisibilityEvent.hideKeyboard(this, binding.addressContainer.inputAddress);
        binding.addressContainer.getRoot().setVisibility(View.GONE);
    }

    @Override
    public void displayInvalidAddressError() {
        binding.addressContainer.errorAddress.setVisibility(View.VISIBLE);
        binding.addressContainer.errorAddress.setText(getString(R.string.invalid_address));
    }

    @Override
    public void displayInvalidMobileError() {
        binding.phoneContainer.errorPhone.setVisibility(View.VISIBLE);
        binding.phoneContainer.errorPhone.setText(getString(R.string.invalid_mobile));
    }

    @Override
    public void showMobileLoading() {
        binding.phoneContainer.phoneButton.hide();
        binding.phoneContainer.loader.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideMobileLoading() {
        binding.phoneContainer.phoneButton.show();
        binding.phoneContainer.loader.setVisibility(View.GONE);
        KeyboardVisibilityEvent.hideKeyboard(this, binding.phoneContainer.inputPhone);
        binding.phoneContainer.getRoot().setVisibility(View.GONE);
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onMobileConflict(Customer customer) {
        binding.phoneContainer.errorPhone.setVisibility(View.VISIBLE);
        if (customer == null) {
            binding.phoneContainer.errorPhone.setText(
                    getString(R.string.err_mobile_conflict_default));
        } else {
            binding.phoneContainer.errorPhone.setText(
                    getString(R.string.err_mobile_conflict, customer.getDescription()));
        }
        binding.phoneContainer.phoneButton.show();
        binding.phoneContainer.loader.setVisibility(View.GONE);
    }

    @Override
    public void gotoDeleteScreen(String customerId) {
        startActivity(DeleteCustomerActivity.startingIntent(this, customerId));
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
                        tracker.get()
                                .trackRuntimePermission(
                                        PropertyValue.CUSTOMER, PropertyValue.STORAGE, true);
                    }

                    @Override
                    public void onPermissionGranted() {
                        Belvedere.from(CustomerProfileActivity.this)
                                .camera()
                                .open(CustomerProfileActivity.this);
                    }

                    @Override
                    public void onPermissionDenied() {
                        tracker.get()
                                .trackRuntimePermission(
                                        PropertyValue.CUSTOMER, PropertyValue.STORAGE, false);
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
                        tracker.get()
                                .trackRuntimePermission(
                                        PropertyValue.CUSTOMER, PropertyValue.STORAGE, true);
                    }

                    @Override
                    public void onPermissionGranted() {
                        Belvedere.from(CustomerProfileActivity.this)
                                .document()
                                .contentType("image/*")
                                .allowMultiple(false)
                                .open(CustomerProfileActivity.this);
                    }

                    @Override
                    public void onPermissionDenied() {
                        tracker.get()
                                .trackRuntimePermission(
                                        PropertyValue.CUSTOMER, PropertyValue.STORAGE, false);
                    }
                });
    }

    @Override
    public void onClickCamera() {
        isCameraImage = true;
        tracker.get()
                .trackSelectProfileV1(
                        PropertyValue.CUSTOMER, "", PropertyValue.CAMERA, customerId);
        viewModel.get().onCameraClicked();
        bottomSheet.dismiss();
    }

    @Override
    public void onClickGallery() {
        isCameraImage = false;
        tracker.get()
                .trackSelectProfileV1(
                        PropertyValue.CUSTOMER, "", PropertyValue.GALLERY, customerId);
        viewModel.get().onGalleryClicked();
        bottomSheet.dismiss();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {}

    @Override
    public void onClickSms() {
        viewModel.get().onReminderModeSelected("sms");
    }

    @Override
    public void onClickWhatsapp() {
        Timber.d("whatsapp mode selected");
        viewModel.get().onReminderModeSelected("whatsapp");
    }

    @Override
    public void setCustomer(Customer customer) {
        customerId = customer.getId();
        profileUrl = customer.getProfileImage();
        if (customer.getMobile() == null || "".equals(customer.getMobile())) {
            binding.llCommunication.setVisibility(View.GONE);
        } else {
            binding.llCommunication.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void supplierCreditEnabledCustomer(boolean supplierCreditEnabledCustomer) {
        isRegistered = supplierCreditEnabledCustomer;

        if (supplierCreditEnabledCustomer) {
            binding.switchBox.setClickable(true);
            binding.switchSms.setClickable(false);
            binding.switchSms.setEnabled(false);
            binding.personalsIcon.setVisibility(View.VISIBLE);

            binding.callIcon.setColorFilter(getResources().getColor(R.color.grey800));
            binding.mobile.setTextColor(ContextCompat.getColor(this, R.color.grey800));

            binding.homeIcon.setColorFilter(getResources().getColor(R.color.grey800));
            binding.address.setTextColor(ContextCompat.getColor(this, R.color.grey800));

            binding.smsIcon.setColorFilter(getResources().getColor(R.color.grey800));
            binding.smsText.setTextColor(ContextCompat.getColor(this, R.color.grey800));

            binding.globeIcon.setColorFilter(getResources().getColor(R.color.grey800));
            binding.smsLangText.setTextColor(ContextCompat.getColor(this, R.color.grey800));
            binding.smsLang.setTextColor(ContextCompat.getColor(this, R.color.grey800));

            binding.smsText.setText(getString(R.string.transaction_notification));
            binding.smsLangText.setText(getString(R.string.notification_language));

            binding.blockContainer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void showBlockRelationShipDialog(String type, Customer customer) {
        BlockRelationShipDialogFragment fragment =
                BlockRelationShipDialogFragment.Companion.newInstance(
                        BlockRelationShipDialogFragment.Companion.getSCREEN_CUSTOMER(),
                        type,
                        customer.getDescription(),
                        customer.getProfileImage(),
                        customer.getMobile());
        fragment.show(getSupportFragmentManager(), fragment.getTag());
        fragment.setListener(
                action ->
                        viewModel
                                .get()
                                .blockTransaction(
                                        action.equals(getString(R.string.block))
                                                ? Customer.State.BLOCKED
                                                : Customer.State.ACTIVE));
    }

    @Override
    public void setBlockField(boolean supplierCreditEnabledCustomer, Customer customer) {
        if (supplierCreditEnabledCustomer) {
            setBlockState(customer.getState() == Customer.State.BLOCKED);
        }
    }

    @Override
    public void setBlockState(boolean isBlocked) {
        if (isBlocked) {
            binding.blockImg.setImageResource(R.drawable.ic_unblock);
            binding.blockText.setText(getResources().getString(R.string.unblock));
        } else {
            binding.blockImg.setImageResource(R.drawable.ic_block);
            binding.blockText.setText(getResources().getString(R.string.block));
        }
    }

    @Override
    public void setOldPermissionState(boolean permissionCheck) {
        binding.permissionSwitch.setChecked(permissionCheck);
    }

    @Override
    public void supplierAddTransactionRestriction(
            Boolean supplierCreditEnabledCustomer, Customer customer) {
        if (supplierCreditEnabledCustomer) {
            binding.permissionSwitch.setChecked(customer.isAddTransactionPermissionDenied());
        }
    }

    @Override
    public void goToCustomerScreen() {
        setResult(MainActivity.CUSTOMER_PROFILE_ACTIVITY_RESULT_CODE);
        finish();
    }

    @Override
    public void showLoader() {
        binding.progressBarUpdatePref.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoader() {
        binding.progressBarUpdatePref.setVisibility(View.GONE);
    }

    @Override
    public void showActiveCyclicAccount(Supplier info) {
        CyclicAccountDialog.Companion.showSupplierConflict(
                this,
                info,
                () -> startActivity(SupplierActivity.startingSupplierIntent(this, info.getId())));
    }

    @Override
    public void showDeletedCyclicAccount(Supplier info) {
        CyclicAccountDialog.Companion.showSupplierConflict(
                this,
                info,
                () ->
                        startActivity(
                                SupplierActivity.startingSupplierScreenForReactivation(
                                        this, info.getId(), null)));
    }

    private void showNotAllowedDialog() {
        alertDialog = VerifiedDialog.Companion.show(this, profileUrl);
    }

    @Override
    public void openAddNumberPopup() {
        String description;
        if (Strings.isNullOrEmpty(mMobile)) {
            description = getString(R.string.please_add_customer_number);
        } else {
            description = getString(R.string.please_add_customer_new_number_to_update);
        }
        AddNumberDialogScreen dialogScreen =
                AddNumberDialogScreen.Companion.newInstance(
                        customerId,
                        description,
                        mMobile,
                        false,
                        false,
                        CustomerEventTracker.RELATION_CUSTOMER);
        dialogScreen.show(getSupportFragmentManager(), AddNumberDialogScreen.TAG);
    }

    @Override
    public void isAddTransactionRestricted(Boolean isAddTransactionRestricted) {
        if (isAddTransactionRestricted) {
            binding.permissionContianer.setVisibility(View.VISIBLE);
        } else {
            binding.permissionContianer.setVisibility(View.GONE);
        }
    }

    @Override
    public void setUnseenAndTotalBillsCount(Integer total, Integer newBill) {
        if (newBill != 0) {
            binding.newBillCount.setText(newBill.toString());
            binding.totalBillCount.setText(R.string.new_bill_text);
        } else {
            binding.newBillCount.setVisibility(View.GONE);
            binding.totalBillCount.setText(total.toString());
        }
    }

    @Override
    public void isBillFeatureEnabled(Boolean canShow) {
        if (canShow) {
            binding.llbill.setVisibility(View.VISIBLE);
        } else {
            binding.llbill.setVisibility(View.GONE);
        }
    }

    @Override
    public void isNewBillFeatureEnabled(Boolean canShow) {
        if (canShow) {
            binding.labelText.setVisibility(View.VISIBLE);
        } else {
            binding.labelText.setVisibility(View.GONE);
        }
    }

    @Override
    public AndroidInjector<Object> androidInjector() {
        return dispatchingAndroidInjector;
    }
}
