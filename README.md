# MevaStyle v3.0 - T-Shirt Designer 3D

## Novita v3.0
- **SceneView 3D**: Rendering reale con modelli .glb (motore Google Filament)
- **Upload utente**: Carica modelli .glb o immagini proprie
- **Export professionale**: Panoramica 4 viste + singoli lati + design DTF
- **Rotazione 3D reale**: Swipe per ruotare, pinch per zoom
- **Admin Panel**: Gestisci prodotti in tempo reale
- **Google Login**: Ruoli admin/utente

## Modelli 3D
Metti i file .glb nella cartella `assets/models/`. L'app li carica automaticamente.
Gli utenti possono anche caricare i propri .glb dall'app.
Modelli gratuiti: https://sketchfab.com (cerca "t-shirt glb")

## Setup Firebase
1. `google-services.json` in `app/`
2. Authentication -> abilita Google -> copia Web Client ID
3. `AuthManager.kt` -> sostituisci WEB_CLIENT_ID
4. Firestore -> collection `admins` con doc = email admin
5. (Opzionale) Storage per salvataggio cloud

## Build
```bash
./gradlew assembleDebug
```
