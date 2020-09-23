package Client;

import ProfileService.Profiler;
import ProfileService.ProfilerHelper;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;

public class ProfileClient {

    private static boolean checkInput(String[] args) {
        if (args.length < 6) {
            System.out.println("\nPlease provide necessary arguments to run the server." +
                    "\nArguments: -ORBInitialPort <port> -Naive <True/False> -Caching <True/False>");
            return false;
        }
        return true;
    }

    private static String[] getORBArgs(String[] args) {
        String[] orb_args = new String[2];
        System.arraycopy(args, 0, orb_args, 0, orb_args.length);
        return orb_args;
    }

    private static boolean getNaiveOption(String[] args) {
        if ("-Naive".equals(args[2])) {
            return "True".equals(args[3]);
        }
        return false;
    }

    private static boolean getCachingOption(String[] args) {
        if ("-Caching".equals(args[4])) {
            return "True".equals(args[5]);
        }
        return false;
    }

    public static void main(String[] args) {
        /* Create and initialize CORBA ORB*/
        if (!checkInput(args)) return;

        for (String arg : args) System.out.println(arg);

        ORB orb = ORB.init(getORBArgs(args), null);
        boolean naive = getNaiveOption(args);
        boolean caching = getCachingOption(args);

        try {
            /* Get the root naming context*/
            org.omg.CORBA.Object objectRef;
            objectRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objectRef);

            /*Resolve the object reference in naming service*/
            String name = "Profiler";
            Profiler clientRef = ProfilerHelper.narrow(ncRef.resolve_str(name));

            IFileHandler fh = null;
            if (caching)
                fh = new FileHandlerCache(naive, caching);
            else if (naive)
                fh = new FileHandler(naive, caching);

            fh.runClientQueries("input.txt", clientRef);

        } catch (InvalidName | CannotProceed | org.omg.CosNaming.NamingContextPackage.InvalidName
                | NotFound e) {
            e.printStackTrace();
        }
    }
}
