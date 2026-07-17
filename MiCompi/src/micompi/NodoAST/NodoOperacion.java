package micompi.NodoAST;

public class NodoOperacion extends NodoAST {
    public String operador;
    public NodoAST izquierdo;
    public NodoAST derecho;

    public NodoOperacion(String operador, NodoAST izquierdo, NodoAST derecho, int linea) {
        super(linea);
        this.operador = operador;
        this.izquierdo = izquierdo;
        this.derecho = derecho;
        agregarHijo(izquierdo);
        agregarHijo(derecho);
    }
}
