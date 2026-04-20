package com.example.delivgo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.delivgo.api.ApiService;
import com.example.delivgo.api.RetrofitHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.Map;

public class ControleurActivity extends AppCompatActivity {

    TextView textTotal, textLivrees, textAnnulees, textRestantes;
    int idControleur;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controleur);

        // Initialiser les vues
        textTotal     = findViewById(R.id.textView7);   // Total
        textLivrees   = findViewById(R.id.textView9);   // Livrées
        textAnnulees  = findViewById(R.id.textView11);  // Annulées
        textRestantes = findViewById(R.id.textView13);  // Restantes

        idControleur = getIntent().getIntExtra("idControleur", 2);

        // Bouton Messages
        findViewById(R.id.imageView18).setOnClickListener(v -> {
            Intent intent = new Intent(ControleurActivity.this, MessagerieActivity.class);
            intent.putExtra("idUser", idControleur);
            intent.putExtra("idInterlocuteur", 3); // id du livreur
            startActivity(intent);
        });

        chargerStats();
    }

    private void chargerStats() {
        ApiService api = RetrofitHelper.getService();
        Call<Map<String, Object>> call = api.getStatsControleur();

        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> stats = response.body();
                    textTotal.setText(String.valueOf(((Double) stats.get("total")).intValue()));
                    textLivrees.setText(String.valueOf(((Double) stats.get("livrees")).intValue()));
                    textAnnulees.setText(String.valueOf(((Double) stats.get("annulees")).intValue()));
                    textRestantes.setText(String.valueOf(((Double) stats.get("restantes")).intValue()));
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(ControleurActivity.this, "Erreur : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}