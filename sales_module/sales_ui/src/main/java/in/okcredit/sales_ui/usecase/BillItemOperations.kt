package `in`.okcredit.sales_ui.usecase

import `in`.okcredit.sales_sdk.models.BillModel
import `in`.okcredit.sales_ui.ui.add_bill_items.AddBillItemsContract.Intent
import `in`.okcredit.sales_ui.utils.SalesUtil
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import io.reactivex.Observable
import javax.inject.Inject

class BillItemOperations @Inject constructor() :
    UseCase<BillItemOperations.Request, BillItemOperations.Response> {
    data class Request(
        val intent: Intent,
        val inventory: List<BillModel.BillItem>,
        val addedItems: List<BillModel.BillItem>,
        val newItems: List<BillModel.BillItem> = listOf()
    )

    data class Response(
        val billedItems: BillModel.BilledItems,
        val addedItems: List<BillModel.BillItem>,
        val newItems: List<BillModel.BillItem>,
        val refresh: Boolean = false,
        val totalQuantity: Double = billedItems.items.sumByDouble { it.quantity }
    )

    override fun execute(req: Request): Observable<Result<Response>> {
        return UseCase.wrapObservable(
            when (req.intent) {
                is Intent.AddBillItemIntent -> addItem(req.intent.billItem, req)
                is Intent.NewBillItemIntent -> addNewItem(req.intent.billItem, req)
                is Intent.RemoveBillItemIntent -> removeItem(req.intent.billItem, req)
                is Intent.UpdateBillItemIntent -> updateItem(req.intent.billItem, req)
                else -> Observable.just(
                    Response(
                        generateBilledItems(req.addedItems),
                        req.addedItems,
                        req.newItems
                    )
                )
            }
        )
    }

    private fun generateBilledItems(addedItems: List<BillModel.BillItem>): BillModel.BilledItems {
        val billedItems = mutableListOf<BillModel.BilledItem>()
        var total = 0.0
        addedItems.forEach {
            billedItems.add(BillModel.BilledItem(it.id, it.quantity))
            total += (it.quantity * it.rate)
        }
        return BillModel.BilledItems(SalesUtil.displayDecimalNumber(total), billedItems)
    }

    private fun addItem(billItem: BillModel.BillItem, req: Request): Observable<Response> {
        val items = mutableListOf<BillModel.BillItem>()
        items.addAll(req.addedItems.filter { it.id != billItem.id })
        items.add(billItem)
        val newItems = generateNewItems(req, billItem.copy(quantity = 1.0))
        return Observable.just(
            Response(
                generateBilledItems(items),
                items,
                newItems
            )
        )
    }

    private fun addNewItem(billItem: BillModel.BillItem, req: Request): Observable<Response> {
        val items = mutableListOf<BillModel.BillItem>()
        items.addAll(req.addedItems.filter { it.id != billItem.id })
        items.add(billItem)
        val newItems = mutableListOf<BillModel.BillItem>()
        newItems.addAll(req.newItems.filter { it.id != billItem.id })
        newItems.add(billItem)
        return Observable.just(
            Response(
                generateBilledItems(items),
                items,
                newItems,
                true
            )
        )
    }

    private fun removeItem(billItem: BillModel.BillItem, req: Request): Observable<Response> {
        val items = mutableListOf<BillModel.BillItem>()
        items.addAll(req.addedItems.filter { it.id != billItem.id })
        val newItems = generateNewItems(req, billItem.copy(quantity = 0.0))
        return Observable.just(
            Response(
                generateBilledItems(items),
                items,
                newItems
            )
        )
    }

    private fun updateItem(billItem: BillModel.BillItem, req: Request): Observable<Response> {
        val items = mutableListOf<BillModel.BillItem>()
        var refresh = false
        req.inventory.filter { it.id == billItem.id }.map {
            refresh = (it.name != billItem.name || it.rate != billItem.rate)
        }
        items.addAll(req.addedItems.filter { it.id != billItem.id })
        items.add(billItem)
        val newItems = generateNewItems(req, billItem)
        return Observable.just(
            Response(
                generateBilledItems(items),
                items,
                newItems,
                refresh
            )
        )
    }

    private fun generateNewItems(req: Request, billItem: BillModel.BillItem): List<BillModel.BillItem> {
        val newItems = mutableListOf<BillModel.BillItem>()
        var i = -1
        req.newItems.forEachIndexed { index, it ->
            if (billItem.id == it.id) {
                i = index
            }
        }
        if (i != -1) {
            newItems.addAll(req.newItems.filter { it.id != billItem.id })
            newItems.add(i, billItem)
        } else {
            newItems.addAll(req.newItems)
        }
        return newItems
    }
}
