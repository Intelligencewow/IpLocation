package com.example.iplocation;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
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
    private NetworkHandler networkHandler;
    private TextView internetState;
    private ImageView internetStateIcon;
    private static final String BASE_URL = "http://ip-api.com/";
    private IpApiResponse ipApiResponse;
    private TextView country, region, city, isp;
    private TextInputEditText ip;
    String ipToQuery;

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

        ApiService apiService = retrofit.create(ApiService.class);

        getIpInfo(apiService, ipToQuery);

        mapView = findViewById(R.id.mapView);
        country = findViewById(R.id.Country);
        region = findViewById(R.id.Region);
        city = findViewById(R.id.City);
        isp = findViewById(R.id.Isp);
        ip = findViewById(R.id.TextInputIP);
        TextInputLayout textInputLayout = findViewById(R.id.textInputLayout);


        ip.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {

                ipToQuery =  ip.getText().toString();
                return true;
            }
            return false;
        });
        textInputLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng teste = new LatLng(28.385,-81.563);
                updateMapLocation(teste);
                hideKeyboad(v);
            }
        });

        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap map) {
                //googleMap = map;
                //LatLng location = new LatLng(ipApiResponse.getLat(), ipApiResponse.getLon());
                //googleMap.addMarker(new MarkerOptions().position(location).title("Marker in Sydney"));
                //googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location,15));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        networkHandler = new NetworkHandler(this);
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkHandler, filter);
        mapView.onResume();

    }

    @Override
    public void onNetworkStateChanged(String networkType) {
        internetState = findViewById(R.id.InternetState);
        internetStateIcon = findViewById(R.id.internetStateImage);

        if (networkType.equals("Wifi")){
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

    private void getIpInfo(ApiService apiService, String ip){
        Call<IpApiResponse> call = apiService.getIpInfo(ip);

        call.enqueue(new Callback<IpApiResponse>() {
            @Override
            public void onResponse(Call<IpApiResponse> call, Response<IpApiResponse> response) {
                if (response.isSuccessful()){
                    ipApiResponse = response.body();
                    setInfoOnActivity();

                    Log.d("MainActivity", "lat: " + ipApiResponse.getCountry());
                }  else {
                    Log.e("MainActivity", "Erro na resposta da API. Código: " + response.code());
                    Log.e("MainActivity", "Mensagem: " + response.message());
                    try {
                        String errorBody = response.errorBody().string(); // Captura o corpo da resposta de erro
                        Log.e("MainActivity", "Corpo de erro: " + errorBody);
                    } catch (IOException e) {
                        Log.e("MainActivity", "Erro ao ler o corpo de erro: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<IpApiResponse> call, Throwable t) {
                Log.e("MainActivity", "Falha na chamada da API: " + t.getMessage());
            }
        });
    }

    private void setInfoOnActivity(){
        country.setText(ipApiResponse.getCountry());
        region.setText(ipApiResponse.getRegionName());
        city.setText(ipApiResponse.getCity());
        isp.setText(ipApiResponse.getIsp());
    }

    private  void updateMapLocation(LatLng newLocation){
        googleMap.addMarker(new MarkerOptions().position(newLocation));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLocation, 15));
    }

    public void hideKeyboad(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null){
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}