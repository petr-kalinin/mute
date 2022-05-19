package com.example.mute

import android.app.TimePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.media.AudioManager
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import android.widget.TimePicker
import java.text.SimpleDateFormat
import java.util.*


class MuteTileService : TileService(), TimePickerDialog.OnTimeSetListener {
    var prefs : SharedPreferences? = null

    init {
        Log.w("mute","Constructor")
    }

    override fun onCreate() {
        super.onCreate()
        prefs = getSharedPreferences("name", Context.MODE_PRIVATE)
    }

    override fun onClick() {
        super.onClick()
        if (prefs == null) return
        val time = prefs!!.getLong(PREF_KEY, NONE_TIME)
        if (time != NONE_TIME) {
            val editor = prefs!!.edit()
            editor.putLong(PREF_KEY, NONE_TIME)
            editor.commit()
            updateState()
            return
        }
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)
        val dialog = TimePickerDialog(this, this, hour, minute, true)
        dialog.setTitle(R.string.dialog_title)
        showDialog(dialog)
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        if (prefs == null) return
        val now = Calendar.getInstance()
        val mute_time = Calendar.getInstance()
        mute_time.set(Calendar.HOUR_OF_DAY, hourOfDay)
        mute_time.set(Calendar.MINUTE, minute)
        mute_time.set(Calendar.SECOND, 0)
        if (mute_time.before(now)) {
            mute_time.add(Calendar.DAY_OF_MONTH, 1)
        }
        val editor = prefs!!.edit()
        editor.putLong(PREF_KEY, mute_time.timeInMillis)
        editor.commit()
        updateState()
    }

    override fun onTileRemoved() {
        super.onTileRemoved()

        // Do something when the user removes the Tile
    }

    override fun onTileAdded() {
        super.onTileAdded()

        // Do something when the user add the Tile
    }

    override fun onStartListening() {
        super.onStartListening()
        updateState()
    }

    override fun onStopListening() {
        super.onStopListening()

        // Called when the tile is no longer visible
    }

    fun updateState() {
        if (prefs == null) return
        val time = prefs!!.getLong(PREF_KEY, NONE_TIME)
        val tile = getQsTile()
        if (time != NONE_TIME) {
            tile.state = Tile.STATE_ACTIVE
            val c = Calendar.getInstance()
            c.timeInMillis = time
            val format = SimpleDateFormat("HH:mm dd/MM")
            tile.label = format.format(c.time)
            doMute(true)
            startTimer(c)
        } else {
            tile.state = Tile.STATE_INACTIVE
            tile.label = getResources().getString(R.string.tile_name)
            doMute(false)
            stopTimer()
        }
        tile.updateTile()
    }

    fun doMute(state: Boolean) {
        val flag = if (state) AudioManager.ADJUST_MUTE else AudioManager.ADJUST_UNMUTE
        val manager = getSystemService(AUDIO_SERVICE) as AudioManager
        manager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, flag, 0);
        manager.adjustStreamVolume(AudioManager.STREAM_ALARM, flag, 0);
        manager.adjustStreamVolume(AudioManager.STREAM_MUSIC, flag, 0);
        manager.adjustStreamVolume(AudioManager.STREAM_RING, flag, 0);
        manager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, flag, 0);
    }

    fun startTimer(time : Calendar) {
        Log.w("mute", "start timer to " + time.get(Calendar.MINUTE))
        timerTask?.cancel()
        timerTask = Task()
        timer.schedule(timerTask, time.time)
    }

    fun stopTimer() {
        Log.w("mute", "stop timer")
        timerTask?.cancel()
        timerTask = null
    }

    class Task : TimerTask() {
        override fun run() {
            Log.w("mute", "Task run!")
        }
    }

    companion object {
        const val PREF_KEY = "time"
        const val NONE_TIME = -1L

        val timer : Timer = Timer()
        var timerTask : TimerTask? = null
    }
}