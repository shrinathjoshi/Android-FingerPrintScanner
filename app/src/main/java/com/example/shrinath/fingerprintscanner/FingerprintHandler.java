package com.example.shrinath.fingerprintscanner;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Shrinath on 18-01-2018.
 */

@TargetApi(Build.VERSION_CODES.M)
public class FingerprintHandler extends FingerprintManager.AuthenticationCallback {

    private Context context;
    public FingerprintHandler(Context context)
    {
        this.context=context;
    }

    public void startAuthentication(FingerprintManager fingerprintManager,FingerprintManager.CryptoObject cryptoObject)
    {
        CancellationSignal cancellationSignal=new CancellationSignal();
       fingerprintManager.authenticate(cryptoObject,cancellationSignal,0,this,null);
    }

    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {

        this.update("There was an Auth Error"+errString,false);
    }

    private void update(String s, boolean b) {

        TextView tvLabel=((Activity)context).findViewById(R.id.tvLabel);
        ImageView imageView=((Activity)context).findViewById(R.id.ivFinger);

        tvLabel.setText(s);
        if(b==false)
        {
            tvLabel.setTextColor(ContextCompat.getColor(context,R.color.colorAccent));
        }else
        {
            tvLabel.setTextColor(ContextCompat.getColor(context,R.color.colorPrimary));
            imageView.setImageResource(R.mipmap.done);
        }

    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {

        this.update("You can now acesss the app",true);
    }

    @Override
    public void onAuthenticationFailed() {
        this.update("Auth failed" ,false);
    }

    @Override
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {

        this.update("Error: Make sure your finger covers the whole Scanner ",false);
    }
}
