package com.privara.photoapp.view.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.privara.photoapp.R
import com.privara.photoapp.application.PhotoAppApplication
import com.privara.photoapp.databinding.FragmentAllPhotoBinding
import com.privara.photoapp.model.entities.Photo
import com.privara.photoapp.view.activities.AddUpdatePhotoActivity
import com.privara.photoapp.view.activities.MainActivity
import com.privara.photoapp.view.adapters.PhotoAdapter
import com.privara.photoapp.viewmodel.PhotoViewModel
import com.privara.photoapp.viewmodel.PhotoViewModelFactory

class AllPhotoFragment : Fragment() {

    private var _binding: FragmentAllPhotoBinding? = null
    private val binding get() = _binding!!

    private lateinit var photoAdapter: PhotoAdapter

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
        setupRecyclerView()
        observePhotos()
    }

    private fun setupRecyclerView() {
        binding.rvPhotoList.layoutManager = GridLayoutManager(requireContext(), 2)
        photoAdapter = PhotoAdapter(fragment = this)
        binding.rvPhotoList.adapter = photoAdapter
    }

    private fun observePhotos() {
        // Observe the LiveData from ViewModel
        photoViewModel.allPhotos.observe(viewLifecycleOwner) { photos ->
            // Update the list in the adapter
            photoAdapter.submitList(photos)
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
                // Implement your filtering logic here
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
