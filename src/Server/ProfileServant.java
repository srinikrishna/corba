package Server;

import ProfileService.*;
import com.sun.tools.internal.xjc.reader.xmlschema.bindinfo.BIConversion;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProfileServant extends ProfilerPOA {
    private final int RESTAURANT_ID_POS = 0;
    private final int FOOD_TYPE_ID_POS = 1;
    private final int USER_ID_POS = 2;
    private final int TIMES_ORDERED_POS = 3;

    private final int ZONE_POS = 1;

    private final String orderingPath = "train_in5020/restaurant_ordering_profile.txt";
    private final String zonesPath = "train_in5020/restaurant_location_directory.txt";

    private Map zoneToRestaurantProfilesMap = null;

    public ProfileServant() {
        init();
    } //ctor

    private int init() {
        Map<String, RestaurantProfile> popularRestaurants = new HashMap<>(1000);
        Map<String, UserProfile> popularUsers = new HashMap<>(1000);
        Map<String, Map<String, Integer>> popularUserForEachRestaurantMap = new HashMap<>();

        FileInputStream inputStream = null;
        Scanner sc = null;
        try {
            inputStream = new FileInputStream(orderingPath);
            sc = new Scanner(inputStream, "UTF-8");
            int minUserTimesOrderedCount = 0;

            // Init popular users section
            //while (sc.hasNextLine() )
            int minUserOrders = 0;
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] rows = line.split("( |\t)+");

                String restaurant_id = rows[RESTAURANT_ID_POS];
                String foodType_id = rows[FOOD_TYPE_ID_POS];
                String user_id = rows[USER_ID_POS];
                int timesOrdered = Integer.parseInt(rows[TIMES_ORDERED_POS]);

                if (popularUsers.containsKey(user_id)) {
                    UserProfile user = popularUsers.get(user_id);
                    boolean found = false;
                    for (RestaurantCounter rc : user.restaurants) {
                        if (restaurant_id.equals(rc.restaurant_id)) {
                            rc.restaurant_timesOrdered += timesOrdered;
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        RestaurantCounter[] newRc = new RestaurantCounter[user.restaurants.length + 1];
                        for(int i = 0; i < user.restaurants.length; i++) {
                            RestaurantCounter oldRc = user.restaurants[i];
                            newRc[i] = new RestaurantCounter(oldRc.restaurant_id, oldRc.restaurant_timesOrdered);
                        }
                        newRc[user.restaurants.length] = new RestaurantCounter(restaurant_id, timesOrdered);
                        user.restaurants = newRc;
                    }

                } else if (popularUsers.size() <= 1000 && !popularUsers.containsKey(user_id)) {
                    RestaurantCounter rc = new RestaurantCounter(restaurant_id, timesOrdered);
                    UserProfile user = new UserProfile(user_id, new RestaurantCounter[]{rc});
                    popularUsers.put(user_id, new UserProfile(user_id, new RestaurantCounter[]{rc}));
                } else if (timesOrdered > minUserOrders) {
                    // some code to change the lowest user
                }

                for (UserProfile user : popularUsers.values()) {
                    int currentUserTimesOrdered = 0;
                    for (RestaurantCounter rc : user.restaurants) {
                        currentUserTimesOrdered += rc.restaurant_timesOrdered;
                    }

                    if (currentUserTimesOrdered < minUserOrders) minUserOrders = currentUserTimesOrdered;
                }
            }

            if (sc.ioException() != null) {
                throw sc.ioException();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (sc != null) {
                sc.close();
            }
        }

        return 0;
    }

    @Override
    public int getTimesOrdered(String restaurant_id) {
        String path = "testfiles/test_ordering_profile.txt";

        FileInputStream inputStream = null;
        Scanner sc = null;
        int count = 0;
        try {
            TimeUnit.MILLISECONDS.sleep(80);

            inputStream = new FileInputStream(orderingPath);
            sc = new Scanner(inputStream, "UTF-8");
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (!line.contains(restaurant_id)) continue;
                count += Integer.parseInt(line.split("( |\t)+")[TIMES_ORDERED_POS]);
            }

            if (sc.ioException() != null) {
                throw sc.ioException();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (sc != null) {
                sc.close();
            }
        }

        return count;
    }

    @Override
    public int getTimesOrderedByUser(String user_id, String restaurant_id) {

        FileInputStream inputStream = null;
        Scanner sc = null;
        int count = 0;
        try {
            TimeUnit.MILLISECONDS.sleep(80);

            inputStream = new FileInputStream(orderingPath);
            sc = new Scanner(inputStream, "UTF-8");
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (!line.contains(restaurant_id) || !line.contains(user_id)) continue;
                count += Integer.parseInt(line.split("( |\t)+")[TIMES_ORDERED_POS]);
            }

            if (sc.ioException() != null) {
                throw sc.ioException();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (sc != null) {
                sc.close();
            }
        }

        return count;
    }

    @Override
    public UserCounter[] getTopThreeUsersByRestaurant(String restaurant_id) {

        FileInputStream inputStream = null;
        Scanner sc = null;
        Map<String, Integer> userCounterMap = new HashMap<>();
        try {
            TimeUnit.MILLISECONDS.sleep(80);

            inputStream = new FileInputStream(orderingPath);
            sc = new Scanner(inputStream, "UTF-8");

            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (!line.contains(restaurant_id)) continue;
                String[] rows = line.split("( |\t)+");
                String user_id = rows[USER_ID_POS];
                int timesOrdered = Integer.parseInt(rows[TIMES_ORDERED_POS]);
                if (userCounterMap.computeIfPresent(user_id, (k, v) -> v += timesOrdered) == null) {
                    userCounterMap.put(user_id, timesOrdered);
                }
            }

            if (sc.ioException() != null) {
                throw sc.ioException();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (sc != null) {
                sc.close();
            }
        }

        List<Map.Entry<String, Integer>> entryUserCounterList = new ArrayList<>(userCounterMap.entrySet());
        entryUserCounterList.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));

        UserCounter[] userCounters = new UserCounter[3];
        for (int i = 0, j = 2; i < 3; i++, j--) {
            Map.Entry<String, Integer> obj = entryUserCounterList.get(i);
            userCounters[j] = new UserCounter(obj.getKey(), obj.getValue());
        }
        return userCounters;
    }

    @Override
    public FoodTypeCounter[] getTopThreeFoodTypesByZone(String zone_id) {

        FileInputStream inputStream = null;
        Scanner sc = null;
        HashSet<String> restaurants = new HashSet<>();
        HashMap<String, Integer> foodTypeCountersMap = new HashMap<>();
        try {
            TimeUnit.MILLISECONDS.sleep(80);

            inputStream = new FileInputStream(zonesPath);
            sc = new Scanner(inputStream, "UTF-8");

            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] rows = line.split("( |\t)+");
                if (zone_id.equals(rows[ZONE_POS])) {
                    restaurants.add(rows[RESTAURANT_ID_POS]);
                }
            }

            inputStream = new FileInputStream(orderingPath);
            sc = new Scanner(inputStream, "UTF-8");

            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] rows = line.split("( |\t)+");

                if (!restaurants.contains(rows[RESTAURANT_ID_POS])) continue;
                int timesOrdered = Integer.parseInt(rows[TIMES_ORDERED_POS]);
                String foodType_id = rows[FOOD_TYPE_ID_POS];
                if (foodTypeCountersMap.computeIfPresent(foodType_id, (k, v) -> v += timesOrdered) == null) {
                    foodTypeCountersMap.put(foodType_id, timesOrdered);
                }

            }
            if (sc.ioException() != null) throw sc.ioException();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (sc != null) {
                sc.close();
            }
        }

        List<Map.Entry<String, Integer>> entryFoodTypeCountersList = new ArrayList<>(foodTypeCountersMap.entrySet());
        entryFoodTypeCountersList.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));

        FoodTypeCounter[] foodTypeCounters = new FoodTypeCounter[3];
        for (int i = 0, j = 2; i < 3; i++, j--) {
            //if (entryFoodTypeCountersList.size() - 1 < i) break;
            Map.Entry<String, Integer> obj = entryFoodTypeCountersList.get(i);
            foodTypeCounters[j] = new FoodTypeCounter(obj.getKey(), obj.getValue());
        }
        return foodTypeCounters;
    }

    @Override
    public UserProfile getUserProfile(String user_id) {
        return null;
    }

    @Override
    public RestaurantProfile getRestaurantProfile(String restaurant_id) {
        return null;
    }
}
