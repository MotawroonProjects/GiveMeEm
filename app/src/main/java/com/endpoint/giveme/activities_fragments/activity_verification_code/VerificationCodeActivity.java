package com.endpoint.giveme.activities_fragments.activity_verification_code;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Toast;

import com.endpoint.giveme.R;
import com.endpoint.giveme.activities_fragments.activity_confirm_code_success.ConfirmCodeSuccessActivity;
import com.endpoint.giveme.databinding.ActivityVerificationCodeBinding;
import com.endpoint.giveme.language.Language;
import com.endpoint.giveme.share.Common;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.paperdb.Paper;

public class VerificationCodeActivity extends AppCompatActivity {
    private ActivityVerificationCodeBinding binding;
    private String lang;
    private String phone_code = "";
    private String phone = "";
    private String country_id = "";
    private boolean canSend = false;
    private CountDownTimer countDownTimer;
    private FirebaseAuth mAuth;
    private String verificationId;
    private String smsCode = "";
    private boolean fromSplash = true;


    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(Language.updateResources(newBase, Paper.book().read("lang", "ar")));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_verification_code);
        getDataFromIntent();
        initView();

    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            phone_code = intent.getStringExtra("phone_code");
            phone = intent.getStringExtra("phone");
            country_id = intent.getStringExtra("country_id");
            fromSplash = intent.getBooleanExtra("from",true);
        }
    }

    private void initView() {
        mAuth = FirebaseAuth.getInstance();
        Paper.init(this);
        lang = Paper.book().read("lang", "ar");
        String mPhone = phone_code + phone;
        binding.setPhone(mPhone);
        binding.btnResendCode.setOnClickListener(v -> {
            if (canSend) {
                resendCode();
            }
        });

        binding.btnConfirm.setOnClickListener(v -> {
            String sms = binding.edtCode.getText().toString().trim();
            if (!sms.isEmpty()) {
                checkValidCode(sms);
            } else {
                binding.edtCode.setError(getString(R.string.inv_code));
            }
        });
        sendSmsCode();
    }


    private void sendSmsCode() {
        startCounter();
        mAuth.setLanguageCode(lang);
        PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                smsCode = phoneAuthCredential.getSmsCode();
                binding.edtCode.setText(smsCode);
                checkValidCode(smsCode);
            }

            @Override
            public void onCodeSent(@NonNull String verification_id, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(verification_id, forceResendingToken);
                VerificationCodeActivity.this.verificationId = verification_id;
                Log.e("verification_id", verification_id);
            }


            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {

                if (e.getMessage() != null) {
                    Common.CreateDialogAlert(VerificationCodeActivity.this, e.getMessage());
                } else {
                    Common.CreateDialogAlert(VerificationCodeActivity.this, getString(R.string.failed));

                }
            }
        };
        PhoneAuthProvider.getInstance()
                .verifyPhoneNumber(
                        phone_code + phone,
                        120,
                        TimeUnit.SECONDS,
                        this,
                        mCallBack

                );
    }


    private void checkValidCode(String code) {

        if (verificationId != null) {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
            mAuth.signInWithCredential(credential)
                    .addOnSuccessListener(authResult -> {
                        navigateToActivityConfirmSuccess();
                    }).addOnFailureListener(e -> {
                if (e.getMessage() != null) {
                    try {
                        Common.CreateDialogAlert(this, e.getMessage());

                    }catch (Exception ex)
                    {

                    }
                } else {
                    Toast.makeText(this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    private void navigateToActivityConfirmSuccess() {
        Intent intent = new Intent(this, ConfirmCodeSuccessActivity.class);
        intent.putExtra("phone_code", phone_code);
        intent.putExtra("phone", phone);
        intent.putExtra("country_id", country_id);
        intent.putExtra("from",fromSplash);

        startActivity(intent);
        finish();

    }


    private void startCounter() {
        countDownTimer = new CountDownTimer(120000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                int minutes = (int) ((millisUntilFinished / 1000) / 60);
                int seconds = (int) ((millisUntilFinished / 1000) % 60);

                String time = String.format(Locale.ENGLISH, "%02d:%02d", minutes, seconds);
                binding.btnResendCode.setText(String.format(Locale.ENGLISH, "%s %s", getString(R.string.resend_in), time));
                binding.btnResendCode.setTextColor(ContextCompat.getColor(VerificationCodeActivity.this, R.color.color4));
                binding.btnResendCode.setBackgroundResource(R.color.transparent);

            }

            @Override
            public void onFinish() {
                canSend = true;
                binding.btnResendCode.setText(R.string.resend);
                binding.btnResendCode.setTextColor(ContextCompat.getColor(VerificationCodeActivity.this, R.color.colorPrimary));
                binding.btnResendCode.setBackgroundResource(R.color.white);


            }
        };

        countDownTimer.start();
    }

    private void resendCode() {
        if (countDownTimer != null) {
            countDownTimer.start();
        }
        sendSmsCode();
    }

    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
    }
}