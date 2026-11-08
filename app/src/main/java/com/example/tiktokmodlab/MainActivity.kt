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

class MainActivity : Activity() {
    private lateinit var pluginManager: PluginManager
    private lateinit var root: LinearLayout
    private lateinit var title: TextView
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pluginManager = PluginManager(this)

        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 32, 24, 24)
        }

        title = TextView(this).apply {
            text = "TikTok Mod Lab"
            textSize = 26f
            setTextColor(0xFFFFFFFF.toInt())
            gravity = Gravity.CENTER_VERTICAL
        }
        root.addView(title)

        val subtitle = TextView(this).apply {
            text = "Личная лаборатория плагинов · версия 0.3.0"
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
            text = "Это отдельное приложение-компаньон. Плагины работают внутри Mod Lab и не изменяют официальный TikTok."
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

        pluginManager.plugins.forEach { plugin ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(16, 14, 16, 14)
                setBackgroundColor(0xFF1D1D24.toInt())
            }

            val toggle = Switch(this).apply {
                text = plugin.title
                textSize = 17f
                setTextColor(0xFFFFFFFF.toInt())
                isChecked = pluginManager.isEnabled(plugin.id)
                setOnCheckedChangeListener { _, checked ->
                    pluginManager.setEnabled(plugin.id, checked)
                    updateStatus()
                    applyTheme()
                    val message = if (checked) {
                        if (plugin.implemented) "Плагин включён" else "Заготовка включена"
                    } else {
                        "Плагин выключен"
                    }
                    Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
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

            if (!plugin.implemented) {
                val badge = TextView(this).apply {
                    text = "СКОРО"
                    textSize = 10f
                    setTextColor(0xFFFFD27A.toInt())
                    setPadding(0, 8, 0, 0)
                }
                card.addView(badge)
            }

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
        applyTheme()
    }

    private fun updateStatus() {
        status.text = "Плагины включены: ${pluginManager.enabledCount()} из ${pluginManager.plugins.size}"
    }

    /** The first working plugin changes the appearance of Mod Lab itself. */
    private fun applyTheme() {
        val alternativeThemeEnabled = pluginManager.isEnabled("theme")
        root.setBackgroundColor(
            if (alternativeThemeEnabled) 0xFF21152F.toInt() else 0xFF101014.toInt()
        )
        title.setTextColor(
            if (alternativeThemeEnabled) 0xFFD7B8FF.toInt() else 0xFFFFFFFF.toInt()
        )
    }
}
