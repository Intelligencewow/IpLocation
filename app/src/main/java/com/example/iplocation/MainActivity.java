package com.example.iplocation;

import android.content.Context;

import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements NetworkStateListener {
    private MapView mapView;
    private GoogleMap googleMap;
    private static final String BASE_URL = "http://ip-api.com/";
    private IpInformation ipInformation;
    private TextView country;
    private TextView region;
    private TextView city;
    private TextView isp;
    private TextInputEditText ip;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);


        mapView = findViewById(R.id.mapView);
        country = findViewById(R.id.CountryText);
        region = findViewById(R.id.RegionText);
        city = findViewById(R.id.CityText);
        isp = findViewById(R.id.IspText);
        ip = findViewById(R.id.TextInputIP);
        TextInputLayout textInputLayout = findViewById(R.id.textInputLayout);


        ip.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                makeRequestToGetIpInfo();
                hideKeyboad(v);
                return true;
            }
            return false;
        });
        textInputLayout.setEndIconOnClickListener(v -> {
            makeRequestToGetIpInfo();
            hideKeyboad(v);
        });

        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(map -> {
            googleMap = map;
            if (ipInformation != null) {
                LatLng location = new LatLng(ipInformation.getLat(), ipInformation.getLon());
                googleMap.addMarker(new MarkerOptions().position(location));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        NetworkHandler networkHandler = new NetworkHandler(this);
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkHandler, filter);
        mapView.onResume();

    }

    @Override
    public void onNetworkStateChanged(String networkType) {
        TextView internetState = findViewById(R.id.InternetState);
        ImageView internetStateIcon = findViewById(R.id.internetStateImage);

        if (networkType.equals("Wifi")) {
            internetState.setText(R.string.Wifi);
            internetStateIcon.setImageResource(R.drawable.wifi_24dp_000000_fill0_wght400_grad0_opsz24);

        } else if (networkType.equals("Dados_moveis")) {
            internetState.setText(R.string.Dados_moveis);
            internetStateIcon.setImageResource(R.drawable.signal_cellular_alt_24dp_000000_fill0_wght400_grad0_opsz24);

        } else {
            internetState.setText(R.string.Sem_conexão);
            internetStateIcon.setImageResource(R.drawable.wifi_off_24dp_000000_fill0_wght400_grad0_opsz24);

        }

    }

    private void getIpInfo(ApiService apiService, String ip) {
        Call<IpInformation> call = apiService.getIpInfo(ip);

        call.enqueue(new Callback<IpInformation>() {
            @Override
            public void onResponse( Call<IpInformation> call, Response<IpInformation> response) {
                if (response.isSuccessful()) {

                    Log.i("MainActivity", "onResponse:" + response.body().toString());
                    ipInformation = response.body();

                    if (ipInformation.getStatus().equals("success")) {
                        updateMapLocation();
                        setInfoOnActivity();
                    } else {

                        Log.i("MainActivity", "FALHOU");
                        AlertDialog dialog = createAlertDialog();
                        dialog.show();

                    }
                    Log.d("MainActivity", "lat: " + ipInformation.getCountry());
                    Log.d("MainActivity", "Query: " + ipInformation.getQuery());
                    Log.d("MainActivity", "Status: " + ipInformation.getStatus());
                    Log.d("MainActivity", "response Code: " + response.code());
                } else {
                    Log.e("MainActivity", "Erro na resposta da API. Código: " + response.code());
                    try {
                        String errorBody = response.errorBody().string(); // Captura o corpo da resposta de erro
                        Log.e("MainActivity", "Corpo de erro: " + errorBody);
                    } catch (IOException e) {
                        Log.e("MainActivity", "Erro ao ler o corpo de erro: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<IpInformation> call, Throwable t) {
                Log.e("MainActivity", "Falha na chamada da API: " + t.getMessage());
            }
        });
    }

    private void setInfoOnActivity() {
        country.setText(ipInformation.getCountry());
        region.setText(ipInformation.getRegionName());
        city.setText(ipInformation.getCity());
        isp.setText(ipInformation.getIsp());
    }

    private void updateMapLocation() {
        LatLng newLocation = new LatLng(ipInformation.getLat(), ipInformation.getLon());
        if (googleMap != null) {
            googleMap.addMarker(new MarkerOptions().position(newLocation));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLocation, 15));
        }
    }


    public void hideKeyboad(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public AlertDialog createAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Endereço ip inválido")
                .setMessage("Você digitou um endereço de ip inválido");
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        return builder.create();
    }

    public void makeRequestToGetIpInfo() {
        String ipToQuery = ip.getText().toString();
        Log.i("Ip", "makeRequestToGetIpInfo: " + ipToQuery);
        getIpInfo(apiService, ipToQuery);
    }
}