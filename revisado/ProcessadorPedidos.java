/**
 * Versão REVISADA do processador de pedidos (cenário 3).
 *
 * <p>Regras de negócio mantidas do código original:</p>
 * <ul>
 *   <li>PIX: sem acréscimo.</li>
 *   <li>CREDITO: acréscimo de 8% quando parcelas &gt; 1 e de 3% quando parcelas == 1.</li>
 *   <li>Cliente VIP com subtotal estritamente maior que 100: desconto de 10%,
 *       aplicado ANTES do cálculo do acréscimo (o acréscimo incide sobre o subtotal já com desconto).</li>
 * </ul>
 *
 * <p>Correções em relação ao original (os códigos EA-xx referem-se à análise estática
 * registrada na planilha PLANO_DE_TESTE.xlsx e no README):</p>
 * <ul>
 *   <li>EA-01: não existe mais captura vazia de exceção; entradas inválidas geram
 *       {@link IllegalArgumentException} com mensagem explicativa, que chega ao chamador.</li>
 *   <li>EA-02: o atributo {@code aprovado} foi removido; o resultado depende apenas da chamada atual
 *       e nunca reaproveita a aprovação de um pedido anterior.</li>
 *   <li>EA-03, EA-04, EA-10: preços nulos, vazios, negativos, NaN ou infinitos são rejeitados.</li>
 *   <li>EA-05, EA-06: forma de pagamento nula ou não suportada é rejeitada (antes caía no default com acréscimo 0).</li>
 *   <li>EA-07, EA-08: parcelas &lt; 1 no crédito e subtotal negativo/não finito são rejeitados.</li>
 *   <li>EA-09: removida a condição {@code total >= 0}, que era sempre verdadeira (ramo falso inviável).</li>
 *   <li>EA-11, EA-12, EA-14: removidas atribuições redundantes; números mágicos e formas de pagamento viraram constantes.</li>
 *   <li>EA-13, EA-16: métodos documentados; a soma/validação dos itens foi extraída para {@link #somarPrecos(double[])}.</li>
 * </ul>
 *
 * <p>Observação: os valores continuam em {@code double} para manter a mesma API pública do original
 * (ver EA-15 e a seção de limitações do README).</p>
 */
public class ProcessadorPedidos {

    /** Forma de pagamento PIX (sem acréscimo). */
    public static final String PAGAMENTO_PIX = "PIX";

    /** Forma de pagamento cartão de crédito (acréscimo conforme parcelas). */
    public static final String PAGAMENTO_CREDITO = "CREDITO";

    /** Acréscimo do crédito parcelado (parcelas &gt; 1): 8%. */
    private static final double TAXA_CREDITO_PARCELADO = 0.08;

    /** Acréscimo do crédito à vista (parcelas == 1): 3%. */
    private static final double TAXA_CREDITO_A_VISTA = 0.03;

    /** Fator aplicado ao subtotal do cliente VIP (desconto de 10%). */
    private static final double FATOR_DESCONTO_VIP = 0.90;

    /** O desconto VIP só vale para subtotal estritamente maior que este valor. */
    private static final double LIMITE_DESCONTO_VIP = 100.0;

    /** Total do último pedido aprovado; só é alterado quando o pedido inteiro é válido. */
    private double ultimoTotal = 0;

    /**
     * Calcula o acréscimo devido pela forma de pagamento.
     *
     * @param pagamento forma de pagamento: {@value #PAGAMENTO_PIX} ou {@value #PAGAMENTO_CREDITO}
     * @param subtotal  valor sobre o qual o acréscimo incide; deve ser finito e maior ou igual a zero
     * @param parcelas  quantidade de parcelas; obrigatória (&gt;= 1) somente para {@value #PAGAMENTO_CREDITO}
     * @return valor do acréscimo (0 para PIX)
     * @throws IllegalArgumentException se o subtotal for inválido, se a forma de pagamento for nula
     *                                  ou não suportada, ou se as parcelas do crédito forem menores que 1
     */
    public double calcularAcrescimo(String pagamento, double subtotal, int parcelas) {
        // EA-08: subtotal negativo ou não finito produzia acréscimo sem sentido (ex.: -3.0).
        if (!Double.isFinite(subtotal) || subtotal < 0) {
            throw new IllegalArgumentException("Subtotal inválido: " + subtotal);
        }
        // EA-05: switch sobre String nula lançava NullPointerException sem mensagem.
        if (pagamento == null) {
            throw new IllegalArgumentException("Forma de pagamento não informada.");
        }
        switch (pagamento) {
            case PAGAMENTO_PIX:
                // EA-11: retorno direto, sem atribuição redundante.
                return 0.0;
            case PAGAMENTO_CREDITO:
                // EA-07: 0 ou parcelas negativas eram aceitas como crédito à vista.
                if (parcelas < 1) {
                    throw new IllegalArgumentException("Quantidade de parcelas inválida: " + parcelas);
                }
                if (parcelas > 1) {
                    return subtotal * TAXA_CREDITO_PARCELADO;
                }
                return subtotal * TAXA_CREDITO_A_VISTA;
            default:
                // EA-06: forma desconhecida (inclusive "credito" minúsculo) não pode virar acréscimo 0 em silêncio.
                throw new IllegalArgumentException("Forma de pagamento não suportada: " + pagamento);
        }
    }

    /**
     * Processa um pedido: soma os preços, aplica o desconto VIP e o acréscimo da forma de pagamento
     * e registra o total em {@link #getUltimoTotal()}.
     *
     * <p>O retorno {@code boolean} foi mantido por compatibilidade com a assinatura original:
     * {@code true} indica pedido aprovado. Um pedido inválido NÃO retorna {@code false} silenciosamente:
     * ele lança {@link IllegalArgumentException} com o motivo, e o último total aprovado permanece inalterado.</p>
     *
     * @param precos    preços dos itens; ao menos um item, cada preço finito e maior ou igual a zero
     * @param pagamento forma de pagamento (ver {@link #calcularAcrescimo(String, double, int)})
     * @param vip       indica cliente VIP
     * @param parcelas  quantidade de parcelas (validada para crédito)
     * @return {@code true} quando o pedido é aprovado
     * @throws IllegalArgumentException se qualquer dado do pedido for inválido
     */
    public boolean processar(double[] precos, String pagamento, boolean vip, int parcelas) {
        // EA-03/EA-04/EA-10/EA-16: validação e soma dos itens em método próprio.
        double subtotal = somarPrecos(precos);
        if (vip && subtotal > LIMITE_DESCONTO_VIP) {
            subtotal = subtotal * FATOR_DESCONTO_VIP;
        }
        // Interação entre os métodos: o acréscimo incide sobre o subtotal já com desconto (regra original mantida).
        // Exceções de calcularAcrescimo propagam ao chamador (EA-01): nada é capturado e descartado.
        double total = subtotal + calcularAcrescimo(pagamento, subtotal, parcelas);
        // EA-09: a antiga condição "total >= 0" era sempre verdadeira e foi removida.
        // EA-02: o estado só é atualizado depois que todas as validações passaram.
        ultimoTotal = total;
        return true;
    }

    /**
     * Valida os preços do pedido e devolve a soma.
     *
     * @param precos preços dos itens
     * @return soma dos preços
     * @throws IllegalArgumentException se o vetor for nulo ou vazio, se algum preço for negativo, NaN
     *                                  ou infinito, ou se a soma não for representável (estouro)
     */
    private double somarPrecos(double[] precos) {
        if (precos == null || precos.length == 0) {
            throw new IllegalArgumentException("O pedido deve conter ao menos um item.");
        }
        double soma = 0;
        for (int i = 0; i < precos.length; i++) {
            double preco = precos[i];
            // EA-03/EA-10: antes, negativos e NaN eram ignorados e infinitos eram aceitos.
            if (!Double.isFinite(preco) || preco < 0) {
                throw new IllegalArgumentException("Preço inválido na posição " + i + ": " + preco);
            }
            soma += preco;
        }
        // EA-10: soma de valores finitos muito grandes pode estourar para infinito.
        if (!Double.isFinite(soma)) {
            throw new IllegalArgumentException("A soma dos preços excede o limite representável.");
        }
        return soma;
    }

    /**
     * @return total do último pedido aprovado (0 se nenhum pedido foi aprovado nesta instância)
     */
    public double getUltimoTotal() {
        return ultimoTotal;
    }
}
