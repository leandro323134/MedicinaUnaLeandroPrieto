import json
from pathlib import Path
rows=[]
def add(subject,unit,topic,source,page,pairs):
    for q,a in pairs:
        rows.append(dict(id=f"{subject[:3].lower()}-{len(rows)+1:04d}",question=q,answer=a,subject=subject,unit=unit,topic=topic,source=source,page=str(page)))
add('Química','Unidad 1: Materia y energía','Conceptos básicos','Unidad 1 Materia y Energía.pdf',2,[('¿Qué estudia la química?','La composición de la materia y sus transformaciones.'),('¿Qué es la materia?','Todo lo que tiene masa y ocupa un lugar en el espacio.'),('¿Qué mide la masa de un cuerpo?','La cantidad de materia que tiene.')])
add('Química','Unidad 1: Materia y energía','Conservación de la materia','Unidad 1 Materia y Energía.pdf',3,[('¿Qué establece la ley de conservación de la materia?','La masa total de un sistema material permanece constante durante cambios físicos o químicos.'),('En la ecuación 2 Mg + O₂ → 2 MgO, ¿por qué hay un 2 delante del Mg y del MgO?','Para conservar el mismo número de átomos de Mg y O a ambos lados de la ecuación.')])
add('Química','Unidad 2: Estructura atómica','Leyes ponderales','Unidad 2  - Estructura atómica y Clasificación de los elementos.pdf',2,[('¿Qué establece la ley de las proporciones constantes?','Los elementos de un compuesto dado se combinan siempre en la misma proporción de masas.'),('¿Qué establece la ley de las proporciones múltiples?','Si dos elementos forman más de un compuesto, al fijar la masa de uno, las masas del otro guardan relaciones de números enteros sencillos.')])
add('Química','Unidad 5: Reacciones','Reacciones químicas','Unidad 5 Reacciones.pdf',2,[('¿Qué es una reacción química?','Un proceso en que uno o más reactivos se transforman en sustancias diferentes llamadas productos.'),('¿Cómo se llaman las sustancias presentes antes de una reacción?','Reactivos.'),('¿Cómo se llaman las sustancias que se forman en una reacción?','Productos.')])
add('Biología','Capítulo 1: Célula y biología molecular','Teoría celular','CAPÍTULO 1.pdf',3,[('¿Cuáles son los tres principios básicos de la teoría celular?','Todos los organismos están formados por células; la célula es la unidad estructural de la vida; toda célula procede de otra célula preexistente.'),('¿Quién estableció que toda célula procede de otra célula preexistente?','Rudolf Virchow.')])
add('Biología','Capítulo 1: Célula y biología molecular','Funciones celulares','CAPÍTULO 1.pdf',4,[('¿Qué es la apoptosis?','La muerte celular programada, que elimina células innecesarias o dañadas.')])
add('Biología','Capítulo 1: Célula y biología molecular','Genética celular','CAPÍTULO 1.pdf',5,[('¿Qué es una mutación?','Un cambio permanente y heredable en la información genética.')])
add('Biología','Capítulo 1: Célula y biología molecular','División celular','CAPÍTULO 1.pdf',6,[('¿Cuántas células hijas produce generalmente la mitosis y cuántas la meiosis?','La mitosis produce dos y la meiosis cuatro.')])
add('Biología','Capítulo 1: Célula y biología molecular','Metabolismo','CAPÍTULO 1.pdf',7,[('¿Qué es el metabolismo?','El conjunto de reacciones químicas que ocurren en una célula.'),('¿Qué diferencia hay entre anabolismo y catabolismo?','El anabolismo construye moléculas complejas; el catabolismo degrada moléculas y libera energía.')])
add('Biología','Capítulo 3: Enzimas y metabolismo','Enzimas','CAPÍTULO 3.pdf',1,[('¿Qué hacen las enzimas con la energía de activación?','La disminuyen y así aceleran la reacción.'),('¿Las enzimas cambian el equilibrio químico de una reacción?','No. Aceleran la reacción, pero no modifican su equilibrio químico.')])
add('Matemática','Guía 1: Álgebra','Expresiones algebraicas','1.Guia de lecciones_Matematica_Álgebra1.pdf',1,[('¿Qué es una expresión algebraica?','Una combinación de números y letras unidas por operaciones.'),('¿Qué es el grado absoluto de un término algebraico?','La suma de los exponentes de sus variables.'),('¿Qué es el grado relativo respecto de una variable?','El exponente de esa variable en el término.')])
add('Matemática','Guía 1: Álgebra','Exponentes','1.Guia de lecciones_Matematica_Álgebra1.pdf',1,[('¿Cómo se multiplican potencias de igual base?','Se conserva la base y se suman los exponentes: aⁿ·aᵐ = aⁿ⁺ᵐ.')])
add('Lenguaje y Comunicación','Precisión semántica','Vocabulario preciso','1000123493.jpg','imagen 1',[('¿Qué se busca al sustituir «cosa» por un sustantivo más preciso?','Nombrar exactamente la idea según el contexto y evitar un término demasiado general.')])
add('Lenguaje y Comunicación','Precisión semántica','Verbos precisos','1000123494.jpg','imagen 2',[('¿Qué mejora una sustitución contextual de «hacer», «tener» o «haber»?','La precisión del significado del verbo en la oración.'),('¿Por qué un sinónimo no sirve automáticamente en cualquier oración?','Porque debe conservar el significado y encajar en el contexto.')])
add('Lenguaje y Comunicación','Precisión semántica','Adjetivos','1000123495.jpg','imagen 3',[('¿Qué adjetivo describe a una persona que paga con prontitud lo que debe?','Solvente.')])
assert len(rows)==27,len(rows)
root=Path('MedicinaUNA')
for subject in sorted({r['subject'] for r in rows}):
    batch=[r for r in rows if r['subject']==subject]
    name={'Química':'quimica','Biología':'biologia','Matemática':'matematica','Lenguaje y Comunicación':'lenguaje'}[subject]
    (root/'cards'/f'{name}.json').write_text(json.dumps({'formatVersion':1,'cards':batch},ensure_ascii=False,indent=2)+'\n')
(root/'app/src/main/assets/starter.json').write_text(json.dumps({'formatVersion':1,'cards':rows},ensure_ascii=False,indent=2)+'\n')
print(len(rows),'tarjetas')
