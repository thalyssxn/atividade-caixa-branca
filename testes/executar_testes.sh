#!/usr/bin/env bash
# Compila e executa o MESMO programa de testes contra a versão original e a versão revisada.
# Uso (a partir da raiz do repositório):  bash testes/executar_testes.sh
# As saídas são gravadas em evidencias/.

cd "$(dirname "$0")/.." || exit 1

rm -rf build
mkdir -p build/original build/revisado evidencias

echo "== Compilando versao ORIGINAL =="
javac -encoding UTF-8 -d build/original original/ProcessadorPedidos.java testes/TestesProcessadorPedidos.java || exit 1

echo "== Compilando versao REVISADA =="
javac -encoding UTF-8 -d build/revisado revisado/ProcessadorPedidos.java testes/TestesProcessadorPedidos.java || exit 1

# Os testes da versão original terminam com código 1 porque demonstram falhas do legado;
# por isso o script não interrompe a execução nesse ponto.
java -Dstdout.encoding=UTF-8 -cp build/original TestesProcessadorPedidos ORIGINAL | tee evidencias/saida_testes_original.txt
echo "Codigo de saida (original): ${PIPESTATUS[0]}"

java -Dstdout.encoding=UTF-8 -cp build/revisado TestesProcessadorPedidos REVISADO | tee evidencias/saida_testes_revisado.txt
echo "Codigo de saida (revisado): ${PIPESTATUS[0]}"
