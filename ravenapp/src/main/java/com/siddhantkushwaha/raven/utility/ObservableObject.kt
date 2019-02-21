package com.siddhantkushwaha.raven.utility

class ObservableObject<T>(private var data: T, private val onDataChanged: OnDataChanged<T>) {


    fun setData(data: T) {
        this.data = data
        onDataChanged.onChange(data)
    }

    fun getData(): T {
        return data
    }

    interface OnDataChanged<T> {

        fun onChange(data: T)
    }
}