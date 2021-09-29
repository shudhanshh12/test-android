package merchant.android.okstream.contract

import java.lang.Exception

class OkStreamConnectionExistError : Exception("OkStream Already Connected")
class OkStreamNotConnectedError : Exception("OkStream Not Connected")
