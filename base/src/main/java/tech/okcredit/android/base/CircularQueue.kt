package tech.okcredit.android.base

import java.util.*

class CircularQueue<E>(capacity: Int) : LinkedList<E>() {
    private var capacity = 5
    override fun add(element: E): Boolean {
        if (size >= capacity) removeFirst()
        return super.add(element)
    }

    init {
        this.capacity = capacity
    }
}
