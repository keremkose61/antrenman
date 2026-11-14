@file:Suppress("PackageDirectoryMismatch")

package com.pplantreman.app

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Typeface
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.Locale

object MimiAiAssistant {

    // Tab-specific rotating messages to make Mimi feel human and alive
    private var beslenmeIndex = 0
    private var istatistikIndex = 0
    private var antrenmanIndex = 0
    private var araclarIndex = 0

    private val beslenmeMessages = listOf(
        "Bugün hedefin en az 2.5L su içmek! Yanına bir bardak su al ve enerjini yüksek tut 💧",
        "Kas gelişimi için vücut ağırlığının 1.6 - 2.2 katı protein almayı ihmal etme 🥩🥑",
        "Antrenmandan sonra glikojen depolarını tazelemek için sağlıklı karbonhidratları unutma! 🍚",
        "Yeterli su içmek kas kramplarını önler ve protein sentezini %15 hızlandırır 💧⚡"
    )

    private val istatistikMessages = listOf(
        "Unutma: Çalıştırdığın kasların büyümesi için en az 48 saatlik dinlenme şart! 🧘‍♂️💪",
        "Bugünkü adım sayın ve kalori yakımın harika gidiyor! Formunu yakından takip ediyorum 🔥",
        "Kaslar antrenmanda değil, gece kaliteli bir uykuda ve dinlenirken toparlanır 🛌✨",
        "Aşırı antrenmandan (overtraining) kaçın; haftada en az 1-2 gün aktif toparlanma yap 💪"
    )

    private val antrenmanMessages = listOf(
        "Her egzersiz kartına dinlenme butonları ekledim! Set aralarında mola vermeyi unutma ⏱️",
        "Bench Press ve Squat gibi ağır bileşik hareketlerde 90-120 sn dinlenmek gücünü yeniler 🏋️",
        "Ağırlık kaldırırken formu asla bozma. Önce doğru hareket formu, sonra ağırlık artışı! 🎯",
        "Antrenman sırasında küçük yudumlarla su içmek nabzını dengeler ve yorgunluğu azaltır 💧⚡"
    )

    private val araclarMessages = listOf(
        "1RM ve plaka hesaplayıcılarıyla antrenmanını bir üst seviyeye taşıyabilirsin! 🎯",
        "Gelişimini Fotoğraf Günlüğü ile kaydet! 1 ay sonraki değişime kendin bile şaşıracaksın 📸",
        "BMR & Makro hesaplayıcı ile günlük kalori ihtiyacını net olarak belirleyebilirsin 📊",
        "Tabata / HIIT sayacı kardiyo günlerinde yağ yakımını tavan yaptıracak! ⏱️🔥"
    )

    fun getTabSpeech(tabId: Int): String {
        return when (tabId) {
            R.id.nav_beslenme -> {
                val msg = beslenmeMessages[beslenmeIndex % beslenmeMessages.size]
                beslenmeIndex++
                msg
            }
            R.id.nav_istatistik -> {
                val msg = istatistikMessages[istatistikIndex % istatistikMessages.size]
                istatistikIndex++
                msg
            }
            R.id.nav_antrenman -> {
                val msg = antrenmanMessages[antrenmanIndex % antrenmanMessages.size]
                antrenmanIndex++
                msg
            }
            R.id.nav_araclar -> {
                val msg = araclarMessages[araclarIndex % araclarMessages.size]
                araclarIndex++
                msg
            }
            else -> "Seninle birlikteyim! Hedeflerine adım adım ilerliyoruz 💪✨"
        }
    }

    /**
     * Mimi'nin kullanıcı sorularına verdiği akıllı, samimi ve insansı yapay zeka yanıtları
     */
    fun respondToQuery(rawQuery: String): String {
        val q = rawQuery.lowercase(Locale("tr"))

        return when {
            // Su Takibi & Hatırlatması
            q.contains("su") || q.contains("hidrasyon") || q.contains("litre") || q.contains("içmeli") -> {
                "💧 Günlük Su Hatırlatması:\n\n" +
                "Hedefimiz günde en az 2.5 - 3.5 Litre su içmek!\n\n" +
                "• Kaslarımızın %75'i sudan oluşur. Yetersiz su tüketimi güçte %10-15 kayba ve kramplara yol açar.\n" +
                "• Antrenman günlerinde terle kaybettiğin mineralleri telafi etmek için set aralarında yudum yudum su iç.\n" +
                "Hemen şimdi koca bir bardak su içmeye ne dersin? 💧👏"
            }

            // Kas Toparlanması & Dinlenme Kuralı
            q.contains("kas") || q.contains("toparlanma") || q.contains("ağrı") || q.contains("doms") || q.contains("overtraining") -> {
                "💪 Kas Toparlanması Rehberi:\n\n" +
                "Altın Kural: Yoğun çalıştırdığın bir kas grubunun tam toparlanması için en az 48 - 72 saat gerekir!\n\n" +
                "• Ağırlık kaldırırken kas liflerinde mikro yırtıklar oluşur; kaslar salonda değil, dinlenirken ve beslenirken büyür.\n" +
                "• Ağrın (DOMS) varken o kası tekrar ağır çalıştırma; hafif yürüyüş ve esneme (aktif dinlenme) kan akışını hızlandırır.\n" +
                "• Günde 7-8 saat kaliteli uyku en güçlü doğal büyüme hormonu kaynağındır! 🛌✨"
            }

            // Set Arası Mola & Dinlenme Süresi
            q.contains("mola") || q.contains("dinlen") || q.contains("süre") || q.contains("set arası") || q.contains("saniye") -> {
                "⏱️ Set Arası Dinlenme Stratejisi:\n\n" +
                "Egzersiz sekmesindeki her kartta senin için pratik mola sayaçları hazırladım!\n\n" +
                "• Bileşik (Compound) Hareketler (Bench Press, Squat, Row): 90 - 120 saniye dinlen. Sinir sisteminin toparlanması için bu gereklidir.\n" +
                "• İzolasyon Hareketleri (Dumbbell Curl, Lateral Raise, Pushdown): 60 saniye dinlenmek metabolik stresi ve kas pompalamasını maksimize eder.\n" +
                "Sayaç bittiğinde titreşimle seni haberdar edeceğim! ⚡"
            }

            // Protein & Beslenme
            q.contains("protein") || q.contains("yemek") || q.contains("beslen") || q.contains("kalori") || q.contains("kreatin") || q.contains("tavuk") -> {
                "🥩 Beslenme & Protein Tavsiyesi:\n\n" +
                "• Kas kütlesi kazanmak veya korumak için günlük vücut ağırlığın başına 1.6 - 2.2g protein hedefle (Örn: 75 kg için ~130-160g protein).\n" +
                "• Ana kaynakların: Yumurta, tavuk göğsü, lor peyniri, ton balığı, süzme yoğurt ve mercimek.\n" +
                "• Kreatin kullanıyorsan her gün düzenli 3-5g al ve bol su içmeyi ihmal etme.\n" +
                "Antrenman sonrası karbonhidrat ve proteini birlikte alarak toparlanmayı hızlandırabilirsin! 🍚🍗"
            }

            // Motivasyon & Vazgeçme
            q.contains("canım") || q.contains("üşen") || q.contains("motivasyon") || q.contains("yoruldum") || q.contains("bırak") || q.contains("pes") -> {
                "🔥 Zayıflar pes eder, sen devam et!\n\n" +
                "En değerli antrenman, tam da salona gitmeye üşendiğin ama gidip bitirdiğin antrenmandır. " +
                "O kapıdan içeri adım attığında gerisi kendiliğinden gelecek.\n\n" +
                "Unutma: Bugün hissettiğin acı, yarın hissedeceğin güç olacak. Ben buradayım, arkandayım! Hadi kalk ve başla! 💥⚡"
            }

            // PPL & Antrenman Planı
            q.contains("ppl") || q.contains("push pull") || q.contains("program") || q.contains("plan") || q.contains("bölünme") -> {
                "🏋️ PPL (Push - Pull - Legs) Sistemi:\n\n" +
                "• Pazartesi / Perşembe: İtiş (Göğüs + Omuz + Triceps)\n" +
                "• Salı / Cuma: Çekiş (Sırt + Biceps)\n" +
                "• Çarşamba / Cumartesi: Bacak + Karın\n\n" +
                "Bu split, kas protein sentezinin 48 saatlik zirvesini haftada iki kez tetikleyerek en hızlı ve simetrik gelişimi sağlar!"
            }

            // Selamlaşma & Tanışma
            q.contains("selam") || q.contains("merhaba") || q.contains("hey") || q.contains("nasılsın") || q.contains("kimsin") || q.contains("adın ne") || q.contains("mimi") -> {
                "Selam şampiyon! 👋 Ben Mimi, senin kişisel yapay zeka spor ve sağlık koçunum 🤖✨.\n\n" +
                "Uygulamanın her köşesinde seni takip ediyor; su tüketimini, kaslarının dinlenme durumunu ve antrenman molalarını hatırlatıyorum.\n" +
                "Bugün senin için ne yapabilirim? Antrenman, beslenme veya toparlanma hakkında dilediğini sorabilirsin!"
            }

            // Genel Akıllı Yanıt
            else -> {
                "Bunu duyduğuma sevindim! 🎯 Bir yapay zeka koçu olarak sana en iyi tavsiyem; tutarlılığı asla elden bırakmaman.\n\n" +
                "Bugün için:\n" +
                "1. En az 2.5L su hedefini tamamla 💧\n" +
                "2. Egzersiz kartlarındaki mola butonlarıyla dinlenme sürelerini disipline et ⏱️\n" +
                "3. Dün yorulan kaslarına 48 saat toparlanma fırsatı ver 💪\n\n" +
                "Merak ettiğin spesifik bir konu varsa (su, kas toparlanması, protein, dinlenme) hemen sorabilirsin!"
            }
        }
    }

    /**
     * Mimi'nin etkileşimli sohbet penceresini (BottomSheet) açar
     */
    @SuppressLint("SetTextI18n")
    fun showChatDialog(activity: Activity) {
        val dialog = BottomSheetDialog(activity)
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_mimi_chat, null)
        dialog.setContentView(view)

        val layoutMessages = view.findViewById<LinearLayout>(R.id.layoutMimiMessages)
        val scrollChat = view.findViewById<ScrollView>(R.id.scrollMimiChat)
        val etInput = view.findViewById<EditText>(R.id.etMimiInput)
        val btnSend = view.findViewById<Button>(R.id.btnMimiSend)
        val btnClose = view.findViewById<ImageButton>(R.id.btnMimiClose)

        // Initial welcoming message from Mimi
        addMessageBubble(
            activity, layoutMessages, scrollChat,
            "Selam şampiyon! 👋 Ben Mimi, senin yapay zeka koçunum.\n\n" +
            "Su takibini, kaslarının toparlanma sürecini ve antrenman temponu adım adım takip ediyorum. Aşağıdaki başlıklara tıklayabilir veya bana dilediğini yazabilirsin! 🤖✨",
            isAi = true
        )

        fun handleUserSend(text: String) {
            val userText = text.trim()
            if (userText.isEmpty()) return
            etInput.setText("")

            // Hide keyboard
            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(etInput.windowToken, 0)

            // Add User message
            addMessageBubble(activity, layoutMessages, scrollChat, userText, isAi = false)

            // Mimi replies with human-like brief delay
            layoutMessages.postDelayed({
                val aiResponse = respondToQuery(userText)
                addMessageBubble(activity, layoutMessages, scrollChat, aiResponse, isAi = true)
            }, 300)
        }

        btnSend.setOnClickListener {
            handleUserSend(etInput.text.toString())
        }

        etInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                handleUserSend(etInput.text.toString())
                true
            } else false
        }

        // Quick chip clicks
        view.findViewById<View>(R.id.chipWater).setOnClickListener {
            handleUserSend("Günde ne kadar su içmeliyim ve su takibi neden önemli?")
        }
        view.findViewById<View>(R.id.chipRecovery).setOnClickListener {
            handleUserSend("Kas toparlanması ne kadar sürer ve kas ağrısını nasıl azaltırım?")
        }
        view.findViewById<View>(R.id.chipRest).setOnClickListener {
            handleUserSend("Set aralarında ne kadar mola vermeliyim?")
        }
        view.findViewById<View>(R.id.chipProtein).setOnClickListener {
            handleUserSend("Günlük ne kadar protein almalıyım ve en iyi kaynaklar nelerdir?")
        }
        view.findViewById<View>(R.id.chipMotivation).setOnClickListener {
            handleUserSend("Bugün salona gitmeye üşeniyorum, bana motivasyon ver!")
        }

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun addMessageBubble(
        context: Context,
        container: LinearLayout,
        scrollView: ScrollView,
        message: String,
        isAi: Boolean
    ) {
        val rowLayout = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(context, 10)
            }
            orientation = LinearLayout.HORIZONTAL
            gravity = if (isAi) Gravity.START else Gravity.END
        }

        if (isAi) {
            val avatar = ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(dpToPx(context, 32), dpToPx(context, 32)).apply {
                    marginEnd = dpToPx(context, 8)
                    gravity = Gravity.BOTTOM
                }
                setImageResource(R.drawable.ic_mimi_avatar)
            }
            rowLayout.addView(avatar)
        }

        val bubble = TextView(context).apply {
            text = message
            textSize = 13.5f
            setTextColor(ContextCompat.getColor(context, if (isAi) R.color.text_primary_dark else R.color.white))
            background = ContextCompat.getDrawable(
                context,
                if (isAi) R.drawable.bg_mimi_msg_ai else R.drawable.bg_mimi_msg_user
            )
            setPadding(
                dpToPx(context, 14),
                dpToPx(context, 10),
                dpToPx(context, 14),
                dpToPx(context, 10)
            )
            setLineSpacing(dpToPx(context, 4).toFloat(), 1f)
            maxWidth = (context.resources.displayMetrics.widthPixels * 0.75).toInt()
        }

        rowLayout.addView(bubble)
        container.addView(rowLayout)

        // Smooth scroll to bottom
        scrollView.post {
            scrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }

    private fun dpToPx(context: Context, dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }
}
