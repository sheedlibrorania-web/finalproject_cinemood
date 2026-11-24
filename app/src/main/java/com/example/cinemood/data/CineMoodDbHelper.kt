package com.example.cinemood.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class CineMoodDbHelper(context: Context, databaseName: String = "cinemood.db") : SQLiteOpenHelper(context, databaseName, null, 2) {
    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""CREATE TABLE users (
            user_id INTEGER PRIMARY KEY AUTOINCREMENT,
            username TEXT NOT NULL UNIQUE COLLATE NOCASE,
            password_hash TEXT NOT NULL,
            created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP)""")
        db.execSQL("""CREATE TABLE watchlist (
            watchlist_id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_id INTEGER NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
            tmdb_movie_id INTEGER NOT NULL,
            movie_title TEXT NOT NULL,
            poster_path TEXT,
            overview TEXT,
            movie_year INTEGER NOT NULL DEFAULT 0,
            movie_genre TEXT NOT NULL DEFAULT '',
            movie_source TEXT NOT NULL DEFAULT 'TMDB',
            status TEXT NOT NULL DEFAULT 'PLANNED' CHECK(status IN ('PLANNED', 'WATCHED')),
            date_added TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
            UNIQUE(user_id, tmdb_movie_id))""")
        db.execSQL("""CREATE TABLE reviews (
            review_id INTEGER PRIMARY KEY AUTOINCREMENT,
            watchlist_id INTEGER NOT NULL UNIQUE REFERENCES watchlist(watchlist_id) ON DELETE CASCADE,
            rating INTEGER NOT NULL CHECK(rating BETWEEN 1 AND 5),
            notes TEXT,
            updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP)""")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE watchlist ADD COLUMN movie_year INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE watchlist ADD COLUMN movie_genre TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE watchlist ADD COLUMN movie_source TEXT NOT NULL DEFAULT 'LEGACY'")
            val titles = listOf("Paddington", "Little Women", "Interstellar", "The Conjuring", "Knives Out", "Finding Nemo")
            titles.forEachIndexed { index, title ->
                db.execSQL("UPDATE watchlist SET tmdb_movie_id = ?, movie_source = 'SAMPLE' WHERE tmdb_movie_id = ? AND movie_title = ?",
                    arrayOf<Any>(-(index + 1), index + 1, title))
            }
        }
    }
}
