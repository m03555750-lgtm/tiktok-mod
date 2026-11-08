package com.example.tiktokmodlab

import android.content.Context
import java.util.Locale

/**
 * Local text filter for content manually entered or pasted into Mod Lab.
 * It does not read or modify the official TikTok feed.
 */
class ContentFilterStore(context: Context) {
    private val preferences = context.getSharedPreferences(
        "content_filter_settings",
        Context.MODE_PRIVATE
    )

    fun blockedWords(): List<String> =
        preferences.getStringSet(KEY_WORDS, emptySet())
            .orEmpty()
            .sorted()

    fun saveBlockedWords(rawWords: String) {
        val words = rawWords
            .lineSequence()
            .map { it.trim().lowercase(Locale.ROOT) }
            .filter { it.isNotEmpty() }
            .distinct()
            .toSet()

        preferences.edit().putStringSet(KEY_WORDS, words).apply()
    }

    fun findBlockedWord(text: String): String? {
        val normalizedText = text.lowercase(Locale.ROOT)
        return blockedWords().firstOrNull { word ->
            normalizedText.contains(word)
        }
    }

    private companion object {
        const val KEY_WORDS = "blocked_words"
    }
}
