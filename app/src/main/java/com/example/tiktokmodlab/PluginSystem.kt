package com.example.tiktokmodlab

import android.content.Context

data class PluginDefinition(
    val id: String,
    val title: String,
    val description: String,
    val implemented: Boolean
)

/**
 * Central catalog of modules available in Mod Lab.
 * Add new local modules here; MainActivity renders the catalog automatically.
 */
object PluginRegistry {
    val plugins = listOf(
        PluginDefinition(
            id = "player_controls",
            title = "Дополнительные настройки плеера",
            description = "Заготовка для локальных настроек воспроизведения.",
            implemented = false
        ),
        PluginDefinition(
            id = "content_filter",
            title = "Фильтр контента",
            description = "Список нежелательных слов и локальная проверка текста.",
            implemented = true
        ),
        PluginDefinition(
            id = "save_helper",
            title = "Помощник сохранения",
            description = "Сохраняет ссылки на видео и заметки локально внутри Mod Lab.",
            implemented = true
        ),
        PluginDefinition(
            id = "theme",
            title = "Тема Mod Lab",
            description = "Меняет оформление самого приложения Mod Lab.",
            implemented = true
        )
    )
}

/** Stores plugin states locally and exposes a small API for the app. */
class PluginManager(context: Context) {
    private val preferences = context.getSharedPreferences(
        "plugin_settings",
        Context.MODE_PRIVATE
    )

    val plugins: List<PluginDefinition>
        get() = PluginRegistry.plugins

    fun isEnabled(pluginId: String): Boolean =
        preferences.getBoolean(pluginId, false)

    fun setEnabled(pluginId: String, enabled: Boolean) {
        require(plugins.any { it.id == pluginId }) {
            "Unknown plugin: $pluginId"
        }
        preferences.edit().putBoolean(pluginId, enabled).apply()
    }

    fun enabledCount(): Int =
        plugins.count { isEnabled(it.id) }
}
