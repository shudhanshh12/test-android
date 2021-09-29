package in.okcredit.ui.whatsapp._di;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import in.okcredit.ui.whatsapp.WhatsAppActivity;
import in.okcredit.ui.whatsapp.WhatsAppContract;
import in.okcredit.ui.whatsapp.WhatsAppPresenter;
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam;
import tech.okcredit.base.dagger.di.scope.ActivityScope;

@Module
public abstract class WhatsAppActivityModule {

    @Provides
    @ActivityScope
    @ViewModelParam("mobile")
    public static String mobile(WhatsAppActivity activity) {
        return activity.getIntent().getStringExtra(WhatsAppActivity.ARG_MOBILE);
    }

    @Provides
    @ActivityScope
    @ViewModelParam("is_from_help")
    public static boolean isFromHelp(WhatsAppActivity activity) {
        return activity.getIntent().getBooleanExtra(WhatsAppActivity.ARG_IS_WHATSAPP, false);
    }

    @Binds
    @ActivityScope
    public abstract WhatsAppContract.Presenter viewModel(WhatsAppPresenter presenter);
}
