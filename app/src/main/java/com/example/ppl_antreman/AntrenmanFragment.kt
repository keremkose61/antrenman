@file:Suppress("PackageDirectoryMismatch")

package com.pplantreman.app

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import com.google.android.material.tabs.TabLayout
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Exercise(
    val name: String,
    val category: String,
    val sets: Int,
    val reps: Int,
    val emoji: String,
    val gifUrl: String,
    val videoUrl: String,
    val howTo: String,
    val tip: String
)

class AntrenmanFragment : Fragment(R.layout.fragment_antrenman) {

    // Pazartesi: Göğüs + Triceps
    private val mondayExercises = listOf(
        Exercise(
            "Bench Press", "Göğüs - Compound", 4, 8, "🏋️",
            "https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExenI4N3NnMGozeHd0ODczdWdyaTVtdWZ2eTlzYXY0cGszNTUweTB5YyZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/3o7TKuXwUa1nZ2Kk2w/giphy.gif",
            "https://www.youtube.com/watch?v=gRVjAtPip0Y",
            "1. Bench'e sırt üstü uzan, ayaklar yerde sabit olsun.\n2. Barı omuz genişliğinden biraz daha geniş tut.\n3. Barı kontrollü olarak göğsüne indir (dirsekler ~45° açıyla).\n4. Göğsüne değdikten sonra patlatarak yukarı it.\n5. Kolları tam açmadan bir sonraki tekrara geç.",
            "Bileğini düz tut, barı avuç içine kavra. Göğüs kasını hep gergin hissettir!"
        ),
        Exercise(
            "Incline Dumbbell Press", "Göğüs - Compound", 3, 10, "💪",
            "https://media.giphy.com/media/l41YkxvU8c7J7Bba0/giphy.gif",
            "https://www.youtube.com/watch?v=8iPEnn-ltC8",
            "1. Bench'i 30-45° açıya ayarla.\n2. Dumbbell'ları omuz hizasında tut, avuç içleri öne baksın.\n3. Yukarı it, tepede dumbbell'ları hafifçe birbirine yaklaştır.\n4. Yavaşça başlangıç noktasına geri dön.",
            "Üst göğsü hedeflemek için açıyı 30°'de bırak; 45°'yi geçme, aksi hâlde ön omuz devreye girer."
        ),
        Exercise(
            "Chest Fly", "Göğüs - İzolasyon", 3, 12, "🦋",
            "https://media.giphy.com/media/l41YlXXyY7mHq810A/giphy.gif",
            "https://www.youtube.com/watch?v=Z57CtFmRMxA",
            "1. Makineyi göğüs hizasına ayarla.\n2. Her iki kolu yana aç (hafif dirsek bükümüyle).\n3. Göğsünü sıkarak kolları önünde birleştir.\n4. 1 sn bekle, yavaşça geri aç.",
            "Hareketi ağırlık değil, göğüs kasın kontrol etmeli. Dirseği asla tam açma."
        ),
        Exercise(
            "Triceps Pushdown", "Triceps - İzolasyon", 3, 12, "⚒️",
            "https://media.giphy.com/media/4L18D4t0eIV5E29mU9/giphy.gif",
            "https://www.youtube.com/watch?v=2-LAMcpzODU",
            "1. Kabloyu en üste al, halatı (rope) tak.\n2. Dirseklerini yanlarda sabitle.\n3. Sadece ön kolunu aşağı doğru indirerek halatı iki yana aç.\n4. Tamamen düzelttiğinde triceps kasını sık, yavaşça bırak.",
            "Ağırlığı yukarı bırakırken dirseklerinin havaya kalkmasına kesinlikle izin verme."
        ),
        Exercise(
            "Overhead Triceps Extension", "Triceps - İzolasyon", 3, 10, "🔱",
            "https://media.giphy.com/media/l41YkxvU8c7J7Bba0/giphy.gif",
            "https://www.youtube.com/watch?v=YbX7Wd8jQ-Q",
            "1. Dambılı iki elle tut, başının arkasına al.\n2. Dirsekleri sabit tutarak ağırlığı yukarı kaldır.\n3. Kollar tamamen düzleştiğinde tricepsi sık.\n4. Yavaşça başlangıç pozisyonuna geri dön.",
            "Dirseklerin yana açılmasına izin verme, kulaklarına yakın tut."
        ),
        Exercise(
            "Bench Dip", "Triceps - Vücut Ağırlığı", 3, 10, "🪑",
            "https://media.giphy.com/media/l41YkxvU8c7J7Bba0/giphy.gif",
            "https://www.youtube.com/watch?v=0326dy_-CzM",
            "1. Bir bench'in kenarına ellerini koy, parmaklar öne baksın.\n2. Kalçanı bench'ten kaldır, bacaklar öne uzansın.\n3. Dirsekleri bükerek vücudu aşağı indir (dirsekler 90° olana kadar).\n4. Triceps gücüyle kendini yukarı it.",
            "Omuzlarını kulaklarına doğru kaldırma. Hareket boyunca gövde dik kalsın."
        )
    )

    // Salı: Sırt + Biceps
    private val tuesdayExercises = listOf(
        Exercise(
            "Lat Pulldown", "Sırt - Compound", 4, 10, "🏋️",
            "https://media.giphy.com/media/4L18D4t0eIV5E29mU9/giphy.gif",
            "https://www.youtube.com/watch?v=CAwf7n6Luuc",
            "1. Makineye otur, diz minderleri uyluklarını sabitlesin.\n2. Barı omuzdan biraz daha geniş, avuçlar öne bakacak şekilde tut.\n3. Göğsünü öne çıkar, hafif geriye yaslan.\n4. Dirseği aşağı çekerek barı göğsüne doğru indir.\n5. Kontrollü olarak yukarı bırak, skapulayı açık hissettir.",
            "Hareketi dirsekle yönet, bileğini değil. Sırt kasını hissediyorsan doğru yapıyorsun demektir!"
        ),
        Exercise(
            "Barbell Row", "Sırt - Compound", 4, 8, "🚣",
            "https://media.giphy.com/media/l41YkxvU8c7J7Bba0/giphy.gif",
            "https://www.youtube.com/watch?v=9efgWeqAwjg",
            "1. Ayaklar omuz genişliğinde açık.\n2. Dizleri hafif bük, sırtı düz tutarak kalçadan öne eğil.\n3. Barı diz kapağı hizasında tut, barı göbek deliğine doğru çek.\n4. Sırtını (kürek kemiklerini) sık ve barı yavaşça aşağı sal.",
            "Belinin yuvarlanmamasına dikkat et. Bütün işi sırt kasları yapmalı, bel değil."
        ),
        Exercise(
            "Seated Cable Row", "Sırt - Compound", 3, 10, "🪑",
            "https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExenI4N3NnMGozeHd0ODczdWdyaTVtdWZ2eTlzYXY0cGszNTUweTB5YyZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/3o7TKuXwUa1nZ2Kk2w/giphy.gif",
            "https://www.youtube.com/watch?v=GZbfZ033f74",
            "1. Makineye otur, ayaklarını plakalara destekle (dizler hafif bükük).\n2. V barını veya geniş tutamağı al.\n3. Gövdeni dik tutarak kabloyu karnına doğru çek.\n4. Sırtı sık ve yavaşça kolları ileri uzatarak gerilimi hisset.",
            "Geri çekişte omuzlarını kulaklarına yaklaştırma, daima aşağıda ve geride tut."
        ),
        Exercise(
            "Dumbbell Curl", "Biceps - İzolasyon", 3, 10, "💪",
            "https://media.giphy.com/media/l41YlXXyY7mHq810A/giphy.gif",
            "https://www.youtube.com/watch?v=ykJmrZ5v0Oo",
            "1. İki eline dambılları al, avuç içleri öne baksın.\n2. Dirsekler vücuda yapışık kalsın, sallanma olmasın.\n3. Dambılları omuzlara doğru kaldır, üstte bir sn sık.\n4. Yavaşça aşağı bırak.",
            "Dirseklerin hiç yerinden oynamasın. Sadece ön kol hareket etmeli."
        ),
        Exercise(
            "Hammer Curl", "Biceps/Brachialis - İzol.", 3, 10, "🔨",
            "https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExMjRteDhuZWxkZDZxd21ic2g5NWZkZ2htYWxzZHliMDcwa3Jxbnp6OCZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/3o7TKuXwUa1nZ2Kk2w/giphy.gif",
            "https://www.youtube.com/watch?v=zC3nLlEvin4",
            "1. İki eline dambılları al, avuç içleri vücuda baksın.\n2. Vücudu sabitle, dirsekleri oynamadan dambılı omza doğru kaldır.\n3. Yukarıda bicepsi sık.\n4. Kontrollü bir şekilde indir. Kısmi tekrarlardan kaçın.",
            "Vücudunu geri atarak momentum kullanma, ağır kaldırmak yerine formu düzgün koru."
        )
    )

    // Çarşamba: Bacak + Omuz
    private val wednesdayExercises = listOf(
        Exercise(
            "Squat", "Bacak - Compound", 4, 8, "🏋️",
            "https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExMjRteDhuZWxkZDZxd21ic2g5NWZkZ2htYWxzZHliMDcwa3Jxbnp6OCZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/3o7TKuXwUa1nZ2Kk2w/giphy.gif",
            "https://www.youtube.com/watch?v=ultWZbUMPL8",
            "1. Barı trapezin üst kısmına yerleştir.\n2. Ayaklar omuz genişliğinde, parmaklar hafif dışa baksın.\n3. Göğsü dik, sırtı düz tutarak otur (sanki arkanda sandalye var).\n4. Kalça dize paralel ya da daha aşağıya kadar in.\n5. Topuklardan iteleyerek kalk.",
            "Diz asla parmak ucunu geçmemeli. Nefes: inerken al, çıkarken ver."
        ),
        Exercise(
            "Leg Press", "Bacak - Compound", 3, 10, "🦵",
            "https://media.giphy.com/media/l41YlXXyY7mHq810A/giphy.gif",
            "https://www.youtube.com/watch?v=IZxyjW7bcv4",
            "1. Platforma otur, sırtını iyice daya.\n2. Ayaklarını omuz genişliğinde plakaya koy.\n3. Ağırlığı ittir ve kilitleri aç.\n4. Dizlerin göğsüne doğru gelene kadar ağırlığı yavaşça indir.\n5. Topuklarından iterek ağırlığı tekrar yukarı kaldır.",
            "Bacaklarını tamamen düzleştirdiğinde (kilitlediğinde) diz kapağına çok yük biner, hafif bükük bırak!"
        ),
        Exercise(
            "Leg Curl", "Arka Bacak - İzolasyon", 3, 12, "🔥",
            "https://media.giphy.com/media/4L18D4t0eIV5E29mU9/giphy.gif",
            "https://www.youtube.com/watch?v=1Tq3QdYUuHs",
            "1. Makineye yüzüstü uzan veya otur (makine tipine göre).\n2. Ayak bileklerini minderin altına yerleştir.\n3. Bacaklarını kalçana doğru bük, hamstringi sık.\n4. Kontrollü olarak başlangıç pozisyonuna dön.",
            "Hareketi hızlı ve kontrolsüz yapma. Arka bacak kasını (hamstring) her tekrarda hisset."
        ),
        Exercise(
            "Leg Extension", "Ön Bacak - İzolasyon", 3, 12, "🦿",
            "https://media.giphy.com/media/l41YlXXyY7mHq810A/giphy.gif",
            "https://www.youtube.com/watch?v=YyvSfVjQeL0",
            "1. Makineye otur, sırtını iyice daya.\n2. Ayak bileklerini minderin arkasına koy.\n3. Bacaklarını düzleştirerek ağırlığı kaldır.\n4. Üstte quadricepsi sık, yavaşça indir.",
            "Hareketi kontrollü yap, momentum kullanma. Tam hareket aralığında çalış."
        ),
        Exercise(
            "Overhead Press", "Omuz - Compound", 4, 8, "🏋️",
            "https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExenI4N3NnMGozeHd0ODczdWdyaTVtdWZ2eTlzYXY0cGszNTUweTB5YyZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/3o7TKuXwUa1nZ2Kk2w/giphy.gif",
            "https://www.youtube.com/watch?v=2yjwXTZQDDI",
            "1. Barı omuz hizasında kavra (omuz genişliğinden biraz geniş).\n2. Ayaklar omuz genişliğinde, çekirdek kasları sıkı.\n3. Barı başının üstüne doğru iterek kaldır.\n4. Kollar tamamen düzleştiğinde barı kontrollü olarak omuz hizasına indir.",
            "Belini fazla oynatma, core kaslarını sıkı tut. Barı yüzünün önünden geçirirken başını hafif geri al."
        ),
        Exercise(
            "Lateral Raise", "Omuz - İzolasyon", 3, 12, "🦅",
            "https://media.giphy.com/media/l41YlXXyY7mHq810A/giphy.gif",
            "https://www.youtube.com/watch?v=3VcKaXpzqRo",
            "1. İki eline dambılları al, kollar yanlarda.\n2. Hafif dirsek bükümüyle dambılları yana doğru kaldır.\n3. Omuz hizasına kadar kaldır (daha yukarı değil).\n4. Kontrollü olarak yavaşça indir.",
            "Ağırlığı sallamadan kaldır. Trapezi devreye sokmamak için omuzları kulaklarına yaklaştırma."
        ),
        Exercise(
            "Face Pull", "Arka Omuz - İzolasyon", 3, 15, "🎯",
            "https://media.giphy.com/media/4L18D4t0eIV5E29mU9/giphy.gif",
            "https://www.youtube.com/watch?v=rep-qVOkqgk",
            "1. Kablo makinesini baş hizasına ayarla, halat takacağını bağla.\n2. Halatı iki elle tut, geri adım at.\n3. Halatı yüzüne doğru çek, dirsekleri yana aç.\n4. Kürek kemiklerini sık ve yavaşça geri bırak.",
            "Duruş bozukluklarını düzeltmek için harika bir egzersiz. Hafif ağırlıkla yüksek tekrar yap."
        )
    )

    // Perşembe: Göğüs + Triceps (2. gün)
    private val thursdayExercises = mondayExercises

    // Cuma: Sırt + Biceps (2. gün)
    private val fridayExercises = tuesdayExercises

    private lateinit var exerciseContainer: LinearLayout
    private lateinit var pplTabs: TabLayout
    private lateinit var tvDayTitle: TextView
    private lateinit var tvMuscleGroup: TextView
    private lateinit var tvExerciseCount: TextView
    private lateinit var dayBanner: LinearLayout
    private lateinit var exerciseCountLayout: LinearLayout

    // Rest Timer Views
    private lateinit var cardActiveRestTimer: MaterialCardView
    private lateinit var tvRestTimerSeconds: TextView
    private var tvRestTimerTitle: TextView? = null
    private lateinit var btnCancelRestTimer: Button
    private var restCountDownTimer: CountDownTimer? = null

    private val dayNames = arrayOf("Pazartesi", "Salı", "Çarşamba", "Perşembe", "Cuma", "Cumartesi", "Pazar")
    private val muscleGroups = arrayOf(
        "Göğüs + Triceps", "Sırt + Biceps", "Bacak + Omuz",
        "Göğüs + Triceps", "Sırt + Biceps", "Dinlenme", "Dinlenme"
    )
    private val dayEmojis = arrayOf("💪", "🏋️", "🦵", "💪", "🏋️", "😴", "😴")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        exerciseContainer = view.findViewById(R.id.exerciseContainer)
        pplTabs = view.findViewById(R.id.pplTabs)
        tvDayTitle = view.findViewById(R.id.tvDayTitle)
        tvMuscleGroup = view.findViewById(R.id.tvMuscleGroup)
        tvExerciseCount = view.findViewById(R.id.tvExerciseCount)
        dayBanner = view.findViewById(R.id.dayBanner)
        exerciseCountLayout = view.findViewById(R.id.exerciseCountLayout)

        // Rest Timer Setup
        cardActiveRestTimer = view.findViewById(R.id.cardActiveRestTimer)
        tvRestTimerSeconds = view.findViewById(R.id.tvRestTimerSeconds)
        tvRestTimerTitle = view.findViewById(R.id.tvRestTimerTitle)
        btnCancelRestTimer = view.findViewById(R.id.btnCancelRestTimer)

        btnCancelRestTimer.setOnClickListener {
            restCountDownTimer?.cancel()
            cardActiveRestTimer.visibility = View.GONE
        }

        view.findViewById<View>(R.id.btnShareWorkout).setOnClickListener {
            showShareWorkoutDialog()
        }

        updateDayBanner(0, mondayExercises.size)
        updateExercises(mondayExercises)

        pplTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val pos = tab?.position ?: 0
                when (pos) {
                    0 -> { updateDayBanner(pos, mondayExercises.size); updateExercises(mondayExercises) }
                    1 -> { updateDayBanner(pos, tuesdayExercises.size); updateExercises(tuesdayExercises) }
                    2 -> { updateDayBanner(pos, wednesdayExercises.size); updateExercises(wednesdayExercises) }
                    3 -> { updateDayBanner(pos, thursdayExercises.size); updateExercises(thursdayExercises) }
                    4 -> { updateDayBanner(pos, fridayExercises.size); updateExercises(fridayExercises) }
                    5 -> { updateDayBanner(pos, 0); showRestDay() }
                    6 -> { updateDayBanner(pos, 0); showRestDay() }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    // ==========================================
    // ⏱️ DİNLENME SAYACI (REST TIMER)
    // ==========================================
    @SuppressLint("SetTextI18n")
    fun startRestTimer(seconds: Int, exerciseName: String? = null) {
        restCountDownTimer?.cancel()
        cardActiveRestTimer.visibility = View.VISIBLE

        val title = if (exerciseName.isNullOrEmpty()) "Dinlenme Süresi" else "$exerciseName Dinlenmesi"
        tvRestTimerTitle?.text = title

        restCountDownTimer = object : CountDownTimer(seconds * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val sec = (millisUntilFinished / 1000L).toInt() + 1
                tvRestTimerSeconds.text = "$sec sn"
                if (sec <= 3) triggerShortVibration()
            }

            override fun onFinish() {
                tvRestTimerSeconds.text = "0 sn"
                cardActiveRestTimer.visibility = View.GONE
                triggerVibration()
                context?.let { ctx ->
                    val finishMsg = if (exerciseName.isNullOrEmpty()) {
                        "Dinlenme süresi bitti! Sıradaki sete başla! 🏋️⚡"
                    } else {
                        "$exerciseName dinlenmesi bitti! Sıradaki sete başla! 🏋️⚡"
                    }
                    Toast.makeText(ctx, finishMsg, Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    // ==========================================
    // 🏆 WORKOUT INSTAGRAM / WHATSAPP SHARE CARD
    // ==========================================
    @SuppressLint("SetTextI18n")
    private fun showShareWorkoutDialog() {
        val ctx = requireContext()
        val dialog = AlertDialog.Builder(ctx).create()
        val dialogView = LayoutInflater.from(ctx).inflate(R.layout.dialog_share_workout, null)

        val cardShare = dialogView.findViewById<MaterialCardView>(R.id.cardWorkoutShare)
        val tvDay = dialogView.findViewById<TextView>(R.id.tvShareDay)
        val tvMuscle = dialogView.findViewById<TextView>(R.id.tvShareMuscle)
        val tvDate = dialogView.findViewById<TextView>(R.id.tvShareDate)
        val tvExCount = dialogView.findViewById<TextView>(R.id.tvShareExercises)
        val btnShare = dialogView.findViewById<Button>(R.id.btnShareImage)
        val btnClose = dialogView.findViewById<Button>(R.id.btnCloseShareDialog)

        val currentDay = tvDayTitle.text.toString()
        val currentMuscle = tvMuscleGroup.text.toString()
        val todayStr = SimpleDateFormat("dd MMMM yyyy", Locale("tr")).format(Date())

        tvDay.text = currentDay
        tvMuscle.text = currentMuscle
        tvDate.text = todayStr
        tvExCount.text = tvExerciseCount.text.toString()

        btnShare.setOnClickListener {
            try {
                val bitmap = createBitmapFromView(cardShare)
                val cacheDir = File(ctx.cacheDir, "images")
                cacheDir.mkdirs()
                val imageFile = File(cacheDir, "vexa_workout_share.png")
                FileOutputStream(imageFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    out.flush()
                }

                val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.provider", imageFile)
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                ctx.startActivity(Intent.createChooser(shareIntent, "Antrenmanı Paylaş"))
                dialog.dismiss()
            } catch (e: Exception) {
                Toast.makeText(ctx, "Görsel oluşturulamadı: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }

        btnClose.setOnClickListener { dialog.dismiss() }

        dialog.setView(dialogView)
        dialog.show()
    }

    private fun createBitmapFromView(view: View): Bitmap {
        view.measure(
            View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(view.height, View.MeasureSpec.EXACTLY)
        )
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    @SuppressLint("SetTextI18n")
    private fun updateDayBanner(dayIndex: Int, exerciseCount: Int) {
        tvDayTitle.text = "${dayEmojis[dayIndex]}  ${dayNames[dayIndex]}"
        tvMuscleGroup.text = muscleGroups[dayIndex]
        if (exerciseCount > 0) {
            exerciseCountLayout.visibility = View.VISIBLE
            tvExerciseCount.text = "$exerciseCount Egzersiz"
        } else {
            exerciseCountLayout.visibility = View.GONE
        }
    }

    private fun updateExercises(exercises: List<Exercise>) {
        exerciseContainer.removeAllViews()
        exercises.forEachIndexed { index, exercise ->
            val card = createExerciseCard(exercise, index + 1)
            card.alpha = 0f
            card.translationY = dpToPx(24).toFloat()
            card.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(350)
                .setStartDelay((index * 60).toLong())
                .setInterpolator(android.view.animation.DecelerateInterpolator())
                .start()
            exerciseContainer.addView(card)
        }
    }

    private fun showRestDay() {
        exerciseContainer.removeAllViews()
        val card = MaterialCardView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dpToPx(12) }
            setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.surface_dark))
            radius = dpToPx(24).toFloat()
            cardElevation = dpToPx(4).toFloat()
            strokeWidth = dpToPx(1)
            strokeColor = ContextCompat.getColor(requireContext(), R.color.glass_border)
        }

        val layout = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(32), dpToPx(48), dpToPx(32), dpToPx(48))
            gravity = Gravity.CENTER
        }

        val emojiView = TextView(requireContext()).apply {
            text = "😴"
            textSize = 56f
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dpToPx(20) }
        }

        val titleView = TextView(requireContext()).apply {
            text = "Dinlenme Günü"
            textSize = 24f
            setTypeface(null, Typeface.BOLD)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary_dark))
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dpToPx(16) }
        }

        val divider = View(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(dpToPx(60), dpToPx(3)).apply {
                gravity = Gravity.CENTER
                bottomMargin = dpToPx(16)
            }
            setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.gradient_start))
        }

        val descView = TextView(requireContext()).apply {
            text = "Bugün kas toparlanma günü!\n\n🧘 İyi dinlen\n💧 Bol su iç\n🥩 Yeterli protein al\n🛌 Kaliteli uyku uyu"
            textSize = 15f
            setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary_dark))
            gravity = Gravity.CENTER
            setLineSpacing(dpToPx(6).toFloat(), 1f)
        }

        layout.addView(emojiView)
        layout.addView(titleView)
        layout.addView(divider)
        layout.addView(descView)
        card.addView(layout)

        card.alpha = 0f
        card.scaleX = 0.95f
        card.scaleY = 0.95f
        card.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(400)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .start()

        exerciseContainer.addView(card)
    }

    private fun createExerciseCard(exercise: Exercise, number: Int): MaterialCardView {
        val card = MaterialCardView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dpToPx(14) }
            setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.surface_dark))
            radius = dpToPx(20).toFloat()
            cardElevation = dpToPx(4).toFloat()
            strokeWidth = 0
        }

        val outerLayout = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
        }

        val accentBar = View(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(dpToPx(4), LinearLayout.LayoutParams.MATCH_PARENT)
            background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_card_accent_bar)
        }

        val contentLayout = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            )
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
        }

        val topRow = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val numberView = TextView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(dpToPx(32), dpToPx(32)).apply {
                marginEnd = dpToPx(12)
            }
            text = "$number"
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            gravity = Gravity.CENTER
            background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_exercise_number)
        }

        val nameLayout = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            orientation = LinearLayout.VERTICAL
        }

        val nameView = TextView(requireContext()).apply {
            text = exercise.name
            textSize = 17f
            setTypeface(null, Typeface.BOLD)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary_dark))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dpToPx(2) }
        }

        val categoryView = TextView(requireContext()).apply {
            text = exercise.category
            textSize = 12f
            setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary_dark))
        }

        nameLayout.addView(nameView)
        nameLayout.addView(categoryView)

        val setsRepsView = TextView(requireContext()).apply {
            text = if (exercise.reps == 0) "${exercise.sets}×Maks" else "${exercise.sets}×${exercise.reps}"
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.gradient_start))
            background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_sets_reps_pill)
            setPadding(dpToPx(12), dpToPx(6), dpToPx(12), dpToPx(6))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { marginStart = dpToPx(8) }
        }

        topRow.addView(numberView)
        topRow.addView(nameLayout)
        topRow.addView(setsRepsView)

        // Dinlenme sayacı kontrolleri (Her egzersize özel)
        val restRow = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(12)
            }
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val timerIcon = TextView(requireContext()).apply {
            text = "⏱️ Mola:"
            textSize = 12f
            setTypeface(null, Typeface.BOLD)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary_dark))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = dpToPx(8)
            }
        }

        val btnRest60 = TextView(requireContext()).apply {
            text = "60s"
            textSize = 11f
            setTypeface(null, Typeface.BOLD)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_light))
            background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_pill_primary)
            setPadding(dpToPx(12), dpToPx(5), dpToPx(12), dpToPx(5))
            isClickable = true
            isFocusable = true
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = dpToPx(6)
            }
            setOnClickListener {
                startRestTimer(60, exercise.name)
            }
        }

        val btnRest90 = TextView(requireContext()).apply {
            text = "90s"
            textSize = 11f
            setTypeface(null, Typeface.BOLD)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_light))
            background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_pill_primary)
            setPadding(dpToPx(12), dpToPx(5), dpToPx(12), dpToPx(5))
            isClickable = true
            isFocusable = true
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = dpToPx(6)
            }
            setOnClickListener {
                startRestTimer(90, exercise.name)
            }
        }

        val btnRest120 = TextView(requireContext()).apply {
            text = "120s"
            textSize = 11f
            setTypeface(null, Typeface.BOLD)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_light))
            background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_pill_primary)
            setPadding(dpToPx(12), dpToPx(5), dpToPx(12), dpToPx(5))
            isClickable = true
            isFocusable = true
            setOnClickListener {
                startRestTimer(120, exercise.name)
            }
        }

        restRow.addView(timerIcon)
        restRow.addView(btnRest60)
        restRow.addView(btnRest90)
        restRow.addView(btnRest120)

        contentLayout.addView(topRow)
        contentLayout.addView(restRow)
        outerLayout.addView(accentBar)
        outerLayout.addView(contentLayout)
        card.addView(outerLayout)

        card.setOnClickListener {
            showExerciseDetail(exercise)
        }

        return card
    }

    // ==========================================
    // 📝 SET & AĞIRLIK KAYDEDİCİ (PROGRESSIVE OVERLOAD)
    // ==========================================
    @SuppressLint("SetTextI18n")
    private fun showExerciseDetail(exercise: Exercise) {
        val dialog = BottomSheetDialog(requireContext())
        val sheetView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_exercise_detail, null)

        val gifView = sheetView.findViewById<ImageView>(R.id.ivDetailGif)
        gifView?.visibility = View.GONE
        (gifView?.parent as? View)?.visibility = View.GONE

        sheetView.findViewById<TextView>(R.id.tvDetailName).text = exercise.name
        sheetView.findViewById<TextView>(R.id.tvDetailCategory).text = exercise.category
        sheetView.findViewById<TextView>(R.id.tvDetailSets).text = "${exercise.sets}"
        sheetView.findViewById<TextView>(R.id.tvDetailReps).text = if (exercise.reps == 0) "Maks" else "${exercise.reps}"
        sheetView.findViewById<TextView>(R.id.tvDetailHowTo).text = exercise.howTo
        sheetView.findViewById<TextView>(R.id.tvDetailTip).text = exercise.tip

        // Progressive Overload Logic
        val prefs = requireContext().getSharedPreferences("vexa_exercise_logs", Context.MODE_PRIVATE)
        val lastLogKey = "log_${exercise.name}"
        val lastLog = prefs.getString(lastLogKey, null)

        val tvLastLogged = sheetView.findViewById<TextView>(R.id.tvLastLoggedWeight)
        if (lastLog != null) {
            tvLastLogged.text = "📝 Son Kayıt: $lastLog"
        } else {
            tvLastLogged.text = "📝 Son Kayıt: Henüz kayıt yok"
        }

        val etWeight = sheetView.findViewById<EditText>(R.id.etLogWeight)
        val etReps = sheetView.findViewById<EditText>(R.id.etLogReps)
        val btnSaveSet = sheetView.findViewById<Button>(R.id.btnSaveSetAndRest)

        btnSaveSet.setOnClickListener {
            val weight = etWeight.text.toString().trim()
            val reps = etReps.text.toString().trim()
            if (weight.isNotEmpty() && reps.isNotEmpty()) {
                val logEntry = "$weight kg × $reps tekrar (${SimpleDateFormat("dd.MM", Locale.getDefault()).format(Date())})"
                prefs.edit().putString(lastLogKey, logEntry).apply()
                Toast.makeText(requireContext(), "Set kaydedildi: $logEntry", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                startRestTimer(90, exercise.name)
            } else {
                Toast.makeText(requireContext(), "Lütfen ağırlık ve tekrar girin", Toast.LENGTH_SHORT).show()
            }
        }

        sheetView.findViewById<Button>(R.id.btnWatchVideo).setOnClickListener {
            dialog.dismiss()
            openVideo(exercise.videoUrl)
        }

        dialog.setContentView(sheetView)
        dialog.show()
    }

    private fun openVideo(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (_: Exception) {
            context?.let { ctx ->
                Toast.makeText(ctx, "Video bağlantısı açılamadı", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun triggerVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = requireContext().getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator.vibrate(VibrationEffect.createOneShot(400, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(400, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(400)
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
        restCountDownTimer?.cancel()
    }
}
