/**
 * Created by Giorgio-Gabotto
 */

import java.rmi.*;
import java.rmi.server.*;
import java.io.*;
import java.net.*;



public class Server extends UnicastRemoteObject implements Inter {

    public MulticastSocket socket;
    public InetAddress ip_multi;
    public int puerto_multi;
    public byte[] buf;


    Server() throws RemoteException{

        try {
            /*Deberia ser necesario que este wn tambien este el multicast dependiendo de como se
              haga la funcion request*/

            ip_multi = InetAddress.getByName("231.0.0.1");
            puerto_multi = 9000;
            socket = new MulticastSocket(puerto_multi);
            socket.joinGroup(ip_multi);
            }catch(IOException e){
            System.err.println("Error");
            e.printStackTrace();
            }
        }


    public void request(int id,int valor) throws  RemoteException {

        try{
            buf = ((String.valueOf(id))+";"+(String.valueOf(valor))).getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length,ip_multi,puerto_multi);
            try{
                socket.send(packet);
            } catch (IOException e){
                e.printStackTrace();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }




    public void waitToken(int id2) throws RemoteException {
        DatagramSocket socket;
        DatagramPacket packet;
        byte[] buf;
        String envio;
        try {
            socket = new DatagramSocket();
            envio ="1";
            buf = envio.getBytes();
            packet = new DatagramPacket(buf, buf.length, InetAddress.getByName("127.0.0.1"), 4000+id2);
            socket.send(packet);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void takeToken(Token token, int id) throws RemoteException{
        token.tomarToken(id);
    }

    public void sendToken(Token token, int id1,int id2) throws  RemoteException {
        System.out.println("ENTRE AL SENDTOKEN AHHHHHHHHHHHHHHHHHHHHHHHH");
        DatagramSocket socket;
        DatagramPacket packet;
        System.out.println("valor de LN al momento de enviar: "+token.LN[1]);
        byte[] buf;
        String envio;
        try {

            //Se envia un mensaje avisando que se enviara el token

            socket = new DatagramSocket(5000 + id1);
            envio = String.valueOf(id1) + ";2";
            buf = envio.getBytes();
            packet = new DatagramPacket(buf, buf.length, InetAddress.getByName("127.0.0.1"), 4000 + id2);
            System.out.println("Se envia el primer send");
            socket.send(packet);

            //Se espera respuesta

            buf = new byte[256];
            packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);

            //Se serializa el token y se manda
            System.out.println("INICIA SERIAL");
            ByteArrayOutputStream cereal = new ByteArrayOutputStream();
            ObjectOutputStream output = new ObjectOutputStream(cereal);
            output.writeObject(token);
            output.close();

            buf = cereal.toByteArray();
            packet = new DatagramPacket(buf, buf.length, InetAddress.getByName("127.0.0.1"), 4000 +id2);
            socket.send(packet);

            System.out.println("Se envio el token!");


        }catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void kill() throws RemoteException {

        try{
            Naming.unbind("rmi://localhost:12345/susuki");
            UnicastRemoteObject.unexportObject(this, true);
        }
        catch(NotBoundException e){
            e.printStackTrace();
        }
        catch(MalformedURLException e){
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new RMISecurityManager());
        }
        System.out.println("[Servidor] Iniciando...");
        try {
            Inter inter = new Server();
            Naming.rebind("rmi://localhost:" + args[0] + "/susuki", inter);
            System.out.println("[Servidor] Listo!");
        }
        catch (RemoteException e) {
            System.err.println("Error de comunicacion: " + e.toString());
            System.exit(1);
        }
        catch (Exception e) {
            System.err.println("Excepcion en Servidor:");
            e.printStackTrace();
            System.exit(1);
        }
    }

}
