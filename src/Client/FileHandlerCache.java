package Client;

import ProfileService.*;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class FileHandlerCache implements IFileHandler {

    ArrayList<String[]> commands;

    private final Map<String, RestaurantProfile> _restaurantCache = new HashMap<>();
    private final Map<String, UserProfile> _userCache = new HashMap<>();

    ArrayList<String> results;
    ArrayList<String> topUsers;
    ArrayList<String> topFoods;
    boolean naive;
    boolean caching;

    public FileHandlerCache(boolean naive, boolean caching) {
        this.results = new ArrayList<>();
        this.topUsers = new ArrayList<>();
        this.topFoods = new ArrayList<>();
        this.naive = naive;
        this.caching = caching;
    } //ctor

    public int runClientQueries(String inputs, Profiler clientRef) {
        issueCommandsToServer(readInput(inputs), clientRef);
        writeResultsToFile();
        return 0;
    }

    private ArrayList<String[]> readInput(String path) {
        FileInputStream inputStream = null;
        Scanner sc = null;
        ArrayList<String[]> commands = new ArrayList<>();
        try {
            inputStream = new FileInputStream(path);
            sc = new Scanner(inputStream, "UTF-8");

            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                commands.add(line.split("( |\t)+"));
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
        return commands;
    }

    private void issueCommandsToServer(ArrayList<String[]> commands, Profiler clientRef)
    {
        ArrayList<String> combinedResults = new ArrayList<>();
        for (String[] strArray: commands) {
            System.out.println(Arrays.toString(strArray));

            String[] temp = new String[0];

            long start = System.currentTimeMillis();

            switch (strArray[0]) {
                case "getTimesOrdered": {
                    temp = getTimesOrdered(strArray[1], clientRef);
                    long lapsed = System.currentTimeMillis() - start;
                    results.add(temp[0] + "(" + lapsed + " ms)\n");
                    break;
                }
                case "getTimesOrderedByUser": {
                    temp = getTimesOrderedByUser(strArray[1], strArray[2], clientRef);
                    long lapsed = System.currentTimeMillis() - start;
                    results.add(temp[0] + "(" + lapsed + " ms)\n");
                    break;
                }
                case "getTopThreeUsersByRestaurant": {
                    temp = getTopThreeUsersByRestaurant(strArray[1], clientRef);
                    long lapsed = System.currentTimeMillis() - start;
                    for (String str: temp)
                        topUsers.add(str + "(" + lapsed + " ms)\n");
                    break;
                }
                case "getTopThreeFoodTypesByZone": {
                    temp = getTopThreeFoodTypesByZone(strArray[1], clientRef);
                    long lapsed = System.currentTimeMillis() - start;
                    for (String str: temp)
                        topFoods.add(str + "(" + lapsed + " ms)\n");
                    break;
                }
                default: {
                    break;
                }
            }
        }
    }

    private String[] getTimesOrdered(String restaurant_id, Profiler clientRef)
    {
        RestaurantProfile restaurant = _restaurantCache.get(restaurant_id);

        // client cache miss
        if (restaurant == null)
            restaurant = clientRef.getRestaurantProfile(restaurant_id);

        // server cache miss
        if (restaurant == null) {
            int timesOrdered = clientRef.getTimesOrdered(restaurant_id);
            restaurant = new RestaurantProfile(restaurant_id, timesOrdered, null, null);
            _restaurantCache.put(restaurant_id, restaurant);
        }

        return new String[] {"Restaurant " + restaurant_id + " had " + restaurant.total_times_ordered + " orders."};
    }

    private String[] getTimesOrderedByUser(String user_id, String restaurant_id, Profiler clientRef)
    {
        RestaurantCounter counter = null;
        UserProfile user = _userCache.get(user_id);

        // client cache miss
        if (user == null)
            user = clientRef.getUserProfile(user_id);

        // server cache miss
        if (user.restaurants.length == 0) {
            int timesOrdered = clientRef.getTimesOrderedByUser(user_id, restaurant_id);
            counter = new RestaurantCounter(restaurant_id, timesOrdered);
            user = new UserProfile(user_id, new RestaurantCounter[]{ counter });
            _userCache.put(user_id, user);
        } else {
            for (RestaurantCounter rc: user.restaurants) {
                if (restaurant_id.compareTo(rc.restaurant_id) == 0) {
                    counter = rc;
                    break;
                }
            }

            // restaurant not found in user profile - append it to the list
            if (counter == null) {
                int timesOrderedByUser = clientRef.getTimesOrderedByUser(user_id, restaurant_id);
                counter = new RestaurantCounter(restaurant_id, timesOrderedByUser);
                RestaurantCounter[] counterArray = new RestaurantCounter[user.restaurants.length + 1];

                int i;
                for (i = 0; i < user.restaurants.length; i++) {
                    counterArray[i] = user.restaurants[i];
                }
                counterArray[i] = counter;

                user.restaurants = counterArray;
            }
        }

        return new String[] {"Restaurant " + restaurant_id + " had " + counter.restaurant_timesOrdered + " from user "
                + user_id + "."};
    }

    private String[] getTopThreeUsersByRestaurant(String restaurant_id, Profiler clientRef)
    {
        String[] users_str = new String[3];

        RestaurantProfile restaurant = _restaurantCache.get(restaurant_id);

        if (restaurant == null) {
            restaurant = clientRef.getRestaurantProfile(restaurant_id);
            _restaurantCache.put(restaurant_id, restaurant);
        }

        if (restaurant.top_three_users == null)
            restaurant.top_three_users = clientRef.getTopThreeUsersByRestaurant(restaurant_id);

        for (int i = 0; i < restaurant.top_three_users.length; i++) {
            users_str[i] = "User " + restaurant.top_three_users[i].user_id + " ordered "
                    + restaurant.top_three_users[i].restaurant_timesOrdered + " times.";
        }

        return users_str;
    }

    private String[] getTopThreeFoodTypesByZone(String zone, Profiler clientRef)
    {
        String[] food_str = new String[3];

        FoodTypeCounter[] topThreeFoodsByZone = clientRef.getTopThreeFoodTypesByZone(zone);

        for (int i = 0; i < topThreeFoodsByZone.length; i++) {
            food_str[i] = "Food type " + topThreeFoodsByZone[i].foodType_id + " was ordered "
                    + topThreeFoodsByZone[i].foodType_timesOrdered + " times.";
        }

        return food_str;
    }

    private int writeResultsToFile() {
        String filename;
        if (naive) {
            filename = "naive.txt";
        } else if (caching) {
            filename = "clientside_caching_on.txt";
        } else {
            filename = "clientside_caching_off.txt";
        }

        String path = "output_files/" + filename;
        try {
            FileWriter writer1 = new FileWriter(path);
            for (String r : results) writer1.write(r);

            if (caching) {
                String topUsersPath = "output_files/topuser.txt";
                String topFoodsPath = "output_files/topfoods.txt";

                FileWriter writer2 = new FileWriter(topUsersPath);
                for (String user : topUsers) writer2.write(user);

                FileWriter writer3 = new FileWriter(topFoodsPath);
                for (String food : topFoods) writer3.write(food);
                writer2.close();
                writer3.close();
            }

            writer1.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0;
    }
}
