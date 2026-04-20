package com.example.delivgo.model;

public class Message {
    private int id;
    private int expediteur;
    private int destinataire;
    private String contenu;
    private String dateEnvoi;
    private int urgent;

    public int getId() { return id; }
    public int getExpediteur() { return expediteur; }
    public int getDestinataire() { return destinataire; }
    public String getContenu() { return contenu; }
    public String getDateEnvoi() { return dateEnvoi; }
    public int getUrgent() { return urgent; }
    public void setUrgent(int urgent) { this.urgent = urgent; }
}