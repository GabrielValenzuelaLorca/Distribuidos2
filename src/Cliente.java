public class Cliente {
    public static void main(String[] args) {
        int id, n, initialDelay;
        Boolean bearer;

        id = Integer.parseInt(args[0]);
        n = Integer.parseInt(args[1]);
        initialDelay = Integer.parseInt(args[2]);
        bearer = Boolean.valueOf(args[3]);

        if (bearer){
            //DALE WEON, MANDATE EL RMI PAPA

        }
        System.out.println(id);
        System.out.println(n);
        System.out.println(initialDelay);
        System.out.println(bearer);


    }
}
