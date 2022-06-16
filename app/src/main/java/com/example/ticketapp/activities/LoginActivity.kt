package com.example.ticketapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.example.ticketapp.R
import com.example.ticketapp.databinding.ActivityLoginBinding
import com.example.ticketapp.utilities.Constants
import com.example.ticketapp.utilities.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(applicationContext)
        mAuth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()

        setListener()
    }

    private fun setListener() {
        binding.btnKeRegist.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val pass = binding.etPass.text.toString().trim()

            if (notEmpty(email, pass)){
                login(email, pass)
            }
        }

    }

    private fun notEmpty(email: String, pass: String): Boolean {
        if (email.isEmpty()) {
            showToast("Email Wajib Diisi !")
            binding.etEmail.requestFocus()
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Email Tidak Valid !")
            binding.etEmail.requestFocus()
            return false
        } else if (pass.isEmpty() || pass.length < 6) {
            showToast("Password minimal 6 karakter !")
            binding.etPass.requestFocus()
            return false
        } else {
            return true
        }
    }

    private fun login(email: String, pass: String) {
        loading(true)

        mAuth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    database.collection(Constants.KEY_COLLECTION_USERS)
                        .whereEqualTo(Constants.KEY_USER_ID, mAuth.currentUser?.uid)
                        .get()
                        .addOnCompleteListener(this) {
                            if (it.isSuccessful && it.result != null && it.result.documents.size > 0) {
                                val documentSnapshot: DocumentSnapshot = it.result.documents[0]
//                                preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true)
                                preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.id)
                                preferenceManager.putString(Constants.KEY_ASAL, "-")
                                preferenceManager.putString(Constants.KEY_TUJUAN, "-")
                                preferenceManager.putString(Constants.KEY_TANGGAL, "-")

                                documentSnapshot.getString(Constants.KEY_NAME)?.let { it1 ->
                                    preferenceManager.putString(Constants.KEY_NAME, it1)
                                }
                                documentSnapshot.getString(Constants.KEY_IMAGE)?.let { it1 ->
                                    preferenceManager.putString(
                                        Constants.KEY_IMAGE, it1
                                    )
                                }

                                Intent(applicationContext, MainActivity::class.java).also {
                                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(it)
                                    showToast("!!! Selamat Datang !!!")
                                }
                            }
                            else{
                                loading(false)
                                showToast("Login Gagal \n Pastikan akun anda telah terdaftar")
                            }

                        }
                }
                else{
                    loading(false)
                    showToast("Login Gagal \n Pastikan akun anda telah terdaftar")
                }
            }
    }

    private fun loading(isLoading: Boolean) {
        if (isLoading){
            binding.progressBar.visibility = View.VISIBLE
            binding.btnDanLoading.visibility = View.INVISIBLE
        }else{
            binding.progressBar.visibility = View.INVISIBLE
            binding.btnDanLoading.visibility = View.VISIBLE
        }
    }
    private fun showToast(pesan: String){
        Toast.makeText(applicationContext, pesan, Toast.LENGTH_SHORT).show()
    }

    override fun onStart() {
        super.onStart()
        val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
        if (mAuth.currentUser != null) {
            Intent(this@LoginActivity, MainActivity::class.java).also { intent ->
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }
}