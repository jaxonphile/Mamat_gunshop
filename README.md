# Mamat Gunshop 🛒🔫

Aplikasi desktop **e-commerce senjata api (demo lokal)** berbasis **Java 25 + Swing + Gradle**.
Fokus utama: memfilter produk berdasarkan **kaliber**, tipe, merek, dan harga — lengkap dengan keranjang, checkout, struk, riwayat order, dan panel admin.

> ⚠️ **Catatan:** seluruh data adalah dummy untuk keperluan demo/pembelajaran. Tidak ada pembayaran sungguhan maupun verifikasi identitas resmi.

---

## ✨ Fitur

- Katalog 12 produk dummy (pistol, revolver, rifle, shotgun, SMG)
- Pencarian cepat + filter multi-kriteria (kaliber, tipe, merek, harga, stok)
- Sorting: nama, harga, kaliber, stok
- Keranjang dengan qty & validasi stok
- Checkout dengan validasi **KTP 16 digit** dan **usia minimal 21 tahun**
- Struk transaksi (tampil / cetak / simpan `.txt`)
- Riwayat pesanan + ubah status (Diproses / Selesai / Dibatalkan)
- Panel admin: tambah / edit / hapus produk
- Export katalog ke CSV
- Data tersimpan otomatis di `~/.mamatgunshop/*.json`

---

## 📁 Struktur proyek

```
Mamat_gunshop/
├── docs/
│   ├── PRD.md                 # Product Requirement Document
│   └── SRS.md                 # Software Requirement Specification
├── README.md
├── build.gradle
├── settings.gradle
└── src/
    ├── main/
    │   ├── java/com/mamatgunshop/
    │   │   ├── Main.java
    │   │   ├── AppContext.java
    │   │   ├── model/          # Product, Cart, Order, dll.
    │   │   ├── repository/     # JSON persistence
    │   │   ├── service/        # aturan bisnis
    │   │   ├── ui/             # Swing GUI
    │   │   └── util/           # JsonSupport, DataPaths
    │   └── resources/data/products.json
    └── test/java/com/mamatgunshop/service/
```

---

## 🔧 Prasyarat

- JDK 25 (`java -version`)
- Gradle 9.9 (`gradle -version`)
- Koneksi internet saat build pertama (Jackson & JUnit diunduh dari Maven Central)

---

## ▶️ Build & jalankan

```bash
cd /home/jax/jax/Mamat_gunshop

# Kompilasi + jalankan unit test
gradle test

# Jalankan aplikasi
gradle run

# Bangun JAR mandiri (sudah termasuk dependency)
gradle fatJar
java -jar build/libs/mamat-gunshop-1.0.0-all.jar
```

---

## 🔐 Akun admin

| Username | Password |
|---|---|
| `admin` | `admin123` |

Tombol **Admin** di jendela utama akan meminta login ini.

---

## 💾 Lokasi data

Saat pertama kali dijalankan, aplikasi menyalin seed data ke:

```
~/.mamatgunshop/products.json
~/.mamatgunshop/orders.json
```

Hapus folder `~/.mamatgunshop` untuk mengembalikan data ke kondisi awal (seed 12 produk).

---

## 🧪 Pengujian

```bash
gradle test
```

Unit test mencakup: filter katalog, kombinasi filter, sorting, batas stok keranjang, validasi KTP/usia, pembuatan order, pengurangan stok, dan validasi produk admin.

---

## 📜 Lisensi & penggunaan

Proyek ini dibuat untuk keperluan pembelajaran dan demo. Pastikan mematuhi peraturan perundang-undangan yang berlaku jika diadaptasi untuk penggunaan nyata.
