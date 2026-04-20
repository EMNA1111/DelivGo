package com.example.delivgo.model;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import com.example.delivgo.R;

public class LivraisonAdapter extends RecyclerView.Adapter<LivraisonAdapter.ViewHolder> {

    private List<Livraison> livraisonList;
    private Context context;

    public LivraisonAdapter(Context context, List<Livraison> livraisonList) {
        this.context = context;
        this.livraisonList = livraisonList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_livraison, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Livraison livraison = livraisonList.get(position);

        int numero = position + 1;
        holder.textNumero.setText(String.valueOf(numero));
        holder.textNomLivraison.setText("Livraison #" + numero);
        holder.textCommande.setText("CMD-" + livraison.getNocde());
        holder.textClient.setText(livraison.getNomclt());
        holder.textTelephone.setText(livraison.getTelclt());
        holder.textVille.setText(livraison.getVilleclt());
        holder.textArticles.setText(livraison.getNbArticles() + " articles");
        holder.textPrix.setText(livraison.getMontantTotal() + " TND");
        holder.textStatut.setText(livraison.getEtatliv());

        if ("Livré".equals(livraison.getEtatliv())) {
            holder.textStatut.setBackgroundColor(Color.parseColor("#4CAF50"));
        } else {
            holder.textStatut.setBackgroundColor(Color.parseColor("#FFC107"));
        }
    }

    @Override
    public int getItemCount() {
        return livraisonList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textNumero, textNomLivraison, textStatut, textCommande,
                textClient, textTelephone, textVille, textArticles, textPrix;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textNumero       = itemView.findViewById(R.id.textNumero);
            textNomLivraison = itemView.findViewById(R.id.textNomLivraison);
            textStatut       = itemView.findViewById(R.id.textStatut);
            textCommande     = itemView.findViewById(R.id.textCommande);
            textClient       = itemView.findViewById(R.id.textClient);
            textTelephone    = itemView.findViewById(R.id.textTelephone);
            textVille        = itemView.findViewById(R.id.textVille);
            textArticles     = itemView.findViewById(R.id.textArticles);
            textPrix         = itemView.findViewById(R.id.textPrix);
        }
    }
}