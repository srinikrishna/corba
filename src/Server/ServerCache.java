package Server;

import ProfileService.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ServerCache {

    private static final int RESTAURANT_ID_POS = 0;
    private static final int FOOD_TYPE_ID_POS = 1, ZONE_POS = 1;
    private static final int USER_ID_POS = 2;
    private static final int TIMES_ORDERED_POS = 3;

    public static int loadCache(String orderingPath, String zonePath, Map<Integer, ArrayList<String>> zoneCache,
                                Map<String, RestaurantProfile> restaurantCache, Map<String, UserProfile> userCache) {

        loadZoneCache(zoneCache, zonePath);
        System.out.println("Zone Cache Loaded...");
        System.out.println("Loading RestaurantProfiles and UserProfiles...");

        FileInputStream inputStream = null;
        Scanner sc = null;
        try {
            inputStream = new FileInputStream(orderingPath);
            sc = new Scanner(inputStream, "UTF-8");

            long start = System.currentTimeMillis();
            Map<String, Integer> sortedRestaurants;
            Map<String, Integer> sortedUsers;
            {
                Map<String, Integer> popRestaurants = new HashMap<>();
                Map<String, Integer> popUsers = new HashMap<>();
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
                System.out.println("First read through the restaurant_ordering_profile.txt took " + lapsed1 + " ms");
                sortedRestaurants = findGreatest(popRestaurants, 1000).stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                sortedUsers = findGreatest(popUsers, 1000).stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            }
            /*
            long end = System.currentTimeMillis();
            long lapsed2 = System.currentTimeMillis() - end;
            System.out.println(lapsed2);
            long start2 = System.currentTimeMillis();
            */

            inputStream = new FileInputStream(orderingPath);
            sc = new Scanner(inputStream, "UTF-8");

            while(sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] rows = line.split("( |\t)+");
                String restaurant_id = rows[RESTAURANT_ID_POS];
                String user_id = rows[USER_ID_POS];
                String foodType_id = rows[FOOD_TYPE_ID_POS];
                int count = Integer.parseInt(rows[TIMES_ORDERED_POS]);

                if (restaurantCache.containsKey(restaurant_id)) {
                    RestaurantProfile rp = restaurantCache.get(restaurant_id);

                    if (!updateExistingFoodTypeCounter(rp.foodTypes, foodType_id, count))
                        addNewFoodTypeCounter(rp.foodTypes, foodType_id, count);

                    if (!updateExistingUserCounter(rp.top_three_users, user_id, count))
                        addNewUserCounter(rp.top_three_users, user_id, count);

                } else if (sortedRestaurants.containsKey(restaurant_id)) {
                    RestaurantProfile rp = createNewRestaurantProfile(sortedRestaurants,
                            restaurant_id, foodType_id, user_id, count);

                    restaurantCache.put(restaurant_id, rp);
                }
                if (userCache.containsKey(user_id)) {
                    UserProfile up = userCache.get(user_id);

                    if (!updateExistingRestaurantCounter (up.restaurants, restaurant_id, count))
                        up.restaurants = addNewRestaurantCounter(up.restaurants, restaurant_id, count);;
                } else if (sortedUsers.containsKey(user_id))
                    userCache.put(user_id, createNewUserProfile(user_id, restaurant_id, count));
            }
            /*
            long lapsed3 = System.currentTimeMillis() - start2;
            System.out.println(lapsed3); */
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

    private static Map<Integer, ArrayList<String>> loadZoneCache(Map<Integer, ArrayList<String>> zonesToRestaurants,
                                                                 String zonePath) {
        FileInputStream inputStream = null;
        Scanner sc = null;

        try {
            inputStream = new FileInputStream(zonePath);
            sc = new Scanner(inputStream, "UTF-8");

            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] rows = line.split("( |\t)+");
                String restaurant_id = rows[RESTAURANT_ID_POS];
                int zone_id = Integer.parseInt(rows[ZONE_POS]);
                // int zone_id_index = zone_id--;

                if (!zonesToRestaurants.computeIfAbsent(zone_id, k -> new ArrayList<String>()).add(restaurant_id)) {
                    zonesToRestaurants.get(zone_id).add(restaurant_id);
                }
/*
                if (zonesToRestaurants.get(zone_id).isEmpty() || zonesToRestaurants.get(zone_id) == null) {
                    ArrayList<String> restaurants = new ArrayList<>();
                    if (restaurants.add(restaurant_id)) zonesToRestaurants.add(zone_id, restaurants);
                } else {
                    zonesToRestaurants.get(zone_id).add(restaurant_id);
                } */
                if (sc.ioException() != null) {
                    throw sc.ioException();
                }
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
        return zonesToRestaurants;
    }

    private static boolean updateExistingFoodTypeCounter (FoodTypeCounter[] foodTypes, String foodType_id, int count) {
        boolean foodTypeFound = false;
        for (FoodTypeCounter ftc : foodTypes) {
            if (ftc != null && ftc.foodType_id.equals(foodType_id)) {
                ftc.foodType_timesOrdered += count;
                foodTypeFound = true;
                break;
            }
        }

        return foodTypeFound;
    }

    public static boolean updateExistingFoodTypeCounter (FoodTypeCounter[] foodTypes, FoodTypeCounter ftc_input) {
        boolean foodTypeFound = false;
        for (FoodTypeCounter ftc : foodTypes) {
            if (ftc != null && ftc.foodType_id.equals(ftc_input.foodType_id)) {
                ftc.foodType_timesOrdered += ftc_input.foodType_timesOrdered;
                foodTypeFound = true;
                break;
            }
        }

        return foodTypeFound;
    }


    private static void addNewFoodTypeCounter (FoodTypeCounter[] foodTypes, String foodType_id, int count) {
        if (foodTypes[1] == null) {
            foodTypes[1] = new FoodTypeCounter(foodType_id, count);
            sortFirstPairFoodTypeCounters(foodTypes);
        } else if (foodTypes[2] == null) {
            foodTypes[2] = new FoodTypeCounter(foodType_id, count);
            sortAllFoodTypeCounters(foodTypes);
        } else if (foodTypes[0].foodType_timesOrdered < count) {
            foodTypes[0] = new FoodTypeCounter(foodType_id, count);
            sortAllFoodTypeCounters(foodTypes);
        }
    }

    public static void addNewFoodTypeCounter (FoodTypeCounter[] foodTypes, FoodTypeCounter ftc) {
        if (foodTypes[0] == null) {
            foodTypes[0] = ftc;
        } else if (foodTypes[1] == null) {
            foodTypes[1] = ftc;
            sortFirstPairFoodTypeCounters(foodTypes);
        } else if (foodTypes[2] == null) {
            foodTypes[2] = ftc;
            sortAllFoodTypeCounters(foodTypes);
        } else if (foodTypes[0].foodType_timesOrdered < ftc.foodType_timesOrdered) {
            foodTypes[0] = ftc;
            sortAllFoodTypeCounters(foodTypes);
        }
    }

    private static boolean updateExistingUserCounter (UserCounter[] userCounter, String user_id, int count) {
        boolean userCounterFound = false;
        for (UserCounter uc : userCounter) {
            if (uc != null && uc.user_id.equals(user_id)) {
                uc.restaurant_timesOrdered += count;
                userCounterFound = true;
                break;
            }
        }
        return userCounterFound;
    }

    private static void addNewUserCounter (UserCounter[] userCounters, String user_id, int count) {
        if (userCounters[1] == null) {
            userCounters[1] = new UserCounter(user_id, count);
            sortFirstPairUserCounters(userCounters);
        } else if (userCounters[2] == null) {
            userCounters[2] = new UserCounter(user_id, count);
            sortAllUserCounters(userCounters);
        } else if (userCounters[0].restaurant_timesOrdered < count) {
            userCounters[0] = new UserCounter(user_id, count);
            sortAllUserCounters(userCounters);
        } // else return false;?
    }

    private static RestaurantProfile createNewRestaurantProfile (Map<String, Integer> sortedRestaurants,
                                                                 String restaurant_id, String foodType_id,
                                                                 String user_id, int count) {
        RestaurantProfile rp = new RestaurantProfile();
        rp.restaurant_id = restaurant_id;
        rp.total_times_ordered = sortedRestaurants.get(restaurant_id);
        rp.foodTypes = new FoodTypeCounter[3];
        rp.foodTypes[0] = new FoodTypeCounter(foodType_id, count);
        rp.top_three_users = new UserCounter[3];
        rp.top_three_users[0] = new UserCounter(user_id, count);
        return rp;
    }

    private static boolean updateExistingRestaurantCounter (RestaurantCounter[] restaurants, String restaurant_id,
                                                            int count) {
        boolean restaurantFound = false;
        for (RestaurantCounter rc : restaurants) {
            if (rc.restaurant_id.equals(restaurant_id)) {
                rc.restaurant_timesOrdered += count;
                restaurantFound = true;
                break;
            }
        }
        return restaurantFound;
    }

    private static RestaurantCounter[] addNewRestaurantCounter (RestaurantCounter[] restaurants, String restaurant_id, int count) {
        // RestaurantCounter[] addNewRestaurantCounter (RestaurantCounters[] restaurantCounter, RestaurantCounter restaurant)
        RestaurantCounter rc = new RestaurantCounter(restaurant_id, count);
        // Make a new RC array with size of the previous + 1 for the new element
        RestaurantCounter[] rcArray = new RestaurantCounter[restaurants.length + 1];
        // Copy all restaurants into the new array
        System.arraycopy(restaurants, 0, rcArray, 0, restaurants.length);
        // Populate the final element
        rcArray[restaurants.length] = new RestaurantCounter(restaurant_id, count);
        return rcArray;
    }

    private static UserProfile createNewUserProfile(String user_id, String restaurant_id, int count) {
        RestaurantCounter rc = new RestaurantCounter(restaurant_id, count);
        return new UserProfile(user_id, new RestaurantCounter[]{rc});
    }

    private static void sortFirstPairFoodTypeCounters(FoodTypeCounter[] ftc) {
        if (ftc[0].foodType_timesOrdered > ftc[1].foodType_timesOrdered) {
            FoodTypeCounter temp = ftc[0];
            ftc[0] = ftc[1];
            ftc[1] = temp;
        }
    }

    private static void sortSecondPairFoodTypeCounters(FoodTypeCounter[] ftc) {
        if (ftc[1].foodType_timesOrdered > ftc[2].foodType_timesOrdered) {
            FoodTypeCounter temp = ftc[1];
            ftc[1] = ftc[2];
            ftc[2] = temp;
        }
    }

    public static void sortAllFoodTypeCounters(FoodTypeCounter[] ftc) {
        sortFirstPairFoodTypeCounters(ftc);
        sortSecondPairFoodTypeCounters(ftc);
        sortFirstPairFoodTypeCounters(ftc);
    }

    private static void sortFirstPairUserCounters(UserCounter[] uc) {
        if (uc[0].restaurant_timesOrdered > uc[1].restaurant_timesOrdered) {
            UserCounter temp = uc[0];
            uc[0] = uc[1];
            uc[1] = temp;
        }
    }

    private static void sortSecondPairUserCounters(UserCounter[] uc) {
        if (uc[1].restaurant_timesOrdered > uc[2].restaurant_timesOrdered) {
            UserCounter temp = uc[1];
            uc[1] = uc[2];
            uc[2] = temp;
        }
    }

    private static void sortAllUserCounters(UserCounter[] uc) {
        sortFirstPairUserCounters(uc);
        sortSecondPairUserCounters(uc);
        sortFirstPairUserCounters(uc);
    }

    private static <K, V extends Comparable<? super V>> List<Map.Entry<K, V>> findGreatest(Map<K, V> map, int n) {
        Comparator<? super Map.Entry<K, V>> comparator = (Comparator<Map.Entry<K, V>>) (e0, e1) -> {
            V v0 = e0.getValue();
            V v1 = e1.getValue();
            return v0.compareTo(v1);
        };
        PriorityQueue<Map.Entry<K, V>> highest = new PriorityQueue<>(n, comparator);
        for (Map.Entry<K, V> entry : map.entrySet()) {
            highest.offer(entry);
            while (highest.size() > n) {
                highest.poll();
            }
        }

        List<Map.Entry<K, V>> result = new ArrayList<>();
        while (highest.size() > 0) {
            result.add(highest.poll());
        }
        return result;
    }

}
