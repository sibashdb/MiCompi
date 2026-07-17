package micompi.NodoAST;

public class NodoLiteral extends NodoAST {
    public String valor;
    public String tipo; // "int", "float64", "string" o "bool"

    public NodoLiteral(String valor, String tipo, int linea) {
        super(linea);
        this.valor = valor;
        this.tipo = tipo;
    }
}
