package micompi;

public class Cuadruplo {
    public String operador;
    public String arg1;
    public String arg2;
    public String resultado;

    public Cuadruplo(String operador, String arg1, String arg2, String resultado) {
        this.operador = operador;
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.resultado = resultado;
    }

    @Override
    public String toString() {
        // Formato para imprimir la tabla de cuádruplos de manera ordenada
        return String.format("(%s, %s, %s, %s)",
                operador,
                arg1 != null ? arg1 : " ",
                arg2 != null ? arg2 : " ",
                resultado);
    }
}