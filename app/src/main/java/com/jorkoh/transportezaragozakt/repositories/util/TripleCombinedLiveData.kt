package com.jorkoh.transportezaragozakt.repositories.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer

class TripleCombinedLiveData<T, K, L, S>(
    source1: LiveData<T>,
    source2: LiveData<K>,
    source3: LiveData<L>,
    private val combine: (data1: T?, data2: K?, data3: L?) -> S
) : MediatorLiveData<S>() {

    private var data1: T? = null
    private var data2: K? = null
    private var data3: L? = null

    init {
        super.addSource(source1) {
            data1 = it
            update()
        }
        super.addSource(source2) {
            data2 = it
            update()
        }
        super.addSource(source3) {
            data3 = it
            update()
        }
    }

    private fun update() {
        if (data1 != null && data2 != null && data3 != null) {
            value = combine(data1, data2, data3)
        }
    }

    override fun <T : Any?> addSource(source: LiveData<T>, onChanged: Observer<in T>) {
        throw UnsupportedOperationException()
    }

    override fun <T : Any?> removeSource(toRemote: LiveData<T>) {
        throw UnsupportedOperationException()
    }
}