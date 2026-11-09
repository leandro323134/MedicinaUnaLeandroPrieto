# Medicina UNA 2027 · Cobertura del banco

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
