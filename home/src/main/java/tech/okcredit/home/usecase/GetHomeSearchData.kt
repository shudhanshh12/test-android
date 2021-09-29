package tech.okcredit.home.usecase

import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.Supplier
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import com.google.firebase.perf.FirebasePerformance
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.base.extensions.isNotNullOrBlank
import tech.okcredit.contacts.contract.ContactsRepository
import tech.okcredit.contacts.contract.model.Contact
import tech.okcredit.home.ui.analytics.HomeTraces
import tech.okcredit.home.utils.SORT_CONVERTER
import tech.okcredit.home.utils.SearchAndSortUtils
import tech.okcredit.home.utils.containNameOrMobile
import javax.inject.Inject

class GetHomeSearchData @Inject constructor(
    private val customerRepo: dagger.Lazy<CustomerRepo>,
    private val supplierCreditRepository: dagger.Lazy<SupplierCreditRepository>,
    private val contactsRepository: dagger.Lazy<ContactsRepository>,
    private val collectionRepository: dagger.Lazy<CollectionRepository>,
    private val firebasePerformance: dagger.Lazy<FirebasePerformance>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    private fun getObservables(businessId: String) = listOf(
        customerRepo.get().listActiveCustomers(businessId),
        supplierCreditRepository.get().listActiveSuppliers(businessId),
        contactsRepository.get().getContacts(),
        collectionRepository.get().listCustomerQrIntents(businessId),
        collectionRepository.get().isCollectionActivated()
    )

    fun execute(request: Request): Observable<Response> {
        val useCaseTracer = firebasePerformance.get().newTrace(HomeTraces.HomeSearchUseCase)
        useCaseTracer.start()
        var isInitialTraceTracked = false

        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            Observable.combineLatest(getObservables(businessId)) { response ->
                val customers = response[0] as List<Customer>
                val suppliers = response[1] as List<Supplier>
                val contacts = response[2] as List<Contact>
                val customerQrIntents = response[3] as Map<String, String?>
                val collectionActivated = response[4] as Boolean

                val tracer = firebasePerformance.get().newTrace(HomeTraces.HomeSearchFiltering)

                var stepStartTime = System.currentTimeMillis()
                tracer.start()

                val sortedCustomerList =
                    SearchAndSortUtils.sortCustomer(request.customerSortFilters, request.customerSortBy, customers)

                val includedContacts = mutableSetOf<String>()
                val finalCustomerList = sortedCustomerList.filter { customer ->
                    request.searchQuery.containNameOrMobile(customer.description, customer.mobile)
                }.map { customer ->
                    if (customer.mobile.isNotNullOrBlank()) includedContacts.add(customer.mobile!!)

                    CustomerWithQrIntent(
                        customer = customer,
                        qrIntent = if (collectionActivated) customerQrIntents[customer.id] else null
                    )
                }

                tracer.putAttribute("CustomerCount", finalCustomerList.size.toString())
                tracer.putMetric("CustomerFilterDuration", stepStartTime - System.currentTimeMillis())
                stepStartTime = System.currentTimeMillis()

                SearchAndSortUtils.sortSuppliers(
                    SORT_CONVERTER.convert(request.supplierSortBy)!!,
                    suppliers.toMutableList()
                )

                val finalSupplierList = suppliers.filter { supplier ->
                    request.searchQuery.containNameOrMobile(supplier.name, supplier.mobile)
                }

                finalSupplierList.forEach { supplier ->
                    if (supplier.mobile.isNotNullOrBlank()) includedContacts.add(supplier.mobile!!)
                }

                tracer.putAttribute("SupplierCount", finalSupplierList.size.toString())
                tracer.putMetric("SupplierFilterDuration", stepStartTime - System.currentTimeMillis())
                stepStartTime = System.currentTimeMillis()

                val finalContactList = contacts.filter { contact ->
                    request.searchQuery.containNameOrMobile(contact.name, contact.mobile) && !includedContacts.contains(
                        contact.mobile
                    )
                }

                tracer.putAttribute("ContactCount", finalContactList.size.toString())
                tracer.putMetric("ContactFilterDuration", stepStartTime - System.currentTimeMillis())
                tracer.stop()

                return@combineLatest Response(
                    finalCustomerList,
                    finalSupplierList,
                    finalContactList,
                    request.searchQuery
                )
            }.doOnNext {
                if (isInitialTraceTracked.not()) {
                    useCaseTracer.putAttribute("ContactCount", it.contacts.size.toString())
                    useCaseTracer.putAttribute("CustomerCount", it.customers.size.toString())
                    useCaseTracer.putAttribute("SupplierCount", it.suppliers.size.toString())

                    isInitialTraceTracked = true
                    useCaseTracer.stop()
                }
            }
        }
    }

    data class Response(
        val customers: List<CustomerWithQrIntent>,
        val suppliers: List<Supplier>,
        val contacts: List<Contact>,
        val searchQuery: String,
    )

    data class CustomerWithQrIntent(
        val customer: Customer,
        val qrIntent: String?,
    )

    data class Request(
        val customerSortFilters: List<String>,
        val customerSortBy: String,
        val supplierSortBy: String,
        val searchQuery: String,
    )
}
