package com.privara.photoapp.model.database

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.privara.photoapp.model.entities.Photo

class PhotoAppRepository(private val photoAppDao: PhotoAppDao) {

    val allPhotos: LiveData<List<Photo>> = photoAppDao.getAllPhotos()

    @WorkerThread
    suspend fun insertPhotoData(photo: Photo) {
        photoAppDao.insertPhoto(photo)
    }

    @WorkerThread
    suspend fun updatePhotoData(photo: Photo) {
        photoAppDao.updatePhoto(photo)
    }

    @WorkerThread
    suspend fun deletePhotoData(photo: Photo) {
        photoAppDao.deletePhoto(photo)
    }
}
