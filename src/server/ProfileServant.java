package server;

import ProfileService.FoodTypeCounter;
import ProfileService.ProfilerPOA;
import ProfileService.UserCounter;

public class ProfileServant extends ProfilerPOA {
    @Override
    public int getTimesOrdered(String restaurant_id) {

        return 0;
    }

    @Override
    public int getTimesOrderedByUser(String user_id, String restaurant_id) {
        return 0;
    }

    @Override
    public UserCounter[] getTopThreeUsersByRestaurant(String restaurant_id) {
        return new UserCounter[0];
    }

    @Override
    public FoodTypeCounter[] getTopThreeFoodTypesByZone(String zone_id) {
        return new FoodTypeCounter[0];
    }
}
