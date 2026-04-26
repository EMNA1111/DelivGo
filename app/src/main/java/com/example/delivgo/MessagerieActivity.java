package com.example.delivgo;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.delivgo.api.ApiService;
import com.example.delivgo.api.RetrofitHelper;
import com.example.delivgo.model.Message;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;

public class MessagerieActivity extends AppCompatActivity {

    LinearLayout containerMessages;
    ScrollView scrollMessages;
    EditText editMessage;
    int idUser, idInterlocuteur;
    Handler handler = new Handler();
    Runnable rafraichir;
    int dernierMessageId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messagerie);

        containerMessages = findViewById(R.id.containerMessages);
        scrollMessages    = findViewById(R.id.scrollMessages);
        editMessage       = findViewById(R.id.editMessage);

        idUser          = getIntent().getIntExtra("idUser", 0);
        idInterlocuteur = getIntent().getIntExtra("idInterlocuteur", 0);


        // Marquer les messages comme lus dès l'ouverture
        marquerMessagesLus();

        findViewById(R.id.btnRetour).setOnClickListener(v -> finish());

        findViewById(R.id.btnEnvoyer).setOnClickListener(v -> {
            String contenu = editMessage.getText().toString().trim();
            if (!contenu.isEmpty()) {
                envoyerMessage(contenu);
            }
        });

        rafraichir = new Runnable() {
            @Override
            public void run() {
                chargerMessages();
                handler.postDelayed(this, 3000);
            }
        };
        handler.post(rafraichir);
    }

    private void marquerMessagesLus() {
        ApiService api = RetrofitHelper.getService();
        api.marquerLus(idInterlocuteur, idUser).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {}

            @Override
            public void onFailure(Call<Void> call, Throwable t) {}
        });
    }

    private void chargerMessages() {
        ApiService api = RetrofitHelper.getService();
        api.getMessages(idUser, idInterlocuteur).enqueue(new Callback<List<Message>>() {
            @Override
            public void onResponse(Call<List<Message>> call, Response<List<Message>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Message> messages = response.body();
                    if (!messages.isEmpty()) {
                        int lastId = messages.get(messages.size() - 1).getId();
                        if (lastId != dernierMessageId) {
                            dernierMessageId = lastId;
                            afficherMessages(messages);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Message>> call, Throwable t) {
                Toast.makeText(MessagerieActivity.this, "Erreur : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void afficherMessages(List<Message> messages) {
        containerMessages.removeAllViews();

        for (Message msg : messages) {
            boolean estMoi = msg.getExpediteur() == idUser;

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.VERTICAL);
            row.setGravity(estMoi ? Gravity.END : Gravity.START);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            rowParams.setMargins(0, 6, 0, 6);
            row.setLayoutParams(rowParams);

            TextView textMsg = new TextView(this);
            textMsg.setText(msg.getContenu());
            textMsg.setTextSize(14);
            textMsg.setPadding(28, 18, 28, 18);

            if (msg.getUrgent() == 1) {
                textMsg.setTextColor(Color.WHITE);
                textMsg.setBackgroundColor(Color.parseColor("#D32F2F"));
            } else if (estMoi) {
                textMsg.setTextColor(Color.WHITE);
                textMsg.setBackgroundColor(Color.parseColor("#7C53C3"));
            } else {
                textMsg.setTextColor(Color.parseColor("#1A1A2E"));
                textMsg.setBackgroundColor(Color.WHITE);
            }

            LinearLayout.LayoutParams msgParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            msgParams.setMargins(estMoi ? 80 : 16, 0, estMoi ? 16 : 80, 0);
            textMsg.setLayoutParams(msgParams);

            TextView textHeure = new TextView(this);
            String dateEnvoi = msg.getDateEnvoi();
            String heure;
            if (dateEnvoi != null && dateEnvoi.length() >= 16) {
                heure = dateEnvoi.substring(11, 16);
            } else {
                java.util.Calendar cal = java.util.Calendar.getInstance();
                heure = String.format("%02d:%02d",
                        cal.get(java.util.Calendar.HOUR_OF_DAY),
                        cal.get(java.util.Calendar.MINUTE));
            }
            textHeure.setText(heure);
            textHeure.setTextSize(10);
            textHeure.setTextColor(Color.parseColor("#AAAAAA"));
            textHeure.setTypeface(null, Typeface.ITALIC);

            LinearLayout.LayoutParams heureParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            heureParams.setMargins(estMoi ? 0 : 20, 2, estMoi ? 20 : 0, 0);
            textHeure.setLayoutParams(heureParams);
            textHeure.setGravity(estMoi ? Gravity.END : Gravity.START);

            row.addView(textMsg);
            row.addView(textHeure);
            containerMessages.addView(row);
        }

        scrollMessages.post(() -> scrollMessages.fullScroll(ScrollView.FOCUS_DOWN));
    }

    private void envoyerMessage(String contenu) {
        ApiService api = RetrofitHelper.getService();
        api.envoyerMessage(idUser, idInterlocuteur, contenu, 0).enqueue(new Callback<Message>() {
            @Override
            public void onResponse(Call<Message> call, Response<Message> response) {
                if (response.isSuccessful()) {
                    editMessage.setText("");
                    dernierMessageId = -1;
                    chargerMessages();
                }
            }

            @Override
            public void onFailure(Call<Message> call, Throwable t) {
                Toast.makeText(MessagerieActivity.this, "Erreur envoi : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(rafraichir);
    }
}