/**
 * Created by Giorgio-Gabotto
 */

import java.io.*;
import java.net.*;
import java.util.*;
import java.rmi.*;

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
    static private int puerto_multi, puerto_recept;


    public static class recepcion_request implements Runnable {

        byte[] buf;

        public void run() {
            if (System.getSecurityManager() == null) {
                System.setSecurityManager(new SecurityManager());
            }

            try {
                ip_multi = InetAddress.getByName("231.0.0.1");
                puerto_multi = 9000;
                multicastSocket = new MulticastSocket(puerto_multi);
                multicastSocket.joinGroup(ip_multi);


            while (true) {
                buf = new byte[256];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                multicastSocket.receive(packet);

                //Mensaje con el request//

                String mensaje = new String(packet.getData(), packet.getOffset(), packet.getLength()).trim();
                String[] partes = mensaje.split(";");
                if (Integer.parseInt(partes[0]) != id && Integer.parseInt(partes[1]) > RN[Integer.parseInt(partes[0])]) {
                    /*ó RN[Integer.parseInt(partes[0])]=Integer.parseInt(partes[1]);*/
                    RN[Integer.parseInt(partes[0])]++;
                }
            }

        }   catch(IOException e){
                System.err.println("Excepcion: ");
                e.printStackTrace();
            }
        }
    }


    public static class main implements Runnable {

        public void run() {
            if(System.getSecurityManager() == null) {
                System.setSecurityManager(new SecurityManager());
            }
            DatagramPacket paqueteuni;
            DatagramSocket socketuni;
            byte[] buf;
            try{

                Inter inter = (Inter) Naming.lookup("//localhost:12345/susuki");

                if(!tienetoken){

                    ip_multi = InetAddress.getByName("127.0.0.1");
                    estado = 2;
                    System.out.println("SEMAFORO AMARILLO");
                    RN[id]++;
                    inter.request(id,RN[id]);
                    buf = new byte[256];
                    socketuni = new DatagramSocket(puerto_recept);
                    paqueteuni = new DatagramPacket(buf, buf.length);
                    socketuni.receive(paqueteuni);
                    socketuni.close();
                    String ans = new String(paqueteuni.getData()).trim();
                    if(ans=="1") {
                        buf = new byte[256];
                        socketuni = new DatagramSocket(puerto_recept);
                        paqueteuni = new DatagramPacket(buf, buf.length);
                        socketuni.receive(paqueteuni);
                        socketuni.close();
                        ans = new String(paqueteuni.getData()).trim();
                        String[] anser = ans.split(";");
                        if (anser[1] == "2") {
                            String envio = "1";
                            buf = envio.getBytes();
                            paqueteuni = new DatagramPacket(buf, buf.length, InetAddress.getByName("127.0.0.1"), 4000 + Integer.parseInt(anser[0]));
                            socketuni.send(paqueteuni);
                            socketuni.close();

                            //RECIBIR CEREAL
                            paqueteuni = new DatagramPacket(buf, buf.length);
                            socketuni = new DatagramSocket(puerto_recept);
                            socketuni.receive(paqueteuni);
                            ByteArrayInputStream cereal = new ByteArrayInputStream(buf);
                            ObjectInputStream input = new ObjectInputStream(cereal);
                            token = (Token) input.readObject();
                            input.close();
                        }
                    }
                    inter.takeToken(token,id);
                    tienetoken=true;
                }

                if(tienetoken){
                    estado = 3;
                    System.out.println("SEMAFORO ROJO");

                    //Si, esta es la SC :D

                    Thread.sleep(5000);

                    estado = 1;
                    System.out.println("SEMAFORO VERDE");

                    for (int i = 0; i < procesos; i++) {
                        if(RN[i]>token.LN[i]){
                            inter.waitToken(i);
                            token.encolarProceso(i);
                            token.LN[i]=RN[i];
                        }
                    }
                    if(!token.colaRequest.isEmpty()){
                        int proceso=token.desencolarProceso();
                        inter.sendToken(token, id, proceso);
                        tienetoken = false;
                        token = null;
                        inter.kill();
                        System.exit(0);
                    }
                    else{
                        while(token.colaRequest.isEmpty()){
                            for (int i = 0; i < procesos; i++) {
                                if(RN[i]>token.LN[i]){
                                    inter.waitToken(i);
                                    token.encolarProceso(i);
                                    token.LN[i]=RN[i];
                                }
                            }
                            Thread.sleep(5000);
                            System.out.println("Estoy esperando amigos :c");
                        }
                        int proceso=token.desencolarProceso();
                        inter.sendToken(token, id, proceso);
                        tienetoken = false;
                        token = null;
                        inter.kill();
                        System.exit(0);
                    }
                }

            }
            catch(RemoteException e){
                System.err.println("Error: " + e.toString());
            }
            catch (Exception e){
                System.err.println("Excepción: ");
                e.printStackTrace();
            }

        }
    }


    public static void main(String[] args) throws RemoteException {
        id = Integer.parseInt(args[0])-1;
        procesos = Integer.parseInt(args[1]);
        bearer = Boolean.valueOf(args[2]);
        estado = 1;
        puerto_recept=4000+id;

        RN = new int[procesos];
        for (int i = 0; i < procesos; i++) {
            RN[i] = 0;
        }
        token = null;
        tienetoken = false;

        if (bearer) {
            token = new Token(procesos);
            RN[id]++;
            token.LN[id]++;
            tienetoken = true;
        }
        try {
            System.out.println("Se inicio un cliente !");
            Thread request = new Thread(new recepcion_request());
            request.start();
            Thread main = new Thread(new main());
            main.start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
