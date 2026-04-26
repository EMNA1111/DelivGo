package com.example.delivgo;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.delivgo.api.RetrofitHelper;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ControleurActivity extends AppCompatActivity {

    TextView textTotal, textLivrees, textAnnulees, textRestantes;
    BarChart chartLivreurs, chartClients;
    int idControleur;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controleur);

        textTotal     = findViewById(R.id.textView7);
        textLivrees   = findViewById(R.id.textView9);
        textAnnulees  = findViewById(R.id.textView11);
        textRestantes = findViewById(R.id.textView13);
        chartLivreurs = findViewById(R.id.chartLivreurs);
        chartClients  = findViewById(R.id.chartClients);

        idControleur = getIntent().getIntExtra("idControleur", 2);

        findViewById(R.id.btnNavLivraisons).setOnClickListener(v -> {
            startActivity(new Intent(this, LivraisonsControleurActivity.class));
        });

        findViewById(R.id.btnRetour).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity2.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        findViewById(R.id.imageView18).setOnClickListener(v -> {
            Intent intent = new Intent(this, MessagerieControleurActivity.class);
            intent.putExtra("idUser", idControleur);
            intent.putExtra("idInterlocuteur", 3);
            startActivity(intent);
        });

        chargerStats();
        chargerTopLivreurs();
        chargerTopClients();
    }

    private void chargerStats() {
        RetrofitHelper.getService().getStatsControleur().enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> stats = response.body();
                    textTotal.setText(String.valueOf(((Double) stats.get("total")).intValue()));
                    textLivrees.setText(String.valueOf(((Double) stats.get("livrees")).intValue()));
                    textAnnulees.setText(String.valueOf(((Double) stats.get("annulees")).intValue()));
                    textRestantes.setText(String.valueOf(((Double) stats.get("restantes")).intValue()));
                }
            }
            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(ControleurActivity.this, "Erreur : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void chargerTopLivreurs() {
        RetrofitHelper.getService().getTopLivreurs().enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Map<String, Object>> data = response.body();
                    List<BarEntry> livreesEntries = new ArrayList<>();
                    List<String> noms = new ArrayList<>();

                    for (int i = 0; i < data.size(); i++) {
                        Map<String, Object> item = data.get(i);
                        livreesEntries.add(new BarEntry(i, ((Number) item.get("livrees")).floatValue()));
                        noms.add((String) item.get("nomLivreur"));
                    }

                    afficherGraphique(chartLivreurs, livreesEntries, noms);
                }
            }
            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {}
        });
    }

    private void chargerTopClients() {
        RetrofitHelper.getService().getTopClients().enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Map<String, Object>> data = response.body();
                    List<BarEntry> livreesEntries = new ArrayList<>();
                    List<String> noms = new ArrayList<>();

                    for (int i = 0; i < data.size(); i++) {
                        Map<String, Object> item = data.get(i);
                        livreesEntries.add(new BarEntry(i, ((Number) item.get("livrees")).floatValue()));
                        noms.add((String) item.get("nomClient"));
                    }

                    afficherGraphique(chartClients, livreesEntries, noms);
                }
            }
            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {}
        });
    }

    private void afficherGraphique(BarChart chart, List<BarEntry> livrees, List<String> noms) {
        BarDataSet setLivrees = new BarDataSet(livrees, "Livrées");
        setLivrees.setColor(Color.parseColor("#4CAF50"));
        setLivrees.setValueTextSize(12f);

        BarData barData = new BarData(setLivrees);
        barData.setBarWidth(0.5f);

        chart.setData(barData);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setFitBars(true);
        chart.getAxisRight().setEnabled(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(noms));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setLabelRotationAngle(-20f);
        xAxis.setTextSize(10f);
        xAxis.setLabelCount(noms.size());

        chart.setExtraBottomOffset(20f);
        chart.invalidate();
    }
}