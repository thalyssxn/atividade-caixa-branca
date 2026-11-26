
public class ProcessadorPedidos {
    private double ultimoTotal = 0;
    private boolean aprovado = false;

    public double calcularAcrescimo(String pagamento, double subtotal, int parcelas) {
        double acrescimo = 0;
        switch (pagamento) {
            case "PIX":
                acrescimo = 0;
                break;
            case "CREDITO":
                if (parcelas > 1) {
                    acrescimo = subtotal * 0.08;
                } else {
                    acrescimo = subtotal * 0.03;
                }
                break;
            default:
                acrescimo = 0;
        }
        return acrescimo;
    }

    public boolean processar(double[] precos, String pagamento, boolean vip, int parcelas) {
        double subtotal = 0;
        try {
            for (int i = 0; i < precos.length; i++) {
                if (precos[i] >= 0) {
                    subtotal += precos[i];
                }
            }
            if (vip && subtotal > 100) {
                subtotal = subtotal * 0.90;
            }
            double total = subtotal + calcularAcrescimo(pagamento, subtotal, parcelas);
            if (total >= 0) {
                ultimoTotal = total;
                aprovado = true;
            }
        } catch (RuntimeException e) {
            // Exceção ignorada; o estado anterior é preservado.
        }
        return aprovado;
    }

    public double getUltimoTotal() {
        return ultimoTotal;
    }
}
