package com.example.fingerprintauth

import android.Manifest
import android.app.KeyguardManager
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.fingerprint.FingerprintManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import com.example.fingerprintauth.Helper.FingerprintHelper
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class MainActivity : AppCompatActivity() {

    lateinit var fm: FingerprintManager
    lateinit var km: KeyguardManager
    lateinit var keyStore: KeyStore
    lateinit var keyGen: KeyGenerator
    var KEY_NAME = "Gourav"
    lateinit var cipher: Cipher
    lateinit var cryptoObject: FingerprintManager.CryptoObject

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try {

            if (getSystemService(Context.KEYGUARD_SERVICE) != null)
                km = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

            if (getSystemService(Context.FINGERPRINT_SERVICE) != null)
                fm = getSystemService(Context.FINGERPRINT_SERVICE) as FingerprintManager
            else notifyUser("Fingerprint Hardware cannot be found")

            if (!km.isKeyguardSecure){
                notifyUser("Fingerprint security not enabled in Settings")
            }
            else{
                if (!fm.hasEnrolledFingerprints()){
                    notifyUser("Register at least 1 fingerprint in Settings")
                }
                else{
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) !=
                        PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.USE_FINGERPRINT), 111)
                    }
                    else{
                        validateFingerprint()
                    }
                }
            }

        } catch (e:Exception) {}



    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 111 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            validateFingerprint()
        }
    }

    private fun validateFingerprint() {

        try {

            keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")

            keyStore.load(null)
            keyGen.init(KeyGenParameterSpec.Builder(KEY_NAME, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build())
            keyGen.generateKey()

        } catch (e: Exception){
            Log.d("Hello1", e.message)
        }

        if (initCipher()) {
            cipher.let {
                cryptoObject = FingerprintManager.CryptoObject(it)
            }
        }

        var helper = FingerprintHelper(this)

        if (fm!=null && cryptoObject!=null) {
            helper.startAuth(fm, cryptoObject)
        }
    }

    private fun initCipher(): Boolean {

        try {
            cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC +
            "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7)
        } catch (e: Exception){
            Log.d("Hello2", e.message)
        }

        try {
            keyStore.load(null)
            val key = keyStore.getKey(KEY_NAME, null) as SecretKey
            cipher.init(Cipher.ENCRYPT_MODE, key)
            return true
        } catch (e: Exception) {
            Log.d("Hello3", e.message)
            return false
        }
    }

    private fun notifyUser(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}