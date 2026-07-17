package micompi.NodoAST;

public class NodoIdentificador extends NodoAST {

    public String nombre;

    public NodoIdentificador(String nombre, int linea) {
        super(linea);
        this.nombre = nombre;
    }
}
