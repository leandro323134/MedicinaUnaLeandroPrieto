# Compilar y actualizar Medicina UNA

El único identificador de la app es `py.una.medstudy`. El APK se llama
`MedicinaUNA-actualizacion.apk` y se obtiene en el artefacto
`MedicinaUNA-debug-apk` de Actions → Compilar APK Android. La compilación
ejecuta las pruebas antes de preparar el APK.

## Compatibilidad con instalaciones anteriores

Android exige el mismo identificador y la misma clave de firma para actualizar
una instalación conservando sus datos. También se debe incrementar `versionCode`
para cada nueva versión distribuida.

El certificado de la primera versión tiene esta huella SHA-256:
`e89c065d8bbb9167e1806e398b9927978a2deae89cdf00f2d5dee2c088310134`.
Su clave privada no apareció en los artefactos ni en la configuración de caché
de la compilación original. El certificado del APK no permite recuperar esa clave.

La versión 0.2.0 utiliza esta huella:
`d6f4196df346ad76b899acf646eb6ec7c4f5deb842b8a17529575669b9a2d791`.
Por esta diferencia, **no se puede instalar encima de la primera versión** salvo
que se recupere la clave privada original y se firme una compilación con ella.
Una reinstalación borra los datos locales: respaldarlos antes de desinstalar.
La primera versión no incluye exportación de progreso; la versión 0.2.0 sí.

La entrega anterior llamada Medicina UNA Estudio utilizaba por error
`py.una.medstudy.estudio`. Ese paquete ya no se genera. Sus datos se pueden
exportar desde Ajustes e importar en la app corregida antes de quitarla.

## Conservar la firma de 0.2.0

El workflow recupera `.signing/debug.keystore` de la caché existente y comprueba
su huella antes de compilar. **Una caché puede caducar y no sustituye un respaldo.**
Si falta la clave, el workflow se detiene; nunca genera automáticamente otra.

Si se dispone del archivo original de esa clave, se puede guardar su contenido
Base64 en el secreto privado de Actions `MEDICINA_SIGNING_KEYSTORE_BASE64` para
restaurarla. No publicar la clave ni sus contraseñas en el repositorio, registros
o artefactos. Crear una clave nueva no permite actualizar instalaciones anteriores.

Al compilar localmente, definir `APP_DEBUG_KEYSTORE` con la ruta de la misma clave.
Un APK generado con otra clave local no es una actualización compatible.
Desde 0.4.1, omitir la variable genera `app-debug-unsigned.apk`, sin crear una
clave nueva. Ese archivo sirve para inspeccionar la compilación, pero **no se
distribuye para instalar**. Una ruta configurada que no exista detiene Gradle.
El workflow informa si el secreto de respaldo está disponible sin mostrar su
contenido y sigue verificando el certificado antes de compilar.

## Comprobación antes de entregar

1. Ejecutar las pruebas y compilar el APK.
2. Verificar paquete, versión y certificado del APK final.
3. Confirmar compatibilidad con el certificado de la instalación a actualizar.
4. Probar una actualización en un dispositivo cuando esté disponible.

Esta entrega fue compilada y probada automáticamente; no se realizó una
instalación sobre el teléfono del usuario.
