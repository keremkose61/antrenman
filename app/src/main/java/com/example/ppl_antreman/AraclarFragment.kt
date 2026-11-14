@file:Suppress("PackageDirectoryMismatch")

package com.pplantreman.app

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.ChipGroup
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AraclarFragment : Fragment(R.layout.fragment_araclar) {

    // Tab Panels
    private lateinit var panelPlate: MaterialCardView
    private lateinit var panelOneRm: MaterialCardView
    private lateinit var panelMacro: MaterialCardView
    private lateinit var panelHiit: MaterialCardView
    private lateinit var panelPhoto: MaterialCardView

    // HIIT Timer Variables
    private var hiitTimer: CountDownTimer? = null
    private var isHiitRunning = false
    private var currentRound = 1
    private val totalRounds = 8
    private var isWorkPhase = true
    private val workDurationSec = 20L
    private val restDurationSec = 10L

    // Progress Photo
    private var selectedImageUri: Uri? = null
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            view?.findViewById<ImageView>(R.id.ivProgressPhoto)?.setImageURI(it)
            view?.findViewById<TextView>(R.id.tvPhotoPlaceholder)?.visibility = View.GONE
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupChipNavigation(view)
        setupPlateCalculator(view)
        setupOneRmCalculator(view)
        setupMacroCalculator(view)
        setupHiitTimer(view)
        setupPhotoProgress(view)
    }

    private fun initViews(view: View) {
        panelPlate = view.findViewById(R.id.panelPlate)
        panelOneRm = view.findViewById(R.id.panelOneRm)
        panelMacro = view.findViewById(R.id.panelMacro)
        panelHiit = view.findViewById(R.id.panelHiit)
        panelPhoto = view.findViewById(R.id.panelPhoto)
    }

    private fun setupChipNavigation(view: View) {
        val chipGroup = view.findViewById<ChipGroup>(R.id.toolChipGroup)
        chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            val checkedId = checkedIds.firstOrNull() ?: R.id.chipPlate

            panelPlate.visibility = if (checkedId == R.id.chipPlate) View.VISIBLE else View.GONE
            panelOneRm.visibility = if (checkedId == R.id.chipOneRm) View.VISIBLE else View.GONE
            panelMacro.visibility = if (checkedId == R.id.chipMacro) View.VISIBLE else View.GONE
            panelHiit.visibility = if (checkedId == R.id.chipHiit) View.VISIBLE else View.GONE
            panelPhoto.visibility = if (checkedId == R.id.chipPhoto) View.VISIBLE else View.GONE
        }
    }

    // ==========================================
    // 1. PLAKA HESAPLAYICI (PLATE CALCULATOR)
    // ==========================================
    @SuppressLint("SetTextI18n")
    private fun setupPlateCalculator(view: View) {
        val etTarget = view.findViewById<EditText>(R.id.etTargetWeight)
        val etBar = view.findViewById<EditText>(R.id.etBarWeight)
        val btnCalculate = view.findViewById<Button>(R.id.btnCalculatePlates)
        val tvSideHeader = view.findViewById<TextView>(R.id.tvPlateSideHeader)
        val tvDetails = view.findViewById<TextView>(R.id.tvPlateDetails)
        val layoutVisual = view.findViewById<LinearLayout>(R.id.layoutVisualBar)

        btnCalculate.setOnClickListener {
            val target = etTarget.text.toString().toDoubleOrNull() ?: 85.0
            val bar = etBar.text.toString().toDoubleOrNull() ?: 20.0

            if (target < bar) {
                Toast.makeText(requireContext(), "Hedef ağırlık bar ağırlığından az olamaz!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val totalPlateWeight = target - bar
            val sideWeight = totalPlateWeight / 2.0
            tvSideHeader.text = "Her Tarafa: $sideWeight kg"

            val availablePlates = listOf(25.0, 20.0, 15.0, 10.0, 5.0, 2.5, 1.25)
            val plateColors = mapOf(
                25.0 to Color.parseColor("#E53935"), // Kırmızı
                20.0 to Color.parseColor("#1E88E5"), // Mavi
                15.0 to Color.parseColor("#FDD835"), // Sarı
                10.0 to Color.parseColor("#43A047"), // Yeşil
                5.0 to Color.parseColor("#ECEFF1"),  // Beyaz
                2.5 to Color.parseColor("#757575"),  // Gri
                1.25 to Color.parseColor("#424242")  // Koyu Gri
            )
            val plateHeights = mapOf(
                25.0 to 110,
                20.0 to 98,
                15.0 to 86,
                10.0 to 74,
                5.0 to 62,
                2.5 to 50,
                1.25 to 42
            )

            var remaining = sideWeight
            val resultCounts = mutableMapOf<Double, Int>()

            for (p in availablePlates) {
                val count = (remaining / p).toInt()
                if (count > 0) {
                    resultCounts[p] = count
                    remaining -= count * p
                }
            }

            layoutVisual.removeAllViews()

            if (resultCounts.isEmpty()) {
                tvDetails.text = "Herhangi bir plaka takılmasına gerek yok (Sadece Boş Bar)."
                return@setOnClickListener
            }

            // Visual Barbell Collar on Left
            val barCollar = View(requireContext()).apply {
                val params = LinearLayout.LayoutParams(dpToPx(14), dpToPx(30)).apply {
                    gravity = Gravity.CENTER_VERTICAL
                    marginEnd = dpToPx(3)
                }
                layoutParams = params
                val shape = GradientDrawable().apply {
                    cornerRadius = dpToPx(4).toFloat()
                    setColor(Color.parseColor("#90A4AE"))
                }
                background = shape
            }
            layoutVisual.addView(barCollar)

            val detailText = StringBuilder()

            for ((plate, count) in resultCounts) {
                detailText.append("• ${count}x ${plate} kg\n")

                for (i in 0 until count) {
                    val plateView = TextView(requireContext()).apply {
                        val h = dpToPx(plateHeights[plate] ?: 60)
                        val w = dpToPx(28)
                        val params = LinearLayout.LayoutParams(w, h).apply {
                            marginEnd = dpToPx(4)
                            gravity = Gravity.CENTER_VERTICAL
                        }
                        layoutParams = params
                        text = "${if (plate == 1.25 || plate == 2.5) plate else plate.toInt()}"
                        textSize = 9f
                        setTypeface(null, Typeface.BOLD)
                        setTextColor(if (plate == 15.0 || plate == 5.0) Color.BLACK else Color.WHITE)
                        gravity = Gravity.CENTER
                        val shape = GradientDrawable().apply {
                            cornerRadius = dpToPx(6).toFloat()
                            setColor(plateColors[plate] ?: Color.GRAY)
                        }
                        background = shape
                    }
                    layoutVisual.addView(plateView)
                }
            }

            // Metal Bar shaft extending out
            val barSleeve = View(requireContext()).apply {
                val params = LinearLayout.LayoutParams(dpToPx(36), dpToPx(18)).apply {
                    gravity = Gravity.CENTER_VERTICAL
                }
                layoutParams = params
                val shape = GradientDrawable().apply {
                    cornerRadius = dpToPx(3).toFloat()
                    setColor(Color.parseColor("#78909C"))
                }
                background = shape
            }
            layoutVisual.addView(barSleeve)

            if (remaining > 0.1) {
                detailText.append("\n⚠️ Kalan: ${String.format(Locale.getDefault(), "%.2f", remaining * 2)} kg standart plakalarla tam bölünemedi.")
            }

            tvDetails.text = detailText.toString().trim()
        }
    }

    // ==========================================
    // 2. 1RM GÜÇ HESAPLAYICI (ONE REP MAX)
    // ==========================================
    @SuppressLint("SetTextI18n")
    private fun setupOneRmCalculator(view: View) {
        val etWeight = view.findViewById<EditText>(R.id.etOneRmWeight)
        val etReps = view.findViewById<EditText>(R.id.etOneRmReps)
        val btnCalculate = view.findViewById<Button>(R.id.btnCalculateOneRm)
        val tvScore = view.findViewById<TextView>(R.id.tvOneRmScore)
        val tvBreakdown = view.findViewById<TextView>(R.id.tvOneRmBreakdown)

        btnCalculate.setOnClickListener {
            val weight = etWeight.text.toString().toDoubleOrNull() ?: 80.0
            val reps = etReps.text.toString().toIntOrNull() ?: 6

            if (reps < 1) {
                Toast.makeText(requireContext(), "Tekrar sayısı en az 1 olmalıdır", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Epley Formülü: 1RM = Weight * (1 + Reps/30)
            val oneRm = if (reps == 1) weight else weight * (1.0 + reps / 30.0)

            val p90 = oneRm * 0.90
            val p85 = oneRm * 0.85
            val p75 = oneRm * 0.75
            val p65 = oneRm * 0.65

            tvScore.text = "${String.format(Locale.getDefault(), "%.1f", oneRm)} kg"
            tvBreakdown.text = "• %100 (1RM): ${String.format(Locale.getDefault(), "%.1f", oneRm)} kg (1 Tekrar)\n" +
                    "• %90 (Maksimal Güç): ${String.format(Locale.getDefault(), "%.1f", p90)} kg (2-3 Tekrar)\n" +
                    "• %85 (Kuvvet): ${String.format(Locale.getDefault(), "%.1f", p85)} kg (4-6 Tekrar)\n" +
                    "• %75 (Hipertrofi / Kas): ${String.format(Locale.getDefault(), "%.1f", p75)} kg (8-10 Tekrar)\n" +
                    "• %65 (Dayanıklılık): ${String.format(Locale.getDefault(), "%.1f", p65)} kg (12-15 Tekrar)"
        }
    }

    // ==========================================
    // 3. KALORİ & MAKRO HESAPLAYICI (BMR & TDEE)
    // ==========================================
    @SuppressLint("SetTextI18n")
    private fun setupMacroCalculator(view: View) {
        val etWeight = view.findViewById<EditText>(R.id.etMacroWeight)
        val etHeight = view.findViewById<EditText>(R.id.etMacroHeight)
        val etAge = view.findViewById<EditText>(R.id.etMacroAge)
        val toggleGoal = view.findViewById<MaterialButtonToggleGroup>(R.id.toggleGoal)
        val btnCalculate = view.findViewById<Button>(R.id.btnCalculateMacro)
        val tvCalories = view.findViewById<TextView>(R.id.tvTargetCalories)
        val tvDetails = view.findViewById<TextView>(R.id.tvMacroDetails)

        btnCalculate.setOnClickListener {
            val weight = etWeight.text.toString().toDoubleOrNull() ?: 75.0
            val height = etHeight.text.toString().toDoubleOrNull() ?: 178.0
            val age = etAge.text.toString().toIntOrNull() ?: 24

            // Mifflin-St Jeor BMR
            val bmr = (10.0 * weight) + (6.25 * height) - (5.0 * age) + 5.0
            val tdee = bmr * 1.55 // Orta aktif

            val goalOffset = when (toggleGoal.checkedButtonId) {
                R.id.btnGoalFatLoss -> -450.0 // Definasyon
                R.id.btnGoalBulk -> 350.0    // Bulk
                else -> 0.0                   // Koru
            }

            val targetCalories = tdee + goalOffset
            val proteinGrams = (weight * 2.0).toInt()
            val proteinKcal = proteinGrams * 4

            val fatGrams = (weight * 0.9).toInt()
            val fatKcal = fatGrams * 9

            val carbKcal = (targetCalories - proteinKcal - fatKcal).coerceAtLeast(0.0)
            val carbGrams = (carbKcal / 4.0).toInt()

            tvCalories.text = "Hedef Kalori: ${targetCalories.toInt()} kcal / gün"
            tvDetails.text = "🥩 Protein: ${proteinGrams}g (${proteinKcal} kcal)\n" +
                    "🍚 Karbonhidrat: ${carbGrams}g (${carbKcal.toInt()} kcal)\n" +
                    "🥑 Sağlıklı Yağ: ${fatGrams}g (${fatKcal} kcal)\n" +
                    "🔥 Bazal Metabolizma (BMR): ${bmr.toInt()} kcal\n" +
                    "⚡ Günlük Harcanan Enerji (TDEE): ${tdee.toInt()} kcal"
        }
    }

    // ==========================================
    // 4. HIIT & TABATA ZAMANLAYICI
    // ==========================================
    @SuppressLint("SetTextI18n")
    private fun setupHiitTimer(view: View) {
        val tvRound = view.findViewById<TextView>(R.id.tvHiitRound)
        val tvStatus = view.findViewById<TextView>(R.id.tvHiitStatus)
        val tvSeconds = view.findViewById<TextView>(R.id.tvHiitSeconds)
        val btnStart = view.findViewById<Button>(R.id.btnHiitStart)
        val btnReset = view.findViewById<Button>(R.id.btnHiitReset)

        btnStart.setOnClickListener {
            if (isHiitRunning) {
                hiitTimer?.cancel()
                isHiitRunning = false
                btnStart.text = "Devam Et ▶"
            } else {
                isHiitRunning = true
                btnStart.text = "Durdur ⏸"
                startHiitPhase(tvRound, tvStatus, tvSeconds, btnStart)
            }
        }

        btnReset.setOnClickListener {
            hiitTimer?.cancel()
            isHiitRunning = false
            currentRound = 1
            isWorkPhase = true
            btnStart.text = "Başlat ▶"
            tvRound.text = "Tur: 1 / $totalRounds"
            tvStatus.text = "HAZIR"
            tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_light))
            tvSeconds.text = "$workDurationSec"
        }
    }

    @SuppressLint("SetTextI18n")
    private fun startHiitPhase(
        tvRound: TextView,
        tvStatus: TextView,
        tvSeconds: TextView,
        btnStart: Button
    ) {
        val duration = if (isWorkPhase) workDurationSec else restDurationSec

        tvRound.text = "Tur: $currentRound / $totalRounds"
        if (isWorkPhase) {
            tvStatus.text = "ÇALIŞ 🔥"
            tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.success))
        } else {
            tvStatus.text = "DİNLEN ❄️"
            tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.warning))
        }

        triggerVibration()

        hiitTimer = object : CountDownTimer(duration * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val sec = (millisUntilFinished / 1000L).toInt() + 1
                tvSeconds.text = "$sec"
                if (sec <= 3) triggerShortVibration()
            }

            override fun onFinish() {
                if (isWorkPhase) {
                    isWorkPhase = false
                    startHiitPhase(tvRound, tvStatus, tvSeconds, btnStart)
                } else {
                    if (currentRound < totalRounds) {
                        currentRound++
                        isWorkPhase = true
                        startHiitPhase(tvRound, tvStatus, tvSeconds, btnStart)
                    } else {
                        isHiitRunning = false
                        btnStart.text = "Tekrar Başlat ▶"
                        tvStatus.text = "TEBRİKLER! 🏆"
                        tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_light))
                        tvSeconds.text = "0"
                        triggerVibration()
                        Toast.makeText(requireContext(), "Tabata Antrenmanı Tamamlandı!", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }.start()
    }

    // ==========================================
    // 5. FİZİK & KİLO FOTOĞRAF GÜNLÜĞÜ
    // ==========================================
    private fun setupPhotoProgress(view: View) {
        val ivPhoto = view.findViewById<ImageView>(R.id.ivProgressPhoto)
        val etWeight = view.findViewById<EditText>(R.id.etPhotoWeight)
        val btnSave = view.findViewById<Button>(R.id.btnSaveProgressPhoto)

        ivPhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnSave.setOnClickListener {
            val weight = etWeight.text.toString().trim()
            if (selectedImageUri == null) {
                Toast.makeText(requireContext(), "Lütfen önce fotoğraf seçin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val prefs = requireContext().getSharedPreferences("vexa_progress_photos", Context.MODE_PRIVATE)
            val currentEntries = prefs.getString("photo_entries", "") ?: ""
            val todayDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
            val newEntry = "$todayDate|$weight kg|${selectedImageUri.toString()};"

            prefs.edit().putString("photo_entries", currentEntries + newEntry).apply()
            Toast.makeText(requireContext(), "Fizik güncellemeniz ($weight kg) başarıyla kaydedildi! 📸", Toast.LENGTH_SHORT).show()
        }
    }

    private fun triggerVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = requireContext().getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(300)
                }
            }
        } catch (_: Exception) {}
    }

    private fun triggerShortVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibrator.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        } catch (_: Exception) {}
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hiitTimer?.cancel()
    }
}
