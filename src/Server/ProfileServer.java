package Server;

import ProfileService.Profiler;
import ProfileService.ProfilerHelper;
import ProfileService.RestaurantProfile;
import ProfileService.UserProfile;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ProfileServer {
    public static Map<String, RestaurantProfile> restaurantCache = new HashMap<>(1000);
    public static Map<String, UserProfile> userCache = new HashMap<>(1000);
    public static Map<Integer, ArrayList<String>> zoneCache = new LinkedHashMap<>();
    public static String orderingPath = "train_in5020/restaurant_ordering_profile.txt";
    public static String zonePath = "train_in5020/restaurant_location_directory.txt";

    private static boolean checkInput(String[] args) {
        if (args.length < 4) {
            System.out.println("Please provide necessary arguments to run the server. \n" +
                    "\t Arguments: -ORBInitialPort <port> [-Caching <On/Off>]");
            return false;
        }
        return true;
    }
    private static String[] getORBArgs(String[] args) {
        String[] orb_args = new String[2];
        System.arraycopy(args, 0, orb_args, 0, orb_args.length);
        return orb_args;
    }

    private static boolean getCachingOption(String[] args) {
        if ("-Caching".equals(args[2])) {
            return "On".equals(args[3]);
        }
        return false;
    }

    public static void main(String[] args) {

        if(!checkInput(args)) return;
        boolean caching = getCachingOption(args);

        ORB orb = ORB.init(getORBArgs(args), null);
        POA rootpoa = null;
        try {
            rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootpoa.the_POAManager().activate();
            ProfileServant profileServantImpl;

            if (caching) {
                System.out.println("Please wait while the server is loading the cache. This will take some time...");
                ServerCache.loadCache(orderingPath, zonePath, zoneCache, restaurantCache, userCache);
                System.out.println("Cache loading complete...");
                System.out.println("Initializing Profile Servant...");
                profileServantImpl = new ProfileServant(orderingPath, zonePath, zoneCache, restaurantCache, userCache);
            } else {
                profileServantImpl = new ProfileServant(orderingPath, zonePath);
            }

            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(profileServantImpl);
            Profiler pref = ProfilerHelper.narrow(ref);

            org.omg.CORBA.Object objectRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objectRef);

            String name = "Profiler";
            NameComponent[] path = ncRef.to_name(name);
            System.out.println("Connecting to ORB...");
            ncRef.rebind(path, pref);
            System.out.println("ProfilerServer ready to receive RMIs");
            orb.run();
        } catch (InvalidName | AdapterInactive | org.omg.CosNaming.NamingContextPackage.InvalidName |
                WrongPolicy | ServantNotActive | CannotProceed | NotFound e) {
            e.printStackTrace();
        }
    }
}
