package Server;

import ProfileService.*;
import java.util.*;


public class ProfileServant extends ProfilerPOA {
    private Map<Integer, ArrayList<String>> zoneCache;
    private Map<String, RestaurantProfile> restaurantCache;
    private Map<String, UserProfile> userCache;

    private final FileReaderMethods fileReaderMethods;

    public ProfileServant(String orderingPath, String zonePath) {
        this.fileReaderMethods = new FileReaderMethods(orderingPath, zonePath);
    }

    public ProfileServant(String orderingPath, String zonePath, Map<Integer, ArrayList<String>> zoneCache,
                          Map<String, RestaurantProfile> restaurantCache, Map<String, UserProfile> userCache) {
        this.zoneCache = zoneCache;
        this.restaurantCache = restaurantCache;
        this.userCache = userCache;
        this.fileReaderMethods = new FileReaderMethods(orderingPath, zonePath);
    } //ctor

    private void addLatency() {
        try {
            Thread.sleep(80);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getTimesOrdered(String restaurant_id) {
        addLatency();
        if (restaurantCache != null && restaurantCache.containsKey(restaurant_id)) {
            return restaurantCache.get(restaurant_id).total_times_ordered;
        }
        return fileReaderMethods.getTimesOrdered(restaurant_id);
    }

    @Override
    public int getTimesOrderedByUser(String user_id, String restaurant_id) {
        addLatency();
        if (userCache != null && userCache.containsKey(user_id)) {
            for (RestaurantCounter rc : userCache.get(user_id).restaurants) {
                if (restaurant_id.equals(rc.restaurant_id)) return rc.restaurant_timesOrdered;
            }
        }
        return fileReaderMethods.getTimesOrderedByUser(user_id, restaurant_id);
    }

    @Override
    public UserCounter[] getTopThreeUsersByRestaurant(String restaurant_id) {
        addLatency();
        if (restaurantCache != null && restaurantCache.containsKey(restaurant_id)) {
            return restaurantCache.get(restaurant_id).top_three_users;
        }

        return fileReaderMethods.getTopThreeUsersByRestaurant(restaurant_id);
    }

    @Override
    public FoodTypeCounter[] getTopThreeFoodTypesByZone(String zone_id) {
        addLatency();
        ArrayList<String> restaurant_ids = zoneCache.get(Integer.parseInt(zone_id));
        FoodTypeCounter[] foodTypeCounters = new FoodTypeCounter[3];
        if (restaurantCache != null) {
            for (String restaurant_id : restaurant_ids) {
                if (!restaurantCache.containsKey(restaurant_id)) continue;
                RestaurantProfile rp = restaurantCache.get(restaurant_id);
                for (FoodTypeCounter ftc : rp.foodTypes) {
                    if (!ServerCache.updateExistingFoodTypeCounter(foodTypeCounters, ftc))
                        ServerCache.addNewFoodTypeCounter(foodTypeCounters, ftc);
                }
            }
            ServerCache.sortAllFoodTypeCounters(foodTypeCounters);
            return foodTypeCounters;
        }

        return fileReaderMethods.getTopThreeFoodTypesByZone(zone_id);
    }

    @Override
    public UserProfile getUserProfile(String user_id) {
        addLatency();
        if (userCache != null && userCache.containsKey(user_id))
            return userCache.get(user_id);
        return null;
    }

    @Override
    public RestaurantProfile getRestaurantProfile(String restaurant_id) {
        addLatency();
        if (restaurantCache != null && restaurantCache.containsKey(restaurant_id))
            return restaurantCache.get(restaurant_id);
        return null;
    }
}
