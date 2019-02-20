package com.siddhantkushwaha.raven.utility

class ObservableHashMap<T1, T2>(private val onDataChanged: OnDataChanged<T1, T2>) {

    private val data: HashMap<T1, T2> = HashMap()

    fun getData(): HashMap<T1, T2> {
        return data
    }

    fun add(key: T1, value: T2) {
        data[key] = value
        onDataChanged.onDataAdded(key, value)
        onDataChanged.onDataChanged(data)
    }

    fun remove(key: T1) {
        data.remove(key)
        onDataChanged.onDataRemoved(key)
        onDataChanged.onDataChanged(data)
    }

    interface OnDataChanged<T1, T2> {
        fun onDataAdded(key: T1, value: T2)
        fun onDataRemoved(key: T1)
        fun onDataChanged(data: HashMap<T1, T2>)
    }
}