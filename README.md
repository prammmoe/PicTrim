# PicTrim

**PicTrim** adalah aplikasi Android untuk menyiapkan gambar agar lebih ringan dan siap dibagikan—langsung di perangkat Anda. Kompres, ubah ukuran, crop, pilih format keluaran, lalu simpan atau bagikan hasilnya tanpa mengunggah foto ke server.

> Privasi lebih sederhana: gambar diproses secara lokal dan tidak pernah meninggalkan ponsel Anda.

## Fitur

- Kompres gambar dengan pengaturan kualitas dan target ukuran file perkiraan.
- Ubah ukuran berdasarkan dimensi atau persentase, sambil menjaga rasio aspek.
- Crop tengah dengan preset **Original**, **1:1**, **4:5**, **9:16**, dan **16:9**.
- Ekspor sebagai **JPG**, **PNG**, atau **WebP**—atau pertahankan format asal.
- Hapus metadata EXIF, termasuk informasi GPS, kamera, dan tanggal, bila diperlukan.
- Proses hingga **50 gambar** dalam satu batch dengan progres di notifikasi.
- Bandingkan ukuran dan dimensi gambar asli dengan hasil pemrosesan sebelum menyimpan.
- Simpan ke `Pictures/PicTrim`, buka langsung di galeri, atau bagikan hasilnya.
- Antarmuka tersedia dalam Bahasa Inggris dan Bahasa Indonesia.

## Teknologi

- Kotlin dan Jetpack Compose (Material 3)
- Hilt untuk dependency injection
- WorkManager untuk pemrosesan batch di latar belakang
- Android Photo Picker, MediaStore, dan FileProvider
- Coil untuk menampilkan pratinjau gambar
- Preferences DataStore untuk status onboarding

## Menjalankan proyek

### Prasyarat

- Android Studio versi terbaru dengan dukungan Android Gradle Plugin 9.3.1
- JDK 17
- Android SDK Platform 37
- Perangkat atau emulator Android dengan API level 23 (Android 6.0) atau lebih baru

### Build

```bash
git clone https://github.com/prammmoe/PicTrim.git
cd PicTrim
./gradlew assembleDebug
```

APK debug akan tersedia di:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Untuk menjalankan pengujian unit:

```bash
./gradlew testDebugUnitTest
```

Atau buka folder proyek ini melalui Android Studio, tunggu sinkronisasi Gradle selesai, kemudian jalankan konfigurasi `app` pada perangkat/emulator Anda.

## Cara kerja

1. Pilih satu gambar atau beberapa gambar dari galeri.
2. Atur mode pemrosesan: kompres, resize, atau keduanya.
3. Pilih kualitas, ukuran, format keluaran, crop, dan opsi metadata.
4. Proses gambar dan periksa perbandingan hasilnya.
5. Simpan ke galeri atau bagikan langsung.

Untuk batch, PicTrim menerapkan satu set pengaturan yang sama pada semua gambar terpilih dan memprosesnya secara berurutan agar penggunaan memori tetap terjaga.

## Catatan privasi dan penyimpanan

- PicTrim memproses gambar di perangkat; tidak ada layanan unggah atau akun yang diperlukan.
- Hasil sementara batch disimpan secara privat hingga Anda memilih untuk menyimpannya.
- Gambar yang disimpan akan ditempatkan di folder `Pictures/PicTrim` melalui MediaStore.
- Saat opsi **Remove metadata** aktif, data EXIF seperti GPS, informasi kamera, dan tanggal dihilangkan; piksel gambar tidak diubah oleh opsi ini sendiri.

## Struktur proyek

```text
app/src/main/java/com/prammmoe/pictrim/
├── data/       # Implementasi Android: pemrosesan bitmap, MediaStore, dan batch
├── di/         # Modul dependency injection Hilt
├── domain/     # Model, aturan pemrosesan, repository, dan use case
└── ui/         # Layar Compose, ViewModel, komponen, dan tema
```

## Dukungan format

PicTrim menerima gambar yang dapat dibaca Android dan secara eksplisit mendukung keluaran JPG, PNG, serta WebP. Orientasi EXIF diterapkan sebelum gambar diproses agar hasilnya memiliki arah yang benar.

## Lisensi

Hak cipta © 2026 Pramuditha Muhammad Ikhwan. Repository ini tersedia untuk portofolio, pembelajaran, dan referensi. Penggunaan ulang, redistribusi, fork publik, maupun penggunaan komersial memerlukan izin tertulis dari pemegang hak cipta. Lihat [LICENSE.md](LICENSE.md) untuk ketentuan lengkap.
