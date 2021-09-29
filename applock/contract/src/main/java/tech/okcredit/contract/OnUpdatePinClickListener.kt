package tech.okcredit.contract

interface OnUpdatePinClickListener {
    fun onSetNewPinClicked(requestCode: Int)
    fun onUpdateDialogDismissed()
}
