package tech.okcredit.home.ui.activity

import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend.contract.RxSharedPrefValues
import `in`.okcredit.backend.contract.Signout
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import kotlinx.coroutines.rx2.asObservable
import kotlinx.coroutines.rx2.rxCompletable
import tech.okcredit.android.auth.AuthService
import tech.okcredit.android.base.preferences.DefaultPreferences
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.home.R
import tech.okcredit.home.ui.activity.HomeActivityContract.*
import tech.okcredit.home.ui.activity.HomeActivityContract.PartialState.NoChange
import tech.okcredit.home.ui.activity.HomeActivityContract.PartialState.SetupExperiments
import tech.okcredit.home.ui.activity.HomeActivityContract.ViewEvent.GoToLogin
import tech.okcredit.home.ui.activity.usecase.GetHomeBottomMenuOptions
import tech.okcredit.home.ui.activity.viewpager.BottomMenuItem
import tech.okcredit.home.ui.activity.viewpager.NavItem
import tech.okcredit.home.usecase.AppLaunchDataSyncer
import javax.inject.Inject

class HomeActivityViewModel @Inject constructor(
    initialState: Lazy<State>,
    @ViewModelParam(HomeActivity.EXTRA_WEB_URL) val webUrl: String?,
    private val signOut: Lazy<Signout>,
    private val tracker: Lazy<Tracker>,
    private val authService: Lazy<AuthService>,
    private val rxSharedPreference: Lazy<DefaultPreferences>,
    private val getHomeBottomMenuOptions: Lazy<GetHomeBottomMenuOptions>,
    private val appLaunchDataSyncer: Lazy<AppLaunchDataSyncer>,
) : BaseViewModel<State, PartialState, ViewEvent>(initialState.get()) {

    override fun handle(): Observable<out UiState.Partial<State>> {
        return Observable.mergeArray(
            checkForLoadWebUrl(),
            observeAuthState(),
            observableForEnabledFeaturesOnLoad(),
            observeDashboardEducationOnLoad(),
            dashboardEducationShown(),
            appLaunchDataSync(),
        )
    }

    private fun checkForLoadWebUrl() = intent<Intent.Load>().map {
        if (!webUrl.isNullOrEmpty()) {
            emitViewEvent(ViewEvent.GoToWebScreen(webUrl))
        }
        NoChange
    }

    private fun dashboardEducationShown(): Observable<PartialState> {
        return intent<Intent.DashboardEducationShown>()
            .switchMap {
                wrap(
                    rxCompletable {
                        rxSharedPreference.get()
                            .set(RxSharedPrefValues.SHOULD_SHOW_HOME_DASHBOARD_EDUCATION, false, Scope.Individual)
                    }
                )
            }.map { NoChange }
    }

    private fun observeDashboardEducationOnLoad(): Observable<PartialState> {
        return intent<Intent.Load>()
            .take(1)
            .switchMap {
                wrap(
                    rxSharedPreference.get()
                        .getBoolean(RxSharedPrefValues.SHOULD_SHOW_HOME_DASHBOARD_EDUCATION, Scope.Individual)
                        .asObservable().firstOrError()
                )
            }
            .map {
                when (it) {
                    is Result.Progress -> NoChange
                    is Result.Success -> {
                        if (it.value) {
                            emitViewEvent(ViewEvent.ShowDashboardEducation)
                            pushIntent(Intent.DashboardEducationShown)
                        }
                        NoChange
                    }
                    is Result.Failure -> NoChange
                }
            }
    }

    private fun observeAuthState(): Observable<PartialState> {
        return intent<Intent.OnResume>()
            .switchMap { UseCase.wrapObservable(authService.get().authState()) }
            .filter { !signOut.get().isInProgress() }
            .map {
                when (it) {
                    is Result.Success -> {
                        if (!it.value) {
                            tracker.get().trackError(PropertyValue.HOME_PAGE, "Auth", "Auth Error")
                            emitViewEvent(GoToLogin)
                        }
                        NoChange
                    }
                    else -> NoChange
                }
            }
    }

    private fun observableForEnabledFeaturesOnLoad(): Observable<PartialState> {
        return intent<Intent.Load>()
            .switchMap {
                wrap(getHomeBottomMenuOptions.get().execute())
            }
            .map {
                when (it) {
                    is Result.Progress -> NoChange
                    is Result.Success -> {
                        SetupExperiments(bottomNavItemsList = it.value.bottomNavItems)
                    }
                    is Result.Failure -> {
                        // backOff if failed to load experiment data
                        SetupExperiments(
                            bottomNavItemsList = arrayListOf(
                                BottomMenuItem(
                                    navItem = NavItem.HOME_FRAGMENT,
                                    drawableId = R.drawable.ic_ledger,
                                    stringId = R.string.ledger,
                                    contentDescriptionId = R.string.content_description_home_bottom_nav_ledger,
                                ),
                                BottomMenuItem(
                                    navItem = NavItem.HOME_MENU_FRAGMENT,
                                    drawableId = R.drawable.ic_hamburger,
                                    stringId = R.string.menu,
                                    contentDescriptionId = R.string.content_description_home_bottom_nav_menu,
                                )
                            )
                        )
                    }
                }
            }
            .doOnNext {
                if (it is SetupExperiments) {
                    emitViewEvent(ViewEvent.SetupViewPager(it.bottomNavItemsList))
                }
            }
    }

    private fun appLaunchDataSync() = intent<Intent.Load>()
        .switchMap {
            wrap { appLaunchDataSyncer.get().execute() }
        }
        .map {
            NoChange
        }

    override fun reduce(currentState: State, partialState: PartialState): State {
        return when (partialState) {
            is NoChange -> currentState
            is SetupExperiments -> currentState
        }
    }
}
