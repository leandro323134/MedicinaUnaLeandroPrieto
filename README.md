# Medicina UNA

Aplicación Android en Kotlin, Jetpack Compose y Room, compatible con Android 8.0
o posterior. Guarda el estudio localmente y funciona sin conexión.

La versión 0.2.0 incorpora Inicio, Materias, Estudiar, Progreso y Ajustes;
tarjetas con explicaciones y dificultad, repetición espaciada FSRS-5,
simulacros cronometrados con corrección, estadísticas y temas de colores.
La versión 0.3.3 incorpora 1052 tarjetas del programa de ingreso 2027 a las 233
anteriores. Incluye 206 preguntas de opción múltiple y 7 configuraciones de
simulacro. Consulta el [informe de cobertura](COBERTURA_2027.md) para ver qué se
comprobó y qué revisión académica queda pendiente.

Se pueden importar paquetes JSON y exportar/restaurar el progreso desde Ajustes.
Los identificadores estables de tarjetas permiten evitar duplicados. La migración
de la base de datos de la versión 1 conserva tarjetas y registros anteriores,
pero instalar una actualización también requiere una firma Android compatible.

El paquete único es `py.una.medstudy`, con nombre Medicina UNA. Se eliminó la
variante `.estudio` que había generado una segunda aplicación por error.
La clave de firma de la primera compilación no está disponible: consultar
[PUBLICAR_APK.md](PUBLICAR_APK.md) antes de intentar reemplazar esa instalación.

Para compilar se usan JDK 17, Android SDK 35 y Gradle 8.7:

```bash
gradle --no-daemon :app:testDebugUnitTest :app:assembleDebug
```

La firma se configura con `APP_DEBUG_KEYSTORE`. El workflow de GitHub verifica
la huella de la clave y publica `MedicinaUNA-actualizacion.apk`. Las pruebas
cubren migración, importación, repasos, simulacros, respaldo, rachas y navegación.
