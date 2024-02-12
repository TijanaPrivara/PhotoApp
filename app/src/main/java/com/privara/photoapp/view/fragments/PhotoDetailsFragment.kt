package com.privara.photoapp.view.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.privara.photoapp.R
import com.privara.photoapp.application.PhotoAppApplication
import com.privara.photoapp.databinding.FragmentPhotoDetailsBinding
import com.privara.photoapp.view.activities.AddUpdatePhotoActivity
import com.privara.photoapp.viewmodel.PhotoViewModel
import com.privara.photoapp.viewmodel.PhotoViewModelFactory

class PhotoDetailsFragment : Fragment() {

    private var _binding: FragmentPhotoDetailsBinding? = null
    private val binding get() = _binding!!
    private val photoViewModel: PhotoViewModel by viewModels {
        PhotoViewModelFactory((requireActivity().application as PhotoAppApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhotoDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args: PhotoDetailsFragmentArgs by navArgs()
        val photoDetails = args.photoDetails

        Glide.with(this)
            .load(photoDetails.image)
            .into(binding.ivPhotoImage)

        binding.tvTitle.text = photoDetails.title
        binding.tvCategory.text = getString(R.string.photo_category, photoDetails.category)
        binding.tvDescription.text = photoDetails.description
        binding.tvDate.text = getString(R.string.photo_date, photoDetails.date)

        binding.btnUpdatePhoto.setOnClickListener {
            val updateIntent = Intent(context, AddUpdatePhotoActivity::class.java).apply {
                putExtra("EXTRA_PHOTO_DETAILS", photoDetails)
            }
            startActivity(updateIntent)
        }

        binding.btnDeletePhoto.setOnClickListener {
            confirmDeletion {
                photoViewModel.delete(photoDetails)
                Toast.makeText(context, "Photo deleted successfully", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun confirmDeletion(onConfirm: () -> Unit) {
        AlertDialog.Builder(requireContext()).apply {
            setTitle("Delete Photo")
            setMessage("Are you sure you want to delete this photo?")
            setPositiveButton("Delete") { _, _ -> onConfirm() }
            setNegativeButton("Cancel", null)
        }.create().show()
    }
}
