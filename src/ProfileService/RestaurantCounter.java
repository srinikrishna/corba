package ProfileService;


/**
* ProfileService/RestaurantCounter.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from src/profileservice.idl
* 15. september 2020 kl 16.57 CEST
*/

public final class RestaurantCounter implements org.omg.CORBA.portable.IDLEntity
{
  public String restaurant_id = null;
  public int restaurant_timesOrdered = (int)0;

  public RestaurantCounter ()
  {
  } // ctor

  public RestaurantCounter (String _restaurant_id, int _restaurant_timesOrdered)
  {
    restaurant_id = _restaurant_id;
    restaurant_timesOrdered = _restaurant_timesOrdered;
  } // ctor

} // class RestaurantCounter
