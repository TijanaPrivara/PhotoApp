package com.privara.photoapp.utils

object Constants {

    const val PHOTO_CATEGORY: String = "PhotoCategory"
    const val PHOTO_IMAGE_SOURCE_LOCAL: String = "Local"
    const val PHOTO_IMAGE_SOURCE_ONLINE: String = "Online"
    const val ALL_ITEMS: String = "All"
    const val EXTRA_PHOTO_DETAILS = "EXTRA_PHOTO_DETAILS"

    fun photoCategories(): ArrayList<String> {
        val list = ArrayList<String>()
        list.add("Priroda")
        list.add("Grad")
        list.add("Portret")
        return list
    }

}