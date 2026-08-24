package com.kodeelite.nooreislam.core.constants

/** App-wide constants. Only truly global values belong here. */
object AppConst {
    const val DATASTORE_FILE = "dev.nooreislam.sss"
    const val DATABASE_NAME = "noor.db"   // change this to start on a fresh (empty) db while testing
    const val DB_VERSION = 2                  // bump when an entity's schema changes, and add a Migration for it

    // our own host, never a vendor's: who actually answers is a server change, not a release
    // todo: local dev server while studio is not on production — swap back before release
    const val API_BASE_URL = "http://192.168.1.14:3000/api/v1"
    // const val API_BASE_URL = "https://noor-admin.kodeelite.com/api/v1"
}
