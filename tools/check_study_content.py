#!/usr/bin/env python3
"""Controles reproducibles sin red. No certifican la exactitud académica del banco."""
import hashlib
import json
import math
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "app/src/main/assets"


def load(name):
    return json.loads((ASSETS / f"{name}.json").read_text(encoding="utf-8"))


def digest(ids):
    return hashlib.sha256("\n".join(sorted(ids)).encode()).hexdigest()


cards = sum((load(name)["cards"] for name in ("starter", "tarjetas", "programa_2027")), [])
questions = load("preguntas")["questions"]
assert len(cards) == 1449 and len(questions) == 311
assert digest(c["id"] for c in cards) == "d3387e38beb6b8104bac764fa4b8844f81bd5a7bc30a0f72ca6eb0a21e0483c2"
assert digest(q["id"] for q in questions if not q["id"].startswith("practica041-")) == "2430e6aac3a9d9c7018563699f139a2f06edd17a50fb10fbb54b97676ee88104"
for bank in (cards, questions):
    assert len({x["id"] for x in bank}) == len(bank), "IDs duplicados"
    for x in bank:
        for key in ("id", "question", "answer", "subject", "topic", "explanation", "source"):
            assert isinstance(x[key], str) and x[key].strip(), (x["id"], key)
        assert "Found no tool response" not in x["source"], x["id"]
        assert x["difficulty"] in (1, 2, 3), x["id"]
for q in questions:
    assert len(q["options"]) >= 2 and len(set(q["options"])) == len(q["options"]), q["id"]
    assert 0 <= q["correctIndex"] < len(q["options"]), q["id"]
    assert q["options"][q["correctIndex"]] == q["answer"], q["id"]

coverage = json.loads((ROOT / "cobertura_programa_2027.json").read_text())
units = {u["id"]: u for area in coverage["areas"].values() for u in area["unidades"]}
assert len(units) == 57
assert sum(len(unit["conceptos_cotejados"]) for unit in units.values()) == 621
card_ids = {c["id"] for c in cards}
for unit in units.values():
    for ids in unit["referencias"].values():
        assert set(ids) <= card_ids
    for item in unit["conceptos_cotejados"]:
        assert set(item["tarjetas"]) <= card_ids
new = {q["id"]: q for q in questions if q["id"].startswith("practica041-")}
assert Counter(q["subject"] for q in new.values()) == dict.fromkeys(
    ("Biología", "Física", "Lenguaje", "Matemática", "Química"), 10)
for q in new.values():
    assert sum(" ".join(x["question"].casefold().split()) == " ".join(q["question"].casefold().split())
               for x in questions) == 1, q["id"]
mapped = [q for q in questions if q.get("programUnit")]
assert len(mapped) == 105
for q in mapped:
    assert q["programTopic"] in units[q["programUnit"]]["referencias"], q["id"]

# Recalcular ejercicios del lote: los parámetros se cotejan también con el enunciado.
# No sustituye la lectura de distractores ni la evaluación de las hipótesis físicas.
checked = []


def check(suffix, fragments, expected):
    q = new["practica041-" + suffix]
    assert all(f in q["question"] for f in fragments), (suffix, "cambiaron los datos")
    assert q["answer"] == expected, (suffix, q["answer"], expected)
    checked.append(q["id"])


def number(value):
    return f"{value:g}".replace(".", ",")


check("f03-001", ["4 kg", "18 N", "6 N"], f"{number((18-6)/4)} m/s² a la derecha")
check("f03-002", ["8 N", "20 N", "reposo"], f"{min(8,20)} N, opuesta al empuje")
check("f04-001", ["5 cm²", "100 cm²", "25 N"], f"{number(25*100/5)} N")
check("f04-002", ["750 kg/m³", "1000 kg/m³"], f"{number(100*750/1000)} %")
check("f05-001", ["8 cm²", "2 cm²", "1,5 m/s"], f"{number(8*1.5/2)} m/s")
check("f06-001", ["20 °C", "35 °C"], f"{35-20} K")
check("f07-001", ["200 g", "20 °C", "30 °C", "4,18"], f"{number(200*4.18*(30-20)/1000)} kJ")
check("f08-001", ["40 oscilaciones", "10 s"], f"{number(10/40)} s")
check("f09-001", ["+25 cm"], f"+{number(1/(25/100))} D")
check("f10-001", ["6 Ω", "12 V"], f"{number(12**2/6)} W")
truth_cases = [(p, q) for p in (False, True) for q in (False, True) if (not p or q) and not q]
assert truth_cases == [(False, False)]
check("m01-001", ["p → q", "q es falso"], "p es falso")
amount = round(500000 * (1 + 10/100)**2)
check("m02-001", ["500 000", "10 %", "dos años"], "G. " + f"{amount:,}".replace(",", " "))
check("m03-001", ["0,035 m²"], f"{number(0.035*100**2)} cm²")
a, b = 2, -3
check("m04-001", ["(2x − 3)²"], f"{a*a}x² − {abs(2*a*b)}x + {b*b}")
assert all(math.isclose((x*x-9)/(x-3), x+3) for x in (-7, -1, 0, 2, 4, 9))
check("m05-001", ["(x² − 9)/(x − 3)"], "f(x) = x + 3, con x ≠ 3")
roots = [(1+math.sqrt(9))/2, (1-math.sqrt(9))/2]
valid = [x for x in roots if x >= 0 and math.isclose(math.sqrt(x+2), x)]
check("m06-001", ["√(x + 2) = x"], "{" + number(valid[0]) + "}")
a, b, c = 2, -5, -3
roots = [(-b+s*math.sqrt(b*b-4*a*c))/(2*a) for s in (1, -1)]
assert roots == [3, -0.5] and all(a*x*x+b*x+c == 0 for x in roots)
check("m07-001", ["2x² − 5x − 3 = 0"], "3 y −1/2")
roots = [x for x in (math.sqrt(8+1), -math.sqrt(8+1)) if x > 1]
assert math.isclose(math.log10(roots[0]-1)+math.log10(roots[0]+1), math.log10(8))
check("m08-001", ["log₁₀(x − 1)", "log₁₀(x + 1)", "log₁₀(8)"], "x = " + number(roots[0]))
side = math.sqrt(5**2+7**2-2*5*7*math.cos(math.radians(60)))
check("m09-001", ["5 cm", "7 cm", "60°"], f"{side:.2f}".replace(".", ",") + " cm")
check("m10-001", ["tres estudiantes", "seis", "sin cargos"], str(math.comb(6, 3)))
check("b05-003", ["3 Na⁺", "2 K⁺"], "Una carga positiva hacia el exterior")
assert 3 - 2 == 1
check("b11-001", ["3′-TACG-5′"], "5′-" + "TACG".translate(str.maketrans("TACG", "AUGC")) + "-3′")
check("b14-001", ["2n = 6"], f"{6} cromosomas y {2*6} cromátidas")

print(f"OK: {len(cards)} tarjetas; {len(questions)} preguntas; IDs previos conservados; "
      f"57 unidades; 50 preguntas nuevas; {len(checked)} recálculos y contrastes.")
print("Tarjetas:", dict(sorted(Counter(x['subject'].replace('Lenguaje y Comunicación', 'Lenguaje') for x in cards).items())))
print("Preguntas:", dict(sorted(Counter(x['subject'] for x in questions).items())))
