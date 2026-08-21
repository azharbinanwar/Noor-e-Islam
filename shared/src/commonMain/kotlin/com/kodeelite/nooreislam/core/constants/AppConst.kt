package com.kodeelite.nooreislam.core.constants

/** App-wide constants. Only truly global values belong here. */
object AppConst {
    const val DATASTORE_FILE = "dev.nooreislam.sss"
    const val DATABASE_NAME = "noor.db"   // change this to start on a fresh (empty) db while testing
    const val DB_VERSION = 2                  // bump when an entity's schema changes, and add a Migration for it
}
