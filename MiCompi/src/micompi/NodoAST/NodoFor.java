package micompi.NodoAST;

import java.util.List;

public class NodoFor extends NodoAST {

    public NodoAST condicion;
    public List<NodoAST> cuerpo;

    /**
     * Constructor para la sentencia de bucle 'for'.
     * @param condicion El NodoAST que representa la condición de continuación del bucle.
     * @param cuerpo La lista de sentencias en el cuerpo del bucle.
     * @param linea El número de línea de la sentencia.
     */
    public NodoFor(NodoAST condicion, List<NodoAST> cuerpo, int linea) {
        super(linea);
        this.condicion = condicion;
        this.cuerpo = cuerpo;
    }
}