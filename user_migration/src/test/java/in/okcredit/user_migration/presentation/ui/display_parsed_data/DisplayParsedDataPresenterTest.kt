package `in`.okcredit.user_migration.presentation.ui.display_parsed_data

import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.user_migration.presentation.TestViewModel
import `in`.okcredit.user_migration.presentation.fakeCustomerUiTemplate
import `in`.okcredit.user_migration.presentation.testModels
import `in`.okcredit.user_migration.presentation.ui.display_parsed_data.DisplayParsedDataContract.*
import `in`.okcredit.user_migration.presentation.ui.display_parsed_data.usecase.CreateCustomerAndTransaction
import `in`.okcredit.user_migration.presentation.ui.display_parsed_data.usecase.GetControllerDataModels
import `in`.okcredit.user_migration.presentation.ui.display_parsed_data.usecase.GetPdfFilePath
import `in`.okcredit.user_migration.presentation.ui.display_parsed_data.usecase.UpdateParsedDataModels
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Assert
import org.junit.Test
import java.io.File

class DisplayParsedDataPresenterTest : TestViewModel<State, PartialState, ViewEvent>() {

    lateinit var viewModel: DisplayParsedDataViewModel
    private val initialState: State = State().copy(
        models = testModels
    )
    private val getPdfFilePath: GetPdfFilePath = mock()
    private val createCustomerAndTransaction: CreateCustomerAndTransaction = mock()
    private val displayParsedDataFragmentArgs: DisplayParsedDataFragmentArgs = mock()
    private val getFieldedDataModels: GetControllerDataModels = mock()
    private val getUpdatedParsedDataModels: UpdateParsedDataModels = mock()

    override fun createViewModel(): BaseViewModel<State, PartialState, ViewEvent> {
        return DisplayParsedDataViewModel(
            initialState,
            { getPdfFilePath },
            { createCustomerAndTransaction },
            { displayParsedDataFragmentArgs },
            { getFieldedDataModels },
            { getUpdatedParsedDataModels }
        )
    }

    @Test
    fun `EditDetails Intent should emit GoToEditDetailsDialog viewEvent`() {

        pushIntent(Intent.EditDetails(fakeCustomerUiTemplate))

        Assert.assertTrue(lastViewEvent() is ViewEvent.GoToEditDetailsDialog)

        // assert state should not be changed
        assertLastState(initialState)
    }

    @Test
    fun `OpenPdfFile Intent should emit OpenPdfFile viewEvent`() {
        val fakeFileName = "fake file name"
        val fakeFile = File(fakeFileName)
        val fakeResponse = GetPdfFilePath.Response(true, fakeFile)
        whenever(getPdfFilePath.execute(fakeFileName)).thenReturn(Single.just(fakeResponse))

        pushIntent(Intent.OpenPdfFile(fakeFileName))

        Assert.assertTrue(lastViewEvent() is ViewEvent.OpenPdfFile)

        // assert state should not be changed
        assertLastState(initialState)
    }

    //    @Test
//    fun `UpdateCustomer Intent Should Update the models`() {
//        val fakeUpdateEntries = CustomerUiTemplate(
//            index = 1,
//            isCheckedBoxChecked = true,
//            customerId = "1234",
//            phone = "7728398938",
//            name = "James",
//            amount = 2500,
//            type = 2,
//            error = false,
//        )
//        val fakeUpdatedControllerModels = testModels
//            .toMutableList()
//            .apply {
//                getOrNull(fakeUpdateEntries.index)
//                    ?.let { it as? ParsedDataModels.CustomerModel }
//                    ?.takeIf { it.customer.customerId == fakeUpdateEntries.customerId }
//                    ?.also {
//                        set(fakeUpdateEntries.index, ParsedDataModels.CustomerModel(fakeUpdateEntries))
//                    }
//
//            }
//
//        whenever(
//            getUpdatedParsedDataModels.execute(
//                fakeUpdateEntries,
//                testModels
//            )
//        ).thenReturn(Observable.just(fakeUpdatedControllerModels))
//
//        pushIntent(Intent.UpdateCustomer(fakeUpdateEntries), 33)
//
//        assertLastState(
//            initialState.copy(
//                createCustomerLoading = false, models = fakeUpdatedControllerModels
//            )
//        )
//    }
}
