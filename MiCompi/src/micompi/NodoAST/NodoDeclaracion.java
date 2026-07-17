package micompi.NodoAST;

public class NodoDeclaracion extends NodoAST {

    public String nombre;
    public String tipo; // "int", "float64", "string" o "bool"

    /**
     * Constructor para una declaración de variable.
     * @param nombre El nombre del identificador.
     * @param tipo El tipo de dato de la variable.
     * @param linea El número de línea de la declaración.
     */
    public NodoDeclaracion(String nombre, String tipo, int linea) {
        super(linea);
        this.nombre = nombre;
        this.tipo = tipo;
    }
}