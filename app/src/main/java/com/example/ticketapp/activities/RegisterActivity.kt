package com.example.ticketapp.activities

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.ticketapp.R
import com.example.ticketapp.databinding.ActivityRegisterBinding
import com.example.ticketapp.utilities.Constants
import com.example.ticketapp.utilities.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.InputStream

class RegisterActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var binding: ActivityRegisterBinding

    private var encodedImage: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // bagian binding
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferenceManager = PreferenceManager(applicationContext)
        setListeners()
    }

    private fun setListeners(){
        binding.btnRegister.setOnClickListener{
            val nama = binding.etUsername.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()
            var image = encodedImage

            if (notEmpty(nama, email, pass, image)){
                registrasi(nama,email, pass, image)
            }
        }
        binding.layoutImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            pickImage.launch(intent)
        }
        binding.btnKeLogin.setOnClickListener{
            startActivity(Intent(applicationContext, LoginActivity::class.java))
        }
    }

    private fun showToast(pesan: String){
        Toast.makeText(applicationContext,pesan, Toast.LENGTH_SHORT).show()
    }

    private fun notEmpty(nama: String, email:String, pass: String, image: String):Boolean {
        if (nama.isEmpty()){
            showToast("Nama Wajib Diisi !")
            binding.etUsername.requestFocus()
            return false
        }
        else if (image == ""){
            showToast("Masukkan foto anda !")
            return false
        }
        else if (email.isEmpty()){
            showToast("Email Wajib Diisi !")
            binding.etEmail.requestFocus()
            return false
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            showToast("Email Tidak Valid !")
            binding.etEmail.requestFocus()
            return false
        }
        else if (pass.isEmpty() || pass.length<6){
            showToast("Password minimal 6 karakter !")
            binding.etPassword.requestFocus()
            return false
        }
        else if (binding.etConfPassword.text.isEmpty()){
            showToast("Konfirmasi password belum diisi !")
            binding.etPassword.requestFocus()
            return false
        }
        else if (binding.etConfPassword.text.toString() != pass){
            showToast("Konfirmasi password tidak sesuai !")
            binding.etConfPassword.requestFocus()
            return false
        }
        else{
            return true
        }
    }

    private fun registrasi(nama: String, email:String, pass: String, image: String){

        loading(true)

        mAuth = FirebaseAuth.getInstance()
        mAuth.createUserWithEmailAndPassword(email,pass)
            .addOnCompleteListener(this){ task ->
                if (task.isSuccessful){

                    dataUser(nama, image)
                    preferenceManager.putString(Constants.KEY_USER_ID, mAuth.currentUser?.uid!!)
                    preferenceManager.putString(Constants.KEY_NAME, nama)
                    preferenceManager.putString(Constants.KEY_IMAGE, image)
                    preferenceManager.putString(Constants.KEY_ASAL, "-")
                    preferenceManager.putString(Constants.KEY_TUJUAN, "-")
                    preferenceManager.putString(Constants.KEY_TANGGAL, "-")
                    Intent(applicationContext, MainActivity::class.java).also {
                        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(it)
                    }
                    showToast("Anda berhasil register")

                }else{
                    showToast("Gagal Register. Email mungkin telah digunakan")
                    loading(false)
                }
            }
    }

    private fun dataUser(nama: String, image: String){
        database = FirebaseFirestore.getInstance()
        val uid = mAuth.currentUser?.uid!!
        val user = hashMapOf(
            "name" to nama,
            Constants.KEY_IMAGE to image,
            Constants.KEY_USER_ID to uid
        )
        database.collection(Constants.KEY_COLLECTION_USERS)
            .document(uid)
            .set(user)
            .addOnSuccessListener {
                loading(false)
            }
            .addOnFailureListener { e ->
                Log.w(ContentValues.TAG, "Error adding document", e) }

    }

    private fun encodeImage(bitmap: Bitmap): String{
        val previewWidth = 150
        val previewHeight: Int = bitmap.height * previewWidth / bitmap.width
        val previewBitmap: Bitmap = Bitmap.createScaledBitmap(bitmap, previewWidth,previewHeight,false)
        val byteArrayOutputStream = ByteArrayOutputStream()
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
        val bytes: ByteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    private val pickImage: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            if (it.data != null) {
                val imageUri: Uri = it.data!!.data!!
                try {
                    val inputStream: InputStream? = contentResolver.openInputStream(imageUri)
                    val bitmap: Bitmap = BitmapFactory.decodeStream(inputStream)
                    binding.imageProfile.setImageBitmap(bitmap)
                    binding.textAddImage.visibility = View.GONE
                    encodedImage = encodeImage(bitmap)
//                    intent.putExtra(Constants.KEY_ENCODED_IMAGE, encodedImage )

                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }

            }
        }
    }

    private fun loading(isLoading: Boolean){
        if (isLoading){
            binding.btnRegister.visibility = View.INVISIBLE
            binding.progressBar.visibility = View.VISIBLE
        }else{
            binding.progressBar.visibility = View.INVISIBLE
            binding.btnRegister.visibility = View.VISIBLE
        }
    }
}