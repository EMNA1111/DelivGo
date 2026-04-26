package com.example.delivgo.local;

import android.content.Context;
import com.example.delivgo.api.ApiService;
import com.example.delivgo.api.RetrofitHelper;
import com.example.delivgo.model.Livraison;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SyncManager {

    private Context context;
    private AppDatabase db;
    private ApiService api;

    public SyncManager(Context context) {
        this.context = context;
        this.db = AppDatabase.getInstance(context);
        this.api = RetrofitHelper.getService();
    }

    // Charge les livraisons depuis MySQL vers Room (sans filtre de date)
    public void chargerLivraisonsDepuisServeur(int idLivreur, SyncCallback callback) {
        Call<List<Livraison>> call = api.getLivraisons(idLivreur);
        call.enqueue(new Callback<List<Livraison>>() {
            @Override
            public void onResponse(Call<List<Livraison>> call, Response<List<Livraison>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    // Supprime toutes les anciennes livraisons du livreur (pas de filtre date)
                    db.livraisonDao().supprimerLivraisonsLivreur(idLivreur);

                    List<LivraisonLocal> locales = new ArrayList<>();
                    for (Livraison l : response.body()) {
                        LivraisonLocal local = new LivraisonLocal();
                        local.nocde = l.getNocde();
                        local.dateliv = l.getDateliv();
                        local.etatliv = l.getEtatliv();
                        local.modepay = l.getModepay();
                        local.nomclt = l.getNomclt();
                        local.telclt = l.getTelclt();
                        local.adrclt = l.getAdrclt();
                        local.villeclt = l.getVilleclt();
                        local.nbArticles = l.getNbArticles();
                        local.montantTotal = l.getMontantTotal();
                        local.nomLivreur = l.getNomLivreur();
                        local.idLivreur = idLivreur;
                        local.modifie = 0;
                        locales.add(local);
                    }
                    db.livraisonDao().insertAll(locales);
                    if (callback != null) callback.onSuccess();
                } else {
                    if (callback != null) callback.onError("Erreur serveur");
                }
            }

            @Override
            public void onFailure(Call<List<Livraison>> call, Throwable t) {
                if (callback != null) callback.onError(t.getMessage());
            }
        });
    }

    // Appelé à minuit — envoie les états modifiés vers MySQL
    public void synchroniserVersServeur() {
        List<LivraisonLocal> modifiees = db.livraisonDao().getLivraisonsModifiees();
        for (LivraisonLocal l : modifiees) {
            api.modifierEtat(l.nocde, l.etatliv).enqueue(new Callback<>() {
                @Override
                public void onResponse(Call call, Response response) {
                    if (response.isSuccessful()) {
                        db.livraisonDao().marquerSynchronise(l.nocde);
                    }
                }
                @Override
                public void onFailure(Call call, Throwable t) {}
            });
        }
    }

    public interface SyncCallback {
        void onSuccess();
        void onError(String message);
    }
}