package com.example.delivgo.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface LivraisonDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<LivraisonLocal> livraisons);

    @Query("SELECT * FROM livraisons WHERE idLivreur = :idLivreur AND dateliv = :date")
    List<LivraisonLocal> getLivraisonsParJour(int idLivreur, String date);

    @Query("UPDATE livraisons SET etatliv = :etat, modifie = 1 WHERE nocde = :nocde")
    void modifierEtat(int nocde, String etat);

    @Query("SELECT * FROM livraisons WHERE modifie = 1")
    List<LivraisonLocal> getLivraisonsModifiees();

    @Query("UPDATE livraisons SET modifie = 0 WHERE nocde = :nocde")
    void marquerSynchronise(int nocde);

    // REMPLACE supprimerLivraisonsJour — supprime tout le livreur sans filtre date
    @Query("DELETE FROM livraisons WHERE idLivreur = :idLivreur")
    void supprimerLivraisonsLivreur(int idLivreur);
}