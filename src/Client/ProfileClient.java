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
    public static void main(String[] args) {

        /* Create and initialize CORBA ORB*/
        ORB orb = ORB.init(args, null);
        try {
            /* Get the root naming context*/
            org.omg.CORBA.Object objectRef;
            objectRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objectRef);

            /*Resolve the object reference in naming service*/
            String name = "Profiler";
            Profiler clientRef = ProfilerHelper.narrow(ncRef.resolve_str(name));

            IFileHandler fh = null;
            if (args.length == 3 && args[2].compareTo("-c") == 0)
                fh = new FileHandlerCache();
            else
                fh = new FileHandler();

            fh.runClientQueries("test_input.txt", clientRef);

        } catch (InvalidName | CannotProceed | org.omg.CosNaming.NamingContextPackage.InvalidName
                | NotFound e) {
            e.printStackTrace();
        }
    }
}
