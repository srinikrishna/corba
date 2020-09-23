package Server;

import ProfileService.FoodTypeCounter;
import ProfileService.RestaurantProfile;
import ProfileService.UserCounter;
import ProfileService.UserProfile;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class FileReaderMethods {

    private FileInputStream inputStream = null;
    private Scanner sc = null;
    String orderingPath = null;
    String zonePath = null;

    private final int RESTAURANT_ID_POS = 0;
    private final int FOOD_TYPE_ID_POS = 1, ZONE_POS = 1;
    private final int USER_ID_POS = 2;
    private final int TIMES_ORDERED_POS = 3;

    public FileReaderMethods(String orderingPath, String zonePath) {
        this.orderingPath = orderingPath;
        this.zonePath = zonePath;
    }

    public int getTimesOrdered(String restaurant_id) {
        int count = 0;
        try {
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

        return count;
    }

    public int getTimesOrderedByUser(String user_id, String restaurant_id) {

        int count = 0;
        try {
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

        return count;
    }

    public UserCounter[] getTopThreeUsersByRestaurant(String restaurant_id) {

        Map<String, Integer> userCounterMap = new HashMap<>();
        try {
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

        List<Map.Entry<String, Integer>> entryUserCounterList = new ArrayList<>(userCounterMap.entrySet());
        entryUserCounterList.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));

        UserCounter[] userCounters = new UserCounter[3];
        for (int i = 0, j = 2; i < 3; i++, j--) {
            Map.Entry<String, Integer> obj = entryUserCounterList.get(i);
            userCounters[j] = new UserCounter(obj.getKey(), obj.getValue());
        }
        return userCounters;
    }

    public FoodTypeCounter[] getTopThreeFoodTypesByZone(String zone_id) {

        HashSet<String> restaurants = new HashSet<>();
        HashMap<String, Integer> foodTypeCountersMap = new HashMap<>();
        try {
            inputStream = new FileInputStream(zonePath);
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

    public UserProfile getUserProfile(String user_id) {
        return null;
    }

    public RestaurantProfile getRestaurantProfile(String restaurant_id) {
        return null;
    }
}
