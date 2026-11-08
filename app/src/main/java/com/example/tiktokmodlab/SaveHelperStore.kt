package com.example.tiktokmodlab

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class SavedLink(
    val url: String,
    val note: String
)

class SaveHelperStore(context: Context) {
    private val preferences = context.getSharedPreferences("saved_links", Context.MODE_PRIVATE)

    fun all(): List<SavedLink> {
        val raw = preferences.getString("items", "[]") ?: "[]"
        return try {
            val array = JSONArray(raw)
            (0 until array.length()).map { index ->
                val item = array.getJSONObject(index)
                SavedLink(item.optString("url"), item.optString("note"))
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun add(url: String, note: String) {
        val items = all().toMutableList()
        items.add(0, SavedLink(url.trim(), note.trim()))
        save(items)
    }

    fun remove(index: Int) {
        val items = all().toMutableList()
        if (index in items.indices) {
            items.removeAt(index)
            save(items)
        }
    }

    private fun save(items: List<SavedLink>) {
        val array = JSONArray()
        items.forEach { item ->
            array.put(JSONObject().put("url", item.url).put("note", item.note))
        }
        preferences.edit().putString("items", array.toString()).apply()
    }
}
