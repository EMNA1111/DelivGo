package com.example.delivgo.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "livraisons")
public class LivraisonLocal {

    @PrimaryKey
    public int nocde;
    public String dateliv;
    public String etatliv;
    public String modepay;
    public String nomclt;
    public String telclt;
    public String adrclt;
    public String villeclt;
    public int nbArticles;
    public double montantTotal;
    public String nomLivreur;
    public int idLivreur;
    public int modifie; // 0 = non modifié, 1 = modifié à synchroniser
}