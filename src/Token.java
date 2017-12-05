import java.io.*;
import java.net.*;
import java.util.*;

public class Token implements Serializable{

    /* Cola de procesos en espera del Token */

    Queue<Integer> colaRequest;

    public int LN[];

    public int Poseedor;
    public int desencolados;

    /* Constructor de la clase Token */
    public Token(int n){
        colaRequest = new LinkedList();
        Poseedor=0;

        LN = new int[n];
        for (int i = 0; i < n; i++) {
            LN[i] = 0;
        }
    }
    public void tomarToken(int id){
        Poseedor=id;
    }
    public void encolarProceso(int id){
        colaRequest.add(id);
    }

    public int desencolarProceso(){
        desencolados++;
        return colaRequest.poll();
    }
}