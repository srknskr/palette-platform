package com.palette.mobile.palette.usecase

import com.palette.mobile.core.model.AppError
import com.palette.mobile.core.model.AppResult
import com.palette.mobile.core.model.ErrorType
import com.palette.mobile.core.model.PagedList
import com.palette.mobile.core.util.ColorValidator
import com.palette.mobile.core.util.ValidationResult
import com.palette.mobile.palette.model.Palette
import com.palette.mobile.palette.model.PaletteFilter
import com.palette.mobile.palette.repository.PaletteRepository

class GetPalettesUseCase(private val paletteRepository: PaletteRepository) {
    suspend operator fun invoke(filter: PaletteFilter, page: Int = 0, size: Int = 20): AppResult<PagedList<Palette>> {
        return paletteRepository.getPalettes(filter, page, size)
    }
}

class GetPaletteDetailUseCase(private val paletteRepository: PaletteRepository) {
    suspend operator fun invoke(id: String): AppResult<Palette> {
        return paletteRepository.getPaletteById(id)
    }
}

class GetRandomPaletteUseCase(private val paletteRepository: PaletteRepository) {
    suspend operator fun invoke(): AppResult<Palette> {
        return paletteRepository.getRandomPalette()
    }
}

class CreatePaletteUseCase(private val paletteRepository: PaletteRepository) {
    suspend operator fun invoke(
        name: String,
        colors: List<String>,
        tags: List<String>,
        publish: Boolean = true
    ): AppResult<Palette> {
        val trimmedName = name.trim()
        if (trimmedName.length < 2 || trimmedName.length > 80) {
            return AppResult.Error(
                AppError(
                    message = "Palette name must be between 2 and 80 characters",
                    type = ErrorType.VALIDATION,
                    validationErrors = mapOf("name" to "Length must be between 2 and 80 characters")
                )
            )
        }

        when (val validation = ColorValidator.validatePaletteColors(colors)) {
            is ValidationResult.Invalid -> {
                return AppResult.Error(
                    AppError(
                        message = validation.reason,
                        type = ErrorType.VALIDATION,
                        validationErrors = mapOf("colors" to validation.reason)
                    )
                )
            }
            is ValidationResult.Valid -> {
                return paletteRepository.createPalette(
                    name = trimmedName,
                    colors = validation.normalizedColors,
                    tags = tags.map { it.trim().lowercase() }.filter { it.isNotBlank() },
                    publish = publish
                )
            }
        }
    }
}

class DeletePaletteUseCase(private val paletteRepository: PaletteRepository) {
    suspend operator fun invoke(id: String): AppResult<Unit> {
        return paletteRepository.deletePalette(id)
    }
}

class GetMyPalettesUseCase(private val paletteRepository: PaletteRepository) {
    suspend operator fun invoke(page: Int = 0, size: Int = 20): AppResult<PagedList<Palette>> {
        return paletteRepository.getMyPalettes(page, size)
    }
}

class GetPublishedPaletteCountUseCase(private val paletteRepository: PaletteRepository) {
    suspend operator fun invoke(): AppResult<Long> {
        return paletteRepository.getPublishedPaletteCount()
    }
}
