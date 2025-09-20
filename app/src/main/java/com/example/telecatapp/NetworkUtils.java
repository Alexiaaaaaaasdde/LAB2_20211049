package com.example.telecatapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.StrictMode;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkUtils {

    public static boolean hasInternet(Context context) {
        // 1. Verificar si hay red activa
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
        if (capabilities == null ||
                !(capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))) {
            return false;
        }

        // 2. Hacer ping sencillo a un endpoint de Google
        try {
            // ⚠️ Solo para pruebas rápidas, mejor en AsyncTask/Executor
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            HttpURLConnection urlc = (HttpURLConnection) (
                    new URL("https://clients3.google.com/generate_204").openConnection());
            urlc.setConnectTimeout(2000);
            urlc.connect();

            return (urlc.getResponseCode() == 204);
        } catch (IOException e) {
            return false;
        }
    }
}
