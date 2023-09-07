# K6-KotlinArtBook
Basit bir sanat kitabı uygulaması

## Uygulamada aslında 2 aktivite mevcut ancak iki farklı intent ile aynı aktiviteyi farklı şekilde görüntülüyoruz.
### Kullanılanlar
* Recycler View
* Sqlite
* Bitmap, ActivityResultLauncher, ByteArrayOutputStream

# İlk Ekran:
* Ekran ilk açıldığında boş ancak sağ üstteki buton ile sanat eklemek için diğer aktiviteye geçiyoruz.

![](https://github.com/KyneticHaze/K6-KotlinArtBook/blob/master/app/src/main/java/assets/Screenshot%202023-09-07%20150714.png)

# İkinci Ekran:
* Sanat ekleme ekranında kutucukları doldurarark `save` butonuna tıklayıp bilgileri ana ekrandaki reycyler view'a ekliyoruz. 

![](https://github.com/KyneticHaze/K6-KotlinArtBook/blob/master/app/src/main/java/assets/Screenshot%202023-09-07%20145807.png)

# İlk Ekran (Eklenmiş Hali):
* Sanat listesinden bir elemana tıkladığımızda tekrar ikinci aktiviteye gidiyoruz ancak bu sefer bilgierin yerleştirilmiş ve `save` butonunun kaldırılmış olduğu haline gidiyoruz.

![](https://github.com/KyneticHaze/K6-KotlinArtBook/blob/master/app/src/main/java/assets/Screenshot%202023-09-07%20145826.png)

# Son Durum:

![](https://github.com/KyneticHaze/K6-KotlinArtBook/blob/master/app/src/main/java/assets/Screenshot%202023-09-07%20145845.png)

### İşte bu kadar!🥳
