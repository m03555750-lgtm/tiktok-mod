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
    val description: String,
    var enabled: Boolean = false
)

class MainActivity : Activity() {
    private val plugins = mutableListOf(
        Plugin("player_controls", "Дополнительные настройки плеера", "Демонстрационный переключатель настроек воспроизведения."),
        Plugin("content_filter", "Фильтр контента", "Макет будущих пользовательских фильтров."),
        Plugin("save_helper", "Помощник сохранения", "Показывает только разрешённые способы сохранения контента."),
        Plugin("theme", "Оформление", "Настройки внешнего вида самого приложения.")
    )

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
            text = "Личная лаборатория плагинов · версия 0.1.0"
            textSize = 14f
            setTextColor(0xFFB8B8C2.toInt())
            setPadding(0, 8, 0, 24)
        }
        root.addView(subtitle)

        val warning = TextView(this).apply {
            text = "Это отдельное приложение-компаньон. Оно не изменяет APK TikTok, не встраивается в него и пока содержит демонстрационные плагины."
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
                isChecked = plugin.enabled
                setOnCheckedChangeListener { _, checked ->
                    plugin.enabled = checked
                    Toast.makeText(
                        this@MainActivity,
                        if (checked) "Плагин включён (демо)" else "Плагин выключен",
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
    }
}
