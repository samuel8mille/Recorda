#!/bin/bash
set -euo pipefail

REPORT="app/build/reports/jacoco/jacocoUnitTestReport/report.xml"
THRESHOLD=90

if [ ! -f "$REPORT" ]; then
    echo "Relatório de cobertura não encontrado: $REPORT"
    exit 1
fi

python3 - "$REPORT" "$THRESHOLD" <<'EOF'
import re
import sys

report_path = sys.argv[1]
threshold = int(sys.argv[2])

with open(report_path) as f:
    content = f.read()

# O último <counter type="INSTRUCTION"> no arquivo pertence ao elemento
# raiz <report>, agregando métricas de todas as classes incluídas.
matches = re.findall(r'<counter type="INSTRUCTION" missed="(\d+)" covered="(\d+)"/>', content)
if not matches:
    print("Não foi possível ler contadores do relatório de cobertura.")
    sys.exit(1)

missed, covered = int(matches[-1][0]), int(matches[-1][1])
total = missed + covered

if total == 0:
    print("Nenhuma instrução encontrada no relatório.")
    sys.exit(1)

coverage = covered / total * 100
print(f"Cobertura de instruções: {coverage:.2f}% (mínimo exigido: {threshold}%)")

if coverage >= threshold:
    sys.exit(0)
else:
    print(f"Cobertura abaixo do limite. Adicione testes para atingir {threshold}%.")
    sys.exit(1)
EOF
