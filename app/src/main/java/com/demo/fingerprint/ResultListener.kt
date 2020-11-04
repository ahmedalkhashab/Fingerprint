package com.demo.fingerprint

interface ResultListener {
    fun onSuccess()
    fun onFailure(errorMessage: String? = null)
}