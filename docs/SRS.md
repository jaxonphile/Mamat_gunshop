# Software Requirement Specification (SRS)
## Mamat Gunshop Desktop

| Field | Nilai |
|---|---|
| Versi dokumen | 1.0 |
| Tanggal | 2026-11-26 |
| Terkait | PRD.md v1.0.0 |
| Status | Disetujui |

---

## 1. Pendahuluan

### 1.1 Tujuan
Dokumen ini mendefinisikan kebutuhan fungsional dan non-fungsional aplikasi Mamat Gunshop, serta aturan bisnis, model data, dan kriteria penerimaan yang dipakai sebagai acuan implementasi dan pengujian.

### 1.2 Ruang lingkup
Aplikasi desktop berbasis Java Swing untuk mengelola katalog, penjualan, dan stok senjata api dalam lingkup satu toko lokal. Tidak ada layanan jaringan, pembayaran online, atau verifikasi identitas eksternal.

### 1.3 Definisi istilah

| Istilah | Arti |
|---|---|
| Produk | Satu item senjata yang dijual, dikenal oleh `id` unik |
| Kaliber | Ukuran peluru yang dipakai produk, mis. `9mm`, `.45 ACP`, `12 gauge` |
| Keranjang | Kumpulan sementara item pilihan pelanggan sebelum checkout |
| Order | Transaksi yang sudah dikonfirmasi, memiliki nomor dan status |
| Stok | Jumlah produk yang tersedia; tidak boleh negatif |
| KTP | Nomor identitas 16 digit (disimulasikan) |

---

## 2. Kebutuhan fungsional

| ID | Nama | Deskripsi | Input | Output |
|---|---|---|---|---|
| FR-01 | Muat katalog | Memuat produk dari seed saat pertama kali, selanjutnya dari `products.json` | file JSON | daftar produk |
| FR-02 | Tampil katalog | Menampilkan produk dalam tabel sortable | daftar produk | tabel + jumlah produk |
| FR-03 | Pencarian | Mencocokkan keyword (case-insensitive) pada nama, merek, kaliber, deskripsi | teks | daftar terfilter |
| FR-04 | Filter kaliber | Memilih satu atau beberapa kaliber (OR) | himpunan kaliber | daftar terfilter |
| FR-05 | Filter tipe | Memilih satu atau beberapa tipe (PISTOL/RIFLE/SHOTGUN/dll) | himpunan tipe | daftar terfilter |
| FR-06 | Filter merek | Memilih satu atau beberapa merek | himpunan merek | daftar terfilter |
| FR-07 | Filter harga | Membatasi harga minimum/maksimum | dua angka | daftar terfilter |
| FR-08 | Filter stok | Hanya tampilkan produk dengan stok > 0 | boolean | daftar terfilter |
| FR-09 | Sortir | Mengurutkan hasil berdasarkan nama/harga/kaliber/stok, asc/desc | pilihan sort | daftar terurut |
| FR-10 | Reset filter | Mengembalikan semua kriteria ke nilai awal | aksi klik | katalog penuh |
| FR-11 | Detail produk | Menampilkan deskripsi lengkap produk terpilih | baris tabel | panel detail |
| FR-12 | Tambah keranjang | Menambahkan produk dengan quantity tertentu | productId, qty | keranjang bertambah |
| FR-13 | Kelola keranjang | Mengubah qty, menghapus item, melihat total | productId, qty | keranjang terbaru |
| FR-14 | Validasi checkout | Memeriksa identitas, usia, dan ketersediaan stok | Customer | lolos / error |
| FR-15 | Buat order | Mencatat order baru & mengurangi stok | keranjang + customer | Order + struk |
| FR-16 | Struk | Menampilkan, mencetak, dan menyimpan struk .txt | Order | struk |
| FR-17 | Riwayat order | Menampilkan seluruh order beserta item | - | tabel order + detail |
| FR-18 | Ubah status order | Mengubah status Diproses/Selesai/Dibatalkan | orderId, status | order terbaru |
| FR-19 | Login admin | Autentikasi sederhana untuk membuka panel admin | username, password | akses admin |
| FR-20 | CRUD produk | Tambah, ubah, hapus produk | data produk | katalog terbaru |
| FR-21 | Export CSV | Menulis katalog aktif ke file CSV | path file | file CSV |
| FR-22 | Persistensi | Menyimpan produk & order ke JSON setelah perubahan | objek | file JSON terbaru |

---

## 3. Kebutuhan non-fungsional

| ID | Kategori | Kebutuhan |
|---|---|---|
| NFR-01 | Performa | Filter/sort ≤ 200 ms untuk 1.000 produk pada hardware standar |
| NFR-02 | Portabilitas | Berjalan pada Java 25 tanpa modul native tambahan |
| NFR-03 | Ketahanan | File JSON korup → aplikasi tetap berjalan dengan daftar kosong & log error |
| NFR-04 | Konsistensi | Penulisan file dilakukan atomik (tulis file temp lalu pindahkan) |
| NFR-05 | Presisi uang | Semua nilai uang memakai `BigDecimal`, bukan `double` |
| NFR-06 | Keamanan | Password admin tidak ditampilkan; verifikasi usia ≥ 21 tahun |
| NFR-07 | Maintainability | Pemisahan lapisan model, repository, service, UI; minimal 15 unit test |
| NFR-08 | Lokalisasi | Bahasa antarmuka Bahasa Indonesia; mata uang IDR |

---

## 4. Aturan bisnis

| ID | Aturan |
|---|---|
| BR-01 | Usia minimum pembeli adalah **21 tahun**. |
| BR-02 | Nomor KTP harus terdiri dari **16 digit angka**. |
| BR-03 | Quantity minimal 1 dan tidak boleh melebihi stok produk saat ditambahkan. |
| BR-04 | Saat checkout, stok dicek ulang; jika berubah dan tidak mencukupi, checkout ditolak. |
| BR-05 | Total order = Σ (harga satuan × quantity). Harga di-snapshot saat checkout. |
| BR-06 | Stok dikurangi hanya setelah seluruh validasi checkout lolos. |
| BR-07 | Nomor order berformat `MG-yyyyMMdd-XXXX` (UUID 4 karakter pertama, uppercase). |
| BR-08 | Produk baru harus memiliki nama, kaliber, harga > 0, stok ≥ 0, kapasitas ≥ 0. |
| BR-09 | Filter dalam satu kriteria bersifat OR; antar kriteria bersifat AND. |
| BR-10 | Order baru berstatus `DIPROSES`. |

---

## 5. Model data

```
Product  { id, name, brand, type, caliber, capacity, price, stock, description, imagePath }
CartItem { product (snapshot), quantity }
Cart     { items: Map<productId, CartItem> }
Customer { name, ktpNumber, age }
OrderItem{ productId, productName, caliber, unitPrice, quantity, subtotal }
Order    { id, createdAt, customer, items[], total, status }
```

Relasi: `Order` contains many `OrderItem`; `Cart` contains many `CartItem`; `CartItem` mereferensikan `Product`.

---

## 6. Alur utama (happy path)

1. Aplikasi dibuka → katalog termuat → filter kosong.
2. Pelanggan memilih kaliber `9mm` dan merek `Glock` → tabel menampilkan 2 produk.
3. Pelanggan memilih produk → input qty 2 → "Tambah ke Keranjang".
4. Pelanggan membuka keranjang → memeriksa total → "Checkout".
5. Pelanggan mengisi nama, KTP, usia → sistem memvalidasi (BR-01, BR-02).
6. Sistem mengecek stok ulang (BR-04), membuat order, mengurangi stok, menyimpan JSON.
7. Struk muncul → pelanggan menyimpan/mencetak → keranjang kosong.

### Alur alternatif
- **A1** Usia < 21 → checkout ditolak dengan pesan, keranjang tetap utuh.
- **A2** KTP bukan 16 digit → checkout ditolak.
- **A3** Stok tidak cukup saat checkout → checkout ditolak, daftar produk disarankan refresh.

---

## 7. Antarmuka (layar)

| Layar | Elemen utama |
|---|---|
| Utama | Header + pencarian, panel filter kiri, tabel produk kanan, panel detail, tombol Keranjang/Riwayat/Admin/Export |
| Keranjang | Tabel item, tombol +/−/hapus, total, Checkout |
| Checkout | Form nama, KTP, usia; tombol Bayar |
| Struk | Text area struk, Simpan, Cetak, Tutup |
| Riwayat | Tabel order, detail item, tombol ubah status |
| Login Admin | Username, password |
| Admin | Tabel produk + form CRUD |

---

## 8. Kriteria penerimaan

- Seluruh FR-01 s.d. FR-22 terpenuhi.
- Semua BR-01 s.d. BR-10 diuji oleh unit test.
- `gradle test` lolos tanpa kegagalan.
- `gradle run` menampilkan jendela utama dengan 12 produk seed dan filter berfungsi.
- Setelah checkout, file `~/.mamatgunshop/products.json` dan `orders.json` diperbarui dan konsisten.

---

## 9. Asumsi & ketergantungan

- Gradle 9.9 dan Java 25 terpasang.
- Koneksi internet tersedia saat build pertama (untuk Jackson & JUnit).
- `java.home` user writable untuk direktori `~/.mamatgunshop`.
- Tidak ada integrasi pihak ketiga.
