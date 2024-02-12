package com.privara.photoapp.model.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.privara.photoapp.model.entities.Photo

@Dao
interface PhotoAppDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: Photo): Long

    @Update
    suspend fun updatePhoto(photo: Photo)

    @Delete
    suspend fun deletePhoto(photo: Photo)

    @Query("SELECT * FROM photo_table ORDER BY date DESC")
    fun getAllPhotos(): LiveData<List<Photo>>

    @Query("SELECT * FROM photo_table WHERE category = :category ORDER BY date DESC")
    fun getPhotosByCategory(category: String): LiveData<List<Photo>>
}