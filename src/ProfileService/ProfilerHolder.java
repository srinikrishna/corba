package ProfileService;

/**
* ProfileService/ProfilerHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from src/profileservice.idl
* 15. september 2020 kl 16.57 CEST
*/


/* The service interface with the methods that can be invoked remotely by clients */
public final class ProfilerHolder implements org.omg.CORBA.portable.Streamable
{
  public ProfileService.Profiler value = null;

  public ProfilerHolder ()
  {
  }

  public ProfilerHolder (ProfileService.Profiler initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = ProfileService.ProfilerHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    ProfileService.ProfilerHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return ProfileService.ProfilerHelper.type ();
  }

}
