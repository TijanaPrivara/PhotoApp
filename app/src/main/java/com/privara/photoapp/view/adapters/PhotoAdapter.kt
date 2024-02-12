package com.privara.photoapp.view.adapters

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.privara.photoapp.R
import com.privara.photoapp.databinding.ItemPhotoLayoutBinding
import com.privara.photoapp.model.entities.Photo
import com.privara.photoapp.utils.Constants
import com.privara.photoapp.view.activities.AddUpdatePhotoActivity
import com.privara.photoapp.view.fragments.AllPhotoFragment

class PhotoAdapter(private val fragment: Fragment) : RecyclerView.Adapter<PhotoAdapter.ViewHolder>() {

    private var photos: List<Photo> = listOf()


    class ViewHolder(view: ItemPhotoLayoutBinding) : RecyclerView.ViewHolder(view.root) {

        val ivPhotoImage = view.ivPhotoImage
        val tvTile = view.tvPhotoTitle
        val ibMoore = view.ibMore

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemPhotoLayoutBinding =
            ItemPhotoLayoutBinding.inflate(LayoutInflater.from(fragment.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return photos.size
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val photo = photos[position]
        Glide.with(fragment)
            .load(photo.image)
            .into(holder.ivPhotoImage)
        holder.tvTile.text = photo.title

        holder.itemView.setOnClickListener {
            if (fragment is AllPhotoFragment) {
                fragment.photoDetails(photo)
            }
        }

        holder.ibMoore.setOnClickListener {

            val popup = PopupMenu(fragment.context, holder.ibMoore)
            popup.menuInflater.inflate(R.menu.menu_adapter, popup.menu)

            popup.setOnMenuItemClickListener {
                if (it.itemId == R.id.action_edit_photo) {
                    val intent =
                        Intent(fragment.requireActivity(), AddUpdatePhotoActivity::class.java)
                    intent.putExtra(Constants.EXTRA_PHOTO_DETAILS, photo)
                    fragment.requireActivity().startActivity(intent)
                    Log.i("You have clicked on", "Edit Option of ${photo.title}")
                } else if (it.itemId == R.id.action_delete_photo) {
                    if (fragment is AllPhotoFragment) {
                        fragment.deletePhoto(photo)
                        Log.i("You have clicked on", "Delete Option of ${photo.title}")
                    }
                }
                true
            }
            popup.show()
        }
        if (fragment is AllPhotoFragment) {
            holder.ibMoore.visibility = View.VISIBLE
        }
    }

    fun submitList(newPhotos: List<Photo>) {
        this.photos = newPhotos
        notifyDataSetChanged()
    }

}