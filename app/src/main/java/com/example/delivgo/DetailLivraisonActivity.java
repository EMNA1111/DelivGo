package com.example.delivgo;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.example.delivgo.api.ApiService;
import com.example.delivgo.api.RetrofitHelper;
import com.example.delivgo.local.AppDatabase;
import com.example.delivgo.model.Message;

public class DetailLivraisonActivity extends AppCompatActivity {

    TextView textStatut, textNumCommande, textNomClient, textTelephone,
            textAdresse, textNbArticles, textModePaiement, textDate, textMontant;
    AppCompatButton btnMaps, btnEnvoyerProbleme;
    EditText editProbleme;
    AppDatabase db;

    int nocde, idLivreur, idControleur;
    String etatliv, nomclt, telclt, villeclt, adrclt, modepay, dateliv;
    int nbArticles;
    double montantTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_livraison);

        textStatut       = findViewById(R.id.textStatut);
        textNumCommande  = findViewById(R.id.textNumCommande);
        textNomClient    = findViewById(R.id.textNomClient);
        textTelephone    = findViewById(R.id.textTelephone);
        textAdresse      = findViewById(R.id.textAdresse);
        textNbArticles   = findViewById(R.id.textNbArticles);
        textModePaiement = findViewById(R.id.textModePaiement);
        textDate         = findViewById(R.id.textDate);
        textMontant      = findViewById(R.id.textMontant);
        btnMaps          = findViewById(R.id.btnMaps);
        editProbleme     = findViewById(R.id.editProbleme);
        btnEnvoyerProbleme = findViewById(R.id.btnEnvoyerProbleme);

        db = AppDatabase.getInstance(this);

        nocde        = getIntent().getIntExtra("nocde", 0);
        etatliv      = getIntent().getStringExtra("etatliv");
        nomclt       = getIntent().getStringExtra("nomclt");
        telclt       = getIntent().getStringExtra("telclt");
        villeclt     = getIntent().getStringExtra("villeclt");
        adrclt       = getIntent().getStringExtra("adrclt");
        modepay      = getIntent().getStringExtra("modepay");
        dateliv      = getIntent().getStringExtra("dateliv");
        nbArticles   = getIntent().getIntExtra("nbArticles", 0);
        montantTotal = getIntent().getDoubleExtra("montantTotal", 0);
        idLivreur    = getIntent().getIntExtra("idLivreur", 0);
        idControleur = getIntent().getIntExtra("idControleur", 2);

        textNumCommande.setText("Commande: CMD-" + nocde);
        textNomClient.setText(nomclt);
        textTelephone.setText(telclt);
        textAdresse.setText(adrclt);
        textNbArticles.setText(String.valueOf(nbArticles));
        textModePaiement.setText(modepay);
        textDate.setText(dateliv);
        textMontant.setText(String.format("%.0f TND", montantTotal));

        afficherStatut(etatliv);

        findViewById(R.id.btnRetour).setOnClickListener(v -> finish());

        btnMaps.setOnClickListener(v -> {
            String url = "geo:0,0?q=" + Uri.encode(adrclt);
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        });

        // Clic sur statut → modifier l'état
        textStatut.setOnClickListener(v -> {
            String[] etats = {"Livré", "En cours", "En attente", "Annulé"};
            new AlertDialog.Builder(this)
                    .setTitle("Modifier l'état")
                    .setItems(etats, (dialog, which) -> {
                        String nouvelEtat = etats[which];

                        // 1. Sauvegarde dans Room immédiatement (modifie = 1)
                        db.livraisonDao().modifierEtat(nocde, nouvelEtat);
                        etatliv = nouvelEtat;
                        afficherStatut(nouvelEtat);
                        setResult(RESULT_OK);
                        Toast.makeText(this, "État modifié ✓", Toast.LENGTH_SHORT).show();

                        // 2. Essaie d'envoyer vers le serveur
                        RetrofitHelper.getService().modifierEtat(nocde, nouvelEtat)
                                .enqueue(new Callback<Void>() {
                                    @Override
                                    public void onResponse(Call<Void> call, Response<Void> response) {
                                        if (response.isSuccessful()) {
                                            // Marque comme synchronisé dans Room
                                            db.livraisonDao().marquerSynchronise(nocde);
                                        }
                                    }
                                    @Override
                                    public void onFailure(Call<Void> call, Throwable t) {
                                        // Pas de connexion → sera synchronisé à minuit
                                    }
                                });
                    })
                    .show();
        });

        // Téléphone cliquable
        textTelephone.setOnClickListener(v -> {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + telclt));
            startActivity(callIntent);
        });

        // Envoyer problème urgent
        btnEnvoyerProbleme.setOnClickListener(v -> {
            String probleme = editProbleme.getText().toString().trim();
            if (probleme.isEmpty()) {
                Toast.makeText(this, "Décrivez le problème !", Toast.LENGTH_SHORT).show();
                return;
            }

            String message = "🚨 URGENT - CMD-" + nocde +
                    "\nClient : " + nomclt +
                    "\nTél : " + telclt +
                    "\nProblème : " + probleme;

            RetrofitHelper.getService().envoyerMessage(idLivreur, idControleur, message, 1)
                    .enqueue(new Callback<Message>() {
                        @Override
                        public void onResponse(Call<Message> call, Response<Message> response) {
                            if (response.isSuccessful()) {
                                editProbleme.setText("");
                                Toast.makeText(DetailLivraisonActivity.this,
                                        "🚨 Problème signalé !", Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onFailure(Call<Message> call, Throwable t) {
                            Toast.makeText(DetailLivraisonActivity.this,
                                    "Erreur : " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void afficherStatut(String statut) {
        textStatut.setText(statut);
        switch (statut) {
            case "Livré":
                textStatut.setTextColor(Color.parseColor("#4CAF50"));
                textStatut.setBackgroundColor(Color.parseColor("#E8F5E9"));
                break;
            case "En cours":
                textStatut.setTextColor(Color.parseColor("#2196F3"));
                textStatut.setBackgroundColor(Color.parseColor("#E3F2FD"));
                break;
            case "En attente":
                textStatut.setTextColor(Color.parseColor("#FF9800"));
                textStatut.setBackgroundColor(Color.parseColor("#FFF3E0"));
                break;
            case "Annulé":
                textStatut.setTextColor(Color.parseColor("#F44336"));
                textStatut.setBackgroundColor(Color.parseColor("#FFEBEE"));
                break;
        }
    }
}