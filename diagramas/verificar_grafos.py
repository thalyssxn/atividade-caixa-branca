"""Confere, a partir dos arquivos .dot, as contagens usadas no README.

Para cada grafo: N, E, V(G) = E - N + 2P, soma das decisões (grau de saída - 1),
planaridade (regiões = E - N + 2 quando planar e conexo), validade de cada caminho
básico (sequência de arestas existentes, da entrada à saída) e independência linear
(posto da matriz caminho x aresta igual a V(G)).

Uso:  python3 diagramas/verificar_grafos.py   (a partir da raiz do repositório)
"""
import os
import re

import networkx as nx
import numpy as np

PASTA = os.path.dirname(os.path.abspath(__file__))

CAMINHOS = {
    "grafo_calcularAcrescimo.dot": {
        "C1": [1, 2, 3, 4, 10, 11],
        "C2": [1, 2, 3, 5, 6, 8, 10, 11],
        "C3": [1, 2, 3, 5, 7, 8, 10, 11],
        "C4": [1, 2, 3, 9, 10, 11],
    },
    "grafo_processar.dot": {
        "B1": [1, 2, 3, 4, 5, 6, 7, 4, 8, 11, 12, 13, 15, 16],
        "B2": [1, 2, 3, 4, 8, 11, 12, 13, 15, 16],
        "B3": [1, 2, 3, 4, 5, 7, 4, 8, 11, 12, 13, 15, 16],
        "B4": [1, 2, 3, 4, 5, 6, 7, 4, 8, 9, 11, 12, 13, 15, 16],
        "B5": [1, 2, 3, 4, 5, 6, 7, 4, 8, 9, 10, 11, 12, 13, 15, 16],
        "B6": [1, 2, 3, 4, 5, 6, 7, 4, 8, 11, 14, 15, 16],
        "B7": [1, 2, 3, 4, 5, 6, 7, 4, 8, 11, 12, 15, 16],
        "B8": [1, 2, 3, 4, 14, 15, 16],
    },
}


def ler_dot(caminho_arquivo):
    texto = open(caminho_arquivo, encoding="utf-8").read()
    nos = sorted({int(n) for n in re.findall(r"^\s*n(\d+)\s*\[", texto, re.M)})
    arestas = []
    for origem, destino, rotulo in re.findall(r"n(\d+)\s*->\s*n(\d+)\s*\[label=\"(e\d+)", texto):
        arestas.append((rotulo, int(origem), int(destino)))
    return nos, arestas


def main():
    for arquivo, caminhos in CAMINHOS.items():
        nos, arestas = ler_dot(os.path.join(PASTA, arquivo))
        n, e, p = len(nos), len(arestas), 1
        g = nx.MultiDiGraph()
        g.add_nodes_from(nos)
        for rotulo, a, b in arestas:
            g.add_edge(a, b, key=rotulo)

        conexo = nx.is_weakly_connected(g)
        planar, _ = nx.check_planarity(nx.Graph(g.to_undirected()))
        decisoes = {no: g.out_degree(no) - 1 for no in nos if g.out_degree(no) > 1}

        print("=" * 70)
        print(arquivo)
        print(f"N = {n}   E = {e}   P = {p} (conexo: {conexo})")
        print(f"V(G) = E - N + 2P = {e} - {n} + 2 = {e - n + 2 * p}")
        print("Nos com mais de uma saida (grau de saida - 1):", decisoes)
        print(f"Soma das decisoes + 1 = {sum(decisoes.values()) + 1}")
        print(f"Planar: {planar}  ->  regioes = E - N + 2 = {e - n + 2}")

        indice = {(a, b): i for i, (_, a, b) in enumerate(arestas)}
        nomes = [r for r, _, _ in arestas]
        matriz = []
        for nome, seq in caminhos.items():
            vetor = [0] * e
            for a, b in zip(seq, seq[1:]):
                if (a, b) not in indice:
                    raise SystemExit(f"ERRO: aresta {a}->{b} do caminho {nome} nao existe no grafo")
                vetor[indice[(a, b)]] += 1
            usadas = [nomes[i] for i, v in enumerate(vetor) if v]
            valido = seq[0] == nos[0] and seq[-1] == nos[-1]
            print(f"  {nome}: {'-'.join(map(str, seq))}  | arestas: {', '.join(usadas)}  | entrada->saida: {valido}")
            matriz.append(vetor)
        posto = np.linalg.matrix_rank(np.array(matriz))
        print(f"Caminhos: {len(caminhos)}   posto da matriz caminho x aresta: {posto}  "
              f"-> linearmente independentes: {posto == len(caminhos) == e - n + 2}")


if __name__ == "__main__":
    main()
