/**
 * Created by Giorgio-Gabotto
 */

import java.io.*;
import java.net.*;
import java.util.*;
import java.rmi.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Client {

    static public int id;
    static public int procesos;
    static public Boolean bearer;
    static public Token token;
    static public Boolean tienetoken;
    static public int initialdelay;
    //1:Verde, 2:Amarillo, 3:Rojo
    static public int estado;
    static public int RN[];
    static private MulticastSocket multicastSocket;
    static private InetAddress ip_multi;
    static private int puerto_multi;
    static public Logger logger;

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

                String cadenaRN="RN = [";
                for (int i=0;i<RN.length;i++){
                    cadenaRN=cadenaRN+String.valueOf(RN[i])+" ";
                }
                cadenaRN=cadenaRN+"]";
                logger.info(cadenaRN);


                String mensaje = new String(packet.getData(), packet.getOffset(), packet.getLength()).trim();
                String[] partes = mensaje.split(";");
                if (Integer.parseInt(partes[0]) != id && Integer.parseInt(partes[1]) > RN[Integer.parseInt(partes[0])]) {
                    /*ó RN[Integer.parseInt(partes[0])]=Integer.parseInt(partes[1]);*/
                    System.out.println("ENCOLE A UN AMIGO!");
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

                if(!tienetoken) {

                    //Se hace request y se espera a un waitToken

                    ip_multi = InetAddress.getByName("127.0.0.1");
                    estado = 2;
                    Thread.sleep(initialdelay);
                    System.out.println("SEMAFORO AMARILLO");

                    logger.info("SEMAFORO AMARILLO CLIENTE "+ (id+1)+" ESPERANDO SECCION CRITICA");

                    RN[id]++;
                    inter.request(id, RN[id]);
                    buf = new byte[256];
                    socketuni = new DatagramSocket(4000+id);
                    paqueteuni = new DatagramPacket(buf, buf.length);
                    socketuni.receive(paqueteuni);
                    System.out.println("Recibi waitToken");

                    //Se espera nuevamente hasta recibir un sendToken

                    String ans = new String(paqueteuni.getData()).trim();
                    System.out.println(ans);
                    System.out.println(ans == "1");
                    buf = new byte[256];
                    paqueteuni = new DatagramPacket(buf, buf.length);
                    System.out.println("Espero un sendToken");
                    socketuni.receive(paqueteuni);
                    ans = new String(paqueteuni.getData()).trim();
                    String[] anser = ans.split(";");
                    System.out.println("anser vale    " +  anser[1]);


                    //Se responde al sendToken para recibir el Token Serializado

                    String envio = "1";
                    buf = envio.getBytes();
                    paqueteuni = new DatagramPacket(buf, buf.length, InetAddress.getByName("127.0.0.1"), 5000 + Integer.parseInt(anser[0]));
                    socketuni.send(paqueteuni);

                    //Se espera al Token

                    buf = new byte[10000];
                    System.out.println("llegue aca!!!!!");
                    paqueteuni = new DatagramPacket(buf, buf.length);
                    socketuni.receive(paqueteuni);
                    ByteArrayInputStream cereal = new ByteArrayInputStream(buf);
                    ObjectInputStream input = new ObjectInputStream(cereal);
                    token = (Token) input.readObject();
                    input.close();

                    //Se toma control sobre el Token

                    inter.takeToken(token, id);
                    tienetoken = true;
                }

                if(tienetoken){
                    estado = 3;
                    System.out.println("SEMAFORO ROJO");
                    logger.info("SEMAFORO ROJO CLIENTE "+ (id+1)+" EN SECCION CRITICA");

                    System.out.println(token.Poseedor);
                    System.out.println(token.LN[0]);

                    //Si, esta es la SC :D

                    Thread.sleep(5000);

                    estado = 1;
                    System.out.println("SEMAFORO VERDE");
                    logger.info("SEMAFORO VERDE CLIENTE "+ (id+1)+" SE DESOCUPO LA SECCION CRITICA");


                    for (int i = 0; i < procesos; i++) {
                        if(RN[i]>token.LN[i]){
                            inter.waitToken(i);
                            token.encolarProceso(i);
                            System.out.println("Encole el proceso"+i);
                            token.LN[i]=RN[i];
                        }
                    }
                    if(!token.colaRequest.isEmpty()){
                        int proceso=token.desencolarProceso();
                        System.out.println("el proceso para send es:   "+ proceso);
                        inter.sendToken(token, id, proceso);
                        tienetoken = false;
                        token = null;
                        System.out.println("TERMINE");
                        System.exit(0);
                    }
                    else{
                        if(token.desencolados==procesos-1){
                            inter.kill();
                            System.out.println("TERMINE");
                            System.exit(0);
                        }
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
                        System.out.println("el proceso para send es:   "+ proceso);
                        System.out.println("valor de LN al momento de enviar: "+token.LN[1]);
                        inter.sendToken(token, id, proceso);
                        tienetoken = false;
                        token = null;
                        System.out.println("TERMINE");
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
        logger = Logger.getLogger("MyLog");
        FileHandler fh;




        id = Integer.parseInt(args[0])-1;
        procesos = Integer.parseInt(args[1]);
        initialdelay=Integer.parseInt(args[2]);
        bearer = Boolean.valueOf(args[3]);
        estado = 1;

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

            // This block configure the logger with handler and formatter
            fh = new FileHandler("../Log "+(id+1)+".log");
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);

            // the following statement is used to log any messages

        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            String cadenaRN="RN = [",cadenaLN="LN = [";
            for (int i=0;i<RN.length;i++){
                cadenaRN=cadenaRN+String.valueOf(RN[i])+" ";
            }
            cadenaRN=cadenaRN+"]";
            logger.info(cadenaRN);

            if (tienetoken){
                for (int i=0;i<token.LN.length;i++){
                    cadenaLN=cadenaLN+String.valueOf(token.LN[i])+" ";
                }
                cadenaLN=cadenaLN+"]";
                logger.info(cadenaLN);
            }

            logger.info("SEMAFORO VERDE, INICIA EL CLIENTE "+ (id+1));

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
