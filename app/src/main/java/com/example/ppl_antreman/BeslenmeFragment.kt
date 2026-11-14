@file:Suppress("PackageDirectoryMismatch")

package com.pplantreman.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView

data class MealSuggestion(
    val name: String,
    val emoji: String,
    val protein: Int,
    val carbs: Int,
    val fats: Int,
    val calories: Int,
    val ingredients: List<String>,
    val instructions: List<String>
)

data class SupplementSuggestion(
    val name: String,
    val emoji: String,
    val benefit: String,
    val usageTime: String,
    val dosage: String
)

class BeslenmeFragment : Fragment(R.layout.fragment_beslenme) {



    // Sabah: 3 Tam Yumurta, 2 Dilim Tam Buğday Ekmeği, 1 Muz, 1 Bardak Süt
    private val breakfastMeal = MealSuggestion(
        "Sabah Kahvaltısı", "🍳", 30, 55, 18, 500,
        listOf("3 adet tam yumurta", "2 dilim tam buğday ekmeği", "1 adet muz", "1 bardak süt"),
        listOf("Yumurtaları haşlayın veya tavada pişirin", "Tam buğday ekmeğini yanında servis edin", "Muzu soyup tabağa koyun", "1 bardak sütle tamamlayın")
    )

    // Öğle: 150-200g Tavuk Göğsü, 1 Su Bardağı Pirinç Pilavı, Yoğurt
    private val lunchMeal = MealSuggestion(
        "Öğle Yemeği", "🍗", 45, 55, 10, 550,
        listOf("150-200g tavuk göğsü", "1 su bardağı pirinç pilavı", "1 kase yoğurt"),
        listOf("Tavuk göğsünü ızgara veya haşlama olarak pişirin", "Pirinç pilavını hazırlayın", "Yoğurdu yanında servis edin")
    )

    // Ara Öğün: 1 Avuç Yer Fıstığı, 1 Muz veya Elma
    private val snackMeal = MealSuggestion(
        "Ara Öğün", "🥜", 8, 32, 14, 280,
        listOf("1 avuç yer fıstığı", "1 adet muz veya elma"),
        listOf("Yer fıstığını bir kaseye koyun", "Muz veya elmayı yanında yiyin")
    )

    // Akşam: 1 Tabak Makarna, 1 Kase Yoğurt, 2 Yumurta (Haşlanmış)
    private val dinnerMeal = MealSuggestion(
        "Akşam Yemeği", "🍝", 28, 65, 16, 600,
        listOf("1 tabak makarna", "1 kase yoğurt", "2 adet yumurta (haşlanmış)"),
        listOf("Makarnayı haşlayın ve sosla servis edin", "Yumurtaları haşlayın (8-10 dk)", "Yoğurdu yanında servis edin")
    )

    // Gece (Opsiyonel): 1 Bardak Süt, Lor Peyniri
    private val nightMeal = MealSuggestion(
        "Gece Atıştırması (Opsiyonel)", "🌙", 22, 12, 5, 200,
        listOf("1 bardak süt", "Lor peyniri (1 porsiyon)"),
        listOf("Sütü ılık veya soğuk olarak için", "Lor peynirini yanında tüketin")
    )

    private val supplementSuggestions = listOf(
        SupplementSuggestion(
            name = "Whey Protein",
            emoji = "🥛",
            benefit = "Kas onarımını hızlandırır ve büyümeyi destekler.",
            usageTime = "Antrenman Sonrası",
            dosage = "1 Ölçek (~30g)"
        ),
        SupplementSuggestion(
            name = "Kreatin",
            emoji = "⚡",
            benefit = "Güç artışı sağlar ve patlayıcı performansı yükseltir.",
            usageTime = "Günün Herhangi Bir Saati",
            dosage = "Günlük 3-5g"
        ),
        SupplementSuggestion(
            name = "Omega-3 (Balık Yağı)",
            emoji = "🐟",
            benefit = "Eklem sağlığını korur ve kalp-damar sistemini destekler.",
            usageTime = "Yemeklerle Birlikte",
            dosage = "1-2 Kapsül"
        ),
        SupplementSuggestion(
            name = "Multi-Vitamin",
            emoji = "💊",
            benefit = "Mikro besin açıklarını kapatır, bağışıklığı güçlendirir.",
            usageTime = "Sabah Kahvaltısından Sonra",
            dosage = "1 Tablet"
        ),
        SupplementSuggestion(
            name = "Pre-Workout (Eğer İhtiyaç Varsa)",
            emoji = "🔋",
            benefit = "Antrenman esnasında enerji, odaklanma ve pumpta artış.",
            usageTime = "Antrenmandan 30 dk Önce",
            dosage = "Yarım - 1 Ölçek"
        )
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val cards = listOf(
            view.findViewById<MaterialCardView>(R.id.breakfastCard),
            view.findViewById<MaterialCardView>(R.id.lunchCard),
            view.findViewById<MaterialCardView>(R.id.snackCard),
            view.findViewById<MaterialCardView>(R.id.dinnerCard),
            view.findViewById<MaterialCardView>(R.id.nightCard),
            view.findViewById<MaterialCardView>(R.id.supplementCard)
        )

        // Set click listeners
        cards[0]?.setOnClickListener { showRecipeDetail(breakfastMeal) }
        cards[1]?.setOnClickListener { showRecipeDetail(lunchMeal) }
        cards[2]?.setOnClickListener { showRecipeDetail(snackMeal) }
        cards[3]?.setOnClickListener { showRecipeDetail(dinnerMeal) }
        cards[4]?.setOnClickListener { showRecipeDetail(nightMeal) }
        cards[5]?.setOnClickListener { showSupplementSuggestions() }

        // Staggered fade-in animation
        cards.forEachIndexed { index, card ->
            card?.alpha = 0f
            card?.translationY = 24f * resources.displayMetrics.density
            card?.animate()
                ?.alpha(1f)
                ?.translationY(0f)
                ?.setDuration(350)
                ?.setStartDelay((index * 60).toLong())
                ?.setInterpolator(android.view.animation.DecelerateInterpolator())
                ?.start()
        }
    }




    private fun showRecipeDetail(meal: MealSuggestion) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetView = layoutInflater.inflate(R.layout.recipe_detail_bottom_sheet, null)
        
        // Set meal info
        bottomSheetView.findViewById<TextView>(R.id.recipeEmoji).text = meal.emoji
        bottomSheetView.findViewById<TextView>(R.id.recipeName).text = meal.name
        bottomSheetView.findViewById<TextView>(R.id.recipeNutrition).text = 
            "P: ${meal.protein}g  •  C: ${meal.carbs}g  •  F: ${meal.fats}g  •  ${meal.calories} kcal"
        
        // Add ingredients
        val ingredientsContainer = bottomSheetView.findViewById<LinearLayout>(R.id.ingredientsContainer)
        val density = resources.displayMetrics.density
        val pad8 = (8 * density).toInt()
        val pad12 = (12 * density).toInt()

        meal.ingredients.forEach { ingredient ->
            val ingredientView = TextView(requireContext()).apply {
                text = "• $ingredient"
                textSize = 14f
                setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.text_primary_dark))
                setPadding(0, pad8, 0, pad8)
            }
            ingredientsContainer.addView(ingredientView)
        }
        
        // Add instructions
        val instructionsContainer = bottomSheetView.findViewById<LinearLayout>(R.id.instructionsContainer)
        meal.instructions.forEachIndexed { index, instruction ->
            val instructionView = TextView(requireContext()).apply {
                text = "${index + 1}. $instruction"
                textSize = 14f
                setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.text_primary_dark))
                setPadding(0, pad12, 0, pad12)
                setLineSpacing(pad8.toFloat(), 1f)
            }
            instructionsContainer.addView(instructionView)
        }
        
        // Set up close button
        bottomSheetView.findViewById<TextView>(R.id.closeRecipeButton).setOnClickListener {
            bottomSheetDialog.dismiss()
        }
        
        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()
    }

    private fun showSupplementSuggestions() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetView = layoutInflater.inflate(R.layout.supplement_suggestions_bottom_sheet, null)
        
        val recyclerView = bottomSheetView.findViewById<RecyclerView>(R.id.supplementsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = SupplementSuggestionsAdapter(supplementSuggestions) {
            // Option to show more details about a specific supplement could go here
        }
        
        bottomSheetView.findViewById<TextView>(R.id.closeButton).setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()
    }
}

class SupplementSuggestionsAdapter(
    private val supplements: List<SupplementSuggestion>,
    private val onItemClick: (SupplementSuggestion) -> Unit
) : RecyclerView.Adapter<SupplementSuggestionsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val emoji: TextView = view.findViewById(R.id.supplementEmoji)
        val name: TextView = view.findViewById(R.id.supplementName)
        val benefit: TextView = view.findViewById(R.id.supplementBenefit)
        val usage: TextView = view.findViewById(R.id.supplementUsage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_supplement_suggestion, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val supplement = supplements[position]
        holder.emoji.text = supplement.emoji
        holder.name.text = supplement.name
        holder.benefit.text = supplement.benefit
        holder.usage.text = "${supplement.usageTime} - ${supplement.dosage}"

        holder.itemView.setOnClickListener {
            onItemClick(supplement)
        }
    }

    override fun getItemCount() = supplements.size
}

class MealSuggestionsAdapter(
    private val suggestions: List<MealSuggestion>,
    private val onItemClick: (MealSuggestion) -> Unit
) : RecyclerView.Adapter<MealSuggestionsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val mealEmoji: TextView = view.findViewById(R.id.mealEmoji)
        val mealName: TextView = view.findViewById(R.id.mealName)
        val mealNutrition: TextView = view.findViewById(R.id.mealNutrition)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_meal_suggestion, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val suggestion = suggestions[position]
        holder.mealEmoji.text = suggestion.emoji
        holder.mealName.text = suggestion.name
        holder.mealNutrition.text = "P: ${suggestion.protein}g  •  C: ${suggestion.carbs}g  •  F: ${suggestion.fats}g  •  ${suggestion.calories} kcal"
        
        holder.itemView.setOnClickListener {
            onItemClick(suggestion)
        }
    }

    override fun getItemCount() = suggestions.size
}
