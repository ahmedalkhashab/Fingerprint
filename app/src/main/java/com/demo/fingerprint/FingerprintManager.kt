package com.demo.fingerprint

import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.huawei.hms.support.api.fido.bioauthn.BioAuthnCallback
import com.huawei.hms.support.api.fido.bioauthn.BioAuthnManager
import com.huawei.hms.support.api.fido.bioauthn.BioAuthnManager.*
import com.huawei.hms.support.api.fido.bioauthn.BioAuthnPrompt
import com.huawei.hms.support.api.fido.bioauthn.BioAuthnResult
import java.util.concurrent.Executors

class FingerprintManager(private val activity: FragmentActivity) {

    companion object {
        private const val TAG = "BIO_FingerprintManager"
    }

    private val mBioAuthnManager = BioAuthnManager(activity)

    private val mBioAuthnPrompt by lazy {
        BioAuthnPrompt(activity, Executors.newSingleThreadExecutor(), mBioAuthnCallback)
    }

    private var mResultListener: ResultListener? = null

    /**
     *  Checks if device has available fingerprint sensor.
     *
     *  @return either one of BIO_AUTHN_SUCCESS, BIO_AUTHN_ERROR_HW_UNAVAILABLE,
     *  BIO_AUTHN_ERROR_NONE_ENROLLED, BIO_AUTHN_ERROR_NO_HARDWARE or
     *  BIO_AUTHN_ERROR_UNSUPPORTED_OS_VER
     */
    fun isFingerprintAuthAvailable(resultListener: ResultListener) {

        when (val result = mBioAuthnManager.canAuth()) {
            BIO_AUTHN_SUCCESS -> {
                Log.d(TAG, "isFingerprintAuthAvailable --> BIO_AUTHN_SUCCESS")
                activity.runOnUiThread {
                    resultListener.onSuccess()
                }
            }
            BIO_AUTHN_ERROR_HW_UNAVAILABLE -> {
                Log.d(TAG, "isFingerprintAuthAvailable --> BIO_AUTHN_ERROR_HW_UNAVAILABLE")
                activity.runOnUiThread {
                    resultListener.onFailure(activity.getString(R.string.bio_autn_error_hw_unavailable))
                }
            }
            BIO_AUTHN_ERROR_NONE_ENROLLED -> {
                Log.d(TAG, "isFingerprintAuthAvailable --> BIO_AUTHN_ERROR_NONE_ENROLLED")
                activity.runOnUiThread {
                    resultListener.onFailure(activity.getString(R.string.bio_autn_error_none_enrolled))
                }
            }
            BIO_AUTHN_ERROR_NO_HARDWARE -> {
                Log.d(TAG, "isFingerprintAuthAvailable --> BIO_AUTHN_ERROR_NO_HARDWARE")
                activity.runOnUiThread {
                    resultListener.onFailure(activity.getString(R.string.bio_autn_error_no_hardware))
                }
            }
            BIO_AUTHN_ERROR_UNSUPPORTED_OS_VER -> {
                Log.d(TAG, "isFingerprintAuthAvailable --> BIO_AUTHN_ERROR_UNSUPPORTED_OS_VER")
                activity.runOnUiThread {
                    resultListener.onFailure(activity.getString(R.string.bio_autn_error_unsupported_os_ver))
                }
            }
            else -> {
                Log.d(TAG, "isFingerprintAuthAvailable --> Unknown Error: $result")
                activity.runOnUiThread {
                    resultListener.onFailure(activity.getString(R.string.unknown_error))
                }
            }
        }
    }

    private val mBioAuthnCallback = object : BioAuthnCallback() {

        /**
         *  Called to display help information during 3D facial authentication.
         */
        override fun onAuthHelp(helpCode: Int, helpMessage: CharSequence) {
            super.onAuthHelp(helpCode, helpMessage)
            Log.d(TAG, "onAuthHelp --> Help Code: $helpCode, Help Message: $helpMessage")
            activity.runOnUiThread {
                mResultListener?.onFailure(activity.getString(R.string.help_tip, helpMessage))
            }
        }

        /**
         *  Called to indicate authentication failure when fingerprint or 3D facial authentication
         *  of FIDO BioAuthn is complete but an unrecoverable error occurs. When this method is
         *  called, no further operation is performed.
         */
        override fun onAuthError(errorMessageId: Int, errorMessage: CharSequence) {
            super.onAuthError(errorMessageId, errorMessage)
            Log.e(TAG, "onAuthError --> Error Code: $errorMessageId, Error Message: $errorMessage")
            activity.runOnUiThread {
                mResultListener?.onFailure(activity.getString(R.string.fingerprint_auth_error, errorMessage))
            }
        }

        /**
         *  Called when fingerprint or 3D facial authentication fails.
         */
        override fun onAuthFailed() {
            super.onAuthFailed()
            Log.d(TAG, "onAuthFailed")
            activity.runOnUiThread { mResultListener?.onFailure(activity.getString(R.string.fingerprint_auth_failed)) }
        }

        /**
         *  Called when fingerprint or 3D facial authentication is successful.
         */
        override fun onAuthSucceeded(result: BioAuthnResult) {
            super.onAuthSucceeded(result)
            Log.d(TAG, "onAuthSucceeded --> Result: $result")
            activity.runOnUiThread {
                mResultListener?.onSuccess()
            }

            /**
             *  In EMUI 9.x or earlier, fingerprint authentication may work once only.
             *  To solve this problem we recreate the UI after authentication is completed.
             */
            activity.recreate()
        }
    }

    /**
     *  Provides BioAuthn.PromptInfo to display to users when applying fingerprint authentication.
     *
     *  isDeviceCredentialAllowed() is for checking whether a user can choose the PIN, pattern,
     *  or password of their device for authentication.
     *
     *  setDeviceCredentialAllowed(true) allows change from fingerprint authentication to
     *  another authentication mode, for example, lock screen password authentication.
     */
    private fun getPromptInfo(): BioAuthnPrompt.PromptInfo {
        return BioAuthnPrompt.PromptInfo.Builder().let {
            it.setTitle(activity.getString(R.string.fingerprint_prompt_title))
            it.setSubtitle(activity.getString(R.string.fingerprint_prompt_subtitle))
            it.setDescription(activity.getString(R.string.fingerprint_prompt_description))
            it.setNegativeButtonText(activity.getString(R.string.dismiss))
            it.setDeviceCredentialAllowed(false)
            it.build()
        }
    }

    //Perform fingerprint authentication
    fun auth(resultListener: ResultListener) {
        mResultListener = resultListener
        mBioAuthnPrompt.auth(getPromptInfo())
    }
}