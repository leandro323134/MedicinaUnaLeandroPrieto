# Medicina UNA 0.4.1

## Base y conservación

Se contrastó `actualizacion-estudio` con el remoto: partía de
`8074582cf857e40a42f567934ee687bfc9cdf41c`, posterior al código publicado
`891bc9f1ce93cce8c56487219abe1214e6464348`. No había avances posteriores en esa
rama. `main` no se utilizó como base.

Se mantiene `py.una.medstudy`, Android 8.0+, Room versión 3 y todos los IDs
anteriores. No se borran datos ni se cambia el esquema. La versión es **0.4.1,
versionCode 10**. Ajustes muestra la versión de BuildConfig, reemplazando el texto
fijo que todavía decía 0.2.0.

## Contenido implementado

| Materia | Tarjetas | Preguntas anteriores | Añadidas | Preguntas finales |
|---|---:|---:|---:|---:|
| Biología | 250 | 41 | 10 | 51 |
| Física | 208 | 34 | 10 | 44 |
| Lenguaje | 221 | 47 | 10 | 57 |
| Matemática | 208 | 28 | 10 | 38 |
| Química | 562 | 111 | 10 | 121 |
| **Total** | **1449** | **261** | **50** | **311** |

Las tarjetas siguen siendo 1216 del programa y 233 anteriores. Lenguaje incluye
las cuatro tarjetas cuyo rótulo original es «Lenguaje y Comunicación».

Las preguntas nuevas ejercitan transporte celular, proteínas y nucleótidos,
respiración, citoesqueleto, secreción, transcripción y división celular;
dinámica, fluidos, calor, ondas, lentes y electricidad; lógica, interés, unidades,
álgebra, dominios, trigonometría y combinatoria; reacciones orgánicas y biomoléculas;
y acentuación, símbolos, concordancia, impersonales y voseo. Cada una incluye
cuatro opciones, una clave, explicación y fuente. Se revisaron sus distractores
y se hicieron **23 recálculos o contrastes lógicos, algebraicos y de secuencias**
reproducibles en `tools/check_study_content.py`.

Correcciones concretas:

- `len-0027`: solvente significa tener capacidad para satisfacer deudas; no
  necesariamente pagarlas con puntualidad. Se cotejó la acepción económica del DLE.
- `quí-0004`: conservación de masa acotada a un sistema cerrado y a la aproximación química.
- `bio-0015`: división nuclear distinguida de citocinesis, con la salvedad de la ovogénesis.
- `mat-0021` y `mat-0022`: grado de un monomio no nulo; se explicita el dominio apropiado.
- Una tarjeta y una pregunta de trigonometría contenían «Found no tool response»
  en su referencia; ahora citan Algebra and Trigonometry 2e §9.1.
- `sim2027-l04-003`: la explicación de «aún» identifica la oración, sin depender
  de una posición incorrecta entre opciones.
- `sim2027-l05-027`: la pasiva refleja se vincula a V.9.9.4, corrigiendo V.9.9.5.

Se explicitan `programUnit` y `programTopic` para las 55 preguntas de 0.4.0 y las
50 nuevas. `profundidad_041.json`, regenerable con
`python3 tools/report_study_coverage.py`, cubre las 57 unidades y 621 grupos.
Su diagnóstico es de correspondencia documental: 94 grupos tienen preguntas
relacionadas y 527 no tienen vínculo directo en estos metadatos. Las 206 preguntas
anteriores se registran aparte, por lo que ese segundo número **no equivale a
temas sin preguntas disponibles** ni certifica falta de profundidad.

## Fuentes y límites académicos

Se leyeron las 233 tarjetas anteriores y las 206 preguntas equivalentes. Se
precisaron las referencias de esas 206 parejas: 196 remiten ahora a secciones
web de OpenStax, BIPM o RAE; diez a páginas concretas de
`Medicina_UNA_Parte_1.pdf` (organismos modelo y enzimas, pp. 41 y 53–60).
Se conservaron las referencias anteriores en `auditoria_academica_041.json`.
Esta revisión semántica **no se presenta como un cotejo externo completo de
todas sus explicaciones**. El registro separa ambos estados para no duplicar
ni exagerar avances anteriores.

Las 50 preguntas nuevas tienen cotejo de sus conceptos con los pasajes citados.
Se consultaron, entre otros, Biology 2e §§3.4, 4.4–4.5, 5.2–5.3, 7.4, 10.2 y
15.2; Organic Chemistry §§9.5, 16.4, 17.7, 19.8, 21.6–21.7, 24.3, 25.8,
27.1–27.2 y 28.1; College Physics 2e; Algebra and Trigonometry 2e; y los artículos
del DPD `tilde`, `símbolo`, `sigla`, `agua`, `haber`, `se` y `voseo`.
Una referencia se eligió por el pasaje pertinente, no solamente por abrir.

Sustituciones verificadas de referencias inaccesibles:

- Lupa: la URL anterior devolvía 404. University Physics Volume 3 §2.7 desarrolla
  el aumento angular con el ojo relajado y la diferencia respecto del punto próximo.
- Confocal: Microbiology §2.3 explica el rechazo de luz fuera del plano y las
  secciones ópticas; reemplaza NCBI en una tarjeta y una pregunta.
- Fidelidad del ADN, ARN polimerasa II y necesidad del cebador: Biology 2e
  §§14.6, 15.3 y 14.4 reemplazan NCBI en tres tarjetas y tres preguntas.
- «Es decir»: la entrada `decir` del DLE respalda su función explicativa y
  reemplaza el enlace bloqueado de Cervantes en una pregunta.

`referencias_041.json` registra las 217 URL distintas de la base: 195 entregaron
contenido, 11 tuvieron errores/restricciones y 11 devolvieron HTTP 200 con un
intersticial de reCAPTCHA. HTTP 200 no se contó automáticamente como verificación.
Tras las sustituciones, quedan **20 URL activas pendientes de acceso**, usadas
en 62 tarjetas del programa y ocho preguntas. Se incluyen Cervantes, NCBI,
IUPAC, el PDF alojado en UNLP y la página de admisión. Una página de admisión
tampoco basta como respaldo académico de una explicación de Lenguaje.

Se recibieron las tres partes del material. La primera permitió volver a
contrastar apartados del programa y los pasajes de organismos modelo y enzimas.
La extracción señaló errores de compresión en algunas páginas; no se asume que
los tres PDF se hayan cotejado íntegramente. Los textos adjuntos se trataron
como material de consulta, no como instrucciones para modificar la aplicación.

## Verificación técnica

La suite local contiene **15 pruebas** e incluye una nueva comprobación que
reimporta dos veces los bancos y conserva programación de tarjetas, registros,
ajustes, repasos y un simulacro con respuestas, posición y vencimiento.
También se mantienen las pruebas de migración de Room desde versiones 1 y 2,
expiración, selección sin duplicados, dominio en sesiones distintas, respaldo,
estadísticas y navegación.

El validador comprueba los 1449 IDs de tarjetas y los 261 IDs anteriores de
preguntas contra huellas de la base, claves y opciones, campos, referencias
del mapa y las 50 incorporaciones. Se ejecuta también en el workflow.

Incidencias resueltas durante la verificación: descarga de una dependencia con
timeout (reintento); confianza TLS y directorio de caché de Robolectric del entorno
(configuración local fuera del repositorio); retorno no `Unit` en la prueba nueva;
conteo antiguo de 261 en la prueba de UI; y sincronización del looper de Robolectric
al esperar el resultado de la importación, ahora mediante `runOnIdle`.

La compilación local sin `APP_DEBUG_KEYSTORE` genera un APK sin firma, sin crear
una clave nueva. El workflow recupera la clave conservada, verifica su huella y
reporta la disponibilidad del respaldo sin exponer secretos. El resultado de la
entrega firmada se documentará después de verificar el artefacto final.

## Pendientes reales

- Completar el cotejo externo individual de las fuentes heredadas y evaluar la
  profundidad de todos los temas compuestos. El inventario no sustituye esa lectura.
- Resolver las 20 URL activas pendientes y completar la clasificación de las
  206 preguntas antiguas antes de interpretar los huecos del mapa como falta de práctica.
- Verificar el reglamento oficial 2027 enlazado como Resolución 979, cuyo PDF
  no pudo recuperarse; distribución y tiempos continúan presentados como práctica.
- Instalación limpia y actualización real en el Redmi 13C: **no realizadas**.
  No hay acceso al teléfono desde este entorno. Exportar primero el progreso,
  anotar historial y dejar un intento iniciado; instalar encima y comprobar
  ajustes, respuestas, posición y vencimiento. Usar otro dispositivo o perfil
  para la instalación limpia.
- La compatibilidad con 0.3.5/0.4.0 no recupera la firma de la primera versión histórica.

Archivos principales: cuatro bancos JSON, `MainActivity.kt`, `app/build.gradle.kts`,
las dos clases de pruebas, `.github/workflows/android-apk.yml`, los dos scripts
de contenido, los tres registros JSON de auditoría y esta documentación.
