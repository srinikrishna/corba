package ProfileService;


/**
* ProfileService/TopThreeFoodTypesHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from src/profileservice.idl
* 15. september 2020 kl 16.57 CEST
*/

public final class TopThreeFoodTypesHolder implements org.omg.CORBA.portable.Streamable
{
  public ProfileService.FoodTypeCounter value[] = null;

  public TopThreeFoodTypesHolder ()
  {
  }

  public TopThreeFoodTypesHolder (ProfileService.FoodTypeCounter[] initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = ProfileService.TopThreeFoodTypesHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    ProfileService.TopThreeFoodTypesHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return ProfileService.TopThreeFoodTypesHelper.type ();
  }

}