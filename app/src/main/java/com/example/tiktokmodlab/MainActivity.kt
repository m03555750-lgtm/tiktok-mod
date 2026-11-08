package com.example.tiktokmodlab

import android.app.Activity
import android.content.Intent
import android.content.ClipData
import android.content.ClipboardManager
import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var pluginManager: PluginManager
    private lateinit var contentFilter: ContentFilterStore
    private lateinit var saveHelper: SaveHelperStore
    private lateinit var root: LinearLayout
    private lateinit var title: TextView
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pluginManager = PluginManager(this)
        contentFilter = ContentFilterStore(this)
        saveHelper = SaveHelperStore(this)

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
            text = "Личная лаборатория плагинов · версия 0.6.0"
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

            if (plugin.id == "content_filter") {
                val configureButton = Button(this).apply {
                    text = "Настроить фильтр"
                    setOnClickListener { showContentFilterDialog() }
                }
                card.addView(configureButton)
            }

            if (plugin.id == "save_helper") {
                val configureButton = Button(this).apply {
                    text = "Открыть сохранённое"
                    setOnClickListener { showSaveHelperDialog() }
                }
                card.addView(configureButton)
            }

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

    private fun showContentFilterDialog() {
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 8, 24, 0)
        }

        content.addView(TextView(this).apply {
            text = "Нежелательные слова — по одному на строку:"
            textSize = 14f
        })

        val wordsInput = EditText(this).apply {
            minLines = 3
            maxLines = 6
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            setText(contentFilter.blockedWords().joinToString("\n"))
            hint = "Например:\nспойлер\nреклама"
        }
        content.addView(wordsInput)

        content.addView(TextView(this).apply {
            text = "Проверка текста:"
            textSize = 14f
            setPadding(0, 18, 0, 4)
        })

        val sampleInput = EditText(this).apply {
            minLines = 2
            maxLines = 4
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            hint = "Вставь сюда подпись или текст"
        }
        content.addView(sampleInput)

        val checkButton = Button(this).apply {
            text = "Проверить текст"
            setOnClickListener {
                contentFilter.saveBlockedWords(wordsInput.text.toString())
                val sample = sampleInput.text.toString()
                val match = contentFilter.findBlockedWord(sample)
                val result = when {
                    sample.isBlank() -> "Сначала введи текст для проверки"
                    match != null -> "Найдено нежелательное слово: $match"
                    else -> "Совпадений не найдено"
                }
                Toast.makeText(this@MainActivity, result, Toast.LENGTH_LONG).show()
            }
        }
        content.addView(checkButton)

        AlertDialog.Builder(this)
            .setTitle("Фильтр контента")
            .setView(content)
            .setPositiveButton("Сохранить") { _, _ ->
                contentFilter.saveBlockedWords(wordsInput.text.toString())
                Toast.makeText(
                    this,
                    "Список слов сохранён: ${contentFilter.blockedWords().size}",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Закрыть", null)
            .show()
    }


    private fun showSaveHelperDialog() {
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 8, 24, 0)
        }

        val urlInput = EditText(this).apply {
            hint = "Ссылка на видео (https://...)"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
        }
        content.addView(urlInput)

        val noteInput = EditText(this).apply {
            hint = "Заметка (необязательно)"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        }
        content.addView(noteInput)

        val addButton = Button(this).apply { text = "Сохранить ссылку" }
        content.addView(addButton)

        val listTitle = TextView(this).apply {
            text = "Сохранённые ссылки"
            textSize = 16f
            setPadding(0, 16, 0, 8)
        }
        content.addView(listTitle)

        val entries = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        content.addView(entries)

        fun refreshList() {
            entries.removeAllViews()
            val items = saveHelper.all()
            if (items.isEmpty()) {
                entries.addView(TextView(this).apply {
                    text = "Пока ничего не сохранено."
                    textSize = 14f
                })
            } else {
                items.forEachIndexed { index, item ->
                    val row = LinearLayout(this).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(0, 8, 0, 8)
                    }
                    row.addView(TextView(this).apply {
                        text = item.url
                        textSize = 14f
                        setTextColor(0xFF8FCBFF.toInt())
                    })
                    if (item.note.isNotBlank()) {
                        row.addView(TextView(this).apply {
                            text = item.note
                            textSize = 13f
                        })
                    }
                    val actions = LinearLayout(this).apply {
                        orientation = LinearLayout.HORIZONTAL
                    }
                    actions.addView(Button(this).apply {
                        text = "Открыть"
                        setOnClickListener { openSavedLink(item.url) }
                    })
                    actions.addView(Button(this).apply {
                        text = "Поделиться"
                        setOnClickListener { shareSavedLink(item.url) }
                    })
                    actions.addView(Button(this).apply {
                        text = "Копировать"
                        setOnClickListener { copySavedLink(item.url) }
                    })
                    actions.addView(Button(this).apply {
                        text = "Удалить"
                        setOnClickListener {
                            saveHelper.remove(index)
                            refreshList()
                        }
                    })
                    row.addView(actions)
                    entries.addView(row)
                }
            }
        }

        val exportButton = Button(this).apply {
            text = "Экспортировать резервную копию"
            setOnClickListener { exportSavedLinks() }
        }
        content.addView(exportButton)

        addButton.setOnClickListener {
            val url = urlInput.text.toString().trim()
            if (!(url.startsWith("https://") || url.startsWith("http://"))) {
                Toast.makeText(this, "Введи корректную ссылку, начинающуюся с http:// или https://", Toast.LENGTH_LONG).show()
            } else {
                saveHelper.add(url, noteInput.text.toString())
                urlInput.text.clear()
                noteInput.text.clear()
                refreshList()
                Toast.makeText(this, "Ссылка сохранена", Toast.LENGTH_SHORT).show()
            }
        }

        refreshList()
        val scroll = ScrollView(this).apply { addView(content) }
        AlertDialog.Builder(this)
            .setTitle("Помощник сохранения")
            .setView(scroll)
            .setPositiveButton("Готово", null)
            .show()
    }


    private fun openSavedLink(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url)))
        } catch (_: Exception) {
            Toast.makeText(this, "Не удалось открыть ссылку", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareSavedLink(url: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, url)
        }
        startActivity(Intent.createChooser(intent, "Поделиться ссылкой"))
    }

    private fun copySavedLink(url: String) {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("TikTok link", url))
        Toast.makeText(this, "Ссылка скопирована", Toast.LENGTH_SHORT).show()
    }

    private fun exportSavedLinks() {
        val items = saveHelper.all()
        if (items.isEmpty()) {
            Toast.makeText(this, "Список пока пуст", Toast.LENGTH_SHORT).show()
            return
        }
        val backup = buildString {
            appendLine("TikTok Mod Lab — резервная копия ссылок")
            appendLine()
            items.forEachIndexed { index, item ->
                appendLine("${index + 1}. ${item.url}")
                if (item.note.isNotBlank()) appendLine("Заметка: ${item.note}")
                appendLine()
            }
        }
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"
            putExtra(Intent.EXTRA_TITLE, "tiktok-mod-lab-links-backup.txt")
        }
        pendingBackupText = backup
        try {
            startActivityForResult(intent, REQUEST_CREATE_BACKUP)
        } catch (_: Exception) {
            pendingBackupText = null
            Toast.makeText(this, "Не удалось открыть сохранение файла", Toast.LENGTH_SHORT).show()
        }
    }

    private var pendingBackupText: String? = null

    @Deprecated("Deprecated in Android API; retained for broad device compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_CREATE_BACKUP) return
        val text = pendingBackupText
        pendingBackupText = null
        if (resultCode != RESULT_OK || data?.data == null || text == null) return
        try {
            contentResolver.openOutputStream(data.data!!)?.bufferedWriter(Charsets.UTF_8)?.use {
                it.write(text)
            }
            Toast.makeText(this, "Резервная копия сохранена", Toast.LENGTH_LONG).show()
        } catch (_: Exception) {
            Toast.makeText(this, "Не удалось сохранить резервную копию", Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        private const val REQUEST_CREATE_BACKUP = 4107
    }

    private fun updateStatus() {
        status.text = "Плагины включены: ${pluginManager.enabledCount()} из ${pluginManager.plugins.size}"
    }

    /** The theme plugin changes the appearance of Mod Lab itself. */
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
