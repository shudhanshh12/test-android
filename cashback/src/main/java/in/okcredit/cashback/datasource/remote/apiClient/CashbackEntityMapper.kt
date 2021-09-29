package `in`.okcredit.cashback.datasource.remote.apiClient

import `in`.okcredit.cashback.contract.model.CashbackMessageDetails
import com.google.common.base.Converter

object CashbackEntityMapper {
    val CASHBACK_MESSAGE_DETAILS_CONVERTER: Converter<CashbackMessageDetailsDto, CashbackMessageDetails> =
        object : Converter<CashbackMessageDetailsDto, CashbackMessageDetails>() {

            override fun doForward(cashbackMessageDetailsDto: CashbackMessageDetailsDto): CashbackMessageDetails {
                return CashbackMessageDetails(
                    isFirstTransaction = cashbackMessageDetailsDto.isFirstTransaction,
                    cashbackAmount = cashbackMessageDetailsDto.cashbackAmount,
                    minimumPaymentAmount = cashbackMessageDetailsDto.minimumPaymentAmount
                )
            }

            override fun doBackward(cashbackMessageDetails: CashbackMessageDetails): CashbackMessageDetailsDto {

                return CashbackMessageDetailsDto(
                    isFirstTransaction = cashbackMessageDetails.isFirstTransaction,
                    cashbackAmount = cashbackMessageDetails.cashbackAmount,
                    minimumPaymentAmount = cashbackMessageDetails.minimumPaymentAmount
                )
            }
        }
}
