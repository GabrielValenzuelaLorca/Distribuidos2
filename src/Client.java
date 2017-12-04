/**
 * Created by Giorgio-Gabotto
 */

import java.io.*;
import java.net.*;
import java.util.*;
import java.rmi.*;




//Constructor culiao este
public class Client {

    public int id;
    public int procesos;
    public Boolean bearer;
    public Token token;
    public Boolean tienetoken;
    //1:Verde, 2:Amarillo, 3:Rojo
    public int estado;
    public int RN[];
    private MulticastSocket multicastSocket;
    private InetAddress ip_multi;
    private int puerto_multi;

    public Client(int id, int procesos, Boolean bearer) {
        this.id = id;
        this.procesos = procesos;
        this.bearer = bearer;
        this.estado = 1;

        RN = new int[procesos];
        for (int i = 0; i < procesos; i++) {
            RN[i] = 0;
        }

        //AGUANTE MAURO
        if (this.bearer) {
            this.token = new Token(procesos);
            this.tienetoken = true;

        } else {
            this.token = null;
            this.tienetoken = false;

        }
    }


    public static void main(String[] args) throws RemoteException {

    }

}
