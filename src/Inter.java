/**
 * Created by Giorgio-Gabotto
 */

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Inter extends Remote {
    void request(int id, int valor) throws RemoteException;
    void waitToken(int id1,int id2) throws RemoteException;
    void takeToken(Token token) throws RemoteException;
    void kill() throws RemoteException;
    void sendToken(Token token, int id1,int id2) throws  RemoteException;
}