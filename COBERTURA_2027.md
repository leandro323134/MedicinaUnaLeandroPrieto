# Medicina UNA 2027 · Cobertura del banco

## Revisión de Física en la versión 0.3.4

Se cotejaron visualmente las páginas 16 y 17 de la Resolución CD 0914-00-2026. Las 10 unidades tienen **66 apartados numerados**, frente a los 57 detectados en el recuento histórico del OCR. Las 106 tarjetas previas ya representaban los 66 apartados. Se incorporó al menos una tarjeta de aplicación o contraste por apartado, y el [mapa de cobertura](cobertura_programa_2027.json) registra el tema, la página y los IDs.

| Unidad | Apartados | Añadidas | Total del programa |
|---|---:|---:|---:|
| 1. Magnitudes | 4 | 6 | 23 |
| 2. Cinemática | 8 | 8 | 23 |
| 3. Dinámica | 9 | 10 | 23 |
| 4. Hidrostática | 5 | 5 | 12 |
| 5. Hidrodinámica | 4 | 5 | 10 |
| 6. Termometría y dilatación térmica | 8 | 8 | 17 |
| 7. Calorimetría | 4 | 5 | 11 |
| 8. Mecánica ondulatoria | 7 | 8 | 17 |
| 9. Óptica | 6 | 9 | 19 |
| 10. Electricidad | 11 | 15 | 30 |
| **Total** | **66** | **79** | **185** |

La app contiene **1364 tarjetas**, de las cuales 1131 pertenecen al banco del programa. Las 1285 anteriores conservan sus campos e IDs. Los nuevos ejercicios incluyen conversión de unidades, movimiento relativo, balance de energía, continuidad y Bernoulli, calorimetría, Doppler, formación de imágenes, ley de Gauss y circuitos con instrumentos de medida. Cada tarjeta nueva indica una sección de OpenStax o la referencia del Sistema Internacional del BIPM.

El cotejo verifica que los apartados están representados. Queda el cotejo visual de Matemática, la comprobación individual de las fuentes heredadas y la prueba de instalación en un teléfono físico.

Archivos modificados: `app/src/main/assets/programa_2027.json`, `cobertura_programa_2027.json`, `COBERTURA_2027.md`, `README.md`, `app/build.gradle.kts` y `app/src/test/java/py/una/medstudy/StudyRepositoryTest.kt`.

## Revisión de Química en la versión 0.3.3

Se cotejaron las páginas 5 a 13 de la Resolución CD 0914-00-2026. El [mapa de cobertura](cobertura_programa_2027.json) enlaza **287 apartados finales** de las 17 unidades con sus páginas e IDs. El conteo reemplaza el inventario anterior de 279 referencias detectadas por OCR. Los apartados 10.6.6 (gráficos de cinética), 10.8.1 (temperatura y rapidez) y 11.6.2 (pH y pOH) se añadieron como referencias independientes con tarjetas propias.

| Unidad | Apartados | Nuevas | Total del programa |
|---|---:|---:|---:|
| 1. Materia y energía | 9 | 4 | 16 |
| 2. Estructura atómica y clasificación de los elementos | 12 | 4 | 36 |
| 3. Notación y nomenclatura inorgánica | 5 | 4 | 20 |
| 4. Enlaces e interacciones intermoleculares | 13 | 6 | 21 |
| 5. Reacciones inorgánicas | 10 | 3 | 13 |
| 6. Estados de agregación | 19 | 6 | 26 |
| 7. Disoluciones | 10 | 7 | 20 |
| 8. Funciones de la química inorgánica | 15 | 7 | 24 |
| 9. Estequiometría | 7 | 3 | 11 |
| 10. Cinética química | 18 | 6 | 23 |
| 11. Equilibrio químico | 12 | 4 | 15 |
| 12. Termodinámica y electroquímica | 24 | 8 | 32 |
| 13. Compuestos y reacciones orgánicas | 31 | 10 | 43 |
| 14. Hidrocarburos y heterociclos | 21 | 10 | 33 |
| 15. Funciones orgánicas con enlaces simples | 19 | 8 | 30 |
| 16. Funciones orgánicas con enlaces múltiples | 41 | 13 | 56 |
| 17. Biomoléculas | 21 | 10 | 32 |
| **Total** | **287** | **113** | **451** |

El banco del programa contiene 451 tarjetas de Química. La app suma **1285 tarjetas** entre los tres bancos, con los campos y los IDs de las 1172 previas intactos. Los ejercicios nuevos incluyen balance redox, concentración, tampones, equilibrio, energía libre, Nernst, R/S en Fischer, mecanismos orgánicos, carbohidratos y lípidos. Las tarjetas registran la fuente con su capítulo o sección, principalmente OpenStax, IUPAC y Essentials of Glycobiology.

El cotejo confirma la presencia de tarjetas para los apartados finales del programa. Los enunciados amplios pueden requerir más práctica o una comprobación individual de cada ejemplo heredado. Siguen pendientes la revisión visual de Física y Matemática y la prueba en teléfono físico.

Archivos modificados: `app/src/main/assets/programa_2027.json`, `cobertura_programa_2027.json`, `COBERTURA_2027.md`, `README.md`, `app/build.gradle.kts` y `app/src/test/java/py/una/medstudy/StudyRepositoryTest.kt`.

### Verificación de la versión 0.3.3

- Se validaron los 1285 IDs, el contenido de las 1172 tarjetas anteriores y los enlaces a tarjetas de los 287 apartados de Química.
- Se recalcularon 30 resultados numéricos de las tarjetas nuevas y se verificó la conservación de átomos y carga en el ejercicio redox de permanganato y sulfito.
- La [compilación en GitHub Actions](https://github.com/leandro323134/MedicinaUnaLeandroPrieto/actions/runs/42561123600) terminó con 9 pruebas aprobadas, 0 fallos y 0 omitidas.
- Los tres bancos del APK coinciden byte por byte con los del repositorio. Se verificó el identificador `py.una.medstudy`, la versión 0.3.3, el código 6 y la misma huella de certificado que en 0.3.2. La instalación en un teléfono físico sigue pendiente.

## Revisión de Lenguaje y Comunicación en la versión 0.3.2

Se compararon visualmente las páginas 2, 3 y 4 de la Resolución CD 0914-00-2026 con las tarjetas. El [mapa de cobertura](cobertura_programa_2027.json) registra los 80 apartados finales numerados, su página y los IDs de las tarjetas correspondientes. Los encabezados que agrupan apartados no se cuentan de nuevo. En V.9.6 se añadieron también tarjetas propias sobre argumentos, adjuntos y transitividad, porque esos contenidos figuran en el encabezado.

| Bloque | Tarjetas anteriores del programa | Añadidas | Total del programa |
|---|---:|---:|---:|
| Lingüística de la comunicación | 15 | 5 | 20 |
| Semántica | 14 | 7 | 21 |
| Comprensión lectora | 10 | 9 | 19 |
| Ortografía | 10 | 24 | 34 |
| Morfosintaxis | 49 | 38 | 87 |
| **Lenguaje y Comunicación** | **98** | **83** | **181** |

Los refuerzos incluyen modalidades, barreras comunicativas, homonimia, conjeturas, formularios y otros textos breves, acentuación, grafías consonánticas, puntuación, concordancia, clíticos y funciones de los relativos. Las consignas de aplicación son originales. Las referencias se consignan en las tarjetas e incluyen el Diccionario panhispánico de dudas, el Glosario de términos gramaticales, el Diccionario de la lengua española, el apéndice de ortografía del Diccionario del estudiante y el Instituto Cervantes. Algunas entradas del Instituto Cervantes se consultaron por extractos indexados cuando falló el acceso directo.

La app contiene **1172 tarjetas**: 939 del banco del programa y 233 anteriores. Se conservaron las 1089 tarjetas de la versión previa y su contenido. Quedan por verificar individualmente las fuentes de las tarjetas heredadas, el nivel de dominio en cada apartado con práctica de examen y el cotejo visual completo de Química, Física y Matemática.

Archivos modificados: `app/src/main/assets/programa_2027.json`, `cobertura_programa_2027.json`, `COBERTURA_2027.md`, `README.md`, `app/build.gradle.kts` y `app/src/test/java/py/una/medstudy/StudyRepositoryTest.kt`.

### Verificación de la versión 0.3.2

- Se validaron los 1172 IDs, el contenido de las 1089 tarjetas anteriores y los 80 apartados de Lenguaje. Todos sus enlaces apuntan a tarjetas de la unidad y del apartado correctos.
- La [compilación en GitHub Actions](https://github.com/leandro323134/MedicinaUnaLeandroPrieto/actions/runs/42559858957) terminó con 9 pruebas aprobadas, 0 fallos y 0 omitidas.
- Los tres bancos del APK coinciden byte por byte con los del repositorio. Se verificaron el identificador `py.una.medstudy`, la versión 0.3.2, el código 5 y la misma huella de certificado que la versión 0.3.1. La instalación en un teléfono físico sigue pendiente.

## Revisión de la versión 0.3.1

Se cotejaron visualmente las páginas 13 a 16 de la resolución, que contienen las 15 unidades de Biología. Se compararon sus enunciados con las preguntas existentes y se añadieron tarjetas para conceptos ausentes o poco desarrollados. El mapa registra 119 grupos de conceptos de Biología y enlaza cada grupo a sus tarjetas concretas. Se revisaron también los huecos de Lenguaje y Química que identificaba la versión previa. Esta revisión no certifica todavía la suficiencia de todas las explicaciones ni la cobertura de todas las subdivisiones del programa.

| Materia | Incorporaciones en 0.3.1 | Tarjetas del banco 2027 acumuladas |
|---|---:|---:|
| Lenguaje y Comunicación | 6 | 98 |
| Química | 12 | 338 |
| Biología | 37 | 213 |
| Física | 0 | 106 |
| Matemática | 0 | 101 |
| **Total** | **55** | **856** |

Con las 233 tarjetas que preceden al banco 2027, la app contiene **1089 tarjetas**. Los IDs y el contenido de esas 1034 tarjetas previas se conservan. Los simulacros mantienen sus 206 preguntas y siete configuraciones.

- **Lenguaje**. Se incorporaron monosemia, contraste entre sociolecto y registro, y predicativos del complemento indirecto y de régimen.
- **Química**. Se incorporaron densidad y volumen molar, fórmulas molecular, empírica y estructural, y formulación y nomenclatura de ejemplos binarios, ternarios y cuaternarios. Queda ampliar la variedad de familias y auditar las subdivisiones de orgánica.
- **Biología**. Los refuerzos se distribuyen por las 15 unidades, con tarjetas específicas sobre preparación de microscopía electrónica, confocal y FRAP, el potencial de reposo, F₀ y F₁, proteínas de unión a la actina, remodelación de matriz, tráfico lisosomal, organización nuclear, regulación del ARN, síntesis de translesión y etapas de división, entre otros temas.

Las tarjetas nuevas citan secciones del Manual de la Nueva gramática y el DLE de RAE/ASALE, IUPAC, Royal Society of Chemistry, OpenStax y Molecular Biology of the Cell. Se encontraron pasajes académicos para los conceptos añadidos. Parte de la consulta de Alberts se hizo mediante extractos indexados, ya que algunas páginas directas devolvieron una comprobación de navegador. Queda por verificar la bibliografía previa tarjeta por tarjeta y auditar visualmente las demás materias de punta a punta.

Archivos de esta revisión: banco `app/src/main/assets/programa_2027.json`, `cobertura_programa_2027.json`, `COBERTURA_2027.md`, `README.md`, `app/build.gradle.kts` y `app/src/test/java/py/una/medstudy/StudyRepositoryTest.kt`.

### Verificación de la versión 0.3.1

- Se comprobaron 1089 IDs distintos y 57 unidades. Se verificó que las 1034 tarjetas anteriores conservan sus campos y que las referencias del mapa apuntan a IDs válidos. Los 119 grupos de Biología tienen al menos una tarjeta vinculada.
- Se recalcularon los dos volúmenes molares y la velocidad enzimática del refuerzo, y se comprobaron las cargas de las fórmulas de sulfato de aluminio e hidrogenosulfato de calcio.
- El [registro de compilación y pruebas](https://github.com/leandro323134/MedicinaUnaLeandroPrieto/actions/runs/42555685887) corresponde al commit de la versión 0.3.1. Terminó con 9 pruebas aprobadas, 0 fallos y 0 omitidas.
- El APK contiene los tres bancos idénticos a los del repositorio. Se verificó `py.una.medstudy`, versión 0.3.1, código 4 y el certificado de 0.2.0 corregida. No se ha hecho una prueba de instalación en teléfono físico.

## Punto de partida y pruebas de la versión 0.3.0

La versión 0.3.0 incorpora 801 tarjetas nuevas en la aplicación existente. Conserva las 233 anteriores y su identidad. La carga inicial lee los tres archivos de tarjetas con el mismo mecanismo de importación y de actualización de contenido.

| Materia | Bloques con tarjetas | Tarjetas nuevas |
|---|---:|---:|
| Lenguaje y Comunicación | 5 | 92 |
| Química | 17 | 326 |
| Biología | 15 | 176 |
| Física | 10 | 106 |
| Matemática | 10 | 101 |
| **Total** | **57** | **801** |

El alcance se basa en la Resolución CD 0914 de la FCM UNA para ingreso 2027. Física y Matemática pertenecen a Ciencias Exactas en el PDF. El [mapa de cobertura](cobertura_programa_2027.json) relaciona cada unidad y referencia registrada con las tarjetas concretas. Las referencias B son identificadores de trabajo para los párrafos de Biología. La resolución determina el temario. Los textos de cada materia sustentan el contenido.

## Estado académico

- Hay tarjetas en cada bloque. El número de tarjetas y de referencias no certifica que se haya desarrollado cada componente de un subtema compuesto.
- Se reutilizaron las fuentes académicas disponibles y se consignaron referencias de OpenStax, Karp, RAE y bibliografía oficial. La búsqueda web falló en esta sesión. Falta abrir y verificar las referencias incorporadas que no habían sido consultadas en sesiones previas.
- El cotejo de numeración con el texto extraído detectó errores de OCR. Falta la revisión visual completa del PDF y la correspondencia de cada término de los apartados compuestos, en particular los párrafos de Biología y los temas extensos de gramática y química orgánica.
- Las 206 preguntas de opción múltiple y los 7 simulacros existentes conservan sus bancos. Las 801 incorporaciones corresponden a tarjetas de repaso.

## Verificación técnica

- Verificación local de estructura, IDs, campos obligatorios, referencias del mapa y presencia de las cinco materias y los 57 bloques.
- La prueba de repositorio carga los tres paquetes en Room, verifica las 1034 tarjetas y comprueba que se conservan ambas leyes de De Morgan como tarjetas distintas.
- La prueba de interfaz espera la carga del paquete nuevo y recorre ajustes, catálogo, repaso y progreso. La compilación de GitHub ejecuta el conjunto de pruebas y verifica la firma del APK.

### Cotejo de referencias y alcance de la revisión

| Área | Apartados finales numerados detectados y vinculados |
|---|---:|
| Lenguaje y Comunicación | 80 de 80 |
| Química | 279 de 279 |
| Física | 57 de 57 |
| Matemática | 59 de 59 |
| **Total de apartados numerados detectados** | **475 de 475** |

El denominador procede del texto extraído del PDF. Se corrigieron números que el OCR unió a letras o a otros dígitos y se excluyeron encabezados. Este cotejo prueba la presencia de referencias, no la exhaustividad del OCR ni el desarrollo completo de cada apartado. En Biología se comprobó la presencia de las 15 unidades, sin aplicar ese recuento a sus párrafos.

Queda por comprobar o ampliar cada componente de los enunciados compuestos. Entre los huecos detectados están la monosemia, los predicativos del complemento indirecto y del régimen, los niveles de lengua frente a los registros, la comparación de densidad y volumen atómico en la tabla periódica, y la práctica de la clasificación y nomenclatura sistemática de compuestos inorgánicos más allá de los ejemplos incorporados. Aún no hay una lista exhaustiva de huecos porque falta terminar la auditoría visual y académica, incluidos los párrafos de Biología.

### Resultados de las pruebas

- La [ejecución de GitHub Actions](https://github.com/leandro323134/MedicinaUnaLeandroPrieto/actions/runs/42547719870) terminó con **9 pruebas aprobadas, 0 fallos y 0 omitidas**. Incluyó pruebas de carga en Room, migración, importación y respaldo, simulacros, repetición e interfaz.
- Se verificaron **24 cálculos de ejemplo** de Matemática, Física y Química, entre ellos ecuaciones, trigonometría, dilatación, óptica, neutralización, Nernst y conservación de carga en una ecuación redox.
- Los tres paquetes dentro del APK coinciden byte por byte con los del repositorio y suman 1034 tarjetas. El APK compilado es `py.una.medstudy`, versión 0.3.0, código 3. Su certificado coincide con el de 0.2.0 corregida. La primera versión 0.1.0 tenía otra firma.
- La verificación cubre compilación y pruebas automatizadas. La instalación y el comportamiento en un teléfono físico siguen sin comprobarse.

### Archivos modificados en la aplicación existente

| Archivo | Cambio |
|---|---|
| `app/src/main/assets/programa_2027.json` | 801 tarjetas, con referencias al programa. |
| `app/src/main/java/py/una/medstudy/StudyModel.kt` | Carga del banco incluido en la app. |
| `app/build.gradle.kts` | Versión 0.3.0 y código 3. |
| `app/src/test/java/py/una/medstudy/StudyRepositoryTest.kt` | Comprueba importación y recuento de los tres bancos. |
| `app/src/test/java/py/una/medstudy/StudyUiTest.kt` | Espera la carga del nuevo contenido antes de probar la navegación. |
| `cobertura_programa_2027.json` | Correspondencias de unidades, apartados y tarjetas. |
| `COBERTURA_2027.md` | Alcance, límites y pruebas. |
| `README.md` | Recuento del banco y enlace al informe. |
