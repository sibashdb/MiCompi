package micompi.NodoAST;

import java.util.List;

public class NodoIfElse extends NodoAST {

    public NodoAST condicion;
    public List<NodoAST> bloqueIf;
    public List<NodoAST> bloqueElse; // null si no hay 'else'

    /**
     * Constructor para la sentencia 'if-else'.
     * @param condicion El NodoAST que representa la condición lógica.
     * @param bloqueIf La lista de sentencias en el bloque 'if'.
     * @param bloqueElse La lista de sentencias en el bloque 'else' (puede ser null).
     * @param linea El número de línea de la sentencia.
     */
    public NodoIfElse(NodoAST condicion, List<NodoAST> bloqueIf, List<NodoAST> bloqueElse, int linea) {
        super(linea);
        this.condicion = condicion;
        this.bloqueIf = bloqueIf;
        this.bloqueElse = bloqueElse;
    }
}
