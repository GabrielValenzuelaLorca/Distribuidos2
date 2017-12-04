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

    Server() throws RemoteException{

        try {
            /*Deberia ser necesario que este wn tambien este el multicast dependiendo de como se
              haga la funcion request*/
            ip_multi = InetAddress.getByName("231.0.0.1");
            puerto_multi = 4444;
            socket = new MulticastSocket(puerto_multi);
            socket.joinGroup(ip_multi);
            }catch(IOException e){
            System.err.println("Error");
            e.printStackTrace();
            }
        }


    public void request(int id, int seq) throws  RemoteException {


    }

    public void waitToken(int id) throws RemoteException {

    }

    public void takeToken(Token token) throws RemoteException{

    }

    public void kill() throws RemoteException {

    }

    public static void main(String[] args) {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new RMISecurityManager());
        }
        System.out.println("[Servidor] Iniciando...");
        try {
            Inter srv = new Server();
            Naming.rebind("rmi://localhost:" + args[0] + "/SusukiKazami", srv);
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
