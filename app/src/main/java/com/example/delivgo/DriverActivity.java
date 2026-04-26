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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.example.delivgo.api.RetrofitHelper;
import com.example.delivgo.local.AppDatabase;
import com.example.delivgo.local.LivraisonLocal;
import com.example.delivgo.local.SyncManager;
import com.example.delivgo.local.SyncScheduler;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverActivity extends AppCompatActivity {

    LinearLayout containerLivraisons;
    TextView textNomLivreur, textTotal, textLivrees, textRestantes;
    int idLivreur;
    AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);

        findViewById(R.id.btnRetour).setOnClickListener(v -> {
            Intent intent = new Intent(DriverActivity.this, MainActivity2.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        containerLivraisons = findViewById(R.id.containerLivraisons);
        textNomLivreur = findViewById(R.id.textNomLivreur);
        textTotal = findViewById(R.id.textTotal);
        textLivrees = findViewById(R.id.textLivrees);
        textRestantes = findViewById(R.id.textRestantes);

        db = AppDatabase.getInstance(this);
        idLivreur = getIntent().getIntExtra("idLivreur", 1);

        TextView textDateJour = findViewById(R.id.textDateJour);
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM", new Locale("fr"));
        textDateJour.setText(sdf.format(new Date()));

        getSharedPreferences("delivgo", MODE_PRIVATE)
                .edit()
                .putInt("idLivreur", idLivreur)
                .apply();

        SyncScheduler.planifier(this);

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
        RetrofitHelper.getService().getStats(idLivreur).enqueue(new Callback<Map<String, Object>>() {
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
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        SyncManager syncManager = new SyncManager(this);

        syncManager.chargerLivraisonsDepuisServeur(idLivreur, new SyncManager.SyncCallback() {
            @Override
            public void onSuccess() {
                List<LivraisonLocal> livraisons = db.livraisonDao().getLivraisonsParJour(idLivreur, today);
                runOnUiThread(() -> afficherLivraisons(livraisons));
            }

            @Override
            public void onError(String message) {
                List<LivraisonLocal> livraisons = db.livraisonDao().getLivraisonsParJour(idLivreur, today);
                runOnUiThread(() -> {
                    if (livraisons.isEmpty()) {
                        Toast.makeText(DriverActivity.this, "Aucune livraison disponible", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(DriverActivity.this, "Mode hors ligne", Toast.LENGTH_SHORT).show();
                        afficherLivraisons(livraisons);
                    }
                });
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

    private void afficherLivraisons(List<LivraisonLocal> livraisons) {
        containerLivraisons.removeAllViews();
        int numero = 1;

        for (LivraisonLocal livraison : livraisons) {
            View itemView = getLayoutInflater().inflate(R.layout.item_livraison, containerLivraisons, false);

            ((TextView) itemView.findViewById(R.id.textNumero)).setText(String.valueOf(numero));
            ((TextView) itemView.findViewById(R.id.textNomLivraison)).setText("Livraison #" + numero);
            ((TextView) itemView.findViewById(R.id.textCommande)).setText("CMD-" + livraison.nocde);
            ((TextView) itemView.findViewById(R.id.textClient)).setText(livraison.nomclt);
            ((TextView) itemView.findViewById(R.id.textVille)).setText(livraison.villeclt);
            ((TextView) itemView.findViewById(R.id.textArticles)).setText(livraison.nbArticles + " articles");
            ((TextView) itemView.findViewById(R.id.textPrix)).setText(String.format("%.0f TND", livraison.montantTotal));

            TextView telephone = itemView.findViewById(R.id.textTelephone);
            telephone.setText(livraison.telclt);
            telephone.setTextColor(Color.parseColor("#7C53C3"));
            telephone.setOnClickListener(v2 -> {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + livraison.telclt));
                startActivity(callIntent);
            });

            TextView statut = itemView.findViewById(R.id.textStatut);
            statut.setText(livraison.etatliv);
            if ("Livré".equals(livraison.etatliv)) {
                statut.setBackgroundColor(Color.parseColor("#4CAF50"));
            } else if ("Annulé".equals(livraison.etatliv)) {
                statut.setBackgroundColor(Color.parseColor("#F44336"));
            } else {
                statut.setBackgroundColor(Color.parseColor("#FFC107"));
            }

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(DriverActivity.this, DetailLivraisonActivity.class);
                intent.putExtra("nocde", livraison.nocde);
                intent.putExtra("etatliv", livraison.etatliv);
                intent.putExtra("nomclt", livraison.nomclt);
                intent.putExtra("telclt", livraison.telclt);
                intent.putExtra("villeclt", livraison.villeclt);
                intent.putExtra("adrclt", livraison.adrclt);
                intent.putExtra("modepay", livraison.modepay);
                intent.putExtra("dateliv", livraison.dateliv);
                intent.putExtra("nbArticles", livraison.nbArticles);
                intent.putExtra("montantTotal", livraison.montantTotal);
                intent.putExtra("idLivreur", idLivreur);
                intent.putExtra("idControleur", 2);
                startActivityForResult(intent, 1);
            });

            containerLivraisons.addView(itemView);
            numero++;
        }
    }
}