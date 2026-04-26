package com.example.delivgo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MessagerieControleurActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    RequestQueue requestQueue;
    List<String[]> livreursList = new ArrayList<>();
    List<String[]> livreursFiltres = new ArrayList<>();
    LivreurAdapter adapter;

    String url = "http://10.0.2.2:8081/api/livreurs/messages";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messagerie_controleur);

        ImageView btnRetour = findViewById(R.id.imageView6);
        btnRetour.setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.containerLivreurs);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        requestQueue = Volley.newRequestQueue(this);

        EditText editRecherche = findViewById(R.id.editRecherche);
        editRecherche.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrer(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        livreursList.clear();
        chargerLivreurs();
    }

    void chargerLivreurs() {
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    livreursList.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            String nom = obj.getString("prenompers") + " " + obj.getString("nompers");
                            String idpers = obj.getString("idpers");
                            String msg = obj.getString("dernierMessage");
                            String nonLus = obj.getString("nonLus");
                            if (msg.length() > 40) msg = msg.substring(0, 40) + "...";
                            livreursList.add(new String[]{idpers, nom, msg, nonLus});
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    livreursFiltres.clear();
                    livreursFiltres.addAll(livreursList);
                    adapter = new LivreurAdapter(livreursFiltres);
                    recyclerView.setAdapter(adapter);
                },
                error -> error.printStackTrace()
        );
        requestQueue.add(request);
    }

    void filtrer(String texte) {
        livreursFiltres.clear();
        for (String[] livreur : livreursList) {
            if (livreur[1].toLowerCase().contains(texte.toLowerCase())) {
                livreursFiltres.add(livreur);
            }
        }
        adapter.notifyDataSetChanged();
    }

    class LivreurAdapter extends RecyclerView.Adapter<LivreurAdapter.ViewHolder> {

        List<String[]> liste;

        LivreurAdapter(List<String[]> liste) {
            this.liste = liste;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_livreur_message, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            String[] livreur = liste.get(position);
            String nom = livreur[1];
            holder.tvAvatar.setText(nom.substring(0, 2).toUpperCase());
            holder.tvNom.setText(nom);
            holder.tvMsg.setText(livreur[2]);
            holder.tvHeure.setText("");

            int nonLus = Integer.parseInt(livreur[3]);
            if (nonLus > 0) {
                holder.tvBadge.setVisibility(View.VISIBLE);
                holder.tvBadge.setText(String.valueOf(nonLus));
            } else {
                holder.tvBadge.setVisibility(View.GONE);
            }

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(MessagerieControleurActivity.this, MessagerieActivity.class);
                intent.putExtra("idUser", 2);
                intent.putExtra("idInterlocuteur", Integer.parseInt(livreur[0]));
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return liste.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvAvatar, tvNom, tvMsg, tvHeure, tvBadge;

            ViewHolder(View view) {
                super(view);
                tvAvatar = view.findViewById(R.id.textView5);
                tvNom    = view.findViewById(R.id.textView20);
                tvMsg    = view.findViewById(R.id.textView25);
                tvHeure  = view.findViewById(R.id.textView26);
                tvBadge  = view.findViewById(R.id.tvBadge);
            }
        }
    }
}