package com.serkanmusic.palette.palette.infrastructure.persistence

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.io.Serializable
import java.util.UUID
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Embeddable
data class PaletteColorId(
    @Column(name = "palette_id", nullable = false)
    var paletteId: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    var position: Short = 0
) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 1L
    }
}

@Entity
@Table(name = "palette_colors")
class PaletteColorEntity(
    @EmbeddedId
    var id: PaletteColorId = PaletteColorId(),

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "hex_value", nullable = false, length = 7)
    var hexValue: String = ""
)
