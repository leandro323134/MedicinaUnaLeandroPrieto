# Medicina UNA (versión inicial)

Aplicación Android local en Kotlin, Jetpack Compose y Room. Importa paquetes JSON con `formatVersion: 1` y una lista `cards`. Cada tarjeta requiere un `id` estable, `question`, `answer`, `subject`, `unit` y `topic`; `source` y `page` son campos de referencia. La importación ignora IDs existentes y conserva su historial de repasos. Cambiar el texto de una tarjeta existente requiere una migración editorial futura.

El paquete inicial de 27 tarjetas se carga al primer inicio. En `cards/` están los paquetes por materia, y en `app/src/main/assets/starter.json` su unión. Los paquetes se pueden importar nuevamente sin duplicar tarjetas. Room conserva tarjetas, estado FSRS y registro de cada repaso sin conexión.

La programación usa FSRS-5 con sus parámetros predeterminados y retención objetivo del 90 %. `Otra vez` repite en un minuto; la primera calificación `Difícil` repite en diez minutos. El estado posterior se calcula mediante estabilidad, dificultad y recuperabilidad.

Para compilar: abrir el directorio en Android Studio con Android SDK 35 y Gradle 8.7, sincronizar y ejecutar `assembleDebug`. El APK se ubicará en `app/build/outputs/apk/debug/app-debug.apk`.

También se incluye `.github/workflows/android-apk.yml`. Al subir el contenido de esta carpeta a la raíz de un repositorio de GitHub y ejecutar «Compilar APK Android» en Actions, el APK aparecerá como artefacto `MedicinaUNA-debug-apk`. El proyecto todavía necesita una compilación real y la corrección de cualquier error que esa compilación detecte.

Los paquetes de `cards/` son un primer conjunto verificado de las páginas indicadas. Falta extraer la totalidad de conocimientos de las unidades extensas de Química y los demás materiales antes de considerar completo el contenido académico.

Para distribuir la app a otras personas: consulta `PUBLICAR_APK.md`. Se añadió el workflow de APK release firmado `android-release.yml`, con secretos de GitHub para no incluir claves privadas en el código.
