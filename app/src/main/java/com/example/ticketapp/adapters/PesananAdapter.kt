package com.example.ticketapp.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ticketapp.R
import com.example.ticketapp.activities.HistoryActivity
import com.example.ticketapp.models.Pesanan
import com.example.ticketapp.utilities.Constants
import com.example.ticketapp.utilities.PreferenceManager

class PesananAdapter(private var context: Context, private var itemList: List<Pesanan>): RecyclerView.Adapter<PesananAdapter.PesananViewHolder>() {

    private lateinit var preferenceManager: PreferenceManager

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PesananAdapter.PesananViewHolder {
        preferenceManager = PreferenceManager(context)
        var view: View = LayoutInflater.from(context).inflate(
            R.layout.item_container_pesanan, parent, false
        )
        return PesananViewHolder(view)
    }

    override fun onBindViewHolder(holder: PesananAdapter.PesananViewHolder, position: Int) {
        val pesananPilih = itemList[position]
        val jmlAnak = pesananPilih.jumlah_anak2?.toInt()
        val jmlDewasa = pesananPilih.jumlah_dewasa?.toInt()
        val jumlah = jmlAnak!!.plus(jmlDewasa!!)

        holder.transport.text = pesananPilih.transport
        holder.kelas.text = pesananPilih.kelas
        holder.asal.text = pesananPilih.asal
        holder.tujuan.text = pesananPilih.tujuan
        holder.jumlah.text = "$jumlah  Tiket"
        holder.tglBeli.text = pesananPilih.tglDibuat

        holder.itemView.setOnLongClickListener {
            val intent = Intent(context, HistoryActivity::class.java)
            intent.putExtra(Constants.KEY_KELAS, pesananPilih.kelas)
            intent.putExtra(Constants.KEY_TRANSPORT, pesananPilih.transport)
            intent.putExtra(Constants.KEY_NAMA, pesananPilih.nama)
            intent.putExtra(Constants.KEY_JML_ANAK, pesananPilih.jumlah_anak2)
            intent.putExtra(Constants.KEY_JML_DEWASA, pesananPilih.jumlah_dewasa)
            intent.putExtra(Constants.KEY_TANGGAL, pesananPilih.tanggal)
            intent.putExtra(Constants.KEY_ASAL, pesananPilih.asal)
            intent.putExtra(Constants.KEY_TUJUAN, pesananPilih.tujuan)

            preferenceManager.putString(Constants.KEY_POSITION, position.toString())
            context.startActivity(intent)

            return@setOnLongClickListener true
        }


    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    class PesananViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val transport = itemView.findViewById<TextView>(R.id.tvTransport)
        val kelas = itemView.findViewById<TextView>(R.id.tvKelas)
        val jumlah = itemView.findViewById<TextView>(R.id.tvJumlah)
        val asal = itemView.findViewById<TextView>(R.id.tvKeberangkatan)
        val tujuan = itemView.findViewById<TextView>(R.id.tvTujuan)
        val tglBeli = itemView.findViewById<TextView>(R.id.tanggalBeli)
    }

}