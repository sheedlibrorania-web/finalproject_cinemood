package com.example.cinemood.data

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class DatabaseIntegrationTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val dbName = "cinemood_integration_test.db"
    private lateinit var repo: CineMoodRepository
    @Before fun setup() { context.deleteDatabase(dbName); repo = CineMoodRepository(context, dbName) }
    @After fun cleanup() { repo.close(); context.deleteDatabase(dbName) }
    private fun user(name: String = "Alice") = requireNotNull(repo.register(name, "password123"))
    private fun movie(user: Long) = requireNotNull(repo.addMovie(user, 42L, "Sample film"))

    @Test fun registrationAndLogin() { val id = user(); assertEquals(id, repo.login(" Alice ", "password123")) }
    @Test fun rejectsInvalidRegistration() {
        assertNull(repo.register(" ", "password123")); assertNull(repo.register("Alice", "1234567"))
        assertNull(repo.register("Alice", "        "))
        listOf("abcd 1234", " password123", "password123 ", "abcd\t1234", "abcd\u00a01234").forEach {
            assertNull(repo.register("Alice", it))
        }
        assertFalse(repo.usernameExists("Alice"))
    }
    @Test fun rejectsCaseInsensitiveDuplicate() { user(); assertNull(repo.register("ALICE", "otherpass123")) }
    @Test fun rejectsWrongCredentials() { user(); assertNull(repo.login("Alice", "incorrect")); assertNull(repo.login("", "")) }
    @Test fun passwordHashesAreSaltedAndNotPlaintext() {
        user(); user("Bob")
        CineMoodDbHelper(context, dbName).use { helper ->
            helper.readableDatabase.rawQuery("SELECT password_hash FROM users ORDER BY user_id", null).use {
                assertTrue(it.moveToFirst()); val first = it.getString(0)
                assertNotEquals("password123", first); assertTrue(it.moveToNext()); assertNotEquals(first, it.getString(0))
            }
        }
    }
    @Test fun movieCreateReadAndDuplicate() {
        val uid = user(); val id = movie(uid)
        assertEquals(id, repo.getMovies(uid).single().watchlistId)
        assertNull(repo.addMovie(uid, 42L, "Sample film"))
        assertNull(repo.addMovie(uid, 44L, " "))
    }
    @Test fun statusValidationAndUpdate() {
        val uid = user(); val id = movie(uid)
        assertFalse(repo.updateStatus(uid, id, "invalid")); assertTrue(repo.updateStatus(uid, id, "WATCHED"))
        assertEquals("WATCHED", repo.getMovies(uid).single().status)
    }
    @Test fun reviewCreateReadUpdateDelete() {
        val uid = user(); val id = movie(uid)
        assertTrue(repo.saveReview(uid, id, 4, "First")); val reviewId = repo.getReview(uid, id)!!.reviewId
        assertTrue(repo.saveReview(uid, id, 5, "Edited")); val review = repo.getReview(uid, id)!!
        assertEquals(reviewId, review.reviewId); assertEquals(5, review.rating); assertEquals("Edited", review.notes)
        assertTrue(repo.deleteReview(uid, id)); assertNull(repo.getReview(uid, id))
    }
    @Test fun invalidRatingRejected() {
        val uid = user(); val id = movie(uid)
        assertFalse(repo.saveReview(uid, id, 0, null)); assertFalse(repo.saveReview(uid, id, 6, null))
        assertNull(repo.getReview(uid, id))
    }
    @Test fun accountsCannotAccessEachOthersRecords() {
        val alice = user(); val bob = user("Bob"); val id = movie(alice)
        assertTrue(repo.saveReview(alice, id, 5, "Private"))
        assertTrue(repo.getMovies(bob).isEmpty()); assertNull(repo.getReview(bob, id))
        assertFalse(repo.updateStatus(bob, id, "WATCHED")); assertFalse(repo.deleteMovie(bob, id))
        assertFalse(repo.saveReview(bob, id, 1, "overwrite")); assertFalse(repo.deleteReview(bob, id))
        assertEquals("Private", repo.getReview(alice, id)!!.notes)
    }
    @Test fun deleteMovieCascadesReview() {
        val uid = user(); val id = movie(uid); repo.saveReview(uid, id, 4, "Test")
        assertTrue(repo.deleteMovie(uid, id)); assertTrue(repo.getMovies(uid).isEmpty())
        CineMoodDbHelper(context, dbName).use { helper ->
            helper.readableDatabase.rawQuery("SELECT COUNT(*) FROM reviews", null).use {
                it.moveToFirst(); assertEquals(0, it.getInt(0))
            }
        }
    }
    @Test fun dataSurvivesDatabaseReopen() {
        val uid = user(); val id = movie(uid); repo.saveReview(uid, id, 3, "Persistent")
        repo.close(); repo = CineMoodRepository(context, dbName)
        assertEquals(uid, repo.login("Alice", "password123")); assertEquals(id, repo.getMovies(uid).single().watchlistId)
        assertEquals("Persistent", repo.getReview(uid, id)!!.notes)
    }
}
