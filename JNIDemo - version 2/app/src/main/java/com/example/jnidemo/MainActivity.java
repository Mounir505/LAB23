package com.example.jnidemo;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    static {
        try {
            System.loadLibrary("native-lib");
        } catch (UnsatisfiedLinkError e) {
            android.util.Log.e("JNI_JAVA", "Erreur critique de chargement .so", e);
        }
    }

    // --- DÉCLARATION NATIVE EXTENSION LAB 23 ---
    public native boolean isDebugDetected();

    // Méthodes natives du Lab 22 conservées
    public native String helloFromJNI();
    public native long factorial(int n);
    public native String reverseString(String s);
    public native int sumArray(int[] values);
    public native long intenseCalculationNative(int iterations);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Récupération des composants du nouveau bandeau de sécurité
        LinearLayout panelSecurity = findViewById(R.id.panelSecurity);
        TextView tvSecurityStatus = findViewById(R.id.tvSecurityStatus);

        // ÉVALUATION SÉCURITÉ DE LA COUCHE NATIVE
        boolean isCompromised = isDebugDetected();

        // Définition d'un flag d'exécution global basé sur la réponse native
        final boolean allowedToExecute = !isCompromised;

        if (isCompromised) {
            // Changement graphique en mode d'alerte (Rouge)
            panelSecurity.setBackgroundColor(Color.parseColor("#FFEBEE"));
            tvSecurityStatus.setText(getString(R.string.security_compromised));
            tvSecurityStatus.setTextColor(Color.parseColor("#C62828"));
        } else {
            // Confirmation du mode sécurisé (Vert)
            panelSecurity.setBackgroundColor(Color.parseColor("#E8F5E9"));
            tvSecurityStatus.setText(getString(R.string.security_safe));
            tvSecurityStatus.setTextColor(Color.parseColor("#2E7D32"));
        }

        // ====================================================
        // GESTION SECTION 1 : HELLO WORLD (ADAPTÉE)
        // ====================================================
        Button btnTriggerHello = findViewById(R.id.btnTriggerHello);
        TextView tvHelloResult = findViewById(R.id.tvHelloResult);

        btnTriggerHello.setOnClickListener(v -> {
            if (!allowedToExecute) {
                tvHelloResult.setText(getString(R.string.security_blocked_action));
                tvHelloResult.setTextColor(Color.RED);
                return;
            }
            tvHelloResult.setTextColor(Color.parseColor("#2E7D32"));
            tvHelloResult.setText(helloFromJNI());
        });

        // ====================================================
        // GESTION SECTION 2 : FACTORIEL DYNAMIQUE (ADAPTÉE)
        // ====================================================
        EditText etFactorielInput = findViewById(R.id.etFactorielInput);
        Button btnCalculerFact = findViewById(R.id.btnCalculerFact);
        TextView tvFactResult = findViewById(R.id.tvFactResult);

        btnCalculerFact.setOnClickListener(v -> {
            if (!allowedToExecute) {
                tvFactResult.setText(getString(R.string.security_blocked_action));
                return;
            }
            String txtInput = etFactorielInput.getText().toString().trim();
            if (TextUtils.isEmpty(txtInput)) {
                etFactorielInput.setError(getString(R.string.error_empty_field));
                return;
            }

            int nombreSaisi = Integer.parseInt(txtInput);
            long resultatNatif = factorial(nombreSaisi);

            if (resultatNatif == -1) {
                tvFactResult.setText(getString(R.string.factorial_error_negative));
            } else if (resultatNatif == -2) {
                tvFactResult.setText(getString(R.string.factorial_error_overflow));
            } else {
                tvFactResult.setText(getString(R.string.dynamic_factorial_success, nombreSaisi, resultatNatif));
            }
        });

        // ====================================================
        // GESTION SECTION 3 : INVERSION CHAINE DYNAMIQUE (ADAPTÉE)
        // ====================================================
        EditText etStringInput = findViewById(R.id.etStringInput);
        Button btnInverserString = findViewById(R.id.btnInverserString);
        TextView tvReverseResult = findViewById(R.id.tvReverseResult);

        btnInverserString.setOnClickListener(v -> {
            if (!allowedToExecute) {
                tvReverseResult.setText(getString(R.string.security_blocked_action));
                return;
            }
            String texteSaisi = etStringInput.getText().toString();
            if (TextUtils.isEmpty(texteSaisi)) {
                etStringInput.setError(getString(R.string.error_empty_field));
                return;
            }

            String texteInverse = reverseString(texteSaisi);
            tvReverseResult.setText(getString(R.string.dynamic_reverse_success, texteInverse));
        });

        // ====================================================
        // GESTION SECTION 4 : SOMME TABLEAU INT[] DYNAMIQUE (ADAPTÉE)
        // ====================================================
        EditText etArrayInput = findViewById(R.id.etArrayInput);
        Button btnCalculerSomme = findViewById(R.id.btnCalculerSomme);
        TextView tvArrayResult = findViewById(R.id.tvArrayResult);

        btnCalculerSomme.setOnClickListener(v -> {
            if (!allowedToExecute) {
                tvArrayResult.setText(getString(R.string.security_blocked_action));
                return;
            }
            String chaineNombres = etArrayInput.getText().toString().trim();
            if (TextUtils.isEmpty(chaineNombres)) {
                etArrayInput.setError(getString(R.string.error_empty_field));
                return;
            }

            try {
                String[] morceaux = chaineNombres.split(",");
                int[] tableauEntiers = new int[morceaux.length];

                for (int i = 0; i < morceaux.length; i++) {
                    tableauEntiers[i] = Integer.parseInt(morceaux[i].trim());
                }

                int sommeCalculee = sumArray(tableauEntiers);

                if (sommeCalculee == -1) {
                    tvArrayResult.setText(getString(R.string.array_error_null));
                } else if (sommeCalculee == -3) {
                    tvArrayResult.setText(getString(R.string.array_error_overflow));
                } else {
                    tvArrayResult.setText(getString(R.string.dynamic_array_success, tableauEntiers.length, sommeCalculee));
                }

            } catch (NumberFormatException e) {
                etArrayInput.setError(getString(R.string.error_invalid_array));
            }
        });

        // ====================================================
        // GESTION SECTION 5 : BENCHMARK (ADAPTÉE)
        // ====================================================
        Button btnRunBenchmark = findViewById(R.id.btnRunBenchmark);
        TextView tvBenchmarkResult = findViewById(R.id.tvBenchmarkResult);

        btnRunBenchmark.setOnClickListener(v -> {
            if (!allowedToExecute) {
                tvBenchmarkResult.setText(getString(R.string.security_blocked_action));
                return;
            }
            int cycles = 10_000_000;
            tvBenchmarkResult.setText(getString(R.string.benchmark_running));

            long startNative = System.nanoTime();
            intenseCalculationNative(cycles);
            long endNative = System.nanoTime();
            long durationNativeMs = (endNative - startNative) / 1_000_000;

            long startJava = System.nanoTime();
            intenseCalculationJava(cycles);
            long endJava = System.nanoTime();
            long durationJavaMs = (endJava - startJava) / 1_000_000;

            double ratio = (double) durationJavaMs / (durationNativeMs == 0 ? 1 : durationNativeMs);
            tvBenchmarkResult.setText(getString(R.string.benchmark_success, durationJavaMs, durationNativeMs, ratio));
        });
    }

    private long intenseCalculationJava(int iterations) {
        long count = 0;
        for (int i = 0; i < iterations; ++i) {
            count += (i % 3 == 0) ? (long) i * 2 : (long) i / 2;
        }
        return count;
    }
}