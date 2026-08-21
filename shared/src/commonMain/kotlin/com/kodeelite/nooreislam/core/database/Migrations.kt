package com.kodeelite.nooreislam.core.database

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/** An exemption records what it paused, so ending one puts back exactly that. */
private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(connection: SQLiteConnection) {
        // rows written before this defaulted to pausing both, which is what they did
        connection.execSQL("ALTER TABLE excused_period ADD COLUMN pauseAlerts INTEGER NOT NULL DEFAULT 1")
        connection.execSQL("ALTER TABLE excused_period ADD COLUMN pauseFocus INTEGER NOT NULL DEFAULT 1")
    }
}

/**
 * Every schema step, in order. A bump to [com.kodeelite.nooreislam.core.constants.AppConst.DB_VERSION]
 * adds one here — there is no destructive fallback, so a missing step fails the app rather than
 * quietly wiping bookmarks, notes and prayer history.
 */
val DATABASE_MIGRATIONS: Array<Migration> = arrayOf(MIGRATION_1_2)

