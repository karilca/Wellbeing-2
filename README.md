# GuardianCircle

## Hrvatski

### Pregled

GuardianCircle je Android Wear OS projekt usmjeren na sigurnost korisnika kroz detekciju padova, rucni SOS alarm i sinkronizaciju hitnih dogadaja prema Firebase infrastrukturi. Aktivni dio repozitorija cini korijenski Gradle projekt s modulom `app`, dok se u mapi `GuardianCircle/` nalazi odvojeni, pomocni Compose starter projekt koji nije ukljucen u glavni build.

Projekt je oblikovan kao samostalna aplikacija za sat koja:

- kontinuirano prati akcelerometar u pozadini
- omogucuje rucno aktiviranje SOS-a s korisnickog sucelja
- pohranjuje i validira hitne telefonske brojeve
- reproducira alarm i pokusava redom birati spremljene kontakte
- salje telemetriju i hitne dogadaje u Cloud Firestore
- registrira FCM token sata za buduci push kanal prema obitelji ili skrbnicima

### Kljucne funkcionalnosti

- Automatska detekcija pada kroz prag udara, provjeru kratkog mirovanja i otkazivi sigurnosni odbrojivac od 30 sekundi.
- Rucni SOS iz glavnog sucelja sata.
- Lokalna pohrana SOS brojeva s validacijom hrvatskog formata `+385`.
- Zvucni alarm na maksimalnoj glasnoci tijekom aktivne SOS sesije.
- Sekvencijalno biranje vise spremljenih kontakata uz pracenje stanja poziva kada su dozvole dostupne.
- Sinkronizacija `live_stats/current`, `emergency_logs/{eventId}` i `devices/watch` zapisa u Firestore.
- Periodicka sinkronizacija zdravstvene telemetrije svakih 15 minuta preko WorkManagera.
- Automatski restart monitoringa i periodickog rada nakon ponovnog pokretanja uredaja.

### Aktivna struktura repozitorija

```text
Wellbeing-2/
|-- app/                          Aktivni Wear OS modul
|   |-- src/main/AndroidManifest.xml
|   |-- src/main/java/com/guardian/circle/
|   |   |-- data/                Firestore pristup i modeli podataka
|   |   |-- messaging/           Firebase Messaging servis
|   |   |-- receivers/           Boot receiver
|   |   |-- sensors/             Algoritam detekcije pada
|   |   |-- services/            Foreground servis za monitoring
|   |   |-- ui/                  Glavni XML Activity ekran
|   |   |-- utils/               SOS logika, dozvole i kontakti
|   |   `-- workers/             WorkManager sinkronizacija
|   `-- src/main/res/            Layouti, drawable resursi, stilovi i alarm zvuk
|-- docs/firebase/               Firebase vodic i shema baze
|-- gradle/                      Wrapper i version catalog
|-- GuardianCircle/              Odvojeni pomocni Wear Compose starter projekt
|-- build.gradle                 Korijenska Gradle konfiguracija
`-- settings.gradle              Aktivni root projekt; ukljucuje samo :app
```

### Arhitektura i implementacijske napomene

- Aktivni projekt koristi Java + XML pristup u modulu `app`.
- `FallDetectionService` radi kao foreground servis i slusa akcelerometar.
- `FallAnalyzer` implementira prag udara i logiku potvrde pada.
- `EmergencySyncWorker` zapisuje hitne dogadaje u Firestore.
- `HealthSyncWorker` periodicki salje lokaciju, bateriju i ostale dostupne metrike.
- `FirestoreRepository` centralizira pristup Firebase Auth, Firestore batch zapisima i fallback identitetu korisnika.
- `GuardianFirebaseMessagingService` zapisuje FCM token sata u Firestore.
- Vrijednosti pulsa, krvnog tlaka i broja koraka trenutno se citaju iz `SharedPreferences`, sto znaci da je model telemetrije pripremljen, ali izravna integracija sa stvarnim zdravstvenim izvorima jos nije dovrsena u ovom repozitoriju.

### Tehnoloski okvir

- Android application plugin `9.1.0`
- Gradle wrapper `9.3.1`
- Java 11
- Android `minSdk 30`, `targetSdk 36`, `compileSdk 36`
- Wear OS libraries `androidx.wear:wear`
- WorkManager za pozadinske zadatke
- Google Play Services Location i Wearable API
- Firebase Authentication, Cloud Firestore i Firebase Cloud Messaging

### Preduvjeti

- Android Studio s podrskom za AGP 9.1 i Gradle 9.3.1
- JDK 11
- Wear OS emulator ili fizicki sat
- Firebase projekt za cloud funkcionalnosti

### Pokretanje projekta

1. Otvorite repozitorij u Android Studiju.
2. Sinkronizirajte Gradle datoteke.
3. Ako zelite Firebase funkcionalnosti, registrirajte Android aplikaciju s package nazivom `com.guardian.circle.ui`.
4. Postavite vlastiti `google-services.json` u mapu `app/`.
5. Pokrenite modul `app` na Wear OS emulatoru ili uredaju.

Primjer build naredbe:

```powershell
.\gradlew.bat assembleDebug
```

### Firebase i dokumentacija baze

- Hrvatski vodic za Firebase postavljanje nalazi se u [docs/firebase/firebase_setup_hr.md](docs/firebase/firebase_setup_hr.md).
- Predlozena Firestore shema nalazi se u [docs/firebase/firestore_schema_guardiancircle.json](docs/firebase/firestore_schema_guardiancircle.json).
- Aplikacija je pripremljena tako da Google Services plugin bude aktiviran samo kada datoteka `app/google-services.json` postoji.
- Datoteku `app/google-services.json` treba drzati samo lokalno i ne commitati je u repozitorij.
- U slucaju da Firebase nije konfiguriran, aplikacija ostaje funkcionalna za lokalni SOS tijek, ali bez cloud sinkronizacije.

### Sigurnosne i operativne napomene

- Aplikacija koristi osjetljive dozvole za lokaciju, senzore, pozive, notifikacije i rad u pozadini.
- Repozitorij trenutno ne sadrzi automatizirane testove.
- Mapa `GuardianCircle/` predstavlja zaseban eksperimentalni ili referentni projekt temeljen na Wear Compose predlosku i nije ukljucena u aktivni korijenski build.
- Konfiguracijske datoteke poput `google-services.json` trebaju odgovarati stvarnom Firebase projektu okruzenja u kojem se aplikacija pokrece.
- Google API key iz `google-services.json` treba ograniciti na Android aplikaciju i odgovarajuci signing certifikat u Google Cloud Console.

### Status repozitorija

Trenutno stanje repozitorija pokazuje funkcionalan temelj za Wear OS sigurnosnu aplikaciju s lokalnim SOS tokom, pozadinskim monitoringom i Firebase sinkronizacijom. Za produkcijsku uporabu preporucuje se dodatno uvesti stvarne izvore zdravstvenih podataka, zavrsiti obiteljsku ili guardian klijentsku aplikaciju, definirati sigurnosna pravila Firestorea i uvesti testove te release proces.

### Licenca

U repozitoriju trenutno nije definirana zasebna licenca.

---

## English

### Overview

GuardianCircle is an Android Wear OS project focused on user safety through fall detection, manual SOS activation, and Firebase-backed emergency event synchronization. The active repository content is the root Gradle project with the `app` module, while the `GuardianCircle/` directory contains a separate auxiliary Compose starter project that is not included in the main build.

The project is designed as a standalone watch application that:

- continuously monitors accelerometer data in the background
- provides manual SOS activation from the watch UI
- stores and validates emergency phone numbers
- plays an audible alarm and attempts sequential outbound calls
- synchronizes live telemetry and emergency events to Cloud Firestore
- registers the watch FCM token for future push delivery to family members or guardians

### Key Features

- Automatic fall detection based on impact thresholding, short stillness confirmation, and a cancellable 30-second safety countdown.
- Manual SOS activation from the primary watch screen.
- Local emergency contact storage with Croatian `+385` phone number validation.
- Maximum-volume alarm playback during an active SOS session.
- Sequential dialing of multiple saved contacts with call-state observation when permissions are available.
- Firestore synchronization for `live_stats/current`, `emergency_logs/{eventId}`, and `devices/watch`.
- Periodic health telemetry synchronization every 15 minutes via WorkManager.
- Automatic recovery of monitoring and periodic work after device reboot.

### Active Repository Structure

```text
Wellbeing-2/
|-- app/                          Active Wear OS module
|   |-- src/main/AndroidManifest.xml
|   |-- src/main/java/com/guardian/circle/
|   |   |-- data/                Firestore access and data models
|   |   |-- messaging/           Firebase Messaging service
|   |   |-- receivers/           Boot receiver
|   |   |-- sensors/             Fall detection algorithm
|   |   |-- services/            Foreground monitoring service
|   |   |-- ui/                  Main XML-based activity
|   |   |-- utils/               SOS logic, permissions, and contacts
|   |   `-- workers/             WorkManager synchronization
|   `-- src/main/res/            Layouts, drawables, styles, and alarm audio
|-- docs/firebase/               Firebase guide and database schema
|-- gradle/                      Wrapper and version catalog
|-- GuardianCircle/              Separate auxiliary Wear Compose starter project
|-- build.gradle                 Root Gradle configuration
`-- settings.gradle              Active root project; includes only :app
```

### Architecture and Implementation Notes

- The active application uses a Java + XML implementation in the `app` module.
- `FallDetectionService` runs as a foreground service and subscribes to accelerometer data.
- `FallAnalyzer` implements impact threshold and fall confirmation logic.
- `EmergencySyncWorker` writes emergency events to Firestore.
- `HealthSyncWorker` periodically uploads location, battery state, and other available telemetry.
- `FirestoreRepository` centralizes Firebase Auth access, Firestore batch writes, and fallback user identity handling.
- `GuardianFirebaseMessagingService` stores the watch FCM token in Firestore.
- Heart rate, blood pressure, and step count are currently read from `SharedPreferences`, which means the telemetry model is prepared, but direct integration with real health data providers is not yet completed in this repository.

### Technology Stack

- Android application plugin `9.1.0`
- Gradle wrapper `9.3.1`
- Java 11
- Android `minSdk 30`, `targetSdk 36`, `compileSdk 36`
- Wear OS libraries via `androidx.wear:wear`
- WorkManager for background jobs
- Google Play Services Location and Wearable APIs
- Firebase Authentication, Cloud Firestore, and Firebase Cloud Messaging

### Requirements

- Android Studio with support for AGP 9.1 and Gradle 9.3.1
- JDK 11
- Wear OS emulator or physical device
- Firebase project for cloud-enabled features

### Running the Project

1. Open the repository in Android Studio.
2. Sync the Gradle files.
3. If Firebase functionality is required, register the Android application using the package name `com.guardian.circle.ui`.
4. Place your own `google-services.json` file in the `app/` directory.
5. Run the `app` module on a Wear OS emulator or device.

Example build command:

```powershell
.\gradlew.bat assembleDebug
```

### Firebase and Database Documentation

- The Croatian Firebase setup guide is available at [docs/firebase/firebase_setup_hr.md](docs/firebase/firebase_setup_hr.md).
- The proposed Firestore schema is available at [docs/firebase/firestore_schema_guardiancircle.json](docs/firebase/firestore_schema_guardiancircle.json).
- The application is configured so that the Google Services plugin is only applied when `app/google-services.json` exists.
- Keep `app/google-services.json` local-only and do not commit it to the repository.
- If Firebase is not configured, the local SOS flow remains available, but cloud synchronization is disabled.

### Security and Operational Notes

- The application requests sensitive permissions related to location, sensors, calls, notifications, and background execution.
- The repository currently does not include automated tests.
- The `GuardianCircle/` directory is a standalone experimental or reference project based on a Wear Compose starter template and is not part of the active root build.
- Configuration files such as `google-services.json` should match the actual Firebase environment used for deployment.
- Restrict any Google API key generated for Firebase to the Android app package and signing certificate in Google Cloud Console.

### Repository Status

The repository currently provides a solid foundation for a Wear OS safety application with a local SOS flow, background monitoring, and Firebase synchronization. For production use, the project should be extended with real health data sources, a completed family or guardian client application, hardened Firestore security rules, automated tests, and a release pipeline.

### License

No separate license is currently defined in this repository.
