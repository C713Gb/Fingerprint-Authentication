package com.example.fingerprintauth.Helper

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.fingerprint.FingerprintManager
import android.os.CancellationSignal
import android.os.Message
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.fingerprintauth.HomeActivity

class FingerprintHelper(private val context: Context) : FingerprintManager.AuthenticationCallback() {

    lateinit var cancellationSignal: CancellationSignal

    fun startAuth(manager:FingerprintManager, cryptoObject: FingerprintManager.CryptoObject) {

        cancellationSignal = CancellationSignal()

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        manager.authenticate(cryptoObject, cancellationSignal, 0, this, null)

    }

    override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
        super.onAuthenticationError(errorCode, errString)
        notifyUser("Authentication error : $errString")
    }

    override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence?) {
        super.onAuthenticationHelp(helpCode, helpString)
        notifyUser("Authentication help : $helpString")
    }

    override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult?) {
        super.onAuthenticationSucceeded(result)
        notifyUser("Authentication successful!")
        context.startActivity(Intent(context, HomeActivity::class.java))
    }

    override fun onAuthenticationFailed() {
        super.onAuthenticationFailed()
        notifyUser("Authentication failed!")
    }

    private fun notifyUser(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}