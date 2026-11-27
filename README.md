# Mamat Gunshop 🛒🔫

[![CI](https://github.com/jaxonphile/Mamat_gunshop/actions/workflows/ci.yml/badge.svg)](https://github.com/jaxonphile/Mamat_gunshop/actions/workflows/ci.yml)

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
├── run.sh                     # launcher Linux/macOS (auto-install)
├── run.bat                    # launcher Windows (auto-install)
├── build.gradle
├── settings.gradle
├── scripts/
│   ├── bootstrap.sh
│   └── bootstrap.ps1
├── gradle/wrapper/            # Gradle Wrapper (unduh Gradle otomatis)
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

### Jalur launcher (disarankan)

- **Windows 10/11** atau **Linux/macOS** dengan Bash
- PowerShell bawaan Windows (`powershell.exe`, sudah ada di Windows 10+)
- Koneksi internet **saat run pertama**
- Ruang disk ± 400 MB (JDK portabel + Gradle + dependency)
- **Tidak perlu** install Java atau Gradle manual

### Jalur manual

- JDK 25 atau lebih baru (`java -version` → harus `25.x`)
- Gradle 9.9 — atau cukup pakai Gradle Wrapper yang sudah disertakan (`gradlew` / `gradlew.bat`)
- Koneksi internet saat build pertama (Jackson & JUnit diunduh dari Maven Central)

---

## ▶️ Cara menjalankan

### 🚀 Windows — paling gampang (auto-install)

1. Download/clone repo ini, lalu ekstrak ke folder misalnya `Mamat_gunshop`.
2. Buka folder tersebut.
3. **Klik dua kali `run.bat`**.
4. Tunggu jendela PowerShell selesai mengunduh & membangun.
5. Aplikasi `Mamat Gunshop` akan langsung terbuka.

> Kalau muncul peringatan SmartScreen, pilih **More info → Run anyway**.

### 🚀 Linux / macOS — paling gampang (auto-install)

```bash
cd /home/jax/jax/Mamat_gunshop
./run.sh
```

Kalau file tidak executable (misalnya hasil download ZIP, bukan `git clone`):

```bash
chmod +x run.sh scripts/bootstrap.sh gradlew
./run.sh
```

### Apa yang terjadi saat run pertama

Launcher akan otomatis:

1. Memakai JDK 25 yang sudah ada — kalau belum ada, unduh **Temurin 25** ke:
   - Linux/macOS: `~/.mamatgunshop/runtime/jdk-25`
   - Windows: `%USERPROFILE%\.mamatgunshop\runtime\jdk-25`
2. Memverifikasi checksum SHA-256 JDK sebelum dipakai.
3. Mengunduh Gradle 9.9 lewat **Gradle Wrapper** beserta dependency (Jackson, JUnit).
4. Membangun `build/libs/mamat-gunshop-1.0.0-all.jar`.
5. Menjalankan aplikasi.

Run berikutnya tidak mengunduh ulang kecuali source berubah.

### 🔧 Cara manual (opsional)

Sudah ada JDK 25 / Gradle terpasang, atau ingin kontrol penuh:

```bash
cd /home/jax/jax/Mamat_gunshop

# Kompilasi + jalankan unit test
./gradlew test

# Jalankan aplikasi
./gradlew run

# Bangun JAR mandiri (sudah termasuk dependency)
./gradlew fatJar
java -jar build/libs/mamat-gunshop-1.0.0-all.jar
```

Di Windows, ganti `./gradlew` dengan `gradlew.bat`.

---

## 🖥️ Linux / Niri (Wayland)

Niri memakai `xwayland-satellite` sebagai server X11. Karena sifatnya yang **non-reparenting**, Java AWT bisa menampilkan jendela putih/kosong.

✅ **Kalau menjalankan lewat `./run.sh`, ini sudah ditangani otomatis.**

Kalau menjalankan manual, tambahkan variabel berikut:

```bash
_JAVA_AWT_WM_NONREPARENTING=1 java -jar build/libs/mamat-gunshop-1.0.0-all.jar
```

Atau untuk `gradlew run`:

```bash
_JAVA_AWT_WM_NONREPARENTING=1 ./gradlew run
```

Biar permanen di zsh:

```bash
echo 'export _JAVA_AWT_WM_NONREPARENTING=1' >> ~/.zshrc
source ~/.zshrc
```

---

## 🩺 Kalau launcher gagal

- Pastikan koneksi internet aktif saat run pertama.
- Kalau unduhan terputus, hapus file `*.part` di folder `runtime`, lalu jalankan lagi.
- Kalau JDK otomatis gagal dipakai, hapus folder `runtime/jdk-25` lalu ulangi.
- Launcher **tidak perlu hak administrator**; semua file disimpan di folder user.
- Cek log di jendela PowerShell/terminal untuk pesan error spesifik.

---

## 🔐 Akun admin

| Username | Password |
|---|---|
| `admin` | `admin123` |

Tombol **Admin** di jendela utama akan meminta login ini.

---

## 💾 Lokasi data

Saat pertama kali dijalankan, aplikasi menyalin seed data ke:

- Linux/macOS: `~/.mamatgunshop/products.json` dan `~/.mamatgunshop/orders.json`
- Windows: `%USERPROFILE%\.mamatgunshop\products.json` dan `%USERPROFILE%\.mamatgunshop\orders.json`

Hapus folder `.mamatgunshop` untuk mengembalikan data ke kondisi awal (seed 12 produk).

---

## 🧪 Pengujian

```bash
./gradlew test
```

Di Windows: `gradlew.bat test`.

Unit test mencakup: filter katalog, kombinasi filter, sorting, batas stok keranjang, validasi KTP/usia, pembuatan order, pengurangan stok, dan validasi produk admin.

---

## 📜 Lisensi & penggunaan

Proyek ini dibuat untuk keperluan pembelajaran dan demo. Pastikan mematuhi peraturan perundang-undangan yang berlaku jika diadaptasi untuk penggunaan nyata.
