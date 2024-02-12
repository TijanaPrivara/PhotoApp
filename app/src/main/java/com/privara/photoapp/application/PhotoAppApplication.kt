package com.privara.photoapp.application

import android.app.Application
import com.privara.photoapp.model.database.PhotoAppRepository
import com.privara.photoapp.model.database.PhotoAppRoomDatabase

class PhotoAppApplication : Application() {

    private val database by lazy{ PhotoAppRoomDatabase.getDatabase(this)}
    val repository by lazy { PhotoAppRepository(database.photoDao()) }

}