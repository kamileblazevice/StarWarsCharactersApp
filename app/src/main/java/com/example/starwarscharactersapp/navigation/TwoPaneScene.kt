package com.example.starwarscharactersapp.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import com.example.starwarscharactersapp.navigation.TwoPaneScene.Companion.BACK_STACK_KEY

class TwoPaneScene<T : Any>(
    override val key: Any,
    override val previousEntries: List<NavEntry<T>>,
    val firstEntry: NavEntry<T>,
    val secondEntry: NavEntry<T>,
) : Scene<T> {

    override val entries: List<NavEntry<T>>
        get() = listOf(firstEntry, secondEntry)

    override val content: @Composable (() -> Unit)
        get() = {
            Row(
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier.weight(0.4f)
                ) {
                    firstEntry.Content()
                }
                Box(
                    modifier = Modifier.weight(0.6f)
                ) {
                    secondEntry.Content()
                }
            }
        }

    companion object {
        const val TWO_PANE_KEY = "TwoPaneKey"
        const val BACK_STACK_KEY = "BackStackKey"

        fun twoPane(rootKey: Any) = mapOf(
            TWO_PANE_KEY to true, BACK_STACK_KEY to rootKey
        )
    }

}

class TwoPaneSceneStrategy<T : Any>(
    private val windowSizeClass: WindowSizeClass,
) : SceneStrategy<T> {

    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        if (!windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)) {
            return null
        }
        val lastTwoEntries = entries.takeLast(2)
        val hasTwoPanelKey = lastTwoEntries.all {
            it.metadata.containsKey(TwoPaneScene.TWO_PANE_KEY) && it.metadata[TwoPaneScene.TWO_PANE_KEY] == true
        }
        val firstEntry = lastTwoEntries.first()
        val secondEntry = lastTwoEntries.last()
        val firstEntryIsNotDetailScreen =
            firstEntry.metadata[BACK_STACK_KEY] != Route.CharacterDetail
        val secondEntryIsDetailScreen =
            secondEntry.metadata[BACK_STACK_KEY] == Route.CharacterDetail

        return if (lastTwoEntries.size == 2 && hasTwoPanelKey && firstEntryIsNotDetailScreen && secondEntryIsDetailScreen) {
            TwoPaneScene(
                key = firstEntry.contentKey to secondEntry.contentKey,
                previousEntries = entries.dropLast(1),
                firstEntry = firstEntry,
                secondEntry = secondEntry,
            )
        } else null
    }
}