package Server;

import ProfileService.Profiler;
import ProfileService.ProfilerHelper;
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


public class ProfileServer {
    public static void main(String[] args) {

        ORB orb = ORB.init(args, null);
        POA rootpoa = null;
        try {
            rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootpoa.the_POAManager().activate();
            ProfileServant profileServantImpl = new ProfileServant();
            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(profileServantImpl);
            Profiler pref = ProfilerHelper.narrow(ref);

            org.omg.CORBA.Object objectRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objectRef);

            String name = "Profiler";
            NameComponent[] path = ncRef.to_name(name);
            ncRef.rebind(path, pref);
            orb.run();
        } catch (InvalidName | AdapterInactive | org.omg.CosNaming.NamingContextPackage.InvalidName |
                WrongPolicy | ServantNotActive | CannotProceed | NotFound e) {
            e.printStackTrace();
        }
    }
}
