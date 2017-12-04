/**
 * Created by Giorgio-Gabotto
 */

import java.io.*;
import java.net.*;
import java.util.*;
import java.rmi.*;




//Constructor culiao este
public class Client {

    static public int id;
    static public int procesos;
    static public Boolean bearer;
    static public Token token;
    static public Boolean tienetoken;
    //1:Verde, 2:Amarillo, 3:Rojo
    static public int estado;
    static public int RN[];
    static private MulticastSocket multicastSocket;
    static private InetAddress ip_multi;
    static private int puerto_multi;

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

    public static class recepcion_request implements Runnable {

        byte[] buf;

        public void run() {
            try {
                ip_multi = InetAddress.getByName("231.0.0.1");
                puerto_multi = 4444;
                multicastSocket = new MulticastSocket(puerto_multi);
                multicastSocket.joinGroup(ip_multi);
            }catch(IOException e){
                System.err.println("Error");
                e.printStackTrace();
            }


            while(true){
                try{
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    multicastSocket.receive(packet);

                    //Mensaje con el request//

                    String mensaje = new String(packet.getData(),packet.getOffset(), packet.getLength()).trim();
                    int men =Integer.parseInt(mensaje);
                    if(men!=id){
                        RN[men]++;
                    }
                }
                catch (IOException e){
                    System.err.println("Excepcion: ");
                    e.printStackTrace();
                }
            }

        }
    }


    public static void main(String[] args) throws RemoteException {
        Thread request = new Thread(new recepcion_request());
        request.start();

    }

}
