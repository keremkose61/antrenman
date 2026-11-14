@file:Suppress("PackageDirectoryMismatch")

package com.pplantreman.app

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val downloadUrl: String,
    val releaseNotes: String,
    val isForceUpdate: Boolean = false
)

object AppUpdateManager {

    private var downloadJob: Job? = null

    /**
     * Sunucudan veya GitHub'dan update.json dosyasını kontrol eder.
     * @param activity Aktif AppCompatActivity
     * @param updateJsonUrl JSON dosyasının yayınlandığı doğrudan URL
     * @param isManualCheck Kullanıcı Ayarlar'dan "Güncellemeleri Denetle" butonuna bastıysa true verilir
     */
    fun checkForUpdate(
        activity: AppCompatActivity,
        updateJsonUrl: String,
        isManualCheck: Boolean = false
    ) {
        activity.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val connection = (URL(updateJsonUrl).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 10000
                    readTimeout = 10000
                    requestMethod = "GET"
                    setRequestProperty("Accept", "application/json")
                }

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(jsonString)

                    val remoteVersionCode = json.optInt("versionCode", 0)
                    val remoteVersionName = json.optString("versionName", "Bilinmeyen Sürüm")
                    val downloadUrl = json.optString("downloadUrl", "")
                    val releaseNotes = json.optString("releaseNotes", "Performans iyileştirmeleri ve hata düzeltmeleri yapıldı.")
                    val isForceUpdate = json.optBoolean("isForceUpdate", false)

                    val currentVersionCode = getCurrentVersionCode(activity)

                    val updateInfo = UpdateInfo(
                        versionCode = remoteVersionCode,
                        versionName = remoteVersionName,
                        downloadUrl = downloadUrl,
                        releaseNotes = releaseNotes,
                        isForceUpdate = isForceUpdate
                    )

                    withContext(Dispatchers.Main) {
                        if (!activity.isFinishing && !activity.isDestroyed) {
                            if (remoteVersionCode > currentVersionCode && downloadUrl.isNotBlank()) {
                                showUpdateDialog(activity, updateInfo)
                            } else if (isManualCheck) {
                                Toast.makeText(
                                    activity,
                                    "Uygulamanız güncel! (Sürüm: ${getCurrentVersionName(activity)})",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                } else if (isManualCheck) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(activity, "Güncelleme sunucusuna erişilemedi (${connection.responseCode})", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                if (isManualCheck) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(activity, "Güncelleme kontrolü başarısız: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    /**
     * Yeni güncelleme bulunduğunda kullanıcıya gösterilen bilgilendirme penceresi
     */
    private fun showUpdateDialog(activity: AppCompatActivity, updateInfo: UpdateInfo) {
        val currentVersion = getCurrentVersionName(activity)
        val message = "Mevcut Sürüm: v$currentVersion\nYeni Sürüm: v${updateInfo.versionName}\n\n" +
                "Yenilikler:\n${updateInfo.releaseNotes}\n\n" +
                "Şimdi güncellemek istiyor musunuz?"

        val dialogBuilder = MaterialAlertDialogBuilder(activity)
            .setTitle("🚀 Yeni Güncelleme Mevcut!")
            .setMessage(message)
            .setCancelable(!updateInfo.isForceUpdate)
            .setPositiveButton("Güncelle") { _, _ ->
                checkInstallPermissionAndDownload(activity, updateInfo)
            }

        if (!updateInfo.isForceUpdate) {
            dialogBuilder.setNegativeButton("Daha Sonra", null)
        }

        dialogBuilder.show()
    }

    /**
     * Android 8.0+ için bilinmeyen kaynaktan APK yükleme iznini kontrol eder
     */
    private fun checkInstallPermissionAndDownload(activity: AppCompatActivity, updateInfo: UpdateInfo) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!activity.packageManager.canRequestPackageInstalls()) {
                MaterialAlertDialogBuilder(activity)
                    .setTitle("Yükleme İzni Gerekli")
                    .setMessage("Uygulamanın yeni sürümünü yükleyebilmek için lütfen 'Bilinmeyen uygulamaları yükle' iznini etkinleştirin.")
                    .setPositiveButton("Ayarları Aç") { _, _ ->
                        val intent = Intent(
                            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                            Uri.parse("package:${activity.packageName}")
                        )
                        activity.startActivity(intent)
                    }
                    .setNegativeButton("İptal", null)
                    .show()
                return
            }
        }

        startDownloadWithProgress(activity, updateInfo)
    }

    /**
     * APK'yı arka planda indirir ve kullanıcıya indirme yüzdesini gösterir
     */
    @SuppressLint("SetTextI18n")
    private fun startDownloadWithProgress(activity: AppCompatActivity, updateInfo: UpdateInfo) {
        // İlerleme Dialog'u oluştur
        val progressDialog = Dialog(activity).apply {
            setCancelable(false)
            val container = LinearLayout(activity).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(60, 50, 60, 40)
            }

            val titleView = TextView(activity).apply {
                text = "Güncelleme İndiriliyor..."
                textSize = 18f
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 0, 0, 20)
            }

            val progressBar = ProgressBar(activity, null, android.R.attr.progressBarStyleHorizontal).apply {
                isIndeterminate = false
                max = 100
                progress = 0
            }

            val percentText = TextView(activity).apply {
                text = "%0 indirildi"
                setPadding(0, 15, 0, 25)
            }

            val btnCancel = MaterialButton(activity).apply {
                text = "İptal Et"
                setOnClickListener {
                    downloadJob?.cancel()
                    dismiss()
                }
            }

            container.addView(titleView)
            container.addView(progressBar)
            container.addView(percentText)
            if (!updateInfo.isForceUpdate) {
                container.addView(btnCancel)
            }

            setContentView(container)
            show()
        }

        downloadJob = activity.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val url = URL(updateInfo.downloadUrl)
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 15000
                    readTimeout = 30000
                    requestMethod = "GET"
                }

                val fileLength = connection.contentLength
                val downloadsDir = activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                    ?: activity.cacheDir
                val apkFile = File(downloadsDir, "vexa_update_v${updateInfo.versionCode}.apk")

                if (apkFile.exists()) {
                    apkFile.delete()
                }

                connection.inputStream.use { input ->
                    FileOutputStream(apkFile).use { output ->
                        val buffer = ByteArray(8 * 1024)
                        var total: Long = 0
                        var count: Int
                        var lastProgress = 0

                        while (input.read(buffer).also { count = it } != -1) {
                            total += count
                            output.write(buffer, 0, count)

                            if (fileLength > 0) {
                                val currentProgress = ((total * 100) / fileLength).toInt()
                                if (currentProgress != lastProgress) {
                                    lastProgress = currentProgress
                                    withContext(Dispatchers.Main) {
                                        (progressDialog.window?.decorView as? ViewGroup)?.let { root ->
                                            findViewsAndSetProgress(root, currentProgress)
                                        }
                                    }
                                }
                            }
                        }
                        output.flush()
                    }
                }

                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    installApk(activity, apkFile)
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    if (!e.message.isNullOrBlank() && !e.message!!.contains("StandaloneCoroutine was cancelled")) {
                        Toast.makeText(activity, "İndirme hatası: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun findViewsAndSetProgress(viewGroup: ViewGroup, progress: Int) {
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            if (child is ProgressBar) {
                child.progress = progress
            } else if (child is TextView && child.text.toString().contains("indirildi")) {
                child.text = "%$progress indirildi"
            } else if (child is ViewGroup) {
                findViewsAndSetProgress(child, progress)
            }
        }
    }

    /**
     * İndirilen APK dosyasını Android Paket Yükleyicisi (Package Installer) ile açar
     */
    fun installApk(context: Context, apkFile: File) {
        if (!apkFile.exists()) {
            Toast.makeText(context, "APK dosyası bulunamadı!", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val apkUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(installIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "Yükleme başlatılamadı: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Cihazda kurulu olan mevcut uygulamanın versionCode değerini döner
     */
    fun getCurrentVersionCode(context: Context): Long {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }
        } catch (e: Exception) {
            1L
        }
    }

    /**
     * Cihazda kurulu olan mevcut uygulamanın versionName değerini döner (örn: "1.0")
     */
    fun getCurrentVersionName(context: Context): String {
        return try {
            val packageInfo: PackageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            packageInfo.versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }
    }
}
