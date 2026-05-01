# GuardianCircle Firebase povezivanje

Ovo je najkraci produkcijski put da Wear OS sat salje podatke clanovima obitelji u stvarnom vremenu.

## 1. Kreiraj Firebase projekt

1. Otvori Firebase Console i kreiraj projekt `GuardianCircle`.
2. Ukljuci `Authentication`, `Cloud Firestore` i `Cloud Messaging`.
3. Firestore za pocetak mozes otvoriti u test modu samo za razvoj, ali kasnije obavezno prebaci na sigurnosna pravila.

## 2. Dodaj Android aplikaciju

1. U Firebase projektu klikni `Add app` -> `Android`.
2. Kao package name koristi `com.guardian.circle.ui`.
3. Preuzmi `google-services.json`.
4. Spremi ga tocno u `app/google-services.json`, ali zadrzi ga samo lokalno i nemoj ga commitati u git.

Napomena: `app/build.gradle` je vec pripremljen tako da se Google Services plugin aktivira tek kad datoteka postoji.

## Sigurnosna napomena za `google-services.json`

- Firebase Android `google-services.json` nije mjesto za server-side tajne, ali ga svejedno ne treba drzati u javnom repozitoriju.
- Google API key iz tog fajla treba ograniciti u Google Cloud Console na Android aplikaciju `com.guardian.circle.ui` i odgovarajuci SHA-1 ili SHA-256 potpis certifikata.
- Ako je kljuc vec bio objavljen, preporuka je regenerirati ili zamijeniti ga nakon sto postavis restrictione.

## 3. Sinkroniziraj projekt u Android Studiju

1. U Android Studiju otvori ovaj repozitorij.
2. Pokreni `Sync Project with Gradle Files`.
3. Nakon sinkronizacije Firebase klase ce biti dostupne kroz `FirebaseAuth`, `FirebaseFirestore` i `FirebaseMessaging`.

## 4. Ukljuci autentikaciju

1. U Firebase Console ukljuci barem `Email/Password` ili `Anonymous` sign-in za razvoj.
2. U aplikaciji prijavi korisnika i koristi njegov `uid` kao glavni Firestore dokument:
   `users/{uid}`
3. U ovoj bazi ce se zatim citati:
   `users/{uid}/user_profile/main`
   `users/{uid}/live_stats/current`
   `users/{uid}/emergency_logs/{eventId}`

Ako zelis brzo testiranje, trenutni kod ima fallback `demo-...` uid. Za produkciju to zamijeni stvarnim `FirebaseAuth` korisnikom.

## 5. Uvezi JSON model baze

1. Otvori [firestore_schema_guardiancircle.json](C:\Users\Korisnik\AndroidStudioProjects\Wellbeing-2\docs\firebase\firestore_schema_guardiancircle.json)
2. Na temelju njega kreiraj iste dokumente i podkolekcije u Firestoreu.
3. Posebno obrati paznju na:
   `user_profile/main`
   `live_stats/current`
   `emergency_logs/{eventId}`
   `devices/watch`

## 6. Omoguci real-time prikaz obitelji

U family aplikaciji ili dashboardu slusaj promjene preko `addSnapshotListener()`:

```java
FirebaseFirestore.getInstance()
    .collection("users")
    .document(uid)
    .collection("live_stats")
    .document("current")
    .addSnapshotListener((snapshot, error) -> {
        if (error != null || snapshot == null || !snapshot.exists()) {
            return;
        }
        // Ovdje osvjezi puls, GPS, tlak, korake i emergency stanje.
    });
```

Za listu hitnih dogadaja slusaj i:

```java
FirebaseFirestore.getInstance()
    .collection("users")
    .document(uid)
    .collection("emergency_logs");
```

## 7. Povezi FCM push kanal

1. `GuardianFirebaseMessagingService` vec sprema watch FCM token u:
   `users/{uid}/devices/watch`
2. Family aplikacija treba spremiti svoje tokene u:
   `users/{uid}/devices/{guardianDeviceId}`
3. Najsigurniji tok je:
   sat upise emergency log -> Cloud Function procita guardian tokene -> Cloud Function posalje FCM push

To je sigurnije od direktnog slanja svih push poruka sa sata.

## 8. Dodaj Cloud Function za fan-out

Logika funkcije neka bude:

1. Trigger na `users/{uid}/emergency_logs/{eventId}`
2. Provjeri da je `dispatch.requires_push_fan_out == true`
3. Procitaj sve tokene iz `users/{uid}/devices`
4. Posalji FCM poruku svim guardian uredajima
5. Azuriraj `dispatch.state` na `sent`

## 9. Firestore security rules

Produkcijska pravila trebaju znaciti:

1. Korisnik smije pisati samo svoj `users/{uid}` prostor.
2. Guardian smije citati samo profile kojima je eksplicitno pridruzen.
3. Samo backend funkcija smije oznaciti `dispatch.state='sent'`.

## 10. Test u stvarnom vremenu

1. Pokreni sat aplikaciju.
2. Dodirni veliki `SOS` gumb.
3. Provjeri da se u Firestoreu pojavljuje novi zapis u `emergency_logs`.
4. Provjeri da se `live_stats/current` odmah promijeni.
5. U family aplikaciji potvrdi da listener prima promjenu bez refreshanja.
