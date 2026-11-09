# Medicina UNA 2027 · Cobertura del banco

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
