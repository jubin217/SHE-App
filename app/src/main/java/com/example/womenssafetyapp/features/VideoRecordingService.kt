package com.example.womenssafetyapp.features

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.util.Log
import android.view.Surface
import androidx.core.app.NotificationCompat
import com.example.womenssafetyapp.R
import com.example.womenssafetyapp.dashboard.MainDashboardActivity
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VideoRecordingService : Service() {

    private lateinit var cameraManager: CameraManager
    private lateinit var mediaRecorder: MediaRecorder
    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var isRecording = false
    private var outputFile: File? = null

    override fun onCreate() {
        super.onCreate()
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        mediaRecorder = MediaRecorder()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(VIDEO_NOTIFICATION_ID, createNotification())

        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            startRecording()
        } else {
            Log.e("VideoRecording", "Permissions not granted")
            stopSelf()
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startRecording() {
        try {
            outputFile = createVideoFile()

            mediaRecorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setVideoSource(MediaRecorder.VideoSource.SURFACE)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                setVideoSize(1280, 720)
                setVideoFrameRate(30)
                setVideoEncodingBitRate(5000000)
                setOutputFile(outputFile?.absolutePath)
                prepare()
                start()
            }

            isRecording = true
            Log.d("VideoRecording", "Started recording to: ${outputFile?.absolutePath}")

        } catch (e: Exception) {
            Log.e("VideoRecording", "Failed to start recording: ${e.message}")
            stopSelf()
        }
    }

    private fun createVideoFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_MOVIES)
        return File.createTempFile(
            "EMERGENCY_VIDEO_${timeStamp}_",
            ".mp4",
            storageDir
        )
    }

    private fun stopRecording() {
        if (isRecording) {
            try {
                mediaRecorder.stop()
                mediaRecorder.reset()
                isRecording = false

                // Save file info to database
                saveVideoRecord(outputFile)

                Log.d("VideoRecording", "Stopped recording")
            } catch (e: Exception) {
                Log.e("VideoRecording", "Failed to stop recording: ${e.message}")
            }
        }

        releaseCamera()
    }

    private fun saveVideoRecord(file: File?) {
        // Save video metadata to database for later retrieval
        if (file != null && file.exists()) {
            // Could save to Firestore with encrypted path
        }
    }

    private fun releaseCamera() {
        captureSession?.close()
        captureSession = null
        cameraDevice?.close()
        cameraDevice = null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                VIDEO_CHANNEL_ID,
                "Video Recording",
                NotificationManager.IMPORTANCE_LOW
            )
            channel.description = "Recording emergency video"

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainDashboardActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, VIDEO_CHANNEL_ID)
            .setContentTitle("📹 Recording Emergency Video")
            .setContentText("Video is being recorded for evidence")
            .setSmallIcon(R.drawable.ic_safety)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRecording()
    }

    companion object {
        private const val VIDEO_CHANNEL_ID = "video_recording_channel"
        private const val VIDEO_NOTIFICATION_ID = 103
    }
}