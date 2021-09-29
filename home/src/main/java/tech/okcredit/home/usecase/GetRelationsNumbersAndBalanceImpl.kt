package tech.okcredit.home.usecase

import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.customer.contract.RelationshipType
import `in`.okcredit.home.IGetRelationsNumbersAndBalance
import `in`.okcredit.home.MobileNumberAndBalance
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Single
import timber.log.Timber
import javax.inject.Inject

/**
 * This class is used to fetch the contacts that are already added to okCredit (as customers & suppliers)
 *
 * so, that when we populate local contacts (from users device) , we can remove this existing contacts
 *
 * This time user sees only those local contacts which are not already added
 *
 * This prevents users from adding the Redundant Contact & avoid Cyclic Account error
 *
 * Note: * Suppliers -> is only fetched when ab is enabled for the merchant(this device user)
 **/
class GetRelationsNumbersAndBalanceImpl @Inject constructor(
    private val customerRepo: Lazy<CustomerRepo>,
    private val supplierCreditRepository: Lazy<SupplierCreditRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : IGetRelationsNumbersAndBalance {

    override fun execute(): Observable<List<MobileNumberAndBalance>> {
        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            Single.zip(
                supplierCreditRepository.get().listActiveSuppliers(businessId).firstOrError(),
                customerRepo.get().listActiveCustomers(businessId).firstOrError(),
                { suppliers, customers ->
                    val mobiles = mutableListOf<MobileNumberAndBalance>()

                    customers.map {
                        if (!it.mobile.isNullOrEmpty()) {
                            mobiles.add(
                                MobileNumberAndBalance(
                                    it.mobile!!,
                                    it.balanceV2,
                                    it.id,
                                    RelationshipType.ADD_CUSTOMER
                                )
                            )
                        }
                    }

                    if (suppliers.isNotEmpty()) {
                        suppliers.map {
                            if (!it.mobile.isNullOrEmpty()) {
                                mobiles.add(
                                    MobileNumberAndBalance(
                                        it.mobile!!,
                                        it.balance,
                                        it.id,
                                        RelationshipType.ADD_SUPPLIER
                                    )
                                )
                            }
                        }
                    }
                    Timber.i("GetRelationsNumbers suppliers=${suppliers.size} customers=${customers.size} mobiles=${mobiles.size}")
                    mobiles
                }
            ).toObservable()
        }
    }
}
