package com.example.mute

import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

class MuteTileService : TileService() {
    override fun onClick() {
        super.onClick()
        val intent = Intent(this, MainActivity::class.java)
        startActivityAndCollapse(intent)
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
        val tile = getQsTile()
        tile.setState(Tile.STATE_ACTIVE)
        tile.updateTile()
    }

    override fun onStopListening() {
        super.onStopListening()

        // Called when the tile is no longer visible
    }
}