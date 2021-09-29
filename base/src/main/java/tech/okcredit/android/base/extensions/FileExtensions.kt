package tech.okcredit.android.base.extensions

import java.io.File

fun File.makeIfNotExists() {
    if (!exists()) {
        mkdirs()
    }
}

fun File.deleteIfExists() {
    if (exists()) {
        delete()
    }
}

fun File.makeRecursively() {
    if (parentFile?.exists() == false) {
        parentFile?.mkdirs()
    }
    if (!exists()) {
        createNewFile()
    }
}
