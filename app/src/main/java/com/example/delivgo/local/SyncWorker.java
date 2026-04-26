package com.example.delivgo.local;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.delivgo.api.ApiService;
import com.example.delivgo.api.RetrofitHelper;
import com.example.delivgo.model.Livraison;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Response;

public class SyncWorker extends Worker {

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        ApiService api = RetrofitHelper.getService();

        SharedPreferences prefs = getApplicationContext()
                .getSharedPreferences("delivgo", Context.MODE_PRIVATE);
        int idLivreur = prefs.getInt("idLivreur", 0);
        if (idLivreur == 0) return Result.failure();

        try {
            // ÉTAPE 1 — Envoie les états modifiés vers MySQL
            List<LivraisonLocal> modifiees = db.livraisonDao().getLivraisonsModifiees();
            for (LivraisonLocal l : modifiees) {
                Response<Void> response = api.modifierEtat(l.nocde, l.etatliv).execute();
                if (response.isSuccessful()) {
                    db.livraisonDao().marquerSynchronise(l.nocde);
                }
            }

            // ÉTAPE 2 — Charge les nouvelles livraisons du nouveau jour
            Response<List<Livraison>> response = api.getLivraisons(idLivreur).execute();

            if (response.isSuccessful() && response.body() != null) {
                // CORRECTION : supprime par livreur sans filtre date
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
            }

            // ÉTAPE 3 — Replanifie pour le prochain minuit
            SyncScheduler.planifier(getApplicationContext());

            return Result.success();

        } catch (Exception e) {
            return Result.retry();
        }
    }
}