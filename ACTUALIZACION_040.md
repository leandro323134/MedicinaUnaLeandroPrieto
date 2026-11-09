# Medicina UNA 0.4.0

## Simulacros y seguimiento

La práctica personalizada alterna materias y luego temas disponibles dentro de cada materia, sin repetir enunciados o IDs. Si se agota una materia, se completa con las demás hasta el máximo disponible. En prácticas de una sola materia se alternan sus temas. Las siete configuraciones anteriores permanecen accesibles como prácticas fijas. Se revisó la [página de admisión 2027](https://med.una.py/admision-medicina/). El reglamento que enlaza (Resolución 979) no se pudo consultar. La aplicación presenta sus cantidades y tiempos como práctica, no como el formato oficial de 2027.

Al entregar se muestran el puntaje, el desglose por materia y la revisión de errores y blancos. El intento guarda las respuestas, la posición y la fecha límite en Room. Salir no cambia el vencimiento. Los errores se pueden repasar desde el resultado, Inicio o Estudiar, con filtros por materia y tema. Un acierto deja la pregunta en práctica y dos aciertos consecutivos en sesiones distintas la marcan dominada. Un fallo reinicia la cuenta. Se conserva el resultado original. Los intentos antiguos se vinculan por enunciado, materia y respuesta correcta cuando hay una coincidencia única. Si no la hay, se muestran como «Sin tema identificado».

Inicio muestra repasos disponibles, programados para más tarde, meta editable y temas con errores. Progreso distingue las respuestas de simulacro y de repaso por tema, señala las muestras de menos de cinco respuestas y mantiene los blancos como posibles errores de práctica sin contarlos como respuestas contestadas en el porcentaje por tema.

## Contenido y fuentes

| Materia | Tarjetas inspeccionadas estructuralmente | Tarjetas corregidas | Preguntas previas inspeccionadas estructuralmente | Preguntas previas corregidas | Preguntas nuevas | Banco final |
|---|---:|---:|---:|---:|---:|---:|
| Biología | 250 | 0 | 28 | 0 | 13 | 41 |
| Física | 208 | 0 | 23 | 0 | 11 | 34 |
| Lenguaje | 221 | 0 | 36 | 0 | 11 | 47 |
| Matemática | 208 | 0 | 18 | 0 | 10 | 28 |
| Química | 562 | 0 | 101 | 0 | 10 | 111 |
| Total | 1449 | 0 | 206 | 0 | 55 | 261 |

La inspección estructural comprobó campos y presencia de referencias. Las 55 preguntas nuevas tienen enunciados y distractores propios, una clave, explicación y una referencia web académica asociada. Se priorizaron temas con poca o ninguna práctica previa. La cobertura del temario consta por separado en [COBERTURA_2027.md](COBERTURA_2027.md). El cotejo académico de una muestra de diez preguntas anteriores y sus diez tarjetas equivalentes confirmó dos por materia, sin correcciones de contenido. La muestra abarcó teoría celular y Hooke, unidades de newton y watt, «pírrico» y «sucinto», exponentes, número atómico e isótopos. Referencias de contraste: [OpenStax Biología §4.1](https://openstax.org/books/biology-2e/pages/4-1-studying-cells), [SI del BIPM](https://www.bipm.org/en/publications/si-brochure), [DLE pírrico](https://dle.rae.es/p%C3%ADrrico), [DLE sucinto](https://dle.rae.es/sucinto), [OpenStax Álgebra §1.2](https://openstax.org/books/algebra-and-trigonometry-2e/pages/1-2-exponents-and-scientific-notation) y [OpenStax Química §2.3](https://openstax.org/books/chemistry-2e/pages/2-3-atomic-structure-and-symbolism).

Queda pendiente el cotejo académico individual en esta fase de 1439 tarjetas y 196 preguntas anteriores. Se recalcularon los 22 ejercicios numéricos o algebraicos de las preguntas nuevas. De sus 52 enlaces distintos, 41 abrieron directamente y 11 devolvieron una restricción de acceso, mantenimiento o error. Esos 11 enlaces requieren una comprobación directa adicional, aunque se dispuso de referencias académicas y extractos en el trabajo previo. De las tarjetas anteriores, 1216 enlazan una referencia web y 233 remiten a apuntes o PDFs. Las 206 preguntas previas remiten a apuntes o PDFs.

## Verificación de la actualización

La [compilación del APK y las pruebas](https://github.com/leandro323134/MedicinaUnaLeandroPrieto/actions/runs/42615674085) terminaron correctamente con **14 pruebas aprobadas**, 0 fallos y 0 omitidas. Incluyen base limpia, migración de Room desde versiones 1 y 2, reapertura con respuestas guardadas, expiración, selección sin duplicados, repaso en sesiones separadas, respaldo, estadísticas y navegación.

El APK es `py.una.medstudy`, versión 0.4.0, código 9. Contiene los cinco archivos de estudio idénticos a los del repositorio y su certificado coincide con el APK 0.3.5, código 8. La prueba de actualización se hizo a nivel de base de datos. La instalación limpia y la actualización desde un APK anterior en un teléfono siguen pendientes.

## Cómo comprobar en un Redmi 13C

1. En la versión instalada, exportá una copia desde Ajustes y comprobá que podés localizar el JSON. Anotá un dato del historial o contestá una pregunta en un intento nuevo y salí sin entregarlo.
2. Descargá el artefacto `MedicinaUNA-debug-apk` de la compilación, descomprimilo y abrí `MedicinaUNA-actualizacion.apk`. Si Android ofrece actualizar, aceptá. Si la firma no coincide, conservá la instalación y la copia y anotá el mensaje.
3. Comprobá la tarjeta o historial previo y volvé al intento. Deben conservarse respuestas, posición y vencimiento. Entregá el simulacro y probá el repaso de un error.
4. Para una instalación limpia, usá otro dispositivo o un perfil independiente para que la instalación con progreso siga disponible.
