package com.example.delivgo;

import android.content.Intent;
import android.net.Uri;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;
import java.util.Map;
import com.example.delivgo.api.ApiService;
import com.example.delivgo.api.RetrofitHelper;
import com.example.delivgo.model.Livraison;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverActivity extends AppCompatActivity {

    LinearLayout containerLivraisons;
    TextView textNomLivreur, textTotal, textLivrees, textRestantes;
    int idLivreur;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);

        containerLivraisons = findViewById(R.id.containerLivraisons);
        textNomLivreur = findViewById(R.id.textNomLivreur);
        textTotal = findViewById(R.id.textTotal);
        textLivrees = findViewById(R.id.textLivrees);
        textRestantes = findViewById(R.id.textRestantes);

        // idLivreur EN PREMIER
        idLivreur = getIntent().getIntExtra("idLivreur", 1);

        // Bouton Messages
        findViewById(R.id.btnMessages).setOnClickListener(v -> {
            Intent intent = new Intent(DriverActivity.this, MessagerieActivity.class);
            intent.putExtra("idUser", idLivreur);
            intent.putExtra("idInterlocuteur", 2);
            startActivity(intent);
        });

        chargerStats();
        chargerLivraisons();
    }

    private void chargerStats() {
        ApiService api = RetrofitHelper.getService();
        Call<Map<String, Object>> call = api.getStats(idLivreur);

        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> stats = response.body();
                    textNomLivreur.setText("Bonjour, " + stats.get("nomLivreur") + " ! 👋");
                    textTotal.setText(((Double) stats.get("total")).intValue() + " Total");
                    textLivrees.setText(((Double) stats.get("livrees")).intValue() + " Livrées");
                    textRestantes.setText(((Double) stats.get("restantes")).intValue() + " Restantes");
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(DriverActivity.this, "Erreur stats : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void chargerLivraisons() {
        ApiService api = RetrofitHelper.getService();
        Call<List<Livraison>> call = api.getLivraisons(idLivreur);

        call.enqueue(new Callback<List<Livraison>>() {
            @Override
            public void onResponse(Call<List<Livraison>> call, Response<List<Livraison>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    afficherLivraisons(response.body());
                } else {
                    Toast.makeText(DriverActivity.this, "Aucune livraison trouvée", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Livraison>> call, Throwable t) {
                Toast.makeText(DriverActivity.this, "Erreur : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            chargerStats();
            chargerLivraisons();
        }
    }

    private void afficherLivraisons(List<Livraison> livraisons) {
        containerLivraisons.removeAllViews();
        int numero = 1;

        for (Livraison livraison : livraisons) {
            View itemView = getLayoutInflater().inflate(R.layout.item_livraison, containerLivraisons, false);

            ((TextView) itemView.findViewById(R.id.textNumero)).setText(String.valueOf(numero));
            ((TextView) itemView.findViewById(R.id.textNomLivraison)).setText("Livraison #" + numero);
            ((TextView) itemView.findViewById(R.id.textCommande)).setText("CMD-" + livraison.getNocde());
            ((TextView) itemView.findViewById(R.id.textClient)).setText(livraison.getNomclt());
            ((TextView) itemView.findViewById(R.id.textVille)).setText(livraison.getVilleclt());
            ((TextView) itemView.findViewById(R.id.textArticles)).setText(livraison.getNbArticles() + " articles");
            ((TextView) itemView.findViewById(R.id.textPrix)).setText(String.format("%.0f TND", livraison.getMontantTotal()));

            // Téléphone cliquable
            TextView telephone = itemView.findViewById(R.id.textTelephone);
            telephone.setText(livraison.getTelclt());
            telephone.setTextColor(Color.parseColor("#7C53C3"));
            telephone.setOnClickListener(v2 -> {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + livraison.getTelclt()));
                startActivity(callIntent);
            });

            // Statut
            TextView statut = itemView.findViewById(R.id.textStatut);
            statut.setText(livraison.getEtatliv());
            if ("Livré".equals(livraison.getEtatliv())) {
                statut.setBackgroundColor(Color.parseColor("#4CAF50"));
            } else if ("Annulé".equals(livraison.getEtatliv())) {
                statut.setBackgroundColor(Color.parseColor("#F44336"));
            } else {
                statut.setBackgroundColor(Color.parseColor("#FFC107"));
            }

            // Click sur la livraison → ouvre DetailLivraisonActivity
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(DriverActivity.this, DetailLivraisonActivity.class);
                intent.putExtra("nocde", livraison.getNocde());
                intent.putExtra("etatliv", livraison.getEtatliv());
                intent.putExtra("nomclt", livraison.getNomclt());
                intent.putExtra("telclt", livraison.getTelclt());
                intent.putExtra("villeclt", livraison.getVilleclt());
                intent.putExtra("adrclt", livraison.getAdrclt());
                intent.putExtra("modepay", livraison.getModepay());
                intent.putExtra("dateliv", livraison.getDateliv());
                intent.putExtra("nbArticles", livraison.getNbArticles());
                intent.putExtra("montantTotal", livraison.getMontantTotal());
                intent.putExtra("idLivreur", idLivreur);
                intent.putExtra("idControleur", 2);
                startActivityForResult(intent, 1);
            });

            containerLivraisons.addView(itemView);
            numero++;
        }
    }
}