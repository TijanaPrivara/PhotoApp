package com.privara.photoapp.viewmodel

import androidx.lifecycle.*
import com.privara.photoapp.model.database.PhotoAppRepository
import com.privara.photoapp.model.entities.Photo
import kotlinx.coroutines.launch

class PhotoViewModel(private val repository: PhotoAppRepository) : ViewModel() {
    val allPhotos: LiveData<List<Photo>> = repository.allPhotos
    private val _editablePhoto = MutableLiveData<Photo?>()

    fun insert(photo: Photo) = viewModelScope.launch {
        repository.insertPhotoData(photo)
    }

    fun update(photo: Photo) = viewModelScope.launch {
        repository.updatePhotoData(photo)
    }

    fun delete(photo: Photo) = viewModelScope.launch {
        repository.deletePhotoData(photo)
    }


}

    class PhotoViewModelFactory(private val repository: PhotoAppRepository) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PhotoViewModel::class.java)) {
                @Suppress("UNCHECK_CAST")
                return PhotoViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel Class")
        }
    }
