package `in`.okcredit.frontend.usecase

import com.nhaarman.mockitokotlin2.mock
import tech.okcredit.android.ab.AbRepository

class ExpenseOnBoardingTest {
    private val ab: AbRepository = mock()
    private val expenseOnBoarding = ExpenseOnBoarding(ab)

//
//    @Test
//    fun `execute() return correct variant v1`() {
//
//        whenever(ab.isExperimentEnabled("postlogin_android-all-expense_onboarding")).thenReturn(
//            Observable.just(true)
//        )
//
//        whenever(ab.getExperimentVariant("postlogin_android-all-expense_onboarding")).thenReturn(
//            Observable.just("v1")
//        )
//
//        val testObserver = expenseOnBoarding.execute(Unit).test()
//
//        testObserver.assertValues(
//            Result.Progress(),
//            Result.Success(
//                ExpenseManagerContract.OnBoardingVariant.v1
//            )
//        )
//
//        verify(ab).isExperimentEnabled("postlogin_android-all-expense_onboarding")
//        verify(ab).getExperimentVariant("postlogin_android-all-expense_onboarding")
//
//        testObserver.dispose()
//
//    }
//
//    @Test
//    fun `execute() return correct variant v2`() {
//        whenever(ab.isExperimentEnabled("postlogin_android-all-expense_onboarding")).thenReturn(
//            Observable.just(true)
//        )
//
//        whenever(ab.getExperimentVariant("postlogin_android-all-expense_onboarding")).thenReturn(
//            Observable.just("v2")
//        )
//
//        val testObserver = expenseOnBoarding.execute(Unit).test()
//
//        testObserver.assertValues(
//            Result.Progress(),
//            Result.Success(
//                ExpenseManagerContract.OnBoardingVariant.v2
//            )
//        )
//
//        verify(ab).isExperimentEnabled("postlogin_android-all-expense_onboarding")
//        verify(ab).getExperimentVariant("postlogin_android-all-expense_onboarding")
//
//        testObserver.dispose()
//
//    }
//
//    @Test
//    fun `execute() return correct variant v3`() {
//        whenever(ab.isExperimentEnabled("postlogin_android-all-expense_onboarding")).thenReturn(
//            Observable.just(true)
//        )
//
//        whenever(ab.getExperimentVariant("postlogin_android-all-expense_onboarding")).thenReturn(
//            Observable.just("v3")
//        )
//
//        val testObserver = expenseOnBoarding.execute(Unit).test()
//
//        testObserver.assertValues(
//            Result.Progress(),
//            Result.Success(
//                ExpenseManagerContract.OnBoardingVariant.v3
//            )
//        )
//
//        verify(ab).isExperimentEnabled("postlogin_android-all-expense_onboarding")
//        verify(ab).getExperimentVariant("postlogin_android-all-expense_onboarding")
//
//        testObserver.dispose()
//
//    }
//
//    @Test
//    fun `execute() return  variant v1 when isExperimentEnabled return false`() {
//        whenever(ab.isExperimentEnabled("postlogin_android-all-expense_onboarding")).thenReturn(
//            Observable.just(false)
//        )
//
//        whenever(ab.getExperimentVariant("postlogin_android-all-expense_onboarding")).thenReturn(
//            Observable.just("v1")
//        )
//
//        val testObserver = expenseOnBoarding.execute(Unit).test()
//
//        testObserver.assertValues(
//            Result.Progress(),
//            Result.Success(
//                ExpenseManagerContract.OnBoardingVariant.v1
//            )
//        )
//
//        verify(ab).isExperimentEnabled("postlogin_android-all-expense_onboarding")
//
//        testObserver.dispose()
//
//    }
//
//    @Test
//    fun `execute() return error`() {
//
//        val mockError = Exception()
//
//        whenever(ab.isExperimentEnabled("postlogin_android-all-expense_onboarding")).thenReturn(
//            Observable.error(mockError)
//        )
//
//        val testObserver = expenseOnBoarding.execute(Unit).test()
//
//        testObserver.assertValues(
//            Result.Progress(),
//            Result.Failure(
//                mockError
//            )
//        )
//
//        verify(ab).isExperimentEnabled("postlogin_android-all-expense_onboarding")
//
//        testObserver.dispose()
//
//    }
//
//    @Test
//    fun `execute() return error getExperimentVariant() return error `() {
//
//        val mockError = Exception()
//
//        whenever(ab.isExperimentEnabled("postlogin_android-all-expense_onboarding")).thenReturn(
//            Observable.just(true)
//        )
//
//        whenever(ab.getExperimentVariant("postlogin_android-all-expense_onboarding")).thenReturn(
//            Observable.error(mockError)
//        )
//
//        val testObserver = expenseOnBoarding.execute(Unit).test()
//
//        testObserver.assertValues(
//            Result.Progress(),
//            Result.Failure(
//                mockError
//            )
//        )
//
//        verify(ab).isExperimentEnabled("postlogin_android-all-expense_onboarding")
//        verify(ab).getExperimentVariant("postlogin_android-all-expense_onboarding")
//
//        testObserver.dispose()
//
//    }
}
