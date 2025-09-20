package com.example.telecatapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

public class NetworkUtils {
    // Ayuda LLM: ChatGPT (GPT-5), 19-Sep-2025
    public static boolean hasInternet(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        Network active = cm.getActiveNetwork();
        if (active == null) return false;
        NetworkCapabilities nc = cm.getNetworkCapabilities(active);
        return nc != null &&
                nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
    }
}