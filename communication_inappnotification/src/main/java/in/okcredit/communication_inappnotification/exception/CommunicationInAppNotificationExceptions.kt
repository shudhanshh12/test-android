package `in`.okcredit.communication_inappnotification.exception

object TargetViewNotFoundException : Exception()

object ScreenNotResumedException : Exception()

class RendererNotFoundException(kind: String) : IllegalArgumentException(kind)
