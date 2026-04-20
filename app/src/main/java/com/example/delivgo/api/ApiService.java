package com.example.delivgo.api;

import com.example.delivgo.model.Livraison;
import com.example.delivgo.model.Message;
import com.example.delivgo.model.Personnel;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface ApiService {
    @GET("/api/login")
    Call<Personnel> login(@Query("login") String login, @Query("motP") String motP);

    @GET("/api/livraisons")
    Call<List<Livraison>> getLivraisons(@Query("livreur") int livreur);

    @GET("/api/stats")
    Call<Map<String, Object>> getStats(@Query("livreur") int livreur);

    @PUT("/api/livraisons/etat")
    Call<Map<String, Object>> modifierEtat(@Query("nocde") int nocde, @Query("etat") String etat);

    @GET("/api/messages")
    Call<List<Message>> getMessages(@Query("user1") int user1, @Query("user2") int user2);

    @POST("/api/messages/envoyer")
    Call<Message> envoyerMessage(@Query("expediteur") int expediteur,
                                 @Query("destinataire") int destinataire,
                                 @Query("contenu") String contenu,
                                 @Query("urgent") int urgent);
    @GET("/api/controleur/stats")
    Call<Map<String, Object>> getStatsControleur();
}