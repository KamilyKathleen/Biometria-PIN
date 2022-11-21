package com.minhaempresa.biometriaepin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {

    Button btn_dig, btn_digPin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_dig = findViewById(R.id.btn_dig);
        btn_digPin = findViewById(R.id.btn_digPin);
        checkBiometricSupported();
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(MainActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(MainActivity.this, "Erro de autenticação: " + errString, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(MainActivity.this, "Autenticado com sucesso.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(MainActivity.this, "Falha na autenticação.", Toast.LENGTH_SHORT).show();
            }
        });
        // Apenas digital
        btn_dig.setOnClickListener(view -> {
            BiometricPrompt.PromptInfo.Builder promptInfo = dialogMetric();
            promptInfo.setNegativeButtonText("Cancelar");
            biometricPrompt.authenticate(promptInfo.build());
        });
        // Digital e PIN
        btn_digPin.setOnClickListener(view -> {
           BiometricPrompt.PromptInfo.Builder promptInfo = dialogMetric();
           promptInfo.setDeviceCredentialAllowed(true);
           biometricPrompt.authenticate(promptInfo.build());
        });
    }

    BiometricPrompt.PromptInfo.Builder dialogMetric() {
        return new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Login com a Biometria")
                .setSubtitle("Entre usando sua digital.");
    }

    private void checkBiometricSupported() {
        String info = "";
        BiometricManager manager = BiometricManager.from(this);
        switch (manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK
                | BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                info = "O app pode autenticar usando biometria.";
                enableButton(true);
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                info = "A biometria não está disponível neste dispositivo.";
                enableButton(false);
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                info = "A biometria não está disponível neste momento.";
                enableButton(false);
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                info = "É necessário registrar pelo menos uma digital.";
                enableButton(false, true);
                break;
            default:
                info = "Problema desconhecido.";
                break;
        }
        TextView txinfo = findViewById(R.id.tx_info);
        txinfo.setText(info);
    }

    void enableButton (boolean enable) {
        btn_dig.setEnabled(enable);
        btn_digPin.setEnabled(true);
    }

    void enableButton (boolean enable, boolean enroll) {
        enableButton(enable);
        if (!enroll) return;
        Intent enrollIntent = new Intent(Settings.ACTION_BIOMETRIC_ENROLL);
        enrollIntent.putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                BiometricManager.Authenticators.BIOMETRIC_STRONG
                        | BiometricManager.Authenticators.BIOMETRIC_WEAK);
        startActivity(enrollIntent);
    }
}