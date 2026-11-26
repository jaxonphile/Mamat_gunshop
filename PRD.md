# Product Requirement Document (PRD)
## Mamat Gunshop

| Field | Nilai |
|---|---|
| Nama produk | Mamat Gunshop Desktop |
| Versi | 1.0.0 |
| Tanggal | 2026-11-26 |
| Status | Disetujui untuk implementasi |
| Penulis | Tim Pengembangan (developer) |
| Platform | Desktop — Java 25, Swing, Gradle 9.9 |

---

## 1. Visi

Menjadi aplikasi kasir/katalog lokal yang memudahkan pelanggan menemukan senjata api yang sesuai dengan **kaliber**, **tipe**, **merek**, dan **budget**, sekaligus membantu admin mengelola stok dengan cepat — tanpa perlu browser atau server.

## 2. Masalah yang dipecahkan

- Pelanggan sulit membandingkan produk berdasarkan kaliber karena katalog berbasis teks biasa.
- Admin sering lupa update stok setelah transaksi.
- Tidak ada riwayat transaksi yang rapi untuk keperluan revolusi/audit internal.
- Proses verifikasi usia pembeli belum tercatat.

## 3. Target pengguna

| Peran | Kebutuhan utama |
|---|---|
| Pelanggan (kasir) | Melihat katalog, memfilter kaliber, memasukkan keranjang, checkout, melihat struk |
| Admin | Menambah/edit/hapus produk, mengubah stok, melihat & memperbarui status pesanan |

## 4. Tujuan & metrik keberhasilan

| Tujuan | Metrik |
|---|---|
| Filter kaliber cepat | ≤ 12 produk ter-filter langsung saat checkbox diubah (< 200 ms) |
| Transaksi konsisten | Stok berkurang tepat sama dengan qty order; tidak ada stok negatif |
| Kemudahan pakai | Pelanggan baru bisa checkout dalam ≤ 5 klik setelah memilih produk |
| Dapat diuji | ≥ 15 unit test lolos untuk aturan bisnis inti |

## 5. Ruang lingkup

### 5.1 Fitur inti (MVP)
1. Katalog produk dengan data: nama, merek, tipe, kaliber, kapasitas, harga, stok, deskripsi.
2. Pencarian teks bebas (nama, merek, kaliber, deskripsi).
3. **Filter multi-kriteria**: kaliber, tipe, merek, rentang harga, stok tersedia — bisa digabung.
4. Pengurutan: nama, harga, kaliber, stok.
5. Detail produk + penambahan ke keranjang dengan validasi stok.
6. Keranjang: ubah qty, hapus item, lihat total.
7. Checkout: identitas pembeli, KTP 16 digit, verifikasi usia minimum 21 tahun.
8. Struk transaksi (tampil, cetak, simpan .txt).
9. Riwayat pesanan beserta status.

### 5.2 Fitur pendukung
10. Panel admin (login sederhana) untuk CRUD produk & update stok.
11. Perubahan status pesanan: Diproses → Selesai / Dibatalkan.
12. Export katalog ke CSV.
13. Data tersimpan otomatis di `~/.mamatgunshop/*.json`.
14. Seed data 12 produk dummy agar aplikasi langsung bisa dipakai.

### 5.3 Tidak termasuk (out of scope)
- Pembayaran sungguhan / gateway, integrasi bank.
- Verifikasi KTP sungguhan ke Dukcapil.
- Multi-user simultan, server, atau sinkronisasi cloud.
- Manajemen amunisi versi lengkap dan izin kepemilikan resmi.

## 6. User stories

| ID | Sebagai | Saya ingin | Supaya |
|---|---|---|---|
| US-01 | Pelanggan | memilih kaliber 9mm dari daftar filter | saya hanya melihat pistol yang cocok |
| US-02 | Pelanggan | menggabungkan filter merek + harga maksimal | saya mendapat produk sesuai budget |
| US-03 | Pelanggan | menaikkan jumlah barang di keranjang | total belanja otomatis dihitung |
| US-04 | Pelanggan | melihat struk setelah checkout | saya punya bukti transaksi |
| US-05 | Kasir | diblokir jika usia pembeli < 21 | transaksi mematuhi aturan usia |
| US-06 | Admin | mengubah stok produk | katalog selalu akurat |
| US-07 | Admin | menandai pesanan selesai | riwayat tetap rapi |

### Acceptance criteria contoh (US-01)
- **Diberikan** katalog 12 produk, **ketika** pengguna mencentang kaliber `9mm` di panel filter, **maka** tabel hanya menampilkan produk dengan kaliber `9mm` dan label jumlah produk ikut berubah.
- Filter bisa dicentang lebih dari satu kaliber (OR antar nilai dalam satu kriteria).
- Antar kriteria berlaku AND (mis. kaliber `9mm` DAN merek `Glock`).

## 7. Prioritas (MoSCoW)

- **Must**: US-01, US-02, US-03, US-04, US-05, US-06
- **Should**: US-07, export CSV, struk cetak
- **Could**: dark mode, ikon produk
- **Won't (v1)**: pembayaran online, database server, multi-toko

## 8. Batasan & asumsi

- Aplikasi **demo lokal**. Data produk bersifat dummy, bukan data senjata sungguhan untuk transaksi resmi.
- Verifikasi KTP hanya berupa validasi format 16 digit; tidak ada integrasi instansi pemerintah.
- satu proses aplikasi per komputer; konkurensi tidak didukung.
- Mata uang： Rupiah (IDR), disimpan sebagai `BigDecimal`.

## 9. Kebutuhan non-fungsional ringkas

| Aspek | Target |
|---|---|
| Respons UI | Filter & sorting tampil < 200 ms untuk ≤ 1.000 produk |
| Portabilitas | Java 25 + Swing, berjalan di Linux/Windows/macOS |
| Keandalan | Aplikasi tetap jalan jika file data korup (fallback data kosong + pesan) |
| Keamanan | Password admin tidak disimpan plaintext di UI; session hanya selama dialog terbuka |
| Pemeliharaan | Arsitektur berlapis: model → repository → service → UI |

## 10. Roadmap

1. **v1.0** — katalog, filter, keranjang, checkout, struk, riwayat, admin (saat ini)
2. **v1.1** — pencarian fuzzy, dashboard penjualan
3. **v2.0** — database SQLite, multi-kasir, laporan bulanan
