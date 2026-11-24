package com.example.cinemood

import android.database.sqlite.SQLiteDatabase
import androidx.test.platform.app.InstrumentationRegistry
import com.example.cinemood.data.CineMoodDbHelper
import org.junit.Assert.*
import org.junit.Test

class DatabaseMigrationTest {
    @Test fun upgradesVersionOneWithoutLosingReviewsOrAccounts() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "cinemood_migration_test.db"
        context.deleteDatabase(name)
        try {
            context.openOrCreateDatabase(name, 0, null).use { db ->
                db.execSQL("CREATE TABLE users(user_id INTEGER PRIMARY KEY,username TEXT,password_hash TEXT,created_at TEXT)")
                db.execSQL("CREATE TABLE watchlist(watchlist_id INTEGER PRIMARY KEY,user_id INTEGER,tmdb_movie_id INTEGER,movie_title TEXT,poster_path TEXT,overview TEXT,status TEXT,date_added TEXT)")
                db.execSQL("CREATE TABLE reviews(review_id INTEGER PRIMARY KEY,watchlist_id INTEGER,rating INTEGER,notes TEXT,updated_at TEXT)")
                db.execSQL("INSERT INTO users VALUES(1,'Alice','original_hash','today')")
                db.execSQL("INSERT INTO watchlist VALUES(1,1,3,'Interstellar',NULL,'Original overview','WATCHED','today')")
                db.execSQL("INSERT INTO reviews VALUES(1,1,5,'Keep my review','today')")
                db.version = 1
            }
            CineMoodDbHelper(context, name).use { helper ->
                val db = helper.readableDatabase
                assertEquals(2, db.version)
                db.rawQuery("SELECT tmdb_movie_id,movie_source FROM watchlist", null).use {
                    assertTrue(it.moveToFirst()); assertEquals(-3, it.getInt(0)); assertEquals("SAMPLE", it.getString(1))
                }
                db.rawQuery("SELECT notes FROM reviews WHERE watchlist_id = 1", null).use {
                    assertTrue(it.moveToFirst()); assertEquals("Keep my review", it.getString(0))
                }
                db.rawQuery("SELECT password_hash FROM users", null).use {
                    assertTrue(it.moveToFirst()); assertEquals("original_hash", it.getString(0))
                }
            }
        } finally { context.deleteDatabase(name) }
    }
}
