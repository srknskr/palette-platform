package com.palette.mobile.android

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.palette.mobile.android.ui.components.PaletteCard
import com.palette.mobile.palette.model.Palette
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class PaletteCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun displaysPaletteNameAndHandlesClicks() {
        val palette = Palette(
            id = "p-test",
            name = "Sunset Glow",
            status = "PUBLISHED",
            likeCount = 25,
            colors = listOf("#FF5733", "#33FF57", "#3357FF", "#F3FF33"),
            tags = listOf("warm"),
            createdBy = "user-1",
            createdAt = "2026-09-17T08:00:00Z",
            publishedAt = "2026-09-17T08:00:00Z",
            likedByMe = false
        )

        var clickedId: String? = null
        var favoriteClicked = false

        composeTestRule.setContent {
            PaletteCard(
                palette = palette,
                onPaletteClick = { clickedId = it },
                onFavoriteClick = { favoriteClicked = true }
            )
        }

        composeTestRule.onNodeWithText("Sunset Glow").assertIsDisplayed()
        composeTestRule.onNodeWithText("25 likes").assertIsDisplayed()

        composeTestRule.onNodeWithText("Sunset Glow").performClick()
        assertEquals("p-test", clickedId)

        composeTestRule.onNodeWithContentDescription("Favorite palette").performClick()
        assertEquals(true, favoriteClicked)
    }
}
