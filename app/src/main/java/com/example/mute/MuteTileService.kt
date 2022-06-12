package com.example.mute

import android.app.AlarmManager
import android.app.AlarmManager.RTC_WAKEUP
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
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

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int) : Int {
        Log.w("mute", "onStartCommand")
        if (intent.getStringExtra(ACTION_KEY) == ACTION_CANCEL) {
            Log.w("mute", "timer")
            clearMute(qsTile)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onClick() {
        super.onClick()
        if (prefs == null) return
        val time = prefs!!.getLong(PREF_KEY, NONE_TIME)
        if (time != NONE_TIME) {
            clearMute(qsTile)
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
        updateState(qsTile)
    }

    override fun onStartListening() {
        super.onStartListening()
        updateState(qsTile)
    }

    fun updateState(tile: Tile?) {
        if (prefs == null) return
        val time = prefs!!.getLong(PREF_KEY, NONE_TIME)
        if (time != NONE_TIME) {
            val c = Calendar.getInstance()
            c.timeInMillis = time
            if (Calendar.getInstance().after(c)) {
                Log.w("mute", "Already passed")
                clearMute(tile)
                return
            }
            if (tile != null) {
                tile.state = Tile.STATE_ACTIVE
                val format = SimpleDateFormat("HH:mm dd/MM")
                tile.label = format.format(c.time)
            }
            doMute(true)
            startTimer(c)
        } else {
            if (tile != null) {
                tile.state = Tile.STATE_INACTIVE
                tile.label = getResources().getString(R.string.tile_name)
            }
            doMute(false)
            stopTimer()
        }
        if (tile != null)
            tile.updateTile()
    }

    fun clearMute(tile: Tile?) {
        val editor = prefs!!.edit()
        editor.putLong(PREF_KEY, NONE_TIME)
        editor.commit()
        updateState(tile)
    }

    fun doMute(state: Boolean) {
        val flag = if (state) AudioManager.ADJUST_MUTE else AudioManager.ADJUST_UNMUTE
        val manager = getSystemService(AUDIO_SERVICE) as AudioManager
        manager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, flag, 0);
        //manager.adjustStreamVolume(AudioManager.STREAM_ALARM, flag, 0);
        //manager.adjustStreamVolume(AudioManager.STREAM_MUSIC, flag, 0);
        manager.adjustStreamVolume(AudioManager.STREAM_RING, flag, 0);
        manager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, flag, 0);
    }

    fun startTimer(time : Calendar) {
        val format = SimpleDateFormat("HH:mm dd/MM")
        Log.w("mute", "start timer to " + format.format(time.time))

        val serviceIntent = Intent(this, MuteService::class.java)
        startForegroundService(serviceIntent)

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, MuteTileService::class.java)
        intent.putExtra(ACTION_KEY, ACTION_CANCEL)
        val timerIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE)
        alarmManager.setExactAndAllowWhileIdle(RTC_WAKEUP, time.timeInMillis, timerIntent)
    }

    override fun onDestroy() {
        Log.w("mute", "onDestroy")
        super.onDestroy()
    }

    fun stopTimer() {
        Log.w("mute", "stop timer")

        val serviceIntent = Intent(this, MuteService::class.java)
        serviceIntent.putExtra(MuteService.ACTION, MuteService.CANCEL)
        startService(serviceIntent)

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, MuteTileService::class.java)
        intent.putExtra(ACTION_KEY, ACTION_CANCEL)
        val timerIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT or  PendingIntent.FLAG_MUTABLE)
        alarmManager.cancel(timerIntent)
    }

    companion object {
        const val ACTION_KEY = "action"
        const val ACTION_CANCEL = "cancel"
        const val PREF_KEY = "time"
        const val NONE_TIME = -1L
    }
}