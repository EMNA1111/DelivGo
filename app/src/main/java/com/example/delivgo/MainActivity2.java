package com.example.delivgo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.delivgo.api.ApiService;
import com.example.delivgo.api.RetrofitHelper;
import com.example.delivgo.model.Personnel;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        EditText email = findViewById(R.id.email);
        EditText password = findViewById(R.id.password);
        Button btnlogin = findViewById(R.id.btnlogin);

        btnlogin.setOnClickListener(v -> {
            String strEmail = email.getText().toString();
            String strPassword = password.getText().toString();

            if (strEmail.isEmpty() || strPassword.isEmpty()) {
                Toast.makeText(this, "Remplissez tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }

            ApiService api = RetrofitHelper.getService();
            Call<Personnel> call = api.login(strEmail, strPassword);

            call.enqueue(new Callback<Personnel>() {
                @Override
                public void onResponse(Call<Personnel> call, Response<Personnel> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Personnel user = response.body();
                        if (user.getCodeposte().equals("LIVREUR")) {
                            Intent intent = new Intent(MainActivity2.this, DriverActivity.class);
                            intent.putExtra("idLivreur", user.getIdpers());
                            startActivity(intent);
                        } else if (user.getCodeposte().equals("CONTROLEUR")) {
                            Intent intent = new Intent(MainActivity2.this, ControleurActivity.class);
                            intent.putExtra("idControleur", user.getIdpers());
                            startActivity(intent);
                        }
                    } else {
                        Toast.makeText(MainActivity2.this, "Email ou mot de passe incorrect", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Personnel> call, Throwable t) {
                    Toast.makeText(MainActivity2.this, "Erreur de connexion", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}