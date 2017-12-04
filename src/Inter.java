/**
 * Created by Giorgio-Gabotto
 */

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Inter extends Remote {
    void request(int id) throws RemoteException;
    void waitToken(int id) throws RemoteException;
    void takeToken(Token token) throws RemoteException;
    void kill() throws RemoteException;
}