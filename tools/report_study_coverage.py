#!/usr/bin/env python3
"""Inventario de profundidad: correspondencias explícitas, no certificación académica."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
assets = ROOT / "app/src/main/assets"
coverage = json.loads((ROOT / "cobertura_programa_2027.json").read_text())
questions = json.loads((assets / "preguntas.json").read_text())["questions"]
cards = {c["id"]: c for name in ("starter", "tarjetas", "programa_2027")
         for c in json.loads((assets / f"{name}.json").read_text())["cards"]}
units = []
groups = []
for area, data in coverage["areas"].items():
    for unit in data["unidades"]:
        unit_questions = [q for q in questions if q.get("programUnit") == unit["id"]]
        unit_cards = sorted({c for ids in unit["referencias"].values() for c in ids})
        units.append({"id": unit["id"], "materia": area, "titulo": unit["titulo"],
                      "tarjetas_mapeadas": len(unit_cards),
                      "preguntas_con_vinculo_explicito": [q["id"] for q in unit_questions]})
        for index, group in enumerate(unit["conceptos_cotejados"]):
            refs = [group["referencia"]] if "referencia" in group else [
                ref for ref, ids in unit["referencias"].items() if set(ids) & set(group["tarjetas"])]
            linked = [q["id"] for q in unit_questions if q.get("programTopic") in refs]
            groups.append({"unidad": unit["id"], "grupo": index + 1,
                           "referencia": group.get("referencia"), "concepto": group.get("concepto"),
                           "tarjetas": group["tarjetas"], "preguntas_relacionadas": linked,
                           "evaluacion_de_profundidad": "pendiente de cotejo individual"})
assert len(units) == 57 and len(groups) == 621
assert all(c in cards for g in groups for c in g["tarjetas"])
result = {
    "version": "0.4.1",
    "criterio": "Se contabilizan vínculos explícitos programUnit/programTopic; los grupos de Biología se relacionan a través de sus tarjetas. Una coincidencia temática no prueba profundidad ni respaldo de una fuente. No se propaga automáticamente una pregunta de un apartado padre a todos sus subapartados.",
    "limitacion": "Las 206 preguntas heredadas carecen de estos metadatos: aparecen aparte, no se consideran ausentes del banco ni se imputan automáticamente a apartados. Las cifras no equivalen al número total de ejercicios disponibles de cada tema.",
    "preguntas_sin_vinculo_explicito": [q["id"] for q in questions if not q.get("programUnit")],
    "resumen": {"unidades": len(units), "grupos": len(groups),
                "grupos_con_preguntas_relacionadas": sum(bool(g["preguntas_relacionadas"]) for g in groups),
                "grupos_sin_preguntas_relacionadas": sum(not g["preguntas_relacionadas"] for g in groups)},
    "unidades": units, "grupos": groups,
}
(ROOT / "profundidad_041.json").write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n")
print(result["resumen"])
print("Preguntas sin vínculo explícito:", len(result["preguntas_sin_vinculo_explicito"]))
