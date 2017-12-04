import java.io.*;
import java.net.*;
import java.util.*;

//ESTO SE LO ROBE AL MAURO AKJAKJA
public class Token implements Serializable{
    /* Cola de procesos en espera del Token */
    Vector<Integer> listaProcesos;
    Queue<Integer> colaRequest;
    int proxId;

    /* Constructor de la clase Token */
    public Token(int n){
        colaRequest = new LinkedList();
        listaProcesos = new Vector(n);
        for(int i=0; i<n; i++){
            listaProcesos.add(0);
        }
    }

    public void encolarProceso(int id){
        colaRequest.add(id);
    }

    public int getProxId(){
        return colaRequest.poll();
    }
}