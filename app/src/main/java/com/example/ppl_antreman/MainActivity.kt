@file:Suppress("PackageDirectoryMismatch")

package com.pplantreman.app

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.view.View
import android.widget.TextView
import android.widget.Toast
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private var currentFragmentId = R.id.nav_beslenme
    private val noteViewModel: NoteViewModel by viewModels {
        NoteViewModelFactory((application as PplAntremanApplication).repository)
    }

    // Permission launcher for Push Notifications
    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                enqueueDailyMotivationWork()
            } else {
                Toast.makeText(this, "Bildirim izni reddedildi. Motivasyon mesajları kapalı.", Toast.LENGTH_SHORT).show()
            }
        }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState != null) {
            currentFragmentId = savedInstanceState.getInt("KEY_CURRENT_FRAGMENT_ID", R.id.nav_beslenme)
        }

        // Observe motivational quotes
        lifecycleScope.launch {
             noteViewModel.milestoneEvent.collect { message ->
                 Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
             }
        }

        // 1.5. Notification check and Worker setup
        checkNotificationPermissionAndSchedule()

        // 1.6. In-App Otomatik Güncelleme Kontrolü
        checkForAppUpdates()

        // 2. Setup Global UI
        val tvDailyMotivation = findViewById<TextView>(R.id.tvDailyMotivation)

        // 3. Set Daily Motivation
        tvDailyMotivation.text = "\"${noteViewModel.getRandomQuote()}\""

        // 4. Setup Mimi AI Companion (Sağ Köşede Küçük Yuvarlak)
        val cardMimiFab = findViewById<View>(R.id.cardMimiFab)
        val cardMimiSpeechBubble = findViewById<View>(R.id.cardMimiSpeechBubble)
        val tvMimiSpeech = findViewById<TextView>(R.id.tvMimiSpeech)
        val btnDismissBubble = findViewById<View>(R.id.btnDismissBubble)
        val mimiHandler = Handler(Looper.getMainLooper())

        fun showMimiSpeech(tabId: Int) {
            val speech = MimiAiAssistant.getTabSpeech(tabId)
            tvMimiSpeech.text = speech
            cardMimiSpeechBubble.visibility = View.VISIBLE
            cardMimiSpeechBubble.alpha = 0f
            cardMimiSpeechBubble.translationY = 16f
            cardMimiSpeechBubble.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(300)
                .start()

            // 7 saniye sonra baloncuğu otomatik gizle
            mimiHandler.removeCallbacksAndMessages(null)
            mimiHandler.postDelayed({
                cardMimiSpeechBubble.animate()
                    .alpha(0f)
                    .translationY(10f)
                    .setDuration(300)
                    .withEndAction { cardMimiSpeechBubble.visibility = View.GONE }
                    .start()
            }, 7000)
        }

        // Açılışta Mimi'nin ilk hatırlatmasını göster
        showMimiSpeech(currentFragmentId)

        // Küçük yuvarlak Mimi butonuna tıklanınca doğrudan sohbet penceresini aç
        cardMimiFab.setOnClickListener {
            MimiAiAssistant.showChatDialog(this)
        }

        // Baloncuğa tıklanınca da sohbet penceresini aç
        cardMimiSpeechBubble.setOnClickListener {
            MimiAiAssistant.showChatDialog(this)
        }

        // Kapatma butonuna basıldığında baloncuğu gizle
        btnDismissBubble.setOnClickListener {
            mimiHandler.removeCallbacksAndMessages(null)
            cardMimiSpeechBubble.animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction { cardMimiSpeechBubble.visibility = View.GONE }
                .start()
        }

        // İlk açılışta Beslenme
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, BeslenmeFragment())
                .commitAllowingStateLoss()
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        // Handle Back navigation to first tab before exiting app
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (currentFragmentId != R.id.nav_beslenme) {
                    bottomNav.selectedItemId = R.id.nav_beslenme
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        bottomNav.setOnItemSelectedListener { item ->
            
            // Eğer zaten seçili fragment'taysa hiçbir şey yapma
            if (item.itemId == currentFragmentId) {
                return@setOnItemSelectedListener true
            }

            val fragment = when (item.itemId) {
                R.id.nav_beslenme -> BeslenmeFragment()
                R.id.nav_istatistik -> IstatistikFragment()
                R.id.nav_antrenman -> AntrenmanFragment()
                R.id.nav_araclar -> AraclarFragment()
                else -> null
            }

            fragment?.let {
                // Animasyon yönünü belirle
                // Tab sırası: Beslenme (0) -> İstatistik (1) -> Antrenman (2) -> Araçlar (3)
                val tabOrder = mapOf(
                    R.id.nav_beslenme to 0,
                    R.id.nav_istatistik to 1,
                    R.id.nav_antrenman to 2,
                    R.id.nav_araclar to 3
                )
                
                val currentIndex = tabOrder[currentFragmentId] ?: 0
                val newIndex = tabOrder[item.itemId] ?: 0
                val isMovingRight = newIndex > currentIndex
                
                val (enterAnim, exitAnim, popEnterAnim, popExitAnim) = if (isMovingRight) {
                    // Sağa giderken
                    arrayOf(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                    )
                } else {
                    // Sola giderken
                    arrayOf(
                        R.anim.slide_in_left,
                        R.anim.slide_out_right,
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                    )
                }

                supportFragmentManager.beginTransaction()
                    .setCustomAnimations(enterAnim, exitAnim, popEnterAnim, popExitAnim)
                    .replace(R.id.fragmentContainer, it)
                    .commitAllowingStateLoss()
                
                // Mevcut fragment ID'yi güncelle
                currentFragmentId = item.itemId

                // Mimi yeni sekmeye göre konuşur ve tavsiye verir
                showMimiSpeech(item.itemId)
            }
            true
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("KEY_CURRENT_FRAGMENT_ID", currentFragmentId)
    }

    private fun checkNotificationPermissionAndSchedule() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // İzin zaten var
                    enqueueDailyMotivationWork()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Kullanıcıya iznin neden gerektiğini açıklayabilirdik, şimdilik doğrudan isteyelim
                    requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    // İzni iste
                    requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // Android 12 ve öncesi için izne gerek yok, direkt zamanla
            enqueueDailyMotivationWork()
        }
    }

    private fun enqueueDailyMotivationWork() {
        // Her 24 saatte bir çalışacak arka plan görevi (Periodic Work Request)
        val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyMotivationWorker>(24, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "DailyMotivationWork", // Benzersiz bir isim
            ExistingPeriodicWorkPolicy.KEEP, // Zaten varsa yenisini oluşturma
            dailyWorkRequest
        )
    }

    /**
     * Uygulama içi güncelleme kontrolü
     * Buradaki URL'yi kendi GitHub Raw veya sunucu linkinizle değiştirebilirsiniz.
     */
    private fun checkForAppUpdates() {
        // Örnek: "https://raw.githubusercontent.com/KULLANICI_ADI/PPL_ANTREMAN/main/update.json"
        val updateJsonUrl = "https://raw.githubusercontent.com/Kose/PPL_ANTREMAN/main/update.json"
        
        // Sunucu linki henüz ayarlanmadıysa veya geçersizse sessiz kalır
        if (updateJsonUrl.contains("KULLANICI_ADI") || updateJsonUrl.contains("example.com")) {
            return
        }

        AppUpdateManager.checkForUpdate(this, updateJsonUrl, isManualCheck = false)
    }
}
