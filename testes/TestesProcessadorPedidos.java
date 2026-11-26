/**
 * Programa de testes repetível (sem dependências externas) para a classe ProcessadorPedidos.
 *
 * <p>O MESMO arquivo é compilado duas vezes: uma com original/ProcessadorPedidos.java e outra com
 * revisado/ProcessadorPedidos.java (as duas versões têm a mesma API pública). Assim, cada caso de
 * teste tem um único resultado esperado e os resultados observados das duas versões podem ser comparados.</p>
 *
 * <p>Os caminhos citados (C1..C4 e B1..B8) são os caminhos básicos do grafo do código ORIGINAL,
 * descritos no README. O resultado esperado segue o oráculo de teste definido no README (seção 7.1).</p>
 *
 * <p>Uso: java -cp &lt;pasta-compilada&gt; TestesProcessadorPedidos &lt;ROTULO&gt;</p>
 */
public class TestesProcessadorPedidos {

    private static final double TOLERANCIA = 1e-9;

    private static int passou = 0;
    private static int falhou = 0;

    /** Resultado de uma execução: texto observado e se atende ao esperado. */
    private static final class Resultado {
        final String observado;
        final boolean ok;

        Resultado(String observado, boolean ok) {
            this.observado = observado;
            this.ok = ok;
        }
    }

    public static void main(String[] args) {
        String rotulo = args.length > 0 ? args[0] : "SEM ROTULO";
        System.out.println("==============================================================");
        System.out.println("Testes de ProcessadorPedidos - versao: " + rotulo);
        System.out.println("Java: " + System.getProperty("java.version"));
        System.out.println("==============================================================");

        // ---------------- calcularAcrescimo ----------------
        caso("CT01", "calcularAcrescimo", "C1", "(\"PIX\", 200.0, 1)", "objeto novo",
                "retorna 0.0",
                acrescimo(new ProcessadorPedidos(), "PIX", 200.0, 1, 0.0));

        caso("CT02", "calcularAcrescimo", "C2", "(\"CREDITO\", 200.0, 2)", "objeto novo",
                "retorna 16.0 (8%; limite: menor parcelamento > 1)",
                acrescimo(new ProcessadorPedidos(), "CREDITO", 200.0, 2, 16.0));

        caso("CT03", "calcularAcrescimo", "C3", "(\"CREDITO\", 200.0, 1)", "objeto novo",
                "retorna 6.0 (3%; limite: 1 parcela)",
                acrescimo(new ProcessadorPedidos(), "CREDITO", 200.0, 1, 6.0));

        caso("CT04", "calcularAcrescimo", "C4", "(\"BOLETO\", 200.0, 1)", "objeto novo",
                "lanca excecao (forma de pagamento nao suportada)",
                acrescimoComExcecao(new ProcessadorPedidos(), "BOLETO", 200.0, 1));

        caso("CT05", "calcularAcrescimo", "C4", "(\"credito\", 200.0, 3)", "objeto novo",
                "lanca excecao (\"credito\" minusculo nao e forma valida)",
                acrescimoComExcecao(new ProcessadorPedidos(), "credito", 200.0, 3));

        caso("CT06", "calcularAcrescimo", "C3", "(\"CREDITO\", 200.0, 0)", "objeto novo",
                "lanca excecao (parcelas < 1)",
                acrescimoComExcecao(new ProcessadorPedidos(), "CREDITO", 200.0, 0));

        caso("CT07", "calcularAcrescimo", "C3", "(\"CREDITO\", -100.0, 1)", "objeto novo",
                "lanca excecao (subtotal negativo)",
                acrescimoComExcecao(new ProcessadorPedidos(), "CREDITO", -100.0, 1));

        caso("CT08", "calcularAcrescimo", "excecao no no 2", "(null, 200.0, 1)", "objeto novo",
                "lanca excecao (pagamento nulo)",
                acrescimoComExcecao(new ProcessadorPedidos(), null, 200.0, 1));

        // ---------------- processar ----------------
        caso("CT09", "processar", "B1 + C1", "([50.0], \"PIX\", false, 1)", "objeto novo",
                "retorna true; getUltimoTotal() = 50.0",
                processarAprovado(new ProcessadorPedidos(), new double[] {50.0}, "PIX", false, 1, 50.0));

        caso("CT10", "processar", "B2 + C1", "([], \"PIX\", false, 1)", "objeto novo",
                "lanca excecao (pedido sem itens); getUltimoTotal() = 0.0",
                processarComExcecao(new ProcessadorPedidos(), new double[] {}, "PIX", false, 1, 0.0));

        caso("CT11", "processar", "B3 + C1", "([-10.0], \"PIX\", false, 1)", "objeto novo",
                "lanca excecao (preco negativo); getUltimoTotal() = 0.0",
                processarComExcecao(new ProcessadorPedidos(), new double[] {-10.0}, "PIX", false, 1, 0.0));

        caso("CT12", "processar", "B4 + C1", "([100.0], \"PIX\", true, 1)", "objeto novo",
                "retorna true; getUltimoTotal() = 100.0 (limite: 100 nao recebe desconto)",
                processarAprovado(new ProcessadorPedidos(), new double[] {100.0}, "PIX", true, 1, 100.0));

        caso("CT13", "processar", "B5 + C2", "([200.0], \"CREDITO\", true, 2)", "objeto novo",
                "retorna true; getUltimoTotal() = 194.4 (180.0 + 8% de 180.0)",
                processarAprovado(new ProcessadorPedidos(), new double[] {200.0}, "CREDITO", true, 2, 194.4));

        caso("CT14", "processar", "B5 + C1", "([100.01], \"PIX\", true, 1)", "objeto novo",
                "retorna true; getUltimoTotal() = 90.009 (limite: logo acima de 100)",
                processarAprovado(new ProcessadorPedidos(), new double[] {100.01}, "PIX", true, 1, 90.009));

        caso("CT15", "processar", "B6", "([50.0], null, false, 1)", "objeto novo",
                "lanca excecao (pagamento nulo); getUltimoTotal() = 0.0",
                processarComExcecao(new ProcessadorPedidos(), new double[] {50.0}, null, false, 1, 0.0));

        caso("CT16", "processar", "B8", "(null, \"PIX\", false, 1)", "objeto novo",
                "lanca excecao (precos nulo); getUltimoTotal() = 0.0",
                processarComExcecao(new ProcessadorPedidos(), null, "PIX", false, 1, 0.0));

        ProcessadorPedidos comHistorico1 = objetoComPedidoAprovado();
        caso("CT17", "processar", "B8 (apos B1)", "(null, \"PIX\", false, 1)",
                "mesmo objeto ja aprovou ([50.0], \"PIX\", false, 1)",
                "lanca excecao (precos nulo); getUltimoTotal() continua 50.0",
                processarComExcecao(comHistorico1, null, "PIX", false, 1, 50.0));

        ProcessadorPedidos comHistorico2 = objetoComPedidoAprovado();
        caso("CT18", "processar", "B6 (apos B1)", "([80.0], null, false, 1)",
                "mesmo objeto ja aprovou ([50.0], \"PIX\", false, 1)",
                "lanca excecao (pagamento nulo); getUltimoTotal() continua 50.0",
                processarComExcecao(comHistorico2, new double[] {80.0}, null, false, 1, 50.0));

        caso("CT19", "processar", "B1 + B3 (2 iteracoes)", "([10.0, NaN], \"PIX\", false, 1)", "objeto novo",
                "lanca excecao (preco NaN); getUltimoTotal() = 0.0",
                processarComExcecao(new ProcessadorPedidos(), new double[] {10.0, Double.NaN}, "PIX", false, 1, 0.0));

        caso("CT20", "processar", "B1 + C1", "([Infinity], \"PIX\", false, 1)", "objeto novo",
                "lanca excecao (preco infinito); getUltimoTotal() = 0.0",
                processarComExcecao(new ProcessadorPedidos(), new double[] {Double.POSITIVE_INFINITY}, "PIX", false, 1, 0.0));

        caso("CT21", "processar", "B1 (2 iteracoes) + C3", "([0.0, 50.0], \"CREDITO\", false, 1)", "objeto novo",
                "retorna true; getUltimoTotal() = 51.5 (preco 0 e valido; 3%)",
                processarAprovado(new ProcessadorPedidos(), new double[] {0.0, 50.0}, "CREDITO", false, 1, 51.5));

        caso("CT22", "processar", "B1 + C3", "([50.0], \"CREDITO\", false, 0)", "objeto novo",
                "lanca excecao (parcelas < 1); getUltimoTotal() = 0.0",
                processarComExcecao(new ProcessadorPedidos(), new double[] {50.0}, "CREDITO", false, 0, 0.0));

        caso("CT23", "processar", "B1 (2 iteracoes) + C1", "([Double.MAX_VALUE, Double.MAX_VALUE], \"PIX\", false, 1)",
                "objeto novo",
                "lanca excecao (soma dos precos estoura para infinito); getUltimoTotal() = 0.0",
                processarComExcecao(new ProcessadorPedidos(), new double[] {Double.MAX_VALUE, Double.MAX_VALUE},
                        "PIX", false, 1, 0.0));

        caso("CT24", "calcularAcrescimo", "C3", "(\"CREDITO\", Infinity, 1)", "objeto novo",
                "lanca excecao (subtotal nao finito)",
                acrescimoComExcecao(new ProcessadorPedidos(), "CREDITO", Double.POSITIVE_INFINITY, 1));

        System.out.println("==============================================================");
        System.out.println("Resumo (" + rotulo + "): " + (passou + falhou) + " casos | "
                + passou + " PASSOU | " + falhou + " FALHOU");
        System.out.println("==============================================================");
        System.exit(falhou == 0 ? 0 : 1);
    }

    // ------------------------------------------------------------------
    // Execução e verificação
    // ------------------------------------------------------------------

    private static ProcessadorPedidos objetoComPedidoAprovado() {
        ProcessadorPedidos p = new ProcessadorPedidos();
        p.processar(new double[] {50.0}, "PIX", false, 1);
        return p;
    }

    private static Resultado acrescimo(ProcessadorPedidos p, String pagamento, double subtotal,
                                       int parcelas, double esperado) {
        try {
            double obtido = p.calcularAcrescimo(pagamento, subtotal, parcelas);
            return new Resultado("retornou " + obtido, iguais(obtido, esperado));
        } catch (RuntimeException e) {
            return new Resultado("lancou " + descrever(e), false);
        }
    }

    private static Resultado acrescimoComExcecao(ProcessadorPedidos p, String pagamento, double subtotal,
                                                 int parcelas) {
        try {
            double obtido = p.calcularAcrescimo(pagamento, subtotal, parcelas);
            return new Resultado("retornou " + obtido + " (nenhuma excecao)", false);
        } catch (RuntimeException e) {
            return new Resultado("lancou " + descrever(e), true);
        }
    }

    private static Resultado processarAprovado(ProcessadorPedidos p, double[] precos, String pagamento,
                                               boolean vip, int parcelas, double totalEsperado) {
        try {
            boolean retorno = p.processar(precos, pagamento, vip, parcelas);
            double total = p.getUltimoTotal();
            return new Resultado("retornou " + retorno + "; getUltimoTotal() = " + total,
                    retorno && iguais(total, totalEsperado));
        } catch (RuntimeException e) {
            return new Resultado("lancou " + descrever(e) + "; getUltimoTotal() = " + p.getUltimoTotal(), false);
        }
    }

    private static Resultado processarComExcecao(ProcessadorPedidos p, double[] precos, String pagamento,
                                                 boolean vip, int parcelas, double totalPreservado) {
        try {
            boolean retorno = p.processar(precos, pagamento, vip, parcelas);
            return new Resultado("retornou " + retorno + " (nenhuma excecao); getUltimoTotal() = "
                    + p.getUltimoTotal(), false);
        } catch (RuntimeException e) {
            double total = p.getUltimoTotal();
            return new Resultado("lancou " + descrever(e) + "; getUltimoTotal() = " + total,
                    iguais(total, totalPreservado));
        }
    }

    private static boolean iguais(double a, double b) {
        return Math.abs(a - b) <= TOLERANCIA;
    }

    private static String descrever(RuntimeException e) {
        return e.getClass().getSimpleName() + (e.getMessage() != null ? " (\"" + e.getMessage() + "\")" : "");
    }

    private static void caso(String id, String metodo, String caminho, String entrada, String preCondicao,
                             String esperado, Resultado r) {
        if (r.ok) {
            passou++;
        } else {
            falhou++;
        }
        System.out.println();
        System.out.println("[" + id + "] " + metodo + " | caminho: " + caminho);
        System.out.println("  Entrada.......: " + entrada);
        System.out.println("  Pre-condicao..: " + preCondicao);
        System.out.println("  Esperado......: " + esperado);
        System.out.println("  Observado.....: " + r.observado);
        System.out.println("  Situacao......: " + (r.ok ? "PASSOU" : "FALHOU"));
    }
}
