package micompi.NodoAST;

import java.util.ArrayList;
import java.util.List;

public abstract class NodoAST {
    public int linea;
    
    public List<NodoAST> hijos = new ArrayList<>();


    public NodoAST(int linea) {
        this.linea = linea;
    }
    
     public void agregarHijo(NodoAST hijo) {
        if (hijo != null) {
            this.hijos.add(hijo);
        }
    }
}
