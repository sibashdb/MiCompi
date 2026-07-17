package micompi.NodoAST;

public class NodoScanln extends NodoAST {

    public String identificador;

    /**
     * Constructor para la instrucción de lectura (Scanln).
     * @param identificador El nombre de la variable a la que se le asignará el valor.
     * @param linea El número de línea de la instrucción.
     */
    public NodoScanln(String identificador, int linea) {
        super(linea);
        this.identificador = identificador;
    }
}