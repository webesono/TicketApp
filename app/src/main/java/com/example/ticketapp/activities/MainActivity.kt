package com.example.ticketapp.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import com.example.ticketapp.R
import com.example.ticketapp.databinding.ActivityMainBinding
import com.example.ticketapp.utilities.Constants
import com.example.ticketapp.utilities.PreferenceManager
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var mAuth: FirebaseAuth

    private val TIME_INTERVAL = 2000
    private var backPressed: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferenceManager = PreferenceManager(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager.putString(Constants.KEY_POSITION, "-")

        setListener()
        loadUserDetails()
    }

    private fun setListener(){
        binding.plane.setOnClickListener {
            klikBeli(Constants.KEY_PLANE)
        }
        binding.train.setOnClickListener {
            klikBeli(Constants.KEY_TRAIN)
        }
        binding.ship.setOnClickListener {
            klikBeli(Constants.KEY_SHIP)
        }
        binding.bus.setOnClickListener {
            klikBeli(Constants.KEY_BUS)
        }
        binding.lihatHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
        binding.imageProfile.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
        binding.btnLogout.setOnClickListener {
            logout()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadUserDetails(){

        val nama = preferenceManager.getString(Constants.KEY_NAME)
        val asal = preferenceManager.getString(Constants.KEY_ASAL)
        val tujuan = preferenceManager.getString(Constants.KEY_TUJUAN)
        val tanggal = preferenceManager.getString(Constants.KEY_TANGGAL)

        binding.tvWelcome.text = "Welcome, $nama"



        if (asal != "-" || tujuan != "-" || tanggal!= "-"){
            binding.lastPlace.text = "$asal menuju $tujuan"
            binding.lastTime.text = "Pada tanggal $tanggal"
        }
        else{
            binding.atas.text = ""
            binding.lastPlace.text = "Kamu belum bepergian akhir-akhir ini"
            binding.lastTime.text = "Cobalah untuk jalan-jalan atau healing gitu"
        }

        val bytes: ByteArray = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT)
        val bitmap: Bitmap = BitmapFactory.decodeByteArray(bytes, 0,bytes.size)
        binding.imageProfile.setImageBitmap(bitmap)
    }

    private fun klikBeli(textTransport: String){
        preferenceManager.putString(Constants.KEY_TRANSPORT, textTransport)
        startActivity(Intent(this, InputDataActivity::class.java))
    }

    private fun showToast(pesan: String){
        Toast.makeText(applicationContext, pesan, Toast.LENGTH_SHORT).show()
    }

    private fun logout(){
        showToast("Signing out ...")
        mAuth = FirebaseAuth.getInstance()
        mAuth.signOut()
        preferenceManager.clear()
        startActivity(Intent(applicationContext, LoginActivity::class.java))
        finish()
    }

    override fun onBackPressed() {
        if (backPressed + TIME_INTERVAL > System.currentTimeMillis() ){
            super.onBackPressed()
            return@onBackPressed
        }
        else{
            Toast.makeText(baseContext, "Tekan sekali lagi untuk keluar", Toast.LENGTH_SHORT).show()
        }
        backPressed = System.currentTimeMillis()

    }
}