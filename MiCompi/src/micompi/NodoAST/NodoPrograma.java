package micompi.NodoAST;

import java.util.List;

public class NodoPrograma extends NodoAST {

    public List<NodoAST> sentencias;

    /**
     * Constructor para el nodo raíz del programa.
     * @param sentencias Una lista de NodoAST que representan las sentencias del programa.
     * @param linea El número de línea en el código fuente (generalmente 1).
     */
    public NodoPrograma(List<NodoAST> sentencias, int linea) {
        super(linea);
        this.sentencias = sentencias;
    }
}
