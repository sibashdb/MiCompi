package micompi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import micompi.NodoAST.*;

public class GeneradorRPN {

    
    private final List<String> codigoRPN;
    private int contadorEtiquetas;
    private final Map<String, String> tablaSimbolos;

    public GeneradorRPN(Map<String, String> tablaSimbolos) {
        this.codigoRPN = new ArrayList<>();
        this.contadorEtiquetas = 0;
        this.tablaSimbolos = tablaSimbolos;
    }

    
    public List<String> getCodigoRPN() {
        return codigoRPN;
    }
    
    public void generar(NodoAST raiz) {
        System.out.println("--- INICIANDO GENERACIÓN DE CÓDIGO RPN ---");
        visit(raiz);
        System.out.println("Generación de RPN completada.");
    }
    
    // --- Métodos de ayuda ---
    private String nuevaEtiqueta() { return "L" + contadorEtiquetas++; }

    

    private void visit(NodoAST nodo) {
        if (nodo == null) return;

        if (nodo instanceof NodoPrograma) {
            for(NodoAST sentencia : ((NodoPrograma)nodo).sentencias) visit(sentencia);
        }
        else if (nodo instanceof NodoAsignacion) visitAsignacion((NodoAsignacion)nodo);
        else if (nodo instanceof NodoIfElse) visitIfElse((NodoIfElse)nodo);
        else if (nodo instanceof NodoFor) visitFor((NodoFor)nodo);
        else if (nodo instanceof NodoPrintln) visitPrintln((NodoPrintln)nodo);
    }

    private void visitAsignacion(NodoAsignacion nodo) {
        // Para "x = a + b", el RPN es "x a b + ="
        // 1. Agrega el destino (el identificador) a la lista.
        codigoRPN.add(nodo.identificador);
        // 2. Procesa la expresión, que agregará su propia secuencia RPN.
        procesarExpresion(nodo.expresion);
        // 3. Agrega el operador de asignación al final.
        codigoRPN.add("=");
    }

    private void visitIfElse(NodoIfElse nodo) {
        // Para "if (cond) { A } else { B }", el RPN es:
        // RPN(cond) ETIQUETA_FALSA if_false_goto RPN(A) ETIQUETA_FIN goto ETIQUETA_FALSA: RPN(B) ETIQUETA_FIN:
        String etiquetaFalsa = nuevaEtiqueta();
        String etiquetaFin = nuevaEtiqueta();

        procesarExpresion(nodo.condicion);
        codigoRPN.add(etiquetaFalsa);
        codigoRPN.add("if_false_goto"); // Operador especial de salto condicional

        for (NodoAST sentencia : nodo.bloqueIf) visit(sentencia);

        if (nodo.bloqueElse != null) {
            codigoRPN.add(etiquetaFin);
            codigoRPN.add("goto");
        }
        
        codigoRPN.add(etiquetaFalsa + ":");

        if (nodo.bloqueElse != null) {
            for (NodoAST sentencia : nodo.bloqueElse) visit(sentencia);
            codigoRPN.add(etiquetaFin + ":");
        }
    }

    private void visitFor(NodoFor nodo) {
        String etiquetaInicio = nuevaEtiqueta();
        String etiquetaFin = nuevaEtiqueta();

        codigoRPN.add(etiquetaInicio + ":");
        procesarExpresion(nodo.condicion);
        codigoRPN.add(etiquetaFin);
        codigoRPN.add("if_false_goto");

        for (NodoAST sentencia : nodo.cuerpo) visit(sentencia);
        
        codigoRPN.add(etiquetaInicio);
        codigoRPN.add("goto");
        codigoRPN.add(etiquetaFin + ":");
    }

    private void visitPrintln(NodoPrintln nodo) {
        for (NodoAST expr : nodo.expresiones) {
            procesarExpresion(expr);
            codigoRPN.add("PRINT");
        }
    }

    
    private void procesarExpresion(NodoAST nodo) {
        if (nodo instanceof NodoOperacion) {
            NodoOperacion opNode = (NodoOperacion) nodo;
            // 1. Procesa hijo izquierdo.
            procesarExpresion(opNode.izquierdo);
            // 2. Procesa hijo derecho.
            procesarExpresion(opNode.derecho);
            // 3. Agrega el operador al final.
            codigoRPN.add(opNode.operador);
        }
        else if (nodo instanceof NodoOperacionUnaria) {
            NodoOperacionUnaria opNode = (NodoOperacionUnaria) nodo;
            procesarExpresion(opNode.operando);
            codigoRPN.add(opNode.operador);
        }
        else if (nodo instanceof NodoLiteral) {
            codigoRPN.add(((NodoLiteral) nodo).valor);
        }
        else if (nodo instanceof NodoIdentificador) {
            codigoRPN.add(((NodoIdentificador) nodo).nombre);
        }
    }
}