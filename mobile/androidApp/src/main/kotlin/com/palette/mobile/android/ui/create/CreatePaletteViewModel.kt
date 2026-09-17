package com.palette.mobile.android.ui.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palette.mobile.core.model.AppResult
import com.palette.mobile.core.util.ColorValidator
import com.palette.mobile.core.util.ValidationResult
import com.palette.mobile.palette.model.Palette
import com.palette.mobile.palette.usecase.CreatePaletteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface CreateUiState {
    data object Idle : CreateUiState
    data object Submitting : CreateUiState
    data class Success(val palette: Palette) : CreateUiState
    data class Error(val message: String, val fieldErrors: Map<String, String> = emptyMap()) : CreateUiState
}

@HiltViewModel
class CreatePaletteViewModel @Inject constructor(
    private val createPaletteUseCase: CreatePaletteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<CreateUiState>(CreateUiState.Idle)
    val uiState: StateFlow<CreateUiState> = _uiState.asStateFlow()

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _colors = MutableStateFlow(listOf("#2E3440", "#4C566A", "#D8DEE9", "#ECEFF4"))
    val colors: StateFlow<List<String>> = _colors.asStateFlow()

    private val _tags = MutableStateFlow("")
    val tags: StateFlow<String> = _tags.asStateFlow()

    fun setName(value: String) {
        _name.value = value
    }

    fun setTags(value: String) {
        _tags.value = value
    }

    fun updateColor(index: Int, hex: String) {
        if (index in 0..3) {
            val updated = _colors.value.toMutableList()
            updated[index] = hex
            _colors.value = updated
        }
    }

    fun swapColors(fromIndex: Int, toIndex: Int) {
        if (fromIndex in 0..3 && toIndex in 0..3 && fromIndex != toIndex) {
            val updated = _colors.value.toMutableList()
            val temp = updated[fromIndex]
            updated[fromIndex] = updated[toIndex]
            updated[toIndex] = temp
            _colors.value = updated
        }
    }

    fun submit(onAuthRequired: () -> Unit) {
        val trimmedName = _name.value.trim()
        if (trimmedName.length < 2 || trimmedName.length > 80) {
            _uiState.value = CreateUiState.Error(
                message = "Name must be between 2 and 80 characters",
                fieldErrors = mapOf("name" to "Name must be between 2 and 80 characters")
            )
            return
        }

        val validation = ColorValidator.validatePaletteColors(_colors.value)
        if (validation is ValidationResult.Invalid) {
            _uiState.value = CreateUiState.Error(
                message = validation.reason,
                fieldErrors = mapOf("colors" to validation.reason)
            )
            return
        }

        val tagList = _tags.value.split(",")
            .map { it.trim().removePrefix("#").lowercase() }
            .filter { it.isNotBlank() }

        viewModelScope.launch {
            _uiState.value = CreateUiState.Submitting
            when (val result = createPaletteUseCase(
                name = trimmedName,
                colors = (validation as ValidationResult.Valid).normalizedColors,
                tags = tagList,
                publish = true
            )) {
                is AppResult.Success -> {
                    _uiState.value = CreateUiState.Success(result.data)
                }
                is AppResult.Error -> {
                    if (result.error.statusCode == 401) {
                        onAuthRequired()
                    }
                    _uiState.value = CreateUiState.Error(
                        message = result.error.message,
                        fieldErrors = result.error.validationErrors
                    )
                }
            }
        }
    }

    fun resetState() {
        _uiState.value = CreateUiState.Idle
    }
}
