package Client;

import ProfileService.Profiler;
import ProfileService.ProfilerHelper;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import java.util.concurrent.TimeUnit;

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

            //int count = clientRef.getTimesOrdered("SOAAADD12AB018A9DD");
            //TimeUnit.MILLISECONDS.sleep(100);
            //System.out.println("SOAAADD12AB018A9DD" + " " + count);

            InputParser inputParser = new InputParser();

            inputParser.readInput("testfiles/test_input.txt");

        } catch (InvalidName | CannotProceed | org.omg.CosNaming.NamingContextPackage.InvalidName | NotFound e) {
            e.printStackTrace();
        }
    }
}