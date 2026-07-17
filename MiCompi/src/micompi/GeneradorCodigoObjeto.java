package micompi;

import java.util.ArrayList;
import java.util.List;

public class GeneradorCodigoObjeto {

    private final List<Cuadruplo> cuadruplos;
    private final List<String> codigoObjeto;

    public GeneradorCodigoObjeto(List<Cuadruplo> cuadruplos) {
        this.cuadruplos = cuadruplos;
        this.codigoObjeto = new ArrayList<>();
    }

    public List<String> generar() {
        codigoObjeto.add("SECTION .text");
        codigoObjeto.add("global _start");
        codigoObjeto.add("_start:");

        for (Cuadruplo c : cuadruplos) {
            traducirCuadruplo(c);
        }

        codigoObjeto.add("\n; --- Fin del Programa ---");
        codigoObjeto.add("MOV EAX, 1");
        codigoObjeto.add("XOR EBX, EBX");
        codigoObjeto.add("INT 0x80");

        return codigoObjeto;
    }

    private void traducirCuadruplo(Cuadruplo c) {
        switch (c.operador) {
            case "=":
                codigoObjeto.add("MOV AX, " + c.arg1);
                codigoObjeto.add("MOV [" + c.resultado + "], AX");
                break;
            case "+":
            case "-":
            case "*":
            case "/":
                String op = "";
                if(c.operador.equals("+")) op = "ADD";
                if(c.operador.equals("-")) op = "SUB";
                if(c.operador.equals("*")) op = "IMUL"; // Usar IMUL para multiplicación con signo
                if(c.operador.equals("/")) op = "IDIV"; // Usar IDIV para división con signo

                codigoObjeto.add("MOV AX, [" + c.arg1 + "]");
                codigoObjeto.add(op + " AX, [" + c.arg2 + "]");
                codigoObjeto.add("MOV [" + c.resultado + "], AX");
                break;
            
            case ">": case "<": case "==": case "!=": case ">=": case "<=":
                codigoObjeto.add("MOV AX, [" + c.arg1 + "]");
                codigoObjeto.add("CMP AX, [" + c.arg2 + "]");
                break;

            case "IF_FALSE": // Jump if False
                // JE (Jump if Equal) salta si el flag de Cero está activado (resultado de CMP fue 0)
                String jumpInstruction = obtenerSaltoEquivalente(c.operador);
                codigoObjeto.add("JE " + c.resultado);
                break;

            case "GOTO":
                codigoObjeto.add("JMP " + c.resultado);
                break;
            
            case "LABEL":
                codigoObjeto.add(c.resultado + ":");
                break;

            case "PRINT":
                codigoObjeto.add("MOV EAX, " + c.arg1);
                codigoObjeto.add("CALL print_function");
                break;
        }
    }
    
    // Método auxiliar para futuras extensiones de saltos
    private String obtenerSaltoEquivalente(String opComparacion) {
        switch(opComparacion) {
            case "==": return "JE";  // Jump if Equal
            case "!=": return "JNE"; // Jump if Not Equal
            case ">":  return "JG";  // Jump if Greater
            case "<":  return "JL";  // Jump if Less
            case ">=": return "JGE"; // Jump if Greater or Equal
            case "<=": return "JLE"; // Jump if Less or Equal
            default:   return "JMP"; // Salto por defecto
        }
    }
}