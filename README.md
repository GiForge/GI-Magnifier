<center><img width="327" height="725" alt="image" src="https://github.com/user-attachments/assets/0cdd510c-be10-488e-b56f-da714c8374dc" /></center>

# Android CameraX Magnifier & Reader App

[![Status](https://img.shields.io/badge/status-active-success.svg)]()
[![Platform](https://img.shields.io/badge/platform-Android-3DDC84.svg?logo=android&logoColor=white)]()
[![Language](https://img.shields.io/badge/language-Kotlin-7F52FF.svg?logo=kotlin&logoColor=white)]()
[![UI](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg?logo=jetpackcompose&logoColor=white)]()
[![CameraX](https://img.shields.io/badge/CameraX-1.3+-success.svg)]()
[![License](https://img.shields.io/badge/license-MIT-blue.svg)]()

---

## 2. Descrizione Generale
Un'applicazione Android moderna e performante, sviluppata interamente in **Jetpack Compose**, progettata per fungere da lente d'ingrandimento digitale avanzata e strumento di supporto alla lettura. L'app sfrutta le API di **CameraX** per garantire un flusso video fluido, offrendo comandi rapidi a schermo (HUD) in stile scuro ottimizzati per ipovedenti o per l'uso in condizioni di scarsa illuminazione. Funziona **al 100% in locale e offline**, garantendo la massima privacy dei dati dell'utente.

---

## 3. Funzionalità Dettagliate
* **Zoom continuo e preset veloci:** Regolazione fluida tramite gesture o selezione rapida dei livelli preimpostati (**1x, 2x, 4x, 8x, 10x**).
* **Gesti tattili avanzati:** Supporto completo per il *pinch-to-zoom* e *tap-to-focus* con animazione visiva del mirino di messa a fuoco.
* **Controllo torcia LED:** Attivazione e disattivazione immediata del flash per illuminare i testi in ambienti bui.
* **Fermo immagine (Freeze Frame):** Possibilità di congelare il fotogramma corrente per esaminarlo con calma, navigandolo e applicando lo zoom direttamente sull'immagine bloccata.
* **Filtri di contrasto per la lettura:** Preset cromatici dedicati per facilitare la lettura di testi complessi:
  * Normale
  * Negativo
  * Bianco/Nero ad alto contrasto
  * Giallo su Nero
  * Seppia
* **Regolazione esposizione e luminosità:** Slider dedicati per calibrare la luce del sensore in tempo reale.
* **Gestione scatti ad alta risoluzione:** Cattura immagini nitide, consultabili tramite una galleria interna integrata, con funzioni di visualizzazione a schermo intero, condivisione sicura tramite `FileProvider` ed eliminazione dei file.

---

## 4. Stack Tecnologico & Architettura
L'architettura segue i principi raccomandati da Google per lo sviluppo Android moderno:
* **Architettura:** MVVM (Model-View-ViewModel) per una netta separazione tra logica di business e interfaccia utente.
* **Core Camera:** **CameraX** per la gestione stabile e compatibile del ciclo di vita della fotocamera.
* **Interfaccia Utente:** **Jetpack Compose** con tema **Material 3 Dark HUD** ad alto contrasto.
* **Caricamento Immagini:** **Coil** per la gestione efficiente e asincrona delle anteprime in galleria.
* **Asynchrony:** **Kotlin Coroutines** e `Flow` per la gestione reattiva degli stati della UI e dei flussi di dati della fotocamera.

---

## 5. Permessi Richiesti & Sicurezza

| Permesso | Motivazione |
| :--- | :--- |
| `android.permission.CAMERA` | Indispensabile per accedere al flusso video inquadrato dall'ottica e abilitare le funzioni di zoom, autofocus e filtri in tempo reale. |
| `android.permission.FLASHLIGHT` | Richiesto per pilotare il LED della fotocamera come torcia di supporto alla lettura. |
| `android.permission.WRITE_EXTERNAL_STORAGE` / `READ_EXTERNAL_STORAGE` | Necessario esclusivamente in locale per salvare e visualizzare gli scatti ad alta risoluzione effettuati dall'utente nella galleria interna dell'app. |

> **Nota sulla privacy (100% Offline):** L'applicazione non richiede connessione a internet (`INTERNET` permission non presente) e non invia alcun flusso video, foto o dato di utilizzo a server esterni. Tutto viene elaborato ed archiviato esclusivamente sul dispositivo.

---

## 6. Mappa della Struttura del Progetto

```text
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/magnifier/
│   │   │   ├── data/             # Gestione repository locali e persistenza immagini
│   │   │   ├── ui/               # UI components, temi Material 3 e schermate Compose
│   │   │   │   ├── components/   # HUD, controlli zoom, filtri e mirino animato
│   │   │   │   ├── gallery/      # Visualizzatore galleria interna
│   │   │   │   ├── screen/       # Activity principale e composables di navigazione
│   │   │   │   └── theme/        # Definizione colori, typography e Dark HUD theme
│   │   │   ├── viewmodel/        # ViewModel per gestione stato fotocamera e filtri
│   │   │   └── util/             # Helper per FileProvider e utility di estensione
│   │   ├── res/                  # Risorse XML (icone, stringhe, file provider paths)
│   │   └── AndroidManifest.xml   # Dichiarazione permessi e componenti
└── build.gradle.kts              # Configurazione dipendenze (CameraX, Compose, Coil)
```

---

## 7. Istruzioni di Compilazione ed Esecuzione

Per compilare ed eseguire correttamente il progetto sul proprio ambiente di sviluppo:

1. **Requisiti di sistema:**
   * **Android Studio** (versione Hedgehog o superiore raccomandata).
   * **JDK 17** o compatibile configurato nel progetto.
   * Dispositivo fisico Android con supporto a CameraX (Android 8.0 / API 26 o superiore) oppure un emulatore configurato con fotocamera virtuale.

2. **Clonazione e Apertura:**
   * Clona il repository nella tua directory locale:
     ```bash
     git clone https://github.com/tuo-username/tuo-repo-name.git
     ```
   * Avvia **Android Studio**, seleziona *Open* e punta alla cartella radice del progetto clonato.

3. **Sincronizzazione Gradle:**
   * Attendi il completamento della sincronizzazione automatica dei file Gradle e il download delle dipendenze (CameraX, Compose Material 3, Coil).

4. **Esecuzione:**
   * Collega un dispositivo fisico via USB (con il *Debug USB* attivo) o avvia un emulatore Android.
   * Clicca sul pulsante **Run (▶)** nella barra degli strumenti di Android Studio per compilare l'APK ed eseguirlo sul dispositivo.
