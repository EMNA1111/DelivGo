package com.example.delivgo.model;

public class Livraison {
    private int nocde;
    private String dateliv;
    private String etatliv;
    private String modepay;
    private String nomclt;
    private String telclt;
    private String adrclt;
    private String villeclt;
    private int nbArticles;
    private double montantTotal;

    public int getNocde() { return nocde; }
    public String getDateliv() { return dateliv; }
    public String getEtatliv() { return etatliv; }
    public String getModepay() { return modepay; }
    public String getNomclt() { return nomclt; }
    public String getTelclt() { return telclt; }
    public String getVilleclt() { return villeclt; }
    public int getNbArticles() { return nbArticles; }
    public String getAdrclt() { return adrclt; }
    public double getMontantTotal() { return montantTotal; }
}