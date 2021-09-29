package tech.okcredit.camera_contract

import java.io.File
import java.io.Serializable

class CapturedImage(val file: File) : Serializable, MultiScreenItem {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CapturedImage) return false

        if (file != other.file) return false

        return true
    }

    override fun hashCode(): Int {
        return file.hashCode()
    }
}
