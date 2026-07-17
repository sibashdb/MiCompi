package micompi.NodoAST;

public class NodoOperacionUnaria extends NodoAST {
    public String operador;
    public NodoAST operando;
    public NodoOperacionUnaria(String operador, NodoAST operando, int linea) {
        super(linea);
        this.operador = operador;
        this.operando = operando;
        agregarHijo(operando);
    }
}
