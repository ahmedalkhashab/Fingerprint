package com.demo.fingerprint

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.demo.fingerprint.location.activity.LocationMainActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "BIO_MainActivity"
    }

    private val mFingerprintManager by lazy { FingerprintManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonAuth.setOnClickListener {
            //Start fingerprint authentication by checking availability.
            mFingerprintManager.auth(mAuthCallback)
        }
    }

    private val mAvailabilityCallback = object : ResultListener {

        override fun onSuccess() {
            Log.d(TAG, "mAvailabilityCallback --> onSuccess")
            mFingerprintManager.auth(mAuthCallback)
        }

        override fun onFailure(errorMessage: String?) {
            Log.e(TAG, "mAvailabilityCallback --> onFailure($errorMessage)")
            Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_SHORT).show()
        }

    }

    private val mAuthCallback = object : ResultListener {

        override fun onSuccess() {
            Log.d(TAG, "mAuthCallback --> onSuccess")
            val intent = Intent(applicationContext, LocationMainActivity::class.java)
            startActivity(intent)
        }

        override fun onFailure(errorMessage: String?) {
            Log.e(TAG, "mAuthCallback --> onFailure($errorMessage)")
            Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_SHORT).show()
        }

    }

}