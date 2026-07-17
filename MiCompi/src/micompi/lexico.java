
package micompi;

import java.io.RandomAccessFile;

class lexico {
    nodo cabeza = null, p;
    int estado = 0, columna, valorMT, numRenglon = 1;
    int caracter = 0;
    String lexema = "";
    boolean errorEncontrado = false;
    
    

  
    String archivo = "src\\micompi\\codigo.txt";


    int matriz[][] = {
                    // l      d       .       "       +       -       * /       %       =       !       <       >       &       |       (       )       {       }       [       ]       ,       ;       :       \       eb      tab     nl      eof     rt      oc
                    // 0      1       2       3       4       5       6       7       8       9       10      11      12      13      14      15      16      17      18      19      20      21      22      23      24      25      26      27      28      29      30
            /*0*/{   1,      2,    130,      5,      6,      7,    106,     15,      0,      8,      9,     10,     11,     12,     13,    121,    122,    123,    124,    125,    126,    127,    128,    129,     14,      0,      0,      0,       0,      0,    500},
            /*1*/{   1,      1,    100,    100,    100,    100,    100,    100,    100,    100,    100,    100,    100,    100,    100,    100,    100,    100,    100,    100,    100,    100,    100,    100,    100,    100,    100,    100,    100,    100,    100},
            /*2*/{ 101,      2,      3,    101,    101,    101,    101,    101,    101,    101,    101,    101,    101,    101,    101,    101,    101,    101,    101,    101,    101,    101,    101,    101,    101,    101,    101,    101,    101,    101,    101},
            /*3*/{ 501,      4,    501,    501,    501,    501,    501,    501,    501,    501,    501,    501,    501,    501,    501,    501,    501,    501,    501,    501,    501,    501,    501,    501,    501,    501,    501,    501,    501,    501,    501},
            /*4*/{ 102,      4,    102,    102,    102,    102,    102,    102,    102,    102,    102,    102,    102,    102,    102,    102,    102,    102,    102,    102,    102,    102,    102,    102,    102,    102,    102,    102,    102,    102,    102},
            /*5*/{   5,      5,      5,    103,      5,      5,      5,      5,      5,      5,      5,      5,      5,      5,      5,      5,      5,      5,      5,      5,      5,      5,      5,      5,      5,      5,      5,    502,      5,      5,      5},
            /*6*/{ 104,    104,    104,    104,    109,    104,    104,    104,    104,    104,    104,    104,    104,    104,    104,    104,    104,    104,    104,    104,    104,    104,    104,    104,    104,    104,    104,    104,    104,    104,    104},
            /*7*/{ 105,    105,    105,    105,    105,    110,    105,    105,    105,    105,    105,    105,    105,    105,    105,    105,    105,    105,    105,    105,    105,    105,    105,    105,    105,    105,    105,    105,    105,    105,    105},
            /*8*/{ 111,    111,    111,    111,    111,    111,    111,    111,    111,    112,    111,    111,    111,    111,    111,    111,    111,    111,    111,    111,    111,    111,    111,    111,    111,    111,    111,    111,    111,    111,    111},
            /*9*/{ 120,    120,    120,    120,    120,    120,    120,    120,    120,    113,    120,    120,    120,    120,    120,    120,    120,    120,    120,    120,    120,    120,    120,    120,    120,    120,    120,    120,    120,    120,    120},
            /*10*/{116,    116,    116,    116,    116,    116,    116,    116,    116,    114,    116,    116,    116,    116,    116,    116,    116,    116,    116,    116,    116,    116,    116,    116,    116,    116,    116,    116,    116,    116,    116},
            /*11*/{117,    117,    117,    117,    117,    117,    117,    117,    117,    115,    117,    117,    117,    117,    117,    117,    117,    117,    117,    117,    117,    117,    117,    117,    117,    117,    117,    117,    117,    117,    117},
            /*12*/{132,    132,    132,    132,    132,    132,    132,    132,    132,    132,    132,    132,    132,    118,    132,    132,    132,    132,    132,    132,    132,    132,    132,    132,    132,    132,    132,    132,    132,    132,    132},
            /*13*/{133,    133,    133,    133,    133,    133,    133,    133,    133,    133,    133,    133,    133,    133,    119,    133,    133,    133,    133,    133,    133,    133,    133,    133,    133,    133,    133,    133,    133,    133,    133},
            /*14*/{503,    503,    503,    503,    503,    503,    503,    503,    503,    503,    503,    503,    503,    503,    503,    503,    503,    503,    503,    503,    503,    503,    503,    503,    136,    503,    503,    503,    503,    503,    503},
            /*15*/{107,    107,    107,    107,    107,    107,     16,    107,    107,    107,    107,    107,    107,    107,    107,    107,    107,    107,    107,    107,    107,    107,    107,    107,    107,    107,    107,    107,    107,    107,    107},
            /*16*/{ 16,     16,     16,     16,     16,     16,     17,     16,     16,     16,     16,     16,     16,     16,     16,     16,     16,     16,     16,     16,     16,     16,     16,     16,     16,     16,     16,     16,    504,     16,     16},
            /*17*/{ 16,     16,     16,     16,     16,     16,     16,     18,     16,     16,     16,     16,     16,     16,     16,     16,     16,     16,     16,     16,     16,     16,     16,     16,     16,     16,     16,     16,     16,     16,     16},
            /*18*/{  0,      0,      0,      0,      0,      0,      0,      0,      0,      0,      0,      0,      0,      0,      0,      0,      0,      0,      0,      0,      0,      0,      0,      0,      0,      0,      0,      0,      0,      0,      0}
    };

    String palReservadas[][] = {
                    
             // 0          1       
       /*0*/{"break",    "200"},	
       /*1*/{"default",  "201"},
       /*2*/{"func",     "202"},
       /*3*/{"select",   "203"},
       /*4*/{"case",     "204"},
       /*5*/{"defer",    "205"},
       /*6*/{"go",       "206"},
       /*7*/{"map",      "207"},
       /*8*/{"struct",   "208"},
       /*9*/{"chan",     "209"},
      /*10*/{"else",     "210"},
      /*11*/{"goto",     "211"},
      /*12*/{"switch",   "212"},
      /*13*/{"const",    "213"},
      /*14*/{"if",       "214"},
      /*15*/{"range",    "215"},
      /*16*/{"type",     "216"},
      /*17*/{"continue", "217"},
      /*18*/{"for",      "218"},
      /*19*/{"return",   "219"},
      /*20*/{"var",      "220"},
      /*21*/{"packpage", "221"},
      /*22*/{"fmt",      "222"},
      /*23*/{"scanln",   "223"},
      /*24*/{"Println",  "224"},
      /*25*/{"main",     "225"},
      /*26*/{"true",     "226"},
      /*27*/{"false",    "227"},
      /*28*/{"int",      "228"},
      /*29*/{"float64",  "229"},
      /*30*/{"string",   "230"},
      /*31*/{"bool",     "231"}
    };

    String errores[][] = {
                    
             //   0                            1       
       /*0*/{"SÍMBOLO NO RECONOCIDO",        "500"},	
       /*1*/{"SE ESPERA UN DÍGITO",          "501"},
       /*2*/{"SE ESPERA CIERRE DE CADENA",   "502"},
       /*3*/{"SE ESPERA UN \\",              "503"},
       /*4*/{"EOF INESPERADO",               "504"},
       /*5*/{"Se espera Func",               "505"},
       /*6*/{"Se esperaba un '}'",           "506"},
       /*7*/{"Se esperaba un '{'",           "507"},
       /*8*/{"Se esperaba un ')' ",          "508"},
       /*9*/{"Se esperaba un '('",           "509"},
       /*10*/{"Se esperaba un main",         "510"},
       /*11*/{"Identificador esperado",      "511"},
       /*12*/{"Se esperaba un Scanln",       "512"},
       /*13*/{"Se esperaba un '.'",          "513"},
       /*14*/{"Se esperaba un tipo de dato", "514"},
       /*15*/{"Se esperaba un Println",      "515"},
       /*16*/{"Operador relacional esperado","516"},
       /*17*/{"Se esperaba un =",            "517"},      
    };

    RandomAccessFile file = null;
    
    public lexico(String archivo) {
        this.archivo = archivo;
    }


    public void analizar() {
        
        try {
            file = new RandomAccessFile(archivo, "r");            
            while (caracter != -1) {
                caracter = file.read();
                if (caracter == -1) {
                    columna = 29; // EOF
                } else if (Character.isLetter(((char) caracter))) {
                    columna = 0;
                } else if (Character.isDigit((char) caracter)) {
                    columna = 1;
                } else {
                    switch ((char) caracter) {
                        case '.': columna = 2; break;
                        case '"': columna = 3; break;
                        case '+': columna = 4; break;
                        case '-': columna = 5; break;
                        case '*': columna = 6; break;
                        case '/': columna = 7; break;
                        case '%': columna = 8; break;
                        case '=': columna = 9; break;
                        case '!': columna = 10; break;
                        case '<': columna = 11; break;
                        case '>': columna = 12; break;
                        case '&': columna = 13; break;
                        case '|': columna = 14; break;
                        case '(': columna = 15; break;
                        case ')': columna = 16; break;
                        case '{': columna = 17; break;
                        case '}': columna = 18; break;
                        case '[': columna = 19; break;
                        case ']': columna = 20; break;
                        case ',': columna = 21; break;
                        case ';': columna = 22; break;
                        case ':': columna = 23; break;
                        case '\\': columna = 24; break;
                        case 32: columna = 25; break; // backspace (eb)
                        case 9: columna = 26; break;  // tab
                        case 10: columna = 27; numRenglon++; break; // nl
                        case 13: columna = 28; break; // rt
                        default: columna = 30; break; // oc
                    }
                }

                valorMT = matriz[estado][columna];

                if (valorMT < 100) { 
                    estado = valorMT;
                    if (estado != 0) {
                        lexema = lexema + (char) caracter;
                    }
                } else if (valorMT >= 100 && valorMT < 500) { 
                    if (valorMT == 100) { 
                        validarSiEsPalabraReservada();
                    }

                    if (valorMT == 100 || valorMT == 101 || valorMT == 102 || valorMT == 104 ||
                        valorMT == 105 || valorMT == 111 || valorMT == 120 || valorMT == 116 ||
                        valorMT == 117 || valorMT == 132 || valorMT == 133 || valorMT == 107 || valorMT >= 200) {
                        if (caracter != -1) file.seek(file.getFilePointer() - 1);
                    } else {
                        lexema = lexema + (char) caracter;
                    }
                    insertarNodo();
                    estado = 0;
                    lexema = "";
                } else { 
                    imprimirError();
                    break;
                }
            }

            imprimirNodos();            
            

        } catch (Exception e) {
            System.out.println("Error en el constructor: " + e.getMessage());            
        } finally {
            try {
                if (file != null) {
                    file.close();
                }
            } catch (Exception e) {
                System.out.println("\nError al cerrar el archivo: " + e.getMessage());
            }
        }
    }
    
    
    private void insertarNodo() {
        nodo nodo = new nodo(lexema, valorMT, numRenglon);
        if (cabeza == null) {
            cabeza = nodo;
            p = cabeza;
        } else {
            p.sig = nodo;
            p = nodo;
        }
    }
    
    private void imprimirNodos() {
        p = cabeza;
        String formatoTabla = "| %-15s | %-15s | %-7s |%n";
        System.out.println("+-----------------+-----------------+---------+");
        System.out.printf(formatoTabla, "Lexema", "Token", "Renglon");
        System.out.println("+-----------------+-----------------+---------+");

        while (p != null) {
            System.out.printf(formatoTabla, p.lexema, p.token, p.renglon);
            p = p.sig;
            }
        System.out.println("+---------------------------------------------+");
    }
    
        private void validarSiEsPalabraReservada() {
        for (String[] palReservada : palReservadas) {
            if (lexema.equals(palReservada[0])) {
                valorMT = Integer.parseInt(palReservada[1]);
                return; 
            }
        }
    }
    
    private void imprimirError() {
        if (valorMT >= 500) {
            for (String[] errore : errores) {
                if (valorMT == Integer.valueOf(errore[1])) {
                    System.out.println("ERROR LEXICO: " + errore[0] + " (código " + valorMT + ") en el renglón " + numRenglon);
                    break;
                }
            }
            errorEncontrado = true;
        }
    }
    
    public nodo obtenerCabeza() {
        return cabeza;
    }
}