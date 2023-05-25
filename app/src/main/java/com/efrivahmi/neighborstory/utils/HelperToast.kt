package com.efrivahmi.neighborstory.utils

open class HelperToast <out T>(private val content: T) {

    @Suppress("MemberVisibilityCanBePrivate")
    var beenDealtWith = false
        private set

    fun getContentIfNotHandled(): T? {
        return if (beenDealtWith) {
            null
        } else {
            beenDealtWith = true
            content
        }
    }
}