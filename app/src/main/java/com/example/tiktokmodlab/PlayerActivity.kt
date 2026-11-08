package com.example.tiktokmodlab

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class PlayerActivity : Activity() {
    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView
    private lateinit var speedButton: Button
    private lateinit var repeatButton: Button
    private lateinit var status: TextView
    private var speedIndex = 0
    private val speeds = floatArrayOf(1f, 1.25f, 1.5f, 2f)
    private val handler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 24, 16, 16)
            setBackgroundColor(0xFF101014.toInt())
        }
        root.addView(TextView(this).apply {
            text = "Локальный видеоплеер"
            textSize = 22f
            setTextColor(0xFFFFFFFF.toInt())
        })
        status = TextView(this).apply {
            text = "Выбери видеофайл или вставь прямую ссылку на видео."
            textSize = 13f
            setTextColor(0xFFB8B8C2.toInt())
            setPadding(0, 8, 0, 12)
        }
        root.addView(status)
        playerView = PlayerView(this)
        root.addView(playerView, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
        ))
        root.addView(Button(this).apply {
            text = "Выбрать видео на устройстве"
            setOnClickListener {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "video/*"
                }
                startActivityForResult(intent, REQUEST_VIDEO)
            }
        })
        root.addView(Button(this).apply {
            text = "Открыть прямую ссылку"
            setOnClickListener { askForUrl() }
        })
        val controls = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        speedButton = Button(this).apply {
            text = "Скорость: 1×"
            setOnClickListener {
                speedIndex = (speedIndex + 1) % speeds.size
                player?.setPlaybackSpeed(speeds[speedIndex])
                text = "Скорость: ${speeds[speedIndex]}×"
            }
        }
        repeatButton = Button(this).apply {
            text = "Повтор: выкл."
            setOnClickListener {
                val p = player ?: return@setOnClickListener
                val enabled = p.repeatMode != Player.REPEAT_MODE_ONE
                p.repeatMode = if (enabled) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
                text = if (enabled) "Повтор: вкл." else "Повтор: выкл."
            }
        }
        controls.addView(speedButton, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        controls.addView(repeatButton, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        root.addView(controls)
        root.addView(Button(this).apply {
            text = "Таймер остановки"
            setOnClickListener { chooseTimer() }
        })
        root.addView(Button(this).apply {
            text = "Назад"
            setOnClickListener { finish() }
        })
        setContentView(root)
    }

    private fun initializePlayer(uri: Uri) {
        releasePlayer()
        player = ExoPlayer.Builder(this).build().also { exo ->
            playerView.player = exo
            exo.setMediaItem(MediaItem.fromUri(uri))
            exo.prepare()
            exo.playWhenReady = true
            exo.setPlaybackSpeed(speeds[speedIndex])
            status.text = "Видео загружено. Для интернет-ссылок нужна прямая ссылка на видеофайл или поток."
        }
    }

    private fun askForUrl() {
        val input = android.widget.EditText(this).apply {
            hint = "https://example.com/video.mp4"
            setSingleLine(true)
        }
        AlertDialog.Builder(this)
            .setTitle("Прямая ссылка на видео")
            .setView(input)
            .setPositiveButton("Открыть") { _, _ ->
                val value = input.text.toString().trim()
                val uri = Uri.parse(value)
                if ((uri.scheme == "https" || uri.scheme == "http") && !uri.host.isNullOrBlank()) {
                    initializePlayer(uri)
                } else {
                    Toast.makeText(this, "Проверь ссылку", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun chooseTimer() {
        val options = arrayOf("5 минут", "10 минут", "15 минут", "30 минут", "Отменить таймер")
        AlertDialog.Builder(this)
            .setTitle("Остановить воспроизведение через")
            .setItems(options) { _, which ->
                timerRunnable?.let { handler.removeCallbacks(it) }
                if (which == 4) {
                    Toast.makeText(this, "Таймер отменён", Toast.LENGTH_SHORT).show()
                } else {
                    val minutes = intArrayOf(5, 10, 15, 30)[which]
                    timerRunnable = Runnable {
                        player?.pause()
                        Toast.makeText(this, "Таймер: воспроизведение остановлено", Toast.LENGTH_LONG).show()
                    }
                    handler.postDelayed(timerRunnable!!, minutes * 60_000L)
                    Toast.makeText(this, "Таймер установлен на $minutes мин.", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    @Deprecated("Deprecated API retained for compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_VIDEO && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                try {
                    contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } catch (_: Exception) { }
                initializePlayer(uri)
            }
        }
    }

    private fun releasePlayer() {
        playerView.player = null
        player?.release()
        player = null
    }

    override fun onStop() {
        super.onStop()
        player?.pause()
    }

    override fun onDestroy() {
        timerRunnable?.let { handler.removeCallbacks(it) }
        releasePlayer()
        super.onDestroy()
    }

    companion object {
        private const val REQUEST_VIDEO = 5201
    }
}
