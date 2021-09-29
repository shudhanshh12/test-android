package tech.okcredit.android.auth

import java.io.IOException

// errors
class Unauthorized : IOException("unauthorized")

class InvalidPassword : IllegalArgumentException("invalid_password")
class IncorrectPassword : IllegalArgumentException("incorrect_password")
class InvalidOtp : IllegalArgumentException("invalid_otp")
class ExpiredOtp : IllegalArgumentException("expired_otp")
class TooManyRequests : IllegalArgumentException("too_many_requests")
