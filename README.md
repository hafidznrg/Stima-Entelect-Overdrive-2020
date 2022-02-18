# Tugas Besar I IF2211 Strategi Algoritma
Program ini kami buat ditujukan untuk memenuhi tugas besar I IF2211 Strategi Algoritma Semester 2 2021/2022.

## Penjelasan Singkat Algoritma Greedy yang Diimplementasikan
> Kelompok kami memilih menggunakan algoritma greedy by damage dan greedy by speed. Pada strategi ini, mobil kami akan mencoba sebisa mungkin untuk mengambil perintah yang tidak memberikan damage pada mobil kita. Setelah mempertimbangkan jalur yang tidak memberikan damage, kita dapat mempertimbangkan untuk semakin memperbesar kecepatan atau setidaknya mempertahankan kecepatan yang ada. Hal tersebut dapat dicapai dengan menggunakan command BOOST jika tidak ada damage yang diterima dengan kecepatan BOOST, atau menggunakan command ACCELERATE jika tidak ada damage yang diterima dengan tingkat kecepatan selanjutnya. Jika tidak memenuhi, maka pertimbangkan untuk menggunakan powerups lain. Jika semua jalur memberikan damage maka pertimbangkan menggunakan BOOST jika damage masih bisa ditolerir, jika tidak maka ambil jalur yang memberikan damage paling kecil.

> Pemilihan strategi ini dikarenakan damage akan membatasi kecepatan maksimum dengan semakin besar damage, maka semakin kecil kecepatan maksimum, serta kita perlu mengorbankan 1 ronde untuk mengurangi damage sebesar 2 unit dengan command FIX tanpa berpindah tempat. Selain itu, untuk memenangkan pertandingan, diperlukan kecepatan yang tinggi sehingga dapat meminimalkan jumlah ronde yang diperlukan untuk mencapai garis finish terlebih dahulu.

## Requierement dan Instalasi
Untuk menjalankan program ini, diperlukan beberapa requierement dasar sebagai berikut.
- Java (minimal Java 8): https://www.oracle.com/java/technologies/downloads/#java8
- IntelIiJ IDEA: https://www.jetbrains.com/idea/
- NodeJS: https://nodejs.org/en/download/

## Langkah-langkah Kompilasi
Berikut merupakan langkah-langkah kompilasi program kami.
- Buka IntelIiJ IDEA pada folder `src`
- Klik opsi di kiri atas IDE
    ```
    View -> Tool Windows -> Maven
    ```
- Buka opsi `Lifecycle`
- Klik `clean` lalu setelah selesai, klik `package`
- Folder `target` akan dibangun dan didalamnya akan terdapat file `java-starter-bot-jar-with-dependencies.jar` yang dapat digunakan dalam pertandingan nantinya.

## Author
- Fikri Khoiron Fadhila           (13520056 / fikrikhoironn)
- Malik Akbar Hashemi Rafsanjani  (13520105 / malikrafsan)
- Hafidz Nur Rahman Ghozali       (13520117 / hafidznrg)