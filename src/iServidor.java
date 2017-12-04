import java.rmi.Remote;
import java.rmi.RemoteException;

public interface iServidor extends Remote {
    public int request(int id, int seq) throws RemoteException;
    public void waitToken() throws RemoteException; //Podria ser un int
    public int takeToken(Token token) throws RemoteException; //ver que devuelve
    public int kill() throws RemoteException;

}