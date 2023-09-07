package com.furkanharmanci.kotlinartbook.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.furkanharmanci.kotlinartbook.R
import com.furkanharmanci.kotlinartbook.databinding.ActivitySecondBinding
import com.google.android.material.snackbar.Snackbar
import java.io.ByteArrayOutputStream

class SecondActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySecondBinding
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionResultLauncher: ActivityResultLauncher<String>
    private var selectedBitmap : Bitmap? = null
    private val accessStorage = Manifest.permission.READ_EXTERNAL_STORAGE
    private val permissionGallery = "Permission needed for gallery"
    private val permissionGranted = "Give Permission"
    private val permissionNeed = "Permission Needed!"
    private lateinit var database : SQLiteDatabase
    private var artTitle = "Art"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecondBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        database = this.openOrCreateDatabase("Arts", MODE_PRIVATE, null)

        registerLauncher()
        val intent = intent
        val info = intent.getStringExtra("info")

        if (info.equals("new")) {
            binding.artName.setText("")
            binding.artistName.setText("")
            binding.yearName.setText("")
            binding.infoAdd.visibility = View.VISIBLE
            binding.image.setImageResource(R.drawable.image_picker)
        } else {
            binding.infoAdd.visibility = View.INVISIBLE
            binding.addTitle.text = artTitle
            val selectedId = intent.getIntExtra("id", 0)

            val cursor = database.rawQuery("SELECT * FROM arts WHERE id = ?", arrayOf(selectedId.toString()))
            val artNameIx = cursor.getColumnIndex("artName")
            val artistNameIx = cursor.getColumnIndex("artistName")
            val yearNameIx = cursor.getColumnIndex("yearName")
            val imageIx = cursor.getColumnIndex("image")

            while (cursor.moveToNext()) {
                binding.artName.setText(cursor.getString(artNameIx))
                binding.artistName.setText(cursor.getString(artistNameIx))
                binding.yearName.setText(cursor.getString(yearNameIx))

                val byteArray = cursor.getBlob(imageIx)
                val imageBitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
                binding.image.setImageBitmap(imageBitmap)
            }
            cursor.close()
        }
    }

    fun infoAdd(view: View) {
        val artName = binding.artName.text.toString()
        val artistName = binding.artistName.text.toString()
        val yearName = binding.yearName.text.toString()

        if (selectedBitmap != null) {
            val smallBitmap = makeSmallerBitmap(selectedBitmap!!, 300)
            val outputStream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG, 50, outputStream)
            val byteArray = outputStream.toByteArray()

            try {
                database.execSQL("CREATE TABLE IF NOT EXISTS arts (id INTEGER PRIMARY KEY, artName VARCHAR, artistName VARCHAR, yearName VARCHAR, image BLOB)")
                val sqlString = "INSERT INTO arts (artName, artistName, yearName, image) VALUES (?, ?, ?, ?)"
                val statement = database.compileStatement(sqlString)
                statement.bindString(1, artName)
                statement.bindString(2, artistName)
                statement.bindString(3, yearName)
                statement.bindBlob(4, byteArray)
                statement.execute()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val intent = Intent(this@SecondActivity, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
    }

    fun imagePicker(view: View) {
        if (ContextCompat.checkSelfPermission(this@SecondActivity, accessStorage) != PackageManager.PERMISSION_GRANTED) {
            // rationale
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@SecondActivity, accessStorage)) {
                Snackbar.make(view, permissionGallery, Snackbar.LENGTH_INDEFINITE).setAction(permissionGranted
                ) {
                    /// request permission
                    permissionResultLauncher.launch(accessStorage)
                }.show()
            } else {
                // request
                permissionResultLauncher.launch(accessStorage)
            }
        } else {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            activityResultLauncher.launch(intent)
        }
    }

    private fun registerLauncher() {
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {result ->
            if (result.resultCode == RESULT_OK) {
                val intentFromResult = result.data

                if (intentFromResult != null) {
                    val imageUri = intentFromResult.data
                    //binding.image.setImageURI(imageUri)
                    // Bunu yapabiliriz ancak görselin bitmap tipi, görseli sqlite db'ye kaydetmek için bize yardımcı olur. Db'ye uri tipinde değişken kaydedemeyiz.

                    if (imageUri != null) {
                        try {
                            /// ImageDecoder hem kaynak oluşturma hem de kaynağı çevirme'de kullanılan bir sınıf
                            /// uri bir kaynağa(source) çevriliyor.
                            val source = ImageDecoder.createSource(this@SecondActivity.contentResolver,imageUri)

                            /// bu kaynak bir bitmap'e çevriliyor.
                            selectedBitmap = ImageDecoder.decodeBitmap(source)

                            // bitmap tipindeki bu görseli UI'a aktardık.
                            binding.image.setImageBitmap(selectedBitmap)
                        } catch (e : Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
        permissionResultLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {permission ->
            if (permission) {
                ///permission granted
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intent)
            } else {
                ///permission denied
                Toast.makeText(this@SecondActivity, permissionNeed, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun makeSmallerBitmap(imageBitmap : Bitmap, maximumSize : Int) : Bitmap {
        var width = imageBitmap.width
        var height = imageBitmap.height

        val bitmapRatio : Double = width.toDouble() / height.toDouble()

        if (bitmapRatio > 1) {
            //landscape
            width = maximumSize
            val scaledHeight = width / bitmapRatio
            height = scaledHeight.toInt()
        } else {
            //portrait
        }

        return Bitmap.createScaledBitmap(imageBitmap,width, height, true)
    }
}