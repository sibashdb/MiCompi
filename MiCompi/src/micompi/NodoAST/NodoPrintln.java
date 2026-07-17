package micompi.NodoAST;

import java.util.List;

public class NodoPrintln extends NodoAST {

    // Ej: en `Println("hola", x + 1)`, contiene un NodoLiteral y un NodoOperacion.
    public List<NodoAST> expresiones;

    /**
     * Constructor para la instrucción de escritura.
     * @param expresiones Una lista de NodoAST que son los argumentos de la función.
     * @param linea El número de línea de la instrucción.
     */
    public NodoPrintln(List<NodoAST> expresiones, int linea) {
        super(linea);
        this.expresiones = expresiones;
    }
}
