package micompi.NodoAST;

public class NodoAsignacion extends NodoAST {
    public String identificador;
    public NodoAST expresion;

    public NodoAsignacion(String identificador, NodoAST expresion, int linea) {
        super(linea);
        this.identificador = identificador;
        this.expresion = expresion;
        agregarHijo(expresion);
    }
}