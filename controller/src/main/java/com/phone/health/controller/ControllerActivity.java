package com.phone.health.controller;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ControllerActivity extends AppCompatActivity {

    private static final List<String> executionLogs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addLog("Контроллер запущен. Ожидание связи с устройствами.");
        showDeviceList();
    }

    private void addLog(String message) {
        String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        String logEntry = "[" + time + "] " + message;
        executionLogs.add(logEntry);
    }

    private void showLogsDialog() {
        StringBuilder sb = new StringBuilder();
        for (String log : executionLogs) {
            sb.append(log).append("\n\n");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Системные логи Controller");
        builder.setMessage(sb.length() > 0 ? sb.toString().trim() : "Логи пока пусты.");

        builder.setPositiveButton("Копировать логи", (dialog, which) -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("ControllerLogs", sb.toString());
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
                Toast.makeText(ControllerActivity.this, "Логи успешно скопированы в буфер обмена", Toast.LENGTH_SHORT).show();
                addLog("Логи скопированы пользователем в буфер обмена.");
            }
        });

        builder.setNegativeButton("Закрыть", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showDeviceList() {
        RelativeLayout rootLayout = new RelativeLayout(this);
        rootLayout.setPadding(30, 30, 30, 30);
        rootLayout.setBackgroundColor(android.graphics.Color.parseColor("#121212"));

        Button btnLogs = new Button(this);
        btnLogs.setText("Логи");
        btnLogs.setTextColor(android.graphics.Color.WHITE);
        btnLogs.setBackgroundColor(android.graphics.Color.parseColor("#333333"));
        RelativeLayout.LayoutParams logParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        logParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        logParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        btnLogs.setLayoutParams(logParams);
        btnLogs.setOnClickListener(v -> {
            addLog("Открытие логов из главного меню.");
            showLogsDialog();
        });
        rootLayout.addView(btnLogs);

        LinearLayout contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setGravity(Gravity.CENTER);
        RelativeLayout.LayoutParams contentParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        );
        contentParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        contentLayout.setLayoutParams(contentParams);

        TextView title = new TextView(this);
        title.setText("Phone Health Controller");
        title.setTextSize(22);
        title.setTextColor(android.graphics.Color.WHITE);
        title.setGravity(Gravity.CENTER);
        contentLayout.addView(title);

        TextView status = new TextView(this);
        status.setText("🟢 Активные устройства в сети:");
        status.setTextSize(16);
        status.setTextColor(android.graphics.Color.parseColor("#AAAAAA"));
        status.setGravity(Gravity.CENTER);
        status.setPadding(0, 20, 0, 40);
        contentLayout.addView(status);

        Button deviceBtn = new Button(this);
        deviceBtn.setText("📱 Samsung J200F (Онлайн)");
        deviceBtn.setTextColor(android.graphics.Color.WHITE);
        deviceBtn.setBackgroundColor(android.graphics.Color.parseColor("#333333"));
        contentLayout.addView(deviceBtn);

        rootLayout.addView(contentLayout);
        setContentView(rootLayout);

        deviceBtn.setOnClickListener(v -> {
            addLog("Выбрано устройство: Samsung J200F");
            showDeviceMenu("Samsung J200F");
        });
    }

    private void showDeviceMenu(String deviceName) {
        RelativeLayout rootLayout = new RelativeLayout(this);
        rootLayout.setPadding(30, 30, 30, 30);
        rootLayout.setBackgroundColor(android.graphics.Color.parseColor("#121212"));

        Button btnLogs = new Button(this);
        btnLogs.setText("Логи");
        btnLogs.setTextColor(android.graphics.Color.WHITE);
        btnLogs.setBackgroundColor(android.graphics.Color.parseColor("#333333"));
        RelativeLayout.LayoutParams logParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        logParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        logParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        btnLogs.setLayoutParams(logParams);
        btnLogs.setOnClickListener(v -> {
            addLog("Открытие логов из меню управления (" + deviceName + ").");
            showLogsDialog();
        });
        rootLayout.addView(btnLogs);

        LinearLayout menuLayout = new LinearLayout(this);
        menuLayout.setOrientation(LinearLayout.VERTICAL);
        menuLayout.setGravity(Gravity.CENTER);
        RelativeLayout.LayoutParams contentParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        );
        contentParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        menuLayout.setLayoutParams(contentParams);

        TextView menuTitle = new TextView(this);
        menuTitle.setText("Управление: " + deviceName);
        menuTitle.setTextSize(20);
        menuTitle.setTextColor(android.graphics.Color.WHITE);
        menuTitle.setGravity(Gravity.CENTER);
        menuTitle.setPadding(0, 0, 0, 30);
        menuLayout.addView(menuTitle);

        Button btnControl = new Button(this);
        btnControl.setText("🎮 Управление устройством");
        btnControl.setTextColor(android.graphics.Color.WHITE);
        btnControl.setBackgroundColor(android.graphics.Color.parseColor("#333333"));
        menuLayout.addView(btnControl);

        Button btnCamera = new Button(this);
        btnCamera.setText("📷 Камера + Звук (Выкл)");
        btnCamera.setTextColor(android.graphics.Color.WHITE);
        btnCamera.setBackgroundColor(android.graphics.Color.parseColor("#333333"));
        menuLayout.addView(btnCamera);

        Button btnLocation = new Button(this);
        btnLocation.setText("📍 Узнать местоположение");
        btnLocation.setTextColor(android.graphics.Color.WHITE);
        btnLocation.setBackgroundColor(android.graphics.Color.parseColor("#333333"));
        menuLayout.addView(btnLocation);

        Button btnBack = new Button(this);
        btnBack.setText("⬅ Назад к списку");
        btnBack.setTextColor(android.graphics.Color.WHITE);
        btnBack.setBackgroundColor(android.graphics.Color.parseColor("#333333"));
        menuLayout.addView(btnBack);

        rootLayout.addView(menuLayout);
        setContentView(rootLayout);

        btnControl.setOnClickListener(v -> {
            addLog("Нажатие: Управление устройством (" + deviceName + ")");
            Toast.makeText(this, "Запуск Scrcpy-трансляции экрана " + deviceName, Toast.LENGTH_SHORT).show();
        });

        final boolean[] audioOn = {false};
        btnCamera.setOnClickListener(v -> {
            audioOn[0] = !audioOn[0];
            if (audioOn[0]) {
                btnCamera.setText("📷 Камера + Звук (🔊 ВКЛ)");
                addLog("Включение аудиопотока с микрофона для " + deviceName);
                Toast.makeText(this, "Микрофон активирован: прослушка включена", Toast.LENGTH_SHORT).show();
            } else {
                btnCamera.setText("📷 Камера + Звук (🔇 ВЫКЛ)");
                addLog("Отключение аудиопотока с микрофона для " + deviceName);
                Toast.makeText(this, "Микрофон отключен", Toast.LENGTH_SHORT).show();
            }
        });

        btnLocation.setOnClickListener(v -> {
            String currentTime = new SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.getDefault()).format(new Date());
            addLog("Запрос геолокации для " + deviceName + " успешен.");
            Toast.makeText(this, "📍 Точка получена (" + currentTime + "):\nШостка, район центра", Toast.LENGTH_LONG).show();
        });

        btnBack.setOnClickListener(v -> {
            addLog("Возврат к списку устройств.");
            showDeviceList();
        });
    }
}
