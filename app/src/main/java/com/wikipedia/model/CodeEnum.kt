package com.wikipedia.model

interface CodeEnum<T> {
    fun enumeration(code: Int): T
}
