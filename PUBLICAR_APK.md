# Medicina UNA: cómo generar un APK para cualquier persona

La aplicación usa Kotlin / Android. Es compatible con **Android 8.0 o superior** (minSdk 26); **no se puede instalar en iPhone**. Los datos de progreso se guardan localmente en cada teléfono.

## Opción rápida: APK de prueba (sin configurar firmas)

1. Descomprime el ZIP y sube **todo su contenido** (no una carpeta contenedora) a la raíz de un repositorio de GitHub.
2. En el repositorio, abre **Actions > Compilar APK Android > Run workflow**.
3. Descarga el artefacto `MedicinaUNA-debug-apk` y descomprímelo para obtener `app-debug.apk`.
4. Puedes enviar ese APK a cualquier usuario de Android compatible. El usuario tendrá que aprobar la instalación desde esa fuente.

**Importante:** esta opción es solo para pruebas. El APK de depuración no está recomendado para una distribución pública duradera: entre compilaciones la clave de firma puede cambiar, lo que impediría actualizar la aplicación sobre la versión anterior.

## Opción recomendada: APK release firmado (para distribución pública)

Necesitas una **clave privada de firma (.jks)**. Se debe conservar siempre: sin ella no podrás actualizar la app instalada sin desinstalarla (lo que podría borrar el progreso local).

### 1. Crear la clave en tu computadora (una sola vez)

Si tienes Java JDK instalado, en PowerShell o terminal ejecuta:

```bash
keytool -genkeypair -v -keystore medicinauna-release.jks -alias medicinauna -keyalg RSA -keysize 3072 -validity 10000
```

El programa te pedirá contraseña y datos para crear el certificado. Guarda la clave `.jks` y sus contraseñas en un lugar privado, con copia de seguridad. **Nunca subas el archivo .jks al repositorio.**

### 2. Añadir los secretos a GitHub

En el repositorio, abre **Settings > Secrets and variables > Actions > New repository secret**.

Crea estos cuatro secretos:

- `ANDROID_KEYSTORE_BASE64`: contenido del archivo `.jks` codificado en Base64.
- `ANDROID_KEYSTORE_PASSWORD`: contraseña del archivo `.jks`.
- `ANDROID_KEY_ALIAS`: `medicinauna` (si utilizaste el alias indicado).
- `ANDROID_KEY_PASSWORD`: contraseña de la clave/alias (puede ser igual a la del archivo).

En **Windows PowerShell**, para copiar la clave en Base64 al portapapeles:

```powershell
[Convert]::ToBase64String([System.IO.File]::ReadAllBytes((Resolve-Path '.\medicinauna-release.jks'))) | Set-Clipboard
```

Pega el resultado como **valor del secreto** `ANDROID_KEYSTORE_BASE64`, no en un archivo del repositorio. No pegues esa cadena ni las contraseñas en chats ni en páginas públicas.

### 3. Generar el APK

En **Actions > Generar APK firmado para compartir > Run workflow**, ejecuta la compilación. Descarga el artefacto `MedicinaUNA-APK-para-compartir`; dentro encontrarás `app-release.apk` firmado. Puedes renombrarlo como `MedicinaUNA.apk` para distribuirlo.

Si tienes Android Studio, también puedes usar **Build > Generate Signed Bundle / APK > APK**, generar o seleccionar la clave y compilar una versión *release* localmente.

### 4. Compartirlo

Sube el APK a un sitio de descarga, Google Drive, GitHub Releases o compártelo directamente. Quien lo reciba debe permitir la instalación desde la aplicación que utilizó para abrir el archivo (según su versión de Android). Avisa de que la aplicación no proviene de Play Store.

### 5. Futuras actualizaciones

- Mantén el `applicationId` `py.una.medstudy`.
- Incrementa `versionCode` en `app/build.gradle.kts` (ej. 1 -> 2).
- Conserva **exactamente la misma clave de firma** para todas las versiones.
- Antes de distribuir masivamente, instala y prueba en teléfonos Android reales. **Este proyecto no ha sido compilado o probado todavía en esta entrega.**

Si posteriormente quieres publicarla en Google Play, normalmente necesitarás crear un Android App Bundle (`.aab`) y cumplir sus requisitos de publicación.
