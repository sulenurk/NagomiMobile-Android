package com.sklabs.nagomi.ui.subjects

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sklabs.nagomi.data.local.NagomiDatabase
import com.sklabs.nagomi.data.repository.NagomiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SubjectsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = NagomiRepository(NagomiDatabase.getInstance(application))

    val subjects = repository.subjects.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList(),
    )

    private val _selectedColor = MutableStateFlow(NagomiRepository.SUBJECT_COLORS.first())
    val selectedColor = _selectedColor.asStateFlow()

    private val _status = MutableStateFlow("")
    val status = _status.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureDefaultSubject()
            selectNextAvailableColor()
        }
    }

    fun selectColor(color: String) {
        if (color in NagomiRepository.SUBJECT_COLORS) _selectedColor.value = color
    }

    fun addSubject(name: String, onAdded: () -> Unit) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) {
            _status.value = "Subject name is required"
            return
        }

        val duplicate = subjects.value.any {
            val displayName = if (it.isDefault) "Other" else it.name.orEmpty()
            displayName.equals(trimmed, ignoreCase = true)
        }
        if (duplicate) {
            _status.value = "This subject already exists"
            return
        }

        viewModelScope.launch {
            repository.addSubject(trimmed, _selectedColor.value)
            _status.value = "Subject added"
            onAdded()
            selectNextAvailableColor(extraUsedColor = _selectedColor.value)
        }
    }

    fun deleteSubject(id: String) {
        viewModelScope.launch {
            repository.deleteSubject(id)
            _status.value = "Subject deleted"
            selectNextAvailableColor()
        }
    }

    fun cycleColor(id: String, currentColor: String) {
        val palette = NagomiRepository.SUBJECT_COLORS
        val currentIndex = palette.indexOf(currentColor).takeIf { it >= 0 } ?: 0
        val nextColor = palette[(currentIndex + 1) % palette.size]
        viewModelScope.launch { repository.changeSubjectColor(id, nextColor) }
    }

    private fun selectNextAvailableColor(extraUsedColor: String? = null) {
        val used = subjects.value.map { it.color }.toMutableSet()
        extraUsedColor?.let(used::add)
        _selectedColor.value = NagomiRepository.SUBJECT_COLORS.firstOrNull { it !in used }
            ?: NagomiRepository.SUBJECT_COLORS[subjects.value.size % NagomiRepository.SUBJECT_COLORS.size]
    }
}
