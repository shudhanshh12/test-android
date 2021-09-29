package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.database.DueInfoRepo
import `in`.okcredit.backend._offline.usecase.UpdateCustomer
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.merchant.customer_ui.utils.calender.MonthView
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import io.reactivex.Observable
import org.joda.time.DateTime
import javax.inject.Inject

class UpdateDueInfo @Inject constructor(
    private val dueInfoRepo: DueInfoRepo,
    private val updateCustomer: UpdateCustomer,
) : UseCase<UpdateDueInfo.Request, Unit> {

    override fun execute(req: Request): Observable<Result<Unit>> {

        val activeDate = DateTime(req.pair.first.okcDate.timeInMillis)
        return UseCase.wrapCompletable(
            (
                when (req.pair.first.dateStatus!!) {
                    MonthView.CapturedDate.DateStatus.ADDED -> {
                        val customer = req.pair.second
                        dueInfoRepo.updateCustomDueDateSet(true, customer.id, true, activeDate)
                            .andThen(
                                updateCustomer.execute(
                                    customer.id,
                                    customer.description,
                                    customer.address,
                                    customer.profileImage,
                                    customer.mobile,
                                    customer.lang,
                                    customer.reminderMode,
                                    customer.isTxnAlertEnabled(),
                                    false,
                                    false,
                                    activeDate,
                                    true,
                                    false,
                                    customer.isAddTransactionPermissionDenied(),
                                    false,
                                    customer.state,
                                    false
                                )
                            )
                    }

                    MonthView.CapturedDate.DateStatus.DELETED -> {
                        val customer = req.pair.second
                        dueInfoRepo.updateCustomDueDateSet(
                            false,
                            customer.id,
                            false,
                            DateTime(req.pair.first.okcDate.timeInMillis)
                        ).andThen(
                            updateCustomer.execute(
                                customer.id,
                                customer.description,
                                customer.address,
                                customer.profileImage,
                                customer.mobile,
                                customer.lang,
                                customer.reminderMode,
                                customer.isTxnAlertEnabled(),
                                false,
                                false,
                                activeDate,
                                false,
                                true,
                                customer.isAddTransactionPermissionDenied(),
                                false,
                                customer.state,
                                false
                            )
                        )
                    }
                }
                )
        )
    }

    class Request(val pair: Pair<MonthView.CapturedDate, Customer>)
}
