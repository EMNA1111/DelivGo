package com.example.delivgo;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.delivgo.api.ApiService;
import com.example.delivgo.api.RetrofitHelper;
import com.example.delivgo.model.Livraison;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class LivraisonsControleurActivity extends AppCompatActivity {

    RecyclerView recyclerLivraisons;
    List<Livraison> toutesLivraisons = new ArrayList<>();
    List<Livraison> livraisonsFiltrees = new ArrayList<>();
    LivraisonControleurAdapter adapter;
    EditText editRecherche;
    TextView btnDateDebut, btnDateFin;

    String filtreEtat = null;
    String filtreLivreur = null;
    String filtreClient = null;
    String filtreCommande = null;
    String filtreDateDebut = null;
    String filtreDateFin = null;

    boolean chargerToutesCommandes = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_livraisons_controleur);

        recyclerLivraisons = findViewById(R.id.recyclerLivraisons);
        recyclerLivraisons.setLayoutManager(new LinearLayoutManager(this));

        editRecherche = findViewById(R.id.editRecherche);
        btnDateDebut = findViewById(R.id.btnDateDebut);
        btnDateFin = findViewById(R.id.btnDateFin);
        // Bouton Accueil
        findViewById(R.id.btnAccueil).setOnClickListener(v -> {
            Intent intent = new Intent(LivraisonsControleurActivity.this, ControleurActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

// Bouton Messages
        findViewById(R.id.btnMessages).setOnClickListener(v -> {
            Intent intent = new Intent(LivraisonsControleurActivity.this, MessagerieControleurActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btnRetour).setOnClickListener(v -> finish());

        findViewById(R.id.btnResetAll).setOnClickListener(v -> {
            filtreEtat = null;
            filtreLivreur = null;
            filtreClient = null;
            filtreCommande = null;
            filtreDateDebut = null;
            filtreDateFin = null;
            btnDateDebut.setText("Date début");
            btnDateFin.setText("Date fin");
            editRecherche.setText("");
            chargerToutesCommandes = false;
            chargerLivraisons();
        });

        editRecherche.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                appliquerFiltres();
            }
        });

        findViewById(R.id.btnFiltreEtat).setOnClickListener(v -> {
            String[] etats = {"Tous", "Livré", "En cours", "En attente", "Annulé"};
            new AlertDialog.Builder(this)
                    .setTitle("Filtrer par état")
                    .setItems(etats, (dialog, which) -> {
                        filtreEtat = which == 0 ? null : etats[which];
                        appliquerFiltres();
                    }).show();
        });

        findViewById(R.id.btnFiltrelivreur).setOnClickListener(v -> {
            List<String> livreurs = new ArrayList<>();
            livreurs.add("Tous");
            for (Livraison l : toutesLivraisons) {
                if (l.getNomLivreur() != null && !livreurs.contains(l.getNomLivreur())) {
                    livreurs.add(l.getNomLivreur());
                }
            }
            new AlertDialog.Builder(this)
                    .setTitle("Filtrer par livreur")
                    .setItems(livreurs.toArray(new String[0]), (dialog, which) -> {
                        filtreLivreur = which == 0 ? null : livreurs.get(which);
                        appliquerFiltres();
                    }).show();
        });

        findViewById(R.id.btnFiltreClient).setOnClickListener(v -> {
            List<String> clients = new ArrayList<>();
            clients.add("Tous");
            for (Livraison l : toutesLivraisons) {
                if (l.getNomclt() != null && !clients.contains(l.getNomclt())) {
                    clients.add(l.getNomclt());
                }
            }
            new AlertDialog.Builder(this)
                    .setTitle("Filtrer par client")
                    .setItems(clients.toArray(new String[0]), (dialog, which) -> {
                        filtreClient = which == 0 ? null : clients.get(which);
                        appliquerFiltres();
                    }).show();
        });

        findViewById(R.id.btnFiltreCommande).setOnClickListener(v -> {
            EditText input = new EditText(this);
            input.setHint("Ex: 21 ou CMD-21");
            new AlertDialog.Builder(this)
                    .setTitle("Rechercher une commande")
                    .setView(input)
                    .setPositiveButton("Rechercher", (dialog, which) -> {
                        String val = input.getText().toString().trim();
                        if (val.isEmpty()) {
                            filtreCommande = null;
                        } else {
                            filtreCommande = val.toUpperCase().startsWith("CMD-") ? val.substring(4) : val;
                        }
                        appliquerFiltres();
                    })
                    .setNegativeButton("Annuler", null)
                    .show();
        });

        btnDateDebut.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) -> {
                filtreDateDebut = String.format("%04d-%02d-%02d", year, month + 1, day);
                btnDateDebut.setText(filtreDateDebut);
                chargerToutesCommandes = true;
                chargerLivraisons();
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnDateFin.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) -> {
                filtreDateFin = String.format("%04d-%02d-%02d", year, month + 1, day);
                btnDateFin.setText(filtreDateFin);
                chargerToutesCommandes = true;
                chargerLivraisons();
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnDateDebut.setOnLongClickListener(v -> {
            filtreDateDebut = null;
            btnDateDebut.setText("Date début");
            appliquerFiltres();
            return true;
        });

        btnDateFin.setOnLongClickListener(v -> {
            filtreDateFin = null;
            btnDateFin.setText("Date fin");
            appliquerFiltres();
            return true;
        });

        chargerLivraisons();
    }

    private void appliquerFiltres() {
        if (adapter == null) return;
        String recherche = editRecherche.getText().toString().trim().toLowerCase();
        livraisonsFiltrees.clear();

        for (Livraison l : toutesLivraisons) {
            boolean ok = true;

            // Recherche texte libre
            if (!recherche.isEmpty()) {
                boolean contient =
                        (l.getNomclt() != null && l.getNomclt().toLowerCase().contains(recherche))
                                || String.valueOf(l.getNocde()).contains(recherche)
                                || ("cmd-" + l.getNocde()).contains(recherche)
                                || (l.getNomLivreur() != null && l.getNomLivreur().toLowerCase().contains(recherche));
                if (!contient) ok = false;
            }

            // Filtre état
            if (ok && filtreEtat != null && !filtreEtat.equals(l.getEtatliv()))
                ok = false;

            // Filtre livreur
            if (ok && filtreLivreur != null && !filtreLivreur.equals(l.getNomLivreur()))
                ok = false;

            // Filtre client
            if (ok && filtreClient != null && !filtreClient.equals(l.getNomclt()))
                ok = false;

            // Filtre commande
            if (ok && filtreCommande != null && !String.valueOf(l.getNocde()).equals(filtreCommande))
                ok = false;

            // Filtre date début
            if (ok && filtreDateDebut != null && l.getDateliv() != null) {
                String dateLiv = l.getDateliv().length() >= 10 ? l.getDateliv().substring(0,10) : l.getDateliv();
                if (dateLiv.compareTo(filtreDateDebut) < 0) ok = false;
            }

            // Filtre date fin
            if (ok && filtreDateFin != null && l.getDateliv() != null) {
                String dateLiv = l.getDateliv().length() >= 10 ? l.getDateliv().substring(0,10) : l.getDateliv();
                if (dateLiv.compareTo(filtreDateFin) > 0) ok = false;
            }

            if (ok) livraisonsFiltrees.add(l);
        }

        adapter.notifyDataSetChanged();

        // Indicateur visuel des filtres actifs
        updateFilterButton(findViewById(R.id.btnFiltreEtat), filtreEtat != null);
        updateFilterButton(findViewById(R.id.btnFiltrelivreur), filtreLivreur != null);
        updateFilterButton(findViewById(R.id.btnFiltreClient), filtreClient != null);
        updateFilterButton(findViewById(R.id.btnFiltreCommande), filtreCommande != null);
        updateFilterButton(btnDateDebut, filtreDateDebut != null);
        updateFilterButton(btnDateFin, filtreDateFin != null);
    }

    private void updateFilterButton(TextView button, boolean isActive) {
        if (isActive) {
            button.setBackgroundColor(getColor(android.R.color.holo_orange_light));
            button.setTextColor(getColor(android.R.color.black));
        } else {
            button.setBackgroundResource(R.drawable.bg_input);
            button.setTextColor(getColor(android.R.color.darker_gray));
        }
    }

    private void chargerLivraisons() {
        ApiService api = RetrofitHelper.getService();
        Call<List<Livraison>> call;

        if (chargerToutesCommandes) {
            call = api.getLivraisonsControleurToutes();
        } else {
            call = api.getLivraisonsControleurJour();
        }

        call.enqueue(new Callback<List<Livraison>>() {
            @Override
            public void onResponse(Call<List<Livraison>> call, Response<List<Livraison>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    toutesLivraisons.clear();
                    toutesLivraisons.addAll(response.body());
                    livraisonsFiltrees.clear();
                    livraisonsFiltrees.addAll(toutesLivraisons);
                    adapter = new LivraisonControleurAdapter(livraisonsFiltrees);
                    recyclerLivraisons.setAdapter(adapter);
                    // Applique les filtres après chargement
                    appliquerFiltres();
                } else {
                    Toast.makeText(LivraisonsControleurActivity.this, "Aucune livraison", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Livraison>> call, Throwable t) {
                Toast.makeText(LivraisonsControleurActivity.this, "Erreur : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    class LivraisonControleurAdapter extends RecyclerView.Adapter<LivraisonControleurAdapter.ViewHolder> {

        List<Livraison> livraisons;

        LivraisonControleurAdapter(List<Livraison> livraisons) {
            this.livraisons = livraisons;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_livraison, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Livraison livraison = livraisons.get(position);
            int numero = position + 1;

            holder.textNumero.setText(String.valueOf(numero));
            holder.textNomLivraison.setText("Livraison #" + numero);
            holder.textCommande.setText("CMD-" + livraison.getNocde());
            holder.textClient.setText(livraison.getNomclt() != null ? livraison.getNomclt() : "");
            holder.textTelephone.setText(livraison.getTelclt() != null ? livraison.getTelclt() : "");
            holder.textVille.setText(livraison.getVilleclt() != null ? livraison.getVilleclt() : "");
            holder.textArticles.setText(livraison.getNbArticles() + " articles");
            holder.textPrix.setText(String.format("%.0f TND", livraison.getMontantTotal()));
            holder.textStatut.setText(livraison.getEtatliv() != null ? livraison.getEtatliv() : "");

            if ("Livré".equals(livraison.getEtatliv())) {
                holder.textStatut.setBackgroundColor(android.graphics.Color.parseColor("#4CAF50"));
            } else if ("Annulé".equals(livraison.getEtatliv())) {
                holder.textStatut.setBackgroundColor(android.graphics.Color.parseColor("#F44336"));
            } else {
                holder.textStatut.setBackgroundColor(android.graphics.Color.parseColor("#FFC107"));
            }
        }

        @Override
        public int getItemCount() { return livraisons.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView textNumero, textNomLivraison, textCommande, textClient,
                    textTelephone, textVille, textArticles, textPrix, textStatut;

            ViewHolder(View view) {
                super(view);
                textNumero       = view.findViewById(R.id.textNumero);
                textNomLivraison = view.findViewById(R.id.textNomLivraison);
                textCommande     = view.findViewById(R.id.textCommande);
                textClient       = view.findViewById(R.id.textClient);
                textTelephone    = view.findViewById(R.id.textTelephone);
                textVille        = view.findViewById(R.id.textVille);
                textArticles     = view.findViewById(R.id.textArticles);
                textPrix         = view.findViewById(R.id.textPrix);
                textStatut       = view.findViewById(R.id.textStatut);
            }
        }
    }
}