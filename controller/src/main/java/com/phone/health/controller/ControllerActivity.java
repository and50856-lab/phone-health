package com.phone.health.controller;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class ControllerActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 200;
    private SharedPreferences sharedPreferences;
    private LinearLayout devicesContainer;
    private TextView titleHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences("ControllerPrefs", Context.MODE_PRIVATE);
        showDeviceListScreen();
    }

    // --- ЭКРАН 1: СПИСОК УСТРОЙСТВ И ДОБАВЛЕНИЕ ---
    private void showDeviceListScreen() {
        LinearLayout rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        rootLayout.setPadding(40, 50, 40, 40);
        rootLayout.setBackgroundColor(android.graphics.Color.parseColor("#121212"));

        // Надпись по центру вверху (всегда присутствует)
        titleHeader = new TextView(this);
        titleHeader.setText("Мои устройства");
        titleHeader.setTextSize(24);
        titleHeader.setTextColor(android.graphics.Color.WHITE);
        titleHeader.setGravity(Gravity.CENTER);
        titleHeader.setPadding(0, 0, 0, 30);
        rootLayout.addView(titleHeader);

        // Кнопка / плашка "Добавить новое"
        Button btnAddDevice = new Button(this);
        btnAddDevice.setText("+ Добавить новое устройство");
        btnAddDevice.setTextColor(android.graphics.Color.WHITE);
        btnAddDevice.setBackgroundColor(android.graphics.Color.parseColor("#2196F3"));
        btnAddDevice.setPadding(20, 10, 20, 10);
        rootLayout.addView(btnAddDevice);

        btnAddDevice.setOnClickListener(v -> showAddDeviceOptionsDialog());

        // Контейнер для списка добавленных устройств с прокруткой
        ScrollView scrollView = new ScrollView(this);
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        scrollView.setPadding(0, 30, 0, 0);

        devicesContainer = new LinearLayout(this);
        devicesContainer.setOrientation(LinearLayout.VERTICAL);
        devicesContainer.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        scrollView.addView(devicesContainer);

        rootLayout.addView(scrollView);
        setContentView(rootLayout);

        // Загружаем и отображаем сохраненные устройства
        loadAndDisplayDevices();
    }

    private void loadAndDisplayDevices() {
        devicesContainer.removeAllViews();
        Set<String> devices = sharedPreferences.getStringSet("saved_devices", new HashSet<>());

        if (devices.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText("Список пуст. Нажмите «Добавить новое».");
            emptyText.setTextColor(android.graphics.Color.parseColor("#888888"));
            emptyText.setPadding(0, 40, 0, 0);
            devicesContainer.addView(emptyText);
            return;
        }

        for (String deviceId : devices) {
            Button devBtn = new Button(this);
            devBtn.setText("📱 Устройство: " + deviceId);
            devBtn.setTextColor(android.graphics.Color.WHITE);
            devBtn.setBackgroundColor(android.graphics.Color.parseColor("#333333"));
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 140
            );
            params.setMargins(0, 15, 0, 15);
            devBtn.setLayoutParams(params);

            devBtn.setOnClickListener(v -> showDeviceMenu(deviceId));
            devicesContainer.addView(devBtn);
        }
    }

    // --- ДИАЛОГ ВЫБОРА ДОБАВЛЕНИЯ (QR или ID) ---
    private void showAddDeviceOptionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Добавление устройства");
        builder.setItems(new CharSequence[]{
                "Сканировать QR-код с помощью камеры",
                "Добавить с помощью ID"
        }, (dialog, which) -> {
            if (which == 0) {
                // Сканирование QR-кода
                checkCameraPermissionAndScan();
            } else {
                // Ввод ID вручную
                showAddByIdDialog();
            }
        });
        builder.show();
    }

    private void checkCameraPermissionAndScan() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startQrScannerSimulation();
        } else {
            // Запрашиваем доступ к камере один раз
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startQrScannerSimulation();
            } else {
                Toast.makeText(this, "Доступ к камере отклонен", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startQrScannerSimulation() {
        // Здесь в будущем запускается активити сканирования QR. 
        // Для теста эмулируем успешное сканирование уникального ID:
        String scannedId = "Device_" + System.currentTimeMillis() % 10000;
        saveNewDevice(scannedId);
        Toast.makeText(this, "QR успешно отсканирован! ID: " + scannedId, Toast.LENGTH_LONG).show();
    }

    private void showAddByIdDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ввод ID устройства");

        final EditText input = new EditText(this);
        input.setHint("Введите ID (например, J200F_1234)");
        input.setTextColor(android.graphics.Color.BLACK);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String deviceId = input.getText().toString().trim();
            if (!deviceId.isEmpty()) {
                saveNewDevice(deviceId);
                Toast.makeText(this, "Устройство " + deviceId + " успешно добавлено!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "ID не может быть пустым", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void saveNewDevice(String deviceId) {
        Set<String> devices = new HashSet<>(sharedPreferences.getStringSet("saved_devices", new HashSet<>()));
        devices.add(deviceId);
        sharedPreferences.edit().putStringSet("saved_devices", devices).apply();
        loadAndDisplayDevices();
    }

    // --- ЭКРАН 2: ПАНЕЛЬ УПРАВЛЕНИЯ 3 ФУНКЦИЯМИ ---
    private void showDeviceMenu(String deviceId) {
        LinearLayout menuLayout = new LinearLayout(this);
        menuLayout.setOrientation(LinearLayout.VERTICAL);
        menuLayout.setGravity(Gravity.CENTER);
        menuLayout.setPadding(40, 40, 40, 40);
        menuLayout.setBackgroundColor(android.graphics.Color.parseColor("#121212"));

        TextView menuTitle = new TextView(this);
        menuTitle.setText("Управление:\n" + deviceId);
        menuTitle.setTextSize(22);
        menuTitle.setTextColor(android.graphics.Color.WHITE);
        menuTitle.setGravity(Gravity.CENTER);
        menuTitle.setPadding(0, 0, 0, 40);
        menuLayout.addView(menuTitle);

        // Функция 1: Управление устройством
        Button btnControl = new Button(this);
        btnControl.setText("🎮 Управление устройством");
        btnControl.setTextColor(android.graphics.Color.WHITE);
        btnControl.setBackgroundColor(android.graphics.Color.parseColor("#333333"));
        menuLayout.addView(btnControl);

        // Функция 2: Камера + Звук
        Button btnCamera = new Button(this);
        btnCamera.setText("📷 Камера + Звук (Выкл)");
        btnCamera.setTextColor(android.graphics.Color.WHITE);
        btnCamera.setBackgroundColor(android.graphics.Color.parseColor("#333333"));
        menuLayout.addView(btnCamera);

        // Функция 3: Узнать местоположение
        Button btnLocation = new Button(this);
        btnLocation.setText("📍 Узнать местоположение");
        btnLocation.setTextColor(android.graphics.Color.WHITE);
        btnLocation.setBackgroundColor(android.graphics.Color.parseColor("#333333"));
        menuLayout.addView(btnLocation);

        // Кнопка назад к списку
        Button btnBack = new Button(this);
        btnBack.setText("⬅ Назад к списку");
        btnBack.setTextColor(android.graphics.Color.WHITE);
        btnBack.setBackgroundColor(android.graphics.Color.parseColor("#444444"));
        btnBack.setPadding(0, 40, 0, 0);
        menuLayout.addView(btnBack);

        setContentView(menuLayout);

        // Обработчики кнопок
        btnControl.setOnClickListener(v -> 
            Toast.makeText(this, "Запуск Scrcpy-экрана для " + deviceId, Toast.LENGTH_SHORT).show()
        );

        final boolean[] audioOn = {false};
        btnCamera.setOnClickListener(v -> {
            audioOn[0] = !audioOn[0];
            if (audioOn[0]) {
                btnCamera.setText("📷 Камера + Звук (🔊 ВКЛ)");
                Toast.makeText(this, "Прослушка микрофона включена", Toast.LENGTH_SHORT).show();
            } else {
                btnCamera.setText("📷 Камера + Звук (🔇 ВЫКЛ)");
                Toast.makeText(this, "Микрофон отключен", Toast.LENGTH_SHORT).show();
            }
        });

        btnLocation.setOnClickListener(v -> {
            String currentTime = new SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.getDefault()).format(new Date());
            Toast.makeText(this, "📍 Точка для " + deviceId + " (" + currentTime + "):\nШостка, центр", Toast.LENGTH_LONG).show();
        });

        btnBack.setOnClickListener(v -> showDeviceListScreen());
    }
}
