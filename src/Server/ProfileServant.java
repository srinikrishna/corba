package Server;

import ProfileService.*;
import com.sun.tools.internal.xjc.reader.xmlschema.bindinfo.BIConversion;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProfileServant extends ProfilerPOA {
    private int RESTAURANT_ID_POS = 0;
    private int FOOD_TYPE_ID_POS = 1;
    private int USER_ID_POS = 2;
    private int TIMES_ORDERED_POS = 3;

    private int ZONE_POS = 2;

    @Override
    public int getTimesOrdered(String restaurant_id) {
        String path = "testfiles/test_ordering_profile.txt";

        FileInputStream inputStream = null;
        Scanner sc = null;
        int count = 0;
        try {
            inputStream = new FileInputStream(path);
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

    @Override
    public int getTimesOrderedByUser(String user_id, String restaurant_id) {
        String path = "testfiles/test_ordering_profile.txt";

        FileInputStream inputStream = null;
        Scanner sc = null;
        int count = 0;
        try {
            inputStream = new FileInputStream(path);
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

    @Override
    public UserCounter[] getTopThreeUsersByRestaurant(String restaurant_id) {
        String path = "testfiles/test_ordering_profile.txt";

        FileInputStream inputStream = null;
        Scanner sc = null;
        ArrayList<UserCounter> userCounterArrayList = new ArrayList<>();
        Map<String, Integer> userCounterMap = new HashMap<>();
        try {
            inputStream = new FileInputStream(path);
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
                /*
                boolean found = false;
                for (UserCounter us: userCounterArrayList) {
                    if (us.user_id.equals(user_id)) {
                        us.restaurant_timesOrdered += Integer.parseInt(rows[TIMES_ORDERED_POS]);
                        found = true;
                        break;
                    }
                }
                if(!found) {
                    userCounterArrayList.add(new UserCounter(user_id, Integer.parseInt(rows[TIMES_ORDERED_POS])));
                }*/
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

        /*
        userCounterArrayList.sort(Comparator.comparingInt(o -> o.restaurant_timesOrdered));
        return userCounterArrayList.subList((userCounterArrayList.size() - 3), userCounterArrayList.size()).
                toArray(new UserCounter[0]); */

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
        String orders = "testfiles/test_ordering_profile.txt";
        String zones = "train_in5020/restaurat_location_directory.txt";

        FileInputStream inputStream = null;
        Scanner sc = null;
        ArrayList<String> restaurants = new ArrayList<>();
        ArrayList<FoodTypeCounter> foodTypeCounters = new ArrayList<>();
        try {
            inputStream = new FileInputStream(zones);
            sc = new Scanner(inputStream, "UTF-8");

            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] rows = line.split("( |\t)+");
                if (rows[ZONE_POS].equals(zone_id)) {
                    restaurants.add(rows[RESTAURANT_ID_POS]);
                }
            }

            inputStream = new FileInputStream(orders);
            sc = new Scanner(inputStream, "UTF-8");

            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] rows = line.split("( |\t)+");

                if (!restaurants.contains(rows[RESTAURANT_ID_POS])) continue;
/*
                boolean found = false;
                for (FoodTypeCounter ftc: foodTypeCounters) {
                    if (foodTypeCounters.equals(user_id)) {
                        us.restaurant_timesOrdered += Integer.parseInt(rows[TIMES_ORDERED_POS]);
                        found = true;
                        break;
                    }
                }
                if(!found) {
                    userCounterArrayList.add(new UserCounter(user_id, Integer.parseInt(rows[TIMES_ORDERED_POS])));
                }
                foodTypeCounters.add(new FoodTypeCounter(rows[FOOD_TYPE_ID_POS], Integer.parseInt(rows[TIMES_ORDERED_POS])))
            }*/
                /*
                boolean found = false;

                for (FoodTypeCounter ftc: foodTypeCounters) {
                    if (ftc.foodType_id.equals(foodTypeId)) {
                        ftc.foodType_timesOrdered += Integer.parseInt(rows[TIMES_ORDERED_POS]);
                        found = true;
                    }
                }
                if (!found) {
                    foodTypeCounters.add(new FoodTypeCounter(foodTypeId, Integer.parseInt(rows[TIMES_ORDERED_POS])));
                }
*/
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
        foodTypeCounters.sort(Comparator.comparingInt(f -> f.foodType_timesOrdered));

        return foodTypeCounters.subList((foodTypeCounters.size() - 3), foodTypeCounters.size()).
                toArray(new FoodTypeCounter[0]);
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
