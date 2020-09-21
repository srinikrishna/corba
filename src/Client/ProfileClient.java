package Client;

import ProfileService.Profiler;
import ProfileService.ProfilerHelper;
import ProfileService.UserCounter;
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

            String restaurant_id = "SOAAADD12AB018A9DD";
            UserCounter[] userCounters = clientRef.getTopThreeUsersByRestaurant(restaurant_id);

            TimeUnit.MILLISECONDS.sleep(100);

            for (UserCounter us : userCounters) {
                System.out.println("Res: "+ restaurant_id + " User id: " + us.user_id + " Count: "
                        + us.restaurant_timesOrdered);

            }


            //InputParser inputParser = new InputParser();

            //inputParser.readInput("testfiles/test_input.txt");

        } catch (InvalidName | CannotProceed | org.omg.CosNaming.NamingContextPackage.InvalidName
                | NotFound | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
