import java.io.*;
import java.net.*;
import java.util.*;

public class Token implements Serializable{

    /* Cola de procesos en espera del Token */

    Queue<Integer> colaRequest;

    static public int LN[];

    int proxId;

    /* Constructor de la clase Token */
    public Token(int n){
        colaRequest = new LinkedList();

        LN = new int[n];
        for (int i = 0; i < n; i++) {
            LN[i] = 0;
        }
    }

    public void encolarProceso(int id){
        colaRequest.add(id);
    }

    public int desencolarProceso(){
        return colaRequest.poll();
    }
}