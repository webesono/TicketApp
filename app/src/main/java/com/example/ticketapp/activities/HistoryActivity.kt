package com.example.ticketapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.example.ticketapp.R
import com.example.ticketapp.adapters.PesananAdapter
import com.example.ticketapp.databinding.ActivityHistoryBinding
import com.example.ticketapp.models.Pesanan
import com.example.ticketapp.utilities.Constants
import com.example.ticketapp.utilities.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var pesananList: ArrayList<Pesanan>
    private lateinit var pesananAdapter: PesananAdapter
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        database = FirebaseDatabase.getInstance()
        mAuth = FirebaseAuth.getInstance()
        pesananList = ArrayList()
        pesananAdapter = PesananAdapter(this, pesananList)
        binding.pesananRecycleView.adapter = pesananAdapter
        preferenceManager = PreferenceManager(applicationContext)

        getItems()
        setListener()
        if (preferenceManager.getString(Constants.KEY_POSITION) != "-"){
            getDetails()
        }

    }

    private fun setListener(){
        binding.back.setOnClickListener { onBackPressed() }
    }

    private fun getItems(){
        loading(true)
        database.reference.child(Constants.KEY_COLLECTION_PESANAN)
            .child(mAuth.currentUser?.uid!!)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    pesananList.clear()

                    for (postSnapshot in snapshot.children){
                        val pesanan = postSnapshot.getValue(Pesanan::class.java)
                        pesananList.add(pesanan!!)
                    }
                    pesananAdapter.notifyDataSetChanged()
                    loading(false)
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private fun getDetails(){
        val bottomSheetDialog = BottomSheetDialog(
            this, R.style.BottomSheetDialogTheme
        )
        val bottomSheetView: View = LayoutInflater.from(applicationContext)
            .inflate(
                R.layout.history_ticket,
                findViewById(R.id.cvHistoryContainer1)
            )
        bottomSheetDialog.setContentView(bottomSheetView)

        val tvKelas = bottomSheetView.findViewById<TextView>(R.id.tvKelas)
        val tvNama = bottomSheetView.findViewById<TextView>(R.id.tvNama)
        val tvDewasa = bottomSheetView.findViewById<TextView>(R.id.tvDewasa)
        val tvAnak = bottomSheetView.findViewById<TextView>(R.id.tvAnak)
        val tvTanggal = bottomSheetView.findViewById<TextView>(R.id.tvTanggalBerangkat)
        val tvAsal = bottomSheetView.findViewById<TextView>(R.id.tvKeberangkatan)
        val tvTujuan = bottomSheetView.findViewById<TextView>(R.id.tvTujuan)
        val tvTransport = bottomSheetView.findViewById<TextView>(R.id.tvTransport)

        tvKelas.text = intent.getStringExtra(Constants.KEY_KELAS)
        tvNama.text = intent.getStringExtra(Constants.KEY_NAMA)
        tvDewasa.text = intent.getStringExtra(Constants.KEY_JML_DEWASA)
        tvAnak.text = intent.getStringExtra(Constants.KEY_JML_ANAK)
        tvTanggal.text = intent.getStringExtra(Constants.KEY_TANGGAL)
        tvAsal.text = intent.getStringExtra(Constants.KEY_ASAL)
        tvTujuan.text = intent.getStringExtra(Constants.KEY_TUJUAN)
        tvTransport.text = intent.getStringExtra(Constants.KEY_TRANSPORT)

        bottomSheetDialog.show()
    }

    private fun loading(isLoading: Boolean){
        if (isLoading){
            binding.progressBar.visibility = View.VISIBLE
        }
        else{
            binding.progressBar.visibility = View.INVISIBLE
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(applicationContext, MainActivity::class.java))
    }
}