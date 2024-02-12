package com.privara.photoapp.view.activities

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import com.privara.photoapp.R
import com.privara.photoapp.application.PhotoAppApplication
import com.privara.photoapp.databinding.ActivityAddUpdatePhotoBinding
import com.privara.photoapp.databinding.DialogCustomImageSelectionBinding
import com.privara.photoapp.databinding.DialogCustomListBinding
import com.privara.photoapp.model.entities.Photo
import com.privara.photoapp.utils.Constants
import com.privara.photoapp.view.adapters.CustomListItemAdapter
import com.privara.photoapp.viewmodel.PhotoViewModel
import com.privara.photoapp.viewmodel.PhotoViewModelFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class AddUpdatePhotoActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var mBinding: ActivityAddUpdatePhotoBinding
    private var mImagePath: String = ""
    private var mPhotoDetails : Photo? = null
    private val mPhotoViewModel: PhotoViewModel by viewModels{
        PhotoViewModelFactory((application as PhotoAppApplication).repository)
    }

    private lateinit var mCustomListDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityAddUpdatePhotoBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        if (intent.hasExtra(Constants.EXTRA_PHOTO_DETAILS)) {
            mPhotoDetails = intent.getParcelableExtra(Constants.EXTRA_PHOTO_DETAILS)
        }

        setupActionBar()

        mPhotoDetails?.let { photo ->
            mImagePath = photo.image
            Glide.with(this).load(mImagePath).centerCrop().into(mBinding.ivPhotoImage)
            mBinding.etTitle.setText(photo.title)
            mBinding.etCategory.setText(photo.category)
            mBinding.etDescription.setText(photo.description)
            mBinding.etDate.setText(photo.date)
            mBinding.btnAddPhoto.text = getString(R.string.lbl_update_photo)
        }

        mBinding.ivAddPhotoImage.setOnClickListener(this)
        mBinding.etCategory.setOnClickListener(this)
        mBinding.btnAddPhoto.setOnClickListener(this)
        mBinding.etDate.setOnClickListener {
            showDatePickerDialog()
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.iv_add_photo_image -> customImageSelectionDialog()
            R.id.et_category -> customItemsListDialog(
                title = getString(R.string.title_select_photo_category),
                itemsList = Constants.photoCategories(),
                selection = Constants.PHOTO_CATEGORY
            )
            R.id.btn_add_photo -> {
                val title = mBinding.etTitle.text.toString().trim()
                val date = mBinding.etDate.text.toString().trim()
                val category = mBinding.etCategory.text.toString().trim()
                val description = mBinding.etDescription.text.toString().trim()

                if (validateForm(title, date, category, description)) {
                    val updatedPhoto = mPhotoDetails?.copy(
                        image = mImagePath,
                        title = title,
                        date = date,
                        category = category,
                        description = description
                    ) ?: Photo(
                        image = mImagePath,
                        imageSource = if (mImagePath.startsWith("http")) Constants.PHOTO_IMAGE_SOURCE_ONLINE else Constants.PHOTO_IMAGE_SOURCE_LOCAL,
                        title = title,
                        date = date,
                        category = category,
                        description = description
                    )

                    if (updatedPhoto.id == 0) {
                        mPhotoViewModel.insert(updatedPhoto)
                        showToast("Photo details added.")
                    } else {
                        mPhotoViewModel.update(updatedPhoto)
                        showToast("Photo details updated.")
                    }
                    finish()
                }
            }
        }
    }

    private fun validateForm(title: String, date: String, category: String, description: String): Boolean {
        return when {
            mImagePath.isEmpty() -> showToast(getString(R.string.err_msg_select_photo_image)).let { false }
            title.isEmpty() -> showToast(getString(R.string.err_msg_enter_photo_title)).let { false }
            date.isEmpty() -> showToast(getString(R.string.err_msg_enter_photo_date)).let { false }
            category.isEmpty() -> showToast(getString(R.string.err_msg_select_photo_category)).let { false }
            description.isEmpty() -> showToast(getString(R.string.err_msg_enter_photo_description)).let { false } // Assuming you want to correct the string resource here
            else -> true
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA -> {
                    // Camera Intent
                    val thumbnail: Bitmap = data?.extras?.get("data") as Bitmap
                    Glide.with(this@AddUpdatePhotoActivity)
                        .load(thumbnail)
                        .centerCrop()
                        .into(mBinding.ivPhotoImage)

                    mImagePath = saveImageToInternalStorage(thumbnail)
                    Log.i("ImagePath", mImagePath)

                    mBinding.ivAddPhotoImage.setImageDrawable(
                        ContextCompat.getDrawable(this@AddUpdatePhotoActivity, R.drawable.ic_vector_edit)
                    )
                }
                GALLERY -> {
                    val selectedPhotoUri: Uri? = data?.data
                    selectedPhotoUri?.let { uri ->
                        Glide.with(this@AddUpdatePhotoActivity)
                            .load(uri)
                            .centerCrop()
                            .into(mBinding.ivPhotoImage)
                        val bitmap: Bitmap? = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                        bitmap?.let {
                            mImagePath = saveImageToInternalStorage(it)
                            Log.i("ImagePath", mImagePath)
                        }
                        mBinding.ivAddPhotoImage.setImageDrawable(
                            ContextCompat.getDrawable(this@AddUpdatePhotoActivity, R.drawable.ic_vector_edit)
                        )
                    }
                }
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Log.e("ActivityResult", "Operation Cancelled")
        }
    }

    private fun setupActionBar() {
        setSupportActionBar(mBinding.toolbarAddPhotoActivity)
        supportActionBar?.title = if (mPhotoDetails != null && mPhotoDetails!!.id != 0) {
            getString(R.string.title_edit_photo)
        } else {
            getString(R.string.title_add_photo)
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        mBinding.toolbarAddPhotoActivity.setNavigationOnClickListener { onBackPressed() }
    }

    private fun customImageSelectionDialog() {
        val dialog = Dialog(this)
        val binding = DialogCustomImageSelectionBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)

        binding.tvCamera.setOnClickListener {
            requestMultiplePermissions { launchCamera() }
            dialog.dismiss()
        }

        binding.tvGallery.setOnClickListener {
            requestStoragePermission { openGallery() }
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun requestMultiplePermissions(action: () -> Unit) {
        Dexter.withContext(this)
            .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report?.areAllPermissionsGranted() == true) {
                        action()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?, token: PermissionToken?
                ) {
                    showRationalDialogForPermissions()
                }
            }).check()
    }

    private fun requestStoragePermission(action: () -> Unit) {
        Dexter.withContext(this)
            .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse?) = action()
                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    showToast("You have denied the storage permission to select an image.")
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?, token: PermissionToken?
                ) = showRationalDialogForPermissions()
            }).check()
    }

    private fun launchCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, CAMERA)
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, GALLERY)
    }

    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.permission_rationale))
            .setPositiveButton("GO TO SETTINGS") { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap): String {

        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)

        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file.absolutePath
    }

    private fun customItemsListDialog(title: String, itemsList: List<String>, selection: String) {
        mCustomListDialog = Dialog(this@AddUpdatePhotoActivity)

        val binding: DialogCustomListBinding = DialogCustomListBinding.inflate(layoutInflater)

        mCustomListDialog.setContentView(binding.root)

        binding.tvTitle.text = title

        binding.rvList.layoutManager = LinearLayoutManager(this@AddUpdatePhotoActivity)
        val adapter = CustomListItemAdapter(this@AddUpdatePhotoActivity, null,itemsList, selection)
        binding.rvList.adapter = adapter
        mCustomListDialog.show()
    }

    fun selectedListItem(item: String, selection: String) {
        if (selection == Constants.PHOTO_CATEGORY) {
            mBinding.etCategory.setText(item)
            mCustomListDialog.dismiss()
        }
    }

    companion object {
        private const val CAMERA = 1
        private const val GALLERY = 2
        private const val IMAGE_DIRECTORY = "PhotoImages"
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(this, { _, year, monthOfYear, dayOfMonth ->
            val selectedDate = String.format(Locale.getDefault(), "%d-%02d-%02d", year, monthOfYear + 1, dayOfMonth)
            mBinding.etDate.setText(selectedDate)
        }, year, month, day)

        dpd.show()
    }

}