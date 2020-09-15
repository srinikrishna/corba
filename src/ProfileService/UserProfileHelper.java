package ProfileService;


/**
* ProfileService/UserProfileHelper.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from src/profileservice.idl
* 15. september 2020 kl 16.57 CEST
*/

abstract public class UserProfileHelper
{
  private static String  _id = "IDL:ProfileService/UserProfile:1.0";

  public static void insert (org.omg.CORBA.Any a, ProfileService.UserProfile that)
  {
    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
  }

  public static ProfileService.UserProfile extract (org.omg.CORBA.Any a)
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
          _tcOf_members0 = ProfileService.RestaurantCounterHelper.type ();
          _tcOf_members0 = org.omg.CORBA.ORB.init ().create_sequence_tc (0, _tcOf_members0);
          _members0[1] = new org.omg.CORBA.StructMember (
            "restaurants",
            _tcOf_members0,
            null);
          __typeCode = org.omg.CORBA.ORB.init ().create_struct_tc (ProfileService.UserProfileHelper.id (), "UserProfile", _members0);
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

  public static ProfileService.UserProfile read (org.omg.CORBA.portable.InputStream istream)
  {
    ProfileService.UserProfile value = new ProfileService.UserProfile ();
    value.user_id = istream.read_string ();
    int _len0 = istream.read_long ();
    value.restaurants = new ProfileService.RestaurantCounter[_len0];
    for (int _o1 = 0;_o1 < value.restaurants.length; ++_o1)
      value.restaurants[_o1] = ProfileService.RestaurantCounterHelper.read (istream);
    return value;
  }

  public static void write (org.omg.CORBA.portable.OutputStream ostream, ProfileService.UserProfile value)
  {
    ostream.write_string (value.user_id);
    ostream.write_long (value.restaurants.length);
    for (int _i0 = 0;_i0 < value.restaurants.length; ++_i0)
      ProfileService.RestaurantCounterHelper.write (ostream, value.restaurants[_i0]);
  }

}
