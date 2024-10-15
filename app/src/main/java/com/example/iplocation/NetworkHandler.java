package com.example.iplocation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;

public class NetworkHandler extends BroadcastReceiver {

    private NetworkStateListener listener;

    public NetworkHandler(NetworkStateListener listener){
        this.listener = listener;
    }
    public static String getNetworkType(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());

        if (networkCapabilities != null){
            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)){
                return "Wifi";
            } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                return "Dados_moveis";
            }
        }
        return "Sem_conexão";

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String networkType = getNetworkType(context);
        if (listener != null){
            listener.onNetworkStateChanged(networkType);
        }
    }
}
