package com.privara.photoapp.model.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.privara.photoapp.model.entities.Photo

@Database(entities = [Photo::class], version = 1, exportSchema = false)
abstract class PhotoAppRoomDatabase : RoomDatabase() {
    abstract fun photoDao(): PhotoAppDao

    companion object {
        @Volatile
        private var INSTANCE: PhotoAppRoomDatabase? = null

        fun getDatabase(context: Context): PhotoAppRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PhotoAppRoomDatabase::class.java,
                    "photo_database"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
