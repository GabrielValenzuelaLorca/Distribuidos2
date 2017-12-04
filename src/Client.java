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

    public Client(int id, int procesos, Boolean bearer) {
        this.id = id;
        this.procesos = procesos;
        this.bearer = bearer;
        this.estado = 1;
        this.puerto_recept=4000+id;

        RN = new int[procesos];
        for (int i = 0; i < procesos; i++) {
            RN[i] = 0;
        }

        //AGUANTE MAURO
        if (this.bearer) {
            this.token = new Token(procesos);
            RN[id]++;
            token.LN[id]++;
            this.tienetoken = true;

        } else {
            this.token = null;
            this.tienetoken = false;

        }
    }

    public static class recepcion_request implements Runnable {

        byte[] buf;

        public void run() {
            if(System.getSecurityManager() == null) {
                System.setSecurityManager(new SecurityManager());
            }

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
                    String[] partes=mensaje.split(";");
                    if(Integer.parseInt(partes[0])!=id&&Integer.parseInt(partes[1])>RN[Integer.parseInt(partes[0])]){
                        /*ó RN[Integer.parseInt(partes[0])]=Integer.parseInt(partes[1]);*/
                        RN[Integer.parseInt(partes[0])]++;
                    }
                }
                catch (IOException e){
                    System.err.println("Excepcion: ");
                    e.printStackTrace();
                }
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

                Inter inter = (Inter) Naming.lookup("//localhost:123/susuki");

                if(tienetoken == false){

                    ip_multi = InetAddress.getByName("127.0.0.1");
                    estado = 2;
                    RN[id]++;
                    inter.request(id,RN[id]);
                    buf = new byte[256];
                    socketuni = new DatagramSocket(puerto_recept);
                    paqueteuni = new DatagramPacket(buf, buf.length);
                    socketuni.receive(paqueteuni);
                    socketuni.close();
                    String ans = new String(paqueteuni.getData()).trim();
                    String[] anser=ans.split(";");

                    if(anser[1]=="1") {
                        buf = new byte[256];
                        socketuni = new DatagramSocket(puerto_recept);
                        paqueteuni = new DatagramPacket(buf, buf.length);
                        socketuni.receive(paqueteuni);
                        socketuni.close();
                        String ans1 = new String(paqueteuni.getData()).trim();
                        String[] anser1 = ans.split(";");


                        if (anser1[1] == "2") {
                            String envio = "1";
                            buf = envio.getBytes();
                            paqueteuni = new DatagramPacket(buf, buf.length, InetAddress.getByName("127.0.0.1"), 4000 + Integer.parseInt(anser[0]));
                            socketuni.send(paqueteuni);
                            socketuni.close();
                            //RECIBIR CEREAL
                            paqueteuni = new DatagramPacket(buf, buf.length);
                            System.out.println("Voy a esperar el token para el proceso " + id);

                            socketuni = new DatagramSocket(puerto_recept);
                            socketuni.receive(paqueteuni);
                            try {

                                ByteArrayInputStream cereal = new ByteArrayInputStream(buf);
                                ObjectInputStream input = new ObjectInputStream(cereal);
                                token = (Token) input.readObject();
                                input.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    inter.takeToken(token);
                    tienetoken=true;
                }

                if(tienetoken){
                    estado = 3;
                    //Si, esta es la SC :D

                    Thread.sleep(10000);

                    estado = 1;
                    for (int i = 0; i < procesos; i++) {
                        if(RN[i]>token.LN[i]){
                            inter.waitToken(id,i);
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
                                    inter.waitToken(id,i);
                                    token.encolarProceso(i);
                                    token.LN[i]=RN[i];
                                }
                            }
                            Thread.sleep(3000);
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


        Thread request = new Thread(new recepcion_request());
        request.start();
        Thread main = new Thread(new main());
        main.start();

    }
}
