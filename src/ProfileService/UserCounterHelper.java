package ProfileService;


/**
* ProfileService/UserCounterHelper.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from src/profileservice.idl
* 15. september 2020 kl 16.57 CEST
*/

abstract public class UserCounterHelper
{
  private static String  _id = "IDL:ProfileService/UserCounter:1.0";

  public static void insert (org.omg.CORBA.Any a, ProfileService.UserCounter that)
  {
    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
  }

  public static ProfileService.UserCounter extract (org.omg.CORBA.Any a)
  {
    return read (a.create_input_stream ());
  }

  private static org.omg.CORBA.TypeCode __typeCode = null;
  private static boolean __active = false;
  synchronized public static org.omg.CORBA.TypeCode type ()
  {
    if (__typeCode == null)
    {
      synchronized (org.omg.CORBA.TypeCode.class)
      {
        if (__typeCode == null)
        {
          if (__active)
          {
            return org.omg.CORBA.ORB.init().create_recursive_tc ( _id );
          }
          __active = true;
          org.omg.CORBA.StructMember[] _members0 = new org.omg.CORBA.StructMember [2];
          org.omg.CORBA.TypeCode _tcOf_members0 = null;
          _tcOf_members0 = org.omg.CORBA.ORB.init ().create_string_tc (0);
          _members0[0] = new org.omg.CORBA.StructMember (
            "user_id",
            _tcOf_members0,
            null);
          _tcOf_members0 = org.omg.CORBA.ORB.init ().get_primitive_tc (org.omg.CORBA.TCKind.tk_long);
          _members0[1] = new org.omg.CORBA.StructMember (
            "restaurant_timesOrdered",
            _tcOf_members0,
            null);
          __typeCode = org.omg.CORBA.ORB.init ().create_struct_tc (ProfileService.UserCounterHelper.id (), "UserCounter", _members0);
          __active = false;
        }
      }
    }
    return __typeCode;
  }

  public static String id ()
  {
    return _id;
  }

  public static ProfileService.UserCounter read (org.omg.CORBA.portable.InputStream istream)
  {
    ProfileService.UserCounter value = new ProfileService.UserCounter ();
    value.user_id = istream.read_string ();
    value.restaurant_timesOrdered = istream.read_long ();
    return value;
  }

  public static void write (org.omg.CORBA.portable.OutputStream ostream, ProfileService.UserCounter value)
  {
    ostream.write_string (value.user_id);
    ostream.write_long (value.restaurant_timesOrdered);
  }

}
