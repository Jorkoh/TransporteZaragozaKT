package com.jorkoh.transportezaragozakt.db.daos

import androidx.room.*
import com.jorkoh.transportezaragozakt.db.FavoritePositions
import com.jorkoh.transportezaragozakt.db.FavoriteStop
import com.jorkoh.transportezaragozakt.db.FavoriteStopExtended
import com.jorkoh.transportezaragozakt.db.Stop
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritesDao {
    @Query("SELECT isFavorite FROM stops WHERE stopId = :stopId")
    fun stopIsFavorite(stopId: String): Flow<Boolean>

    @Query("DELETE FROM favoriteStops WHERE stopId = :stopId")
    suspend fun deleteFavorite(stopId: String)

    @Query("DELETE FROM favoriteStops")
    suspend fun deleteAllFavorites()

    @Query("UPDATE favoriteStops SET position = :newPosition WHERE stopId = :stopId")
    suspend fun updatePosition(stopId: String, newPosition: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteStop(favoriteStop: FavoriteStop)

    @Query("UPDATE stops SET isFavorite = :isFavorite WHERE stopId = :stopId")
    fun updateIsFavorite(stopId: String, isFavorite: Boolean)

    @Query("SELECT favoriteStops.stopId, stops.type, stops.number, stops.stopTitle, favoriteStops.alias, favoriteStops.colorHex, stops.lines FROM stops INNER JOIN favoriteStops ON stops.stopId = favoriteStops.stopId ORDER BY favoriteStops.position ASC")
    fun getFavoriteStops(): Flow<List<FavoriteStopExtended>>

    @Query("SELECT COUNT(*) FROM favoriteStops")
    fun getFavoriteCount(): Flow<Int>

    @Query("SELECT stopId, position FROM favoriteStops ORDER BY favoriteStops.position ASC")
    suspend fun getFavoritePositions(): List<FavoritePositions>

    @Query("UPDATE favoriteStops SET alias = :alias, colorHex = :colorHex WHERE stopId = :stopId")
    suspend fun updateFavorite(stopId: String, colorHex: String, alias: String)

    @Query("SELECT IFNULL(position, 0)+1 FROM favoriteStops ORDER BY position LIMIT 1")
    fun getLastPosition(): Int

    @Transaction
    suspend fun removeFavorite(stopId: String) {
        deleteFavorite(stopId)
        updateIsFavorite(stopId, false)
    }

    @Transaction
    suspend fun toggleFavorite(stop: Stop) {
        if (stop.isFavorite) {
            deleteFavorite(stop.stopId)
            updateIsFavorite(stop.stopId, false)
        } else {
            insertFavoriteStop(FavoriteStop(stop.stopId, stop.stopTitle, "", getLastPosition()))
            updateIsFavorite(stop.stopId, true)
        }
    }

    @Transaction
    suspend fun moveFavorite(from: Int, to: Int) {
        val initialPositions = getFavoritePositions()
        val finalPositions = initialPositions.toMutableList()

        finalPositions.removeAt(from)
        finalPositions.add(to, initialPositions[from])

        finalPositions.forEachIndexed { index, finalPosition ->
            if (finalPosition != initialPositions[index]) {
                updatePosition(finalPosition.stopId, index + 1)
            }
        }
    }
}