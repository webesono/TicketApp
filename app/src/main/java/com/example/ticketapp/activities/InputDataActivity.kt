package com.example.ticketapp.activities

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.icu.text.DisplayContext
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.example.ticketapp.databinding.ActivityInputDataBinding
import com.example.ticketapp.utilities.Constants
import com.example.ticketapp.utilities.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import com.example.ticketapp.R
import com.example.ticketapp.models.Pesanan
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class InputDataActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInputDataBinding
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var database: FirebaseDatabase
    private lateinit var datastore: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth
    private lateinit var kotaList: ArrayList<String>
    private lateinit var kelasList: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferenceManager = PreferenceManager(applicationContext)
        binding = ActivityInputDataBinding.inflate(layoutInflater)
        setContentView(binding.root)
        database = FirebaseDatabase.getInstance()
        datastore = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()
        kotaList = ArrayList()
        kelasList = ArrayList()

        setListener()
    }

    @SuppressLint("SetTextI18n")
    private fun setListener(){
        var count = 0
        var count2 = 0

        binding.imageMinus1.setOnClickListener{
            if (count <= 0){
                count == 0
            }else{
                count --
                binding.tvJmlAnak.text = "" + count
            }
        }

        binding.imageMinus2.setOnClickListener {
            if (count2 <= 0){
                count == 0
            }else {
                count2--
                binding.tvJmlDewasa.text = "" + count2
            }
        }

        binding.imageAdd1.setOnClickListener {
            count ++
            binding.tvJmlAnak.text = "" + count
        }

        binding.imageAdd2.setOnClickListener {
            count2 ++
            binding.tvJmlDewasa.text = "" + count2
        }

        dropdownLokasi()
        dropdownKelas()
        calenderTanggal()

        binding.tukar.setOnClickListener { tukar() }

        binding.btnCheckout.setOnClickListener {
            if (isNotEmpty()){
                getSelected()
            }else{
                showToast("Gagal konfirmasi")
            }
        }

        binding.imageBack.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun dropdownLokasi(){
        database.reference.child(Constants.KEY_LOKASI).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                kotaList.clear()

                for (postSnapshot in snapshot.children) {
                    val spinnerLokasi =
                        postSnapshot.child(Constants.KEY_KOTA).value.toString()
                    kotaList.add(spinnerLokasi)
                }

                val arrayAdapter: ArrayAdapter<String> = ArrayAdapter(this@InputDataActivity, android.R.layout.simple_spinner_dropdown_item, kotaList)
                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spBerangkat.adapter = arrayAdapter
                binding.spTujuan.adapter = arrayAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun dropdownKelas(){
        database.reference.child(Constants.KEY_KELAS).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                kelasList.clear()

                for (postSnapshot in snapshot.children){
                    val spinnerKelas =
                        postSnapshot.child(Constants.KEY_TIPE).value.toString()
                    kelasList.add(spinnerKelas)
                }

                val arrayAdapter: ArrayAdapter<String> = ArrayAdapter(this@InputDataActivity, android.R.layout.simple_spinner_dropdown_item, kelasList)
                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spKelas.adapter = arrayAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun calenderTanggal(){
        val calender: Calendar = Calendar.getInstance()

        val datePicker = DatePickerDialog.OnDateSetListener { view, year, month, dayofMonth ->
            calender.set(Calendar.YEAR, year)
            calender.set(Calendar.MONTH, month)
            calender.set(Calendar.DAY_OF_MONTH, dayofMonth)
            updateLable(calender)

        }
        binding.inputTanggal.setOnClickListener {
            DatePickerDialog(this, datePicker,
                calender.get(Calendar.YEAR),
                calender.get(Calendar.MONTH),
                calender.get(Calendar.DAY_OF_MONTH)).show()
        }

    }

    private fun updateLable(calendar: Calendar){
        val myFormat = "dd-MM-yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        binding.inputTanggal.setText(sdf.format(calendar.time))
    }

    private fun tanggalReadable( date: Date): String{
        return SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(date)
    }

    private fun getSelected(){

        val namaPenumpang = binding.inputNama.text.toString().trim()
        val spBerangkat = binding.spBerangkat.selectedItem.toString().trim()
        val spTujuan = binding.spTujuan.selectedItem.toString().trim()
        val jmlAnak = binding.tvJmlAnak.text.toString().trim()
        val jmlDewasa = binding.tvJmlDewasa.text.toString().trim()
        val spKelas = binding.spKelas.selectedItem.toString().trim()
        val tglBerangkat = binding.inputTanggal.text.toString().trim()
        val telp = binding.inputTelepon.text.toString().trim()

        val transport = preferenceManager.getString(Constants.KEY_TRANSPORT).toString()

        val bottomSheetDialog = BottomSheetDialog(
            this, R.style.BottomSheetDialogTheme
        )
        val bottomSheetView: View = LayoutInflater.from(applicationContext)
            .inflate(
                R.layout.confirm_ticket,
                findViewById(R.id.cvHistoryContainer)
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
        val btnPesan = bottomSheetView.findViewById<MaterialButton>(R.id.btnPesan)
        val loading = bottomSheetView.findViewById<ProgressBar>(R.id.loading)

        tvKelas.text = spKelas
        tvNama.text = namaPenumpang
        tvDewasa.text = jmlDewasa
        tvAnak.text = jmlAnak
        tvTanggal.text = tglBerangkat
        tvAsal.text = spBerangkat
        tvTujuan.text = spTujuan
        tvTransport.text = transport

        bottomSheetDialog.show()

        btnPesan.setOnClickListener {
            isLoading(true,loading, btnPesan)
            inputDataToFirebase(spKelas, namaPenumpang, jmlAnak,telp, transport,
                jmlDewasa, tglBerangkat, spBerangkat, spTujuan)
            isLoading(false,loading, btnPesan)
        }
    }

    private fun inputDataToFirebase(spKelas: String, namaPenumpang: String, jmlAnak: String,telp: String, transport: String,
                                    jmlDewasa: String, tglBerangkat: String, spBerangkat: String, spTujuan: String){

        val idUser = mAuth.currentUser?.uid!!
        val dateNow = tanggalReadable(Date())

        val item = Pesanan(spKelas, namaPenumpang, jmlDewasa,
            jmlAnak, tglBerangkat, spBerangkat,
            spTujuan, transport, telp, idUser, dateNow)
        val dataTiket = listOf(
            Pesanan(spKelas, namaPenumpang, jmlDewasa,
                jmlAnak, tglBerangkat, spBerangkat,
                spTujuan, transport, telp, idUser, dateNow)
        )

        database.reference.child(Constants.KEY_COLLECTION_PESANAN)
            .child(idUser)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val count = snapshot.childrenCount.toInt()

                    if (count <= 0){
                        database.reference.child(Constants.KEY_COLLECTION_PESANAN)
                            .child(idUser)
                            .setValue(dataTiket)
                            .addOnCompleteListener {
                                if (it.isSuccessful){
                                    preferenceManager.putString(Constants.KEY_ASAL, spBerangkat)
                                    preferenceManager.putString(Constants.KEY_TANGGAL, tglBerangkat)
                                    preferenceManager.putString(Constants.KEY_TUJUAN, spTujuan)
                                    showToast("Pesanan tiket akan segera diproses")
                                    startActivity(Intent(this@InputDataActivity,MainActivity::class.java ))

                                }else{
                                    showToast("Gagal melakukan pesanan")
                                }
                            }
                    }else{
                        database.reference.child(Constants.KEY_COLLECTION_PESANAN)
                            .child(idUser)
                            .child(count.toString())
                            .setValue(item)
                            .addOnCompleteListener {
                                if (it.isSuccessful){
                                    preferenceManager.putString(Constants.KEY_ASAL, spBerangkat)
                                    preferenceManager.putString(Constants.KEY_TANGGAL, tglBerangkat)
                                    preferenceManager.putString(Constants.KEY_TUJUAN, spTujuan)
                                    showToast("Pesanan tiket akan segera diproses")
                                    startActivity(Intent(this@InputDataActivity,MainActivity::class.java ))

                                }else{
                                    showToast("Gagal melakukan pesanan")
                                }
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })



    }

    private fun isLoading(isLoading: Boolean, loading: ProgressBar, button: MaterialButton){
        if (isLoading){
            loading.visibility = View.VISIBLE
            button.visibility = View.INVISIBLE
        }else{
            loading.visibility = View.INVISIBLE
            button.visibility = View.VISIBLE
        }
    }


    private fun isNotEmpty(): Boolean{

        val namaPenumpang = binding.inputNama.text.toString().trim()
        val spBerangkat = binding.spBerangkat.selectedItem.toString().trim()
        val spTujuan = binding.spTujuan.selectedItem.toString().trim()
        val jmlAnak = binding.tvJmlAnak.text.toString().trim()
        val jmlDewasa = binding.tvJmlDewasa.text.toString().trim()
        val spKelas = binding.spKelas.selectedItem.toString().trim()
        val tglBerangkat = binding.inputTanggal.text.toString().trim()
        val telp = binding.inputTelepon.text.toString().trim()

        if(namaPenumpang.isEmpty()){
            val nama = binding.inputNama
            nama.error = "Nama harus terisi"
            nama.requestFocus()
            return false
        }
        else if (spBerangkat == ""){
            val asal = binding.spBerangkat
            asal.requestFocus()
            showToast("Pilih daerah pemberangkatan")
            return false
        }
        else if (spTujuan == ""){
            val tujuan = binding.spTujuan
            tujuan.requestFocus()
            showToast("Pilih daerah tujuan")
            return false
        }
        else if (spBerangkat == spTujuan){
            showToast("Kota tujuan dan kota asal harus beda")
            return false
        }
        else if (jmlAnak.toInt() + jmlDewasa.toInt() == 0){
            showToast("Minimal memesan 1 tiket")
            return false
        }
        else if (spKelas == ""){
            val kelas = binding.spKelas
            kelas.requestFocus()
            showToast("Pilih salah satu kelas")
            return false
        }
        else if (tglBerangkat.isEmpty()){
            val tanggal = binding.inputTanggal
            tanggal.error = "Masukkan tanggal pemberangkatan"
            tanggal.requestFocus()
            return false
        }
        else if (telp.length < 8){
            val telpon = binding.inputTelepon
            telpon.error = "Masukkan nomor telepon dengan benar"
            telpon.requestFocus()
            return false
        }
        else{
            return true
        }
    }

    private fun tukar(){
        val spBerangkat = binding.spBerangkat.selectedItemPosition
        val spTujuan = binding.spTujuan.selectedItemPosition

        binding.spBerangkat.setSelection(spTujuan)
        binding.spTujuan.setSelection(spBerangkat)
    }

    private fun showToast(pesan: String){
        Toast.makeText(applicationContext, pesan, Toast.LENGTH_SHORT).show()
    }
}



