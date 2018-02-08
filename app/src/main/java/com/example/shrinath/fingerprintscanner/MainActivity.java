package com.example.shrinath.fingerprintscanner;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class MainActivity extends AppCompatActivity {

    TextView tvAuthLabel,tvLabel;
    ImageView ivFinger;
    FingerprintManager fingerprintManager;
    KeyguardManager keyguardManager;
    KeyStore keyStore;
    Cipher cipher;
    String KEY_NAME="Android Key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvAuthLabel=findViewById(R.id.tvAuth);
        tvLabel=findViewById(R.id.tvLabel);
        ivFinger=findViewById(R.id.ivFinger);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
        {
            fingerprintManager= (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);
            keyguardManager= (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
            if(!fingerprintManager.isHardwareDetected())
            {
                tvLabel.setText("Fingerprint Scanner Not present on your device");
            }
            else
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT )!= PackageManager.PERMISSION_GRANTED)
                {
                    tvLabel.setText("Permission not granted");
                }
                else
                    if(keyguardManager.isKeyguardSecure())
                    {
                        tvLabel.setText("Add lock to your phone");
                    }
                    else  if(!fingerprintManager.hasEnrolledFingerprints())
                    {
                        tvLabel.setText("Add a FingerPrint in your setting");
                    }
                    else {
                        tvLabel.setText("Place your finger to Access the App");
                        generateKey();

                        if(cipherInit())
                        {
                            FingerprintManager.CryptoObject cryptoObject=new FingerprintManager.CryptoObject(cipher);
                            FingerprintHandler fingerprintHandler=new FingerprintHandler(this);
                            fingerprintHandler.startAuthentication(fingerprintManager,cryptoObject);

                        }
            }

        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    private void generateKey() {

        try {

            keyStore = KeyStore.getInstance("AndroidKeyStore");
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

            keyStore.load(null);
            keyGenerator.init(new
                    KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                            KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            keyGenerator.generateKey();

        } catch (KeyStoreException | IOException | CertificateException
                | NoSuchAlgorithmException | InvalidAlgorithmParameterException
                | NoSuchProviderException e) {

            e.printStackTrace();

        }

    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean cipherInit() {
        try {
            cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }


        try {

            keyStore.load(null);

            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
                    null);

            cipher.init(Cipher.ENCRYPT_MODE, key);

            return true;

        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }

    }
}