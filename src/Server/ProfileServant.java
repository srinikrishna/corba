package Server;

import ProfileService.*;
import com.sun.tools.internal.xjc.reader.xmlschema.bindinfo.BIConversion;
import sun.security.util.Cache;

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

//    private final String orderingPath = "train_in5020/restaurant_ordering_profile.txt";
//    private final String zonesPath = "train_in5020/restaurant_location_directory.txt";

    private final String orderingPath = "testfiles/test_ordering_profile.txt";
    private final String zonesPath = "testfiles/test_location_directory.txt";


    private Map zoneToRestaurantProfilesMap = null;

    private final Map<String, RestaurantProfile> restaurantCache = new HashMap<>(1000);
    private final Map<String, UserProfile> userCache = new HashMap<>(1000);


    public ProfileServant() {
        init();
    } //ctor

    private static <K, V extends Comparable<? super V>> List<Map.Entry<K, V>> findGreatest(Map<K, V> map, int n)
    {
        Comparator<? super Map.Entry<K, V>> comparator = new Comparator<Map.Entry<K, V>>() {
                    @Override
                    public int compare(Map.Entry<K, V> e0, Map.Entry<K, V> e1)
                    {
                        V v0 = e0.getValue();
                        V v1 = e1.getValue();
                        return v0.compareTo(v1);
                    }
                };
        PriorityQueue<Map.Entry<K, V>> highest = new PriorityQueue<Map.Entry<K,V>>(n, comparator);
        for (Map.Entry<K, V> entry : map.entrySet()) {
            highest.offer(entry);
            while (highest.size() > n)
            {
                highest.poll();
            }
        }

        List<Map.Entry<K, V>> result = new ArrayList<Map.Entry<K,V>>();
        while (highest.size() > 0) {
            result.add(highest.poll());
        }
        return result;
    }


    private int init() {
        FileInputStream inputStream = null;
        Scanner sc = null;
        try {
            inputStream = new FileInputStream(orderingPath);
            sc = new Scanner(inputStream, "UTF-8");

            // Init popular restaurants section
            Map<String, Integer> popRestaurants = new HashMap<>();
            Map<String, Integer> popUsers = new HashMap<>();
            long start = System.currentTimeMillis();
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] rows = line.split("( |\t)+");
                String restaurant_id = rows[RESTAURANT_ID_POS];
                String user_id = rows[USER_ID_POS];
                int count = Integer.parseInt(rows[TIMES_ORDERED_POS]);

                if (popRestaurants.computeIfPresent(restaurant_id, (k, v) -> v += count) == null) {
                    popRestaurants.put(restaurant_id, count);
                }
                if (popUsers.computeIfPresent(user_id, (k, v) -> v += count) == null) {
                    popUsers.put(user_id, count);
                }
            }
            long end = System.currentTimeMillis();
            long lapsed1 = end - start;
            System.out.println(lapsed1);
            Map<String, Integer> sortedRestaurants = findGreatest(popRestaurants, 1000).stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            Map<String, Integer> sortedUsers = findGreatest(popUsers, 1000).stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));


            long lapsed2 = System.currentTimeMillis() - end;
            System.out.println(lapsed2);

            inputStream = new FileInputStream(orderingPath);
            sc = new Scanner(inputStream, "UTF-8");

            long start2 = System.currentTimeMillis();
            while(sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] rows = line.split("( |\t)+");
                String restaurant_id = rows[RESTAURANT_ID_POS];
                String user_id = rows[USER_ID_POS];
                String foodType_id = rows[FOOD_TYPE_ID_POS];
                int count = Integer.parseInt(rows[TIMES_ORDERED_POS]);

                if (restaurantCache.containsKey(restaurant_id)) {
                    RestaurantProfile rp = restaurantCache.get(restaurant_id);
                    boolean foodTypeFound = false;
                    for (FoodTypeCounter ftc : rp.foodTypes) {
                        if (ftc != null && ftc.foodType_id.equals(foodType_id)) {
                            ftc.foodType_timesOrdered += count;
                            foodTypeFound = true;
                            break;
                        }
                    }
                    if (!foodTypeFound) {
                        if (rp.foodTypes[1] == null) {
                            rp.foodTypes[1] = new FoodTypeCounter(foodType_id, count);
                            sortFirstPairFoodTypeCounters(rp.foodTypes);
                        } else if (rp.foodTypes[2] == null) {
                            rp.foodTypes[2] = new FoodTypeCounter(foodType_id, count);
                            sortAllFoodTypeCounters(rp.foodTypes);
                        } else if (rp.foodTypes[0].foodType_timesOrdered < count) {
                            rp.foodTypes[0] = new FoodTypeCounter(foodType_id, count);
                            sortAllFoodTypeCounters(rp.foodTypes);
                        }
                    }

                    boolean userCounterFound = false;
                    for (UserCounter uc : rp.top_three_users) {
                        if (uc != null && uc.user_id.equals(user_id)) {
                            uc.restaurant_timesOrdered += count;
                            userCounterFound = true;
                            break;
                        }
                    }

                    if (!userCounterFound) {
                        if (rp.top_three_users[1] == null) {
                            rp.top_three_users[1] = new UserCounter(foodType_id, count);
                            sortFirstPairUserCounters(rp.top_three_users);
                        } else if (rp.top_three_users[2] == null) {
                            rp.top_three_users[2] = new UserCounter(foodType_id, count);
                            sortAllUserCounters(rp.top_three_users);
                        } else if (rp.top_three_users[0].restaurant_timesOrdered < count) {
                            rp.top_three_users[0] = new UserCounter(foodType_id, count);
                            sortAllUserCounters(rp.top_three_users);
                        }
                    }
                    //restaurantCache.remove(restaurant_id, rp);
                } else if (sortedRestaurants.containsKey(restaurant_id)) {
                    RestaurantProfile rp = new RestaurantProfile();
                    rp.restaurant_id = restaurant_id;
                    rp.total_times_ordered = sortedRestaurants.get(restaurant_id);
                    rp.foodTypes = new FoodTypeCounter[3];
                    rp.foodTypes[0] = new FoodTypeCounter(foodType_id, count);
                    rp.top_three_users = new UserCounter[3];
                    rp.top_three_users[0] = new UserCounter(user_id, count);
                    restaurantCache.put(restaurant_id, rp);
                    //System.out.println("check if replaced");
                }

                if (userCache.containsKey(user_id)) {
                    UserProfile up = userCache.get(user_id);
                    boolean restaurantFound = false;
                    for (RestaurantCounter rc : up.restaurants) {
                        if (rc.restaurant_id.equals(restaurant_id)) {
                            rc.restaurant_timesOrdered += count;
                            restaurantFound = true;
                            break;
                        }
                    }
                    if (!restaurantFound) {
                        RestaurantCounter rc = new RestaurantCounter(restaurant_id, count);
                        // Make a new RC array with size of the previous + 1 for the new element
                        RestaurantCounter[] rcArray = new RestaurantCounter[up.restaurants.length + 1];
                        // Copy all restaurants into the new array
                        System.arraycopy(up.restaurants, 0, rcArray, 0, up.restaurants.length);
                        // Populate the final element
                        rcArray[up.restaurants.length] = new RestaurantCounter(restaurant_id, count);
                        // Replace
                        up.restaurants = rcArray;
                    }
                } else if (sortedUsers.containsKey(user_id)) {
                    RestaurantCounter rc = new RestaurantCounter(restaurant_id, count);
                    UserProfile up = new UserProfile(user_id, new RestaurantCounter[]{rc});
                    userCache.put(user_id, up);
                }
            }
            long lapsed3 = System.currentTimeMillis() - start2;
            System.out.println(lapsed3);

            // Init popular users section
/*
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

                } else if (popularUsers.size() < 1000 && !popularUsers.containsKey(user_id)) {
                    RestaurantCounter rc = new RestaurantCounter(restaurant_id, timesOrdered);
                    UserProfile user = new UserProfile(user_id, new RestaurantCounter[]{rc});
                    popularUsers.put(user_id, new UserProfile(user_id, new RestaurantCounter[]{rc}));

                    if (popularUsers.size() == 1000) {
                        int currUserTimesOrdered = 0;
                        int currMinTimeOrdered = 4000000;
                        String currMinTimesOrderedUser_id = null;
                        for (UserProfile up : popularUsers.values()) {
                            for (RestaurantCounter rCounter : up.restaurants) {
                                currUserTimesOrdered += rCounter.restaurant_timesOrdered;
                            }

                            if (currUserTimesOrdered < currMinTimeOrdered) {
                                currMinTimeOrdered = currUserTimesOrdered;
                            }
                        }
                    }

                } else if (timesOrdered > minUserTimesOrderedCount ) {
                    // some code to change the lowest user

                }

                if (popularUsers.size() == 1000 && timesOrdered > minUserTimesOrderedCount) {
                    assert minUserOrders_id != null;
                    popularUsers.remove(minUserOrders_id);
                    // Add new object
                }
            }
 */

            if (sc.ioException() != null) {
                throw sc.ioException();
            }
        } catch (IOException e) {
            System.out.println("Init failed.");
            e.printStackTrace();
        } finally {
            System.out.println("Init final.");
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

    private static void sortFirstPairFoodTypeCounters(FoodTypeCounter[] ftc) {
        FoodTypeCounter temp = null;
        if (ftc[0].foodType_timesOrdered > ftc[1].foodType_timesOrdered) {
            temp = ftc[0];
            ftc[0] = ftc[1];
            ftc[1] = temp;
        }
    }

    private static void sortSecondPairFoodTypeCounters(FoodTypeCounter[] ftc) {
        FoodTypeCounter temp = null;
        if (ftc[1].foodType_timesOrdered > ftc[2].foodType_timesOrdered) {
            temp = ftc[1];
            ftc[1] = ftc[2];
            ftc[2] = temp;
        }
    }

    private static void sortAllFoodTypeCounters(FoodTypeCounter[] ftc) {
        sortFirstPairFoodTypeCounters(ftc);
        sortSecondPairFoodTypeCounters(ftc);
        sortFirstPairFoodTypeCounters(ftc);
    }

    private static void sortFirstPairUserCounters(UserCounter[] uc) {
        UserCounter temp = null;
        if (uc[0].restaurant_timesOrdered > uc[1].restaurant_timesOrdered) {
            temp = uc[0];
            uc[0] = uc[1];
            uc[1] = temp;
        }
    }

    private static void sortSecondPairUserCounters(UserCounter[] uc) {
        UserCounter temp = null;
        if (uc[1].restaurant_timesOrdered > uc[2].restaurant_timesOrdered) {
            temp = uc[1];
            uc[1] = uc[2];
            uc[2] = temp;
        }
    }

    private static void sortAllUserCounters(UserCounter[] uc) {
        sortFirstPairUserCounters(uc);
        sortSecondPairUserCounters(uc);
        sortFirstPairUserCounters(uc);
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
    public UserProfile getUserProfile(String user_id)
    {
        UserProfile user = userCache.get(user_id);

        if (user == null)
            user = new UserProfile(user_id, new RestaurantCounter[0]);

        return user;
    }

    @Override
    public RestaurantProfile getRestaurantProfile(String restaurant_id)
    {
        RestaurantProfile restaurant = restaurantCache.get(restaurant_id);

        if (restaurant == null)
            restaurant = new RestaurantProfile(restaurant_id, 0, new UserCounter[0], new FoodTypeCounter[0]);

        return restaurant;
    }
}
