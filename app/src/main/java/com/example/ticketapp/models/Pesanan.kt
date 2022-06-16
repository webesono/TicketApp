package com.example.ticketapp.models

class Pesanan {
    var kelas: String? = null
    var nama: String? = null
    var jumlah_dewasa: String? = null
    var jumlah_anak2: String? = null
    var tanggal: String? = null
    var asal: String? = null
    var tujuan: String? = null
    var transport: String? = null
    var telepon: String? = null
    var idUser: String? = null
    var tglDibuat: String? = null

    constructor(){}

    constructor(kelas: String, nama: String, jumlah_dewasa: String,
                jumlah_anak2: String, tanggal: String, asal: String,
                tujuan: String, transport: String, telepon: String,
                idUser: String, tglDibuat: String?){

        this.kelas = kelas
        this. nama = nama
        this. jumlah_dewasa = jumlah_dewasa
        this. jumlah_anak2 = jumlah_anak2
        this. tanggal = tanggal
        this. asal = asal
        this. tujuan = tujuan
        this. transport = transport
        this. telepon = telepon
        this. idUser = idUser
        this.tglDibuat = tglDibuat
    }
}