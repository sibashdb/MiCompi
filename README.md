# MiCompi — Compilador para un subconjunto de Go

MiCompi es un compilador educativo escrito en **Java** que traduce programas
escritos en un subconjunto del lenguaje **Go** a código objeto (ensamblador
simulado estilo x86). Implementa de forma completa las fases clásicas de un
compilador, desde el análisis léxico hasta la generación y optimización de
código intermedio y la generación de código objeto.

El proyecto fue desarrollado como trabajo de la asignatura de Lenguajes y
Autómatas / Compiladores, y sirve como ejemplo práctico de cómo se construye
un compilador por fases.


## Características del lenguaje soportado

El compilador reconoce un subconjunto de Go con las siguientes construcciones:

- Función principal `func main() { ... }`.
- Declaración de variables con tipo: `var x int`, `var y float64`, `var s string`, `var b bool`.
- Asignaciones y expresiones aritméticas con precedencia de operadores
  (`+`, `-`, `*`, `/`) y paréntesis.
- Operadores relacionales (`==`, `!=`, `<`, `>`, `<=`, `>=`) y lógicos (`&&`, `||`, `!`).
- Estructuras de control: `if / else` y bucles `for` (usados como `while`).
- Entrada y salida con `fmt.Scanln(...)` y `fmt.Println(...)`.
- Literales enteros, de punto flotante, cadenas y booleanos (`true` / `false`).

Ejemplo de programa de entrada (`src/micompi/codigo.txt`):

```go
func main () {
    var x int
    var z int
    var y float64
    var mensaje string

    x = 8+(5*5+2)-10*z
    y = 3.5 + 10
    z = x+8
    mensaje = "Iniciando..."

    if y > x {
        fmt.Println ("Condicion compuesta verdadera")
    } else {
        fmt.Println ("Condicion compuesta falsa")
    }

    for x < 5 {
        x = x + 1
    }
    fmt.Println ("Programa finalizado.")
}
```

---

## Arquitectura: fases del compilador

El flujo completo está orquestado en `MiCompi.java` y se compone de las
siguientes fases, cada una implementada en su propia clase:

1. **Análisis léxico** (`lexico.java`)
   Recorre el archivo fuente carácter por carácter usando una **matriz de
   transiciones** (autómata finito determinista). Genera una lista enlazada de
   tokens (`nodo`), reconoce palabras reservadas y reporta errores léxicos
   (símbolo no reconocido, cadena sin cerrar, EOF inesperado, etc.).

2. **Análisis sintáctico** (`sintactico.java`)
   Analizador de **descenso recursivo** que valida la gramática y construye un
   **Árbol de Sintaxis Abstracta (AST)**. La jerarquía de expresiones respeta la
   precedencia de operadores (lógicos → relacionales → aritméticos → término →
   valor). Los nodos del AST están en el paquete `micompi.NodoAST`.

3. **Análisis semántico** (`semantico.java`)
   Recorre el AST con un patrón *visitor*. Construye la **tabla de símbolos**,
   verifica que las variables estén declaradas antes de usarse, detecta
   redeclaraciones y realiza **comprobación de tipos** (compatibilidad en
   asignaciones y operaciones, condiciones booleanas en `if` / `for`).

4. **Generación de código intermedio (RPN)** (`GeneradorRPN.java`)
   Traduce el AST a **notación polaca inversa (RPN)**, incluyendo operadores
   especiales de salto (`if_false_goto`, `goto`) y etiquetas para las
   estructuras de control.

5. **Generación de cuádruplos** (`ProcesadorRPN.java`, `Cuadruplo.java`)
   Procesa la RPN con una pila y produce **código intermedio en forma de
   cuádruplos** `(operador, arg1, arg2, resultado)`, usando variables
   temporales (`t0`, `t1`, …).

6. **Optimización** (`Optimizador.java`)
   Aplica varias pasadas sobre los cuádruplos:
   - **Plegado de constantes** (constant folding): evalúa en tiempo de
     compilación operaciones entre constantes.
   - **Propagación de copias** y **eliminación de código muerto**: elimina
     temporales que no vuelven a usarse.

7. **Generación de código objeto** (`GeneradorCodigoObjeto.java`)
   Traduce los cuádruplos optimizados a **ensamblador simulado** estilo x86
   (`MOV`, `ADD`, `IMUL`, `CMP`, `JMP`, etc.).

Durante la ejecución se imprime en consola la salida de cada fase: la tabla de
tokens, los mensajes de los análisis, el código RPN, los cuádruplos (sin
optimizar y optimizados) y el código objeto final.



## Limitaciones conocidas

Este es un compilador **educativo**; el objetivo es ilustrar las fases, no
producir un ejecutable real. Puntos a tener en cuenta:

- El "código objeto" es **ensamblador simulado** con fines didácticos: no se
  ensambla ni se enlaza a un binario ejecutable.
- La generación de código objeto trata algunos literales como direcciones de
  memoria (p. ej. `ADD AX, [8]`), por lo que no está pensada para ejecutarse en
  un procesador real.
- El operador módulo (`%`) está contemplado en la gramática pero no se tokeniza
  completamente en el autómata léxico.
- No hay manejo de ámbitos anidados ni funciones definidas por el usuario más
  allá de `main`.
- La recuperación de errores es básica: se reporta el primer error de cada fase.

Estas limitaciones son oportunidades naturales de mejora si se quiere extender
el proyecto.
