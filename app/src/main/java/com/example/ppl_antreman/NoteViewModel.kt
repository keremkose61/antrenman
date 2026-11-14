package com.pplantreman.app

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class NoteViewModel(private val repository: NoteRepository) : ViewModel() {
    val allNotes: LiveData<List<Note>> = repository.allNotes.asLiveData()

    fun insert(note: Note) = viewModelScope.launch {
        repository.insert(note)
    }
    
    suspend fun getNoteByDate(date: Long): Note? {
        return repository.getNoteByDate(date)
    }

    // Motivational Quotes
    private val motivationalQuotes = listOf(
        "Acı geçicidir. Vazgeçmek ise sonsuza dek sürer.",
        "Mazeretlerin, hayallerini öldürmesine izin verme.",
        "Bugün yapmadıkların, yarın pişmanlıkların olacak.",
        "Terin, yağların ağlayışıdır. Ağlat onları!",
        "Kolay olmayacak. Buna değecek.",
        "Sınırlarını zorlamazsan, neleri başarabileceğini asla bilemezsin.",
        "Yorgun olduğunda durma, bitirdiğinde dur.",
        "Disiplin, canın istemediğinde bile yapmaktır.",
        "Şampiyonlar salonlarda değil, içlerindeki tutkudan doğar.",
        "Kalk ve savaş. Hayatın seni beklemiyor.",
        "Zayıflık bir seçimdir. Güç de öyle.",
        "Daha iyisini istiyorsan, daha fazlasını ver.",
        "Başarı, her gün tekrar edilen küçük disiplinlerdir.",
        "Kimse sana inanmasa bile, sen kendine inan.",
        "Oturarak tarih yazamazsın."
    )

    fun getRandomQuote(): String {
        return motivationalQuotes.random()
    }

    private val _milestoneEvent = kotlinx.coroutines.flow.MutableSharedFlow<String>()
    val milestoneEvent = _milestoneEvent.asSharedFlow()

    fun checkAndLogTodayStreak() = viewModelScope.launch {
        val startOfToday = startOfDayMillis()
        
        // Check if we already have a note for today
        val existingNote = repository.getNoteByDate(startOfToday)
        
        if (existingNote == null) {
            // No note for today, create one automatically
            val newNote = Note(date = startOfToday, content = "Otomatik Antrenman Kaydı!")
            repository.insert(newNote)
            
            // Recalculate streak to check for milestone
            // We need to fetch all notes again or trust the flow updates. 
            // For immediate feedback, let's query. But repository.allNotes is a Flow.
            // Be pragmatic: We just inserted. The observable `allNotes` will update UI.
            // For the toast, we can do a one-off check.
            
            // We need to get the list to calculate streak. The Flow in ViewModel might not have updated yet in this coroutine scope perfectly synchronously 
            // unless we collect it, but we can't block.
            
            // Let's rely on a helper in Dao if we want exact current list or just wait a bit? 
            // Better: use the logic we have. 
            // Since we just inserted, let's grab all notes one-shot if we could, but our Dao returns Flow.
            // Let's add a one-shot get to Dao or Repository for this logic? Or simplify.
            // Let's just assume the streak incremented by 1. 
            // But we don't know the previous streak easily without keeping state.
            
            // Let's collect the flow once to get current state? No, simply reading `allNotes.value` if it was StateFlow/LiveData would be easier.
            // `allNotes` is LiveData.
            
            val currentNotes = allNotes.value ?: emptyList()
            // This 'currentNotes' might be stale (before insert). 
            // Let's just create a list with the new note added to check logic.
            val updatedNotes = ArrayList(currentNotes)
            updatedNotes.add(newNote)
            
            val newStreak = calculateStreak(updatedNotes.toList()) // Use abstract list
            
            if (newStreak > 0 && newStreak % 10 == 0) {
                // Milestone hit!
                val quote = motivationalQuotes.random()
                _milestoneEvent.emit("Tebrikler! $newStreak Günlük Seri! \n\"$quote\"")
            }
        }
    }

    // Helper to check streak
    // This is a simplified logic. A real streak calculation would be more complex.
    // We will calculate it based on the list of notes.
    fun calculateStreak(notes: List<Note>): Int {
        if (notes.isEmpty()) return 0
        
        // Sort notes by date descending
        val sortedNotes = notes.sortedByDescending { it.date }
        
        var streak = 0
        val today = startOfDayMillis()
        val yesterday = addDays(today, -1)
        val distinctDates = sortedNotes.map { startOfDayMillis(it.date) }.distinct()

        if (distinctDates.isEmpty()) return 0

        // If the latest note is not today and not yesterday, streak is 0.
        if (!distinctDates.contains(today) && !distinctDates.contains(yesterday)) {
            return 0
        }

        // Count backwards
        var current = if (distinctDates.contains(today)) today else yesterday
        
        while (distinctDates.contains(current)) {
            streak++
            current = startOfDayMillis(addDays(current, -1))
        }
        
        return streak
    }

    private fun startOfDayMillis(timeInMillis: Long = System.currentTimeMillis()): Long {
        return Calendar.getInstance().apply {
            this.timeInMillis = timeInMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun addDays(timeInMillis: Long, days: Int): Long {
        return Calendar.getInstance().apply {
            this.timeInMillis = timeInMillis
            add(Calendar.DAY_OF_YEAR, days)
        }.timeInMillis
    }
}

class NoteViewModelFactory(private val repository: NoteRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NoteViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
