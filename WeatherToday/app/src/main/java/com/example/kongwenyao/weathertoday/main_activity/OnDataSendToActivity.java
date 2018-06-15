package com.example.kongwenyao.weathertoday.main_activity;

import org.json.JSONException;

import java.io.IOException;
import java.util.Map;

public interface OnDataSendToActivity {
    void onDataTodaySend(String[] data) throws IOException, JSONException;
    void onDataHourlySend(Map<String, String[]> data) throws IOException, JSONException;
}
