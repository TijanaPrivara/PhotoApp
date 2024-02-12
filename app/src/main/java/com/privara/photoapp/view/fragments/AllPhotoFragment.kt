package com.privara.photoapp.view.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.privara.photoapp.R
import com.privara.photoapp.application.PhotoAppApplication
import com.privara.photoapp.databinding.FragmentAllPhotoBinding
import com.privara.photoapp.model.entities.Photo
import com.privara.photoapp.view.activities.AddUpdatePhotoActivity
import com.privara.photoapp.view.activities.MainActivity
import com.privara.photoapp.viewmodel.PhotoViewModel
import com.privara.photoapp.viewmodel.PhotoViewModelFactory

class AllPhotoFragment : Fragment() {

    private var _binding: FragmentAllPhotoBinding? = null
    private val binding get() = _binding!!

    private val photoViewModel: PhotoViewModel by viewModels {
        PhotoViewModelFactory((requireActivity().application as PhotoAppApplication).repository)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAllPhotoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        observePhotos()
    }

    private fun observePhotos() {
        photoViewModel.allPhotos.observe(viewLifecycleOwner) { photos ->
            displayPhotos(photos)
        }
    }

    private fun displayPhotos(photos: List<Photo>) {
        val container: LinearLayout = binding.photosContainer
        container.removeAllViews()

        photos.forEach { photo ->
            val photoItemLayout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also {
                    it.setMargins(8, 8, 8, 8)
                }
            }
            val imageView = ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    resources.getDimensionPixelSize(R.dimen._100sdp),
                    resources.getDimensionPixelSize(R.dimen._100sdp)
                )
                scaleType = ImageView.ScaleType.CENTER_CROP
            }
            Glide.with(this@AllPhotoFragment).load(photo.image).into(imageView)

            val titleView = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                text = photo.title
                textSize = 16f
            }
            photoItemLayout.addView(imageView)
            photoItemLayout.addView(titleView)
            photoItemLayout.setOnClickListener {
                photoDetails(photo)
            }
            container.addView(photoItemLayout)
        }
    }


    fun photoDetails(photo: Photo) {
        val action = AllPhotoFragmentDirections.actionAllPhotoToPhotoDetails(photo)
        findNavController().navigate(action)
        (activity as? MainActivity)?.hideBottomNavigationView()
    }

    fun deletePhoto(photo: Photo) {
        photoViewModel.delete(photo)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_all_photo, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add_photo -> {
                startActivity(Intent(context, AddUpdatePhotoActivity::class.java))
                return true
            }
            R.id.action_filter_photo -> {
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
