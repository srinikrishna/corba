package ProfileService;

/**
* ProfileService/RestaurantProfileHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from src/profileservice.idl
* 15. september 2020 kl 16.57 CEST
*/

public final class RestaurantProfileHolder implements org.omg.CORBA.portable.Streamable
{
  public ProfileService.RestaurantProfile value = null;

  public RestaurantProfileHolder ()
  {
  }

  public RestaurantProfileHolder (ProfileService.RestaurantProfile initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = ProfileService.RestaurantProfileHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    ProfileService.RestaurantProfileHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return ProfileService.RestaurantProfileHelper.type ();
  }

}