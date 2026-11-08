package com.example.tiktokmodlab

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast

data class Plugin(
    val id: String,
    val title: String,
    val description: String
)

class MainActivity : Activity() {
    private val plugins = listOf(
        Plugin("player_controls", "Дополнительные настройки плеера", "Раздел для будущих локальных настроек воспроизведения."),
        Plugin("content_filter", "Фильтр контента", "Заготовка для пользовательских фильтров контента."),
        Plugin("save_helper", "Помощник сохранения", "Подсказки о доступных и разрешённых способах сохранения."),
        Plugin("theme", "Оформление", "Настройки внешнего вида TikTok Mod Lab.")
    )

    private val preferences by lazy {
        getSharedPreferences("plugin_settings", MODE_PRIVATE)
    }
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 32, 24, 24)
            setBackgroundColor(0xFF101014.toInt())
        }

        val title = TextView(this).apply {
            text = "TikTok Mod Lab"
            textSize = 26f
            setTextColor(0xFFFFFFFF.toInt())
            gravity = Gravity.CENTER_VERTICAL
        }
        root.addView(title)

        val subtitle = TextView(this).apply {
            text = "Личная лаборатория плагинов · версия 0.2.0"
            textSize = 14f
            setTextColor(0xFFB8B8C2.toInt())
            setPadding(0, 8, 0, 12)
        }
        root.addView(subtitle)

        status = TextView(this).apply {
            textSize = 14f
            setTextColor(0xFF8FE3A1.toInt())
            setPadding(0, 0, 0, 16)
        }
        root.addView(status)

        val warning = TextView(this).apply {
            text = "Это отдельное приложение-компаньон. Переключатели сохраняют настройки внутри Mod Lab и пока не изменяют официальный TikTok."
            textSize = 14f
            setTextColor(0xFFFFD27A.toInt())
            setPadding(14, 14, 14, 14)
            setBackgroundColor(0xFF29231A.toInt())
        }
        root.addView(warning)

        val scroll = ScrollView(this)
        val list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 18, 0, 0)
        }

        plugins.forEach { plugin ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(16, 14, 16, 14)
                setBackgroundColor(0xFF1D1D24.toInt())
            }

            val toggle = Switch(this).apply {
                text = plugin.title
                textSize = 17f
                setTextColor(0xFFFFFFFF.toInt())
                isChecked = preferences.getBoolean(plugin.id, false)
                setOnCheckedChangeListener { _, checked ->
                    preferences.edit().putBoolean(plugin.id, checked).apply()
                    updateStatus()
                    Toast.makeText(
                        this@MainActivity,
                        if (checked) "Настройка включена и сохранена" else "Настройка выключена и сохранена",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            card.addView(toggle)

            val description = TextView(this).apply {
                text = plugin.description
                textSize = 13f
                setTextColor(0xFFB8B8C2.toInt())
                setPadding(0, 6, 0, 0)
            }
            card.addView(description)

            val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 12 }
            list.addView(card, params)
        }

        scroll.addView(list)
        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(root)
        updateStatus()
    }

    private fun updateStatus() {
        val enabledCount = plugins.count { preferences.getBoolean(it.id, false) }
        status.text = "Плагины включены: $enabledCount из ${plugins.size}"
    }
}
