package Client;

import ProfileService.FoodTypeCounter;
import ProfileService.Profiler;
import ProfileService.UserCounter;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class FileHandler implements IFileHandler {

    ArrayList<String> results;
    ArrayList<String> topUsers;
    ArrayList<String> topFoods;
    boolean naive;
    boolean caching;

    public FileHandler(boolean naive, boolean caching) {
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

    private void issueCommandsToServer(ArrayList<String[]> commands, Profiler clientRef) {

        for (String[] strArray: commands) {
            System.out.println(Arrays.toString(strArray));

            switch (strArray[0]){
                case "getTimesOrdered": {
                    String restaurant_id = strArray[1];
                    long start = System.currentTimeMillis();
                    int orderCount = clientRef.getTimesOrdered(restaurant_id);
                    long lapsed = System.currentTimeMillis() - start;
                    results.add("Restaurant " + restaurant_id + " had " + orderCount + " orders. " + "(" +
                            lapsed + " ms)\n");
                    break;
                }
                case "getTimesOrderedByUser": {
                    String user_id = strArray[1];
                    String restaurant_id = strArray[2];
                    long start = System.currentTimeMillis();
                    int orderedByUserCount = clientRef.getTimesOrderedByUser(user_id, restaurant_id);
                    long lapsed = System.currentTimeMillis() - start;
                    results.add("Restaurant " + restaurant_id + " had " + orderedByUserCount + " from user " + user_id
                            + ". (" + lapsed + " ms)\n");
                    break;
                }
                case "getTopThreeUsersByRestaurant": {
                    String restaurant_id = strArray[1];
                    long start = System.currentTimeMillis();
                    UserCounter[] topThreeUsers = clientRef.getTopThreeUsersByRestaurant(restaurant_id);
                    long lapsed = System.currentTimeMillis() - start;
                    if (caching) {
                        for (UserCounter us : topThreeUsers) {
                            topUsers.add("User " + us.user_id + " ordered " + us.restaurant_timesOrdered + " times. (" +
                                    lapsed + " ms)\n");
                        }
                    }
                    break;
                }
                case "getTopThreeFoodTypesByZone": {
                    String zone = strArray[1];
                    long start = System.currentTimeMillis();
                    FoodTypeCounter[] topThreeFoodsByZone = clientRef.getTopThreeFoodTypesByZone(zone);
                    long lapsed = System.currentTimeMillis() - start;
                    if (caching) {
                        for (FoodTypeCounter ft : topThreeFoodsByZone) {
                            topFoods.add("Food type " + ft.foodType_id + " was ordered " + ft.foodType_timesOrdered +
                                    " times. (" + lapsed + " ms)\n");
                        }
                    }
                    break;
                }
                default:
                    break;
            }
        }
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
            FileWriter writer = new FileWriter(path);
            for(String r : results) writer.write(r);

            if (caching) {
                String topUsersPath = "output_files/topuser.txt";
                String topFoodsPath = "output_files/topfoods.txt";

                writer = new FileWriter(topUsersPath);
                for (String user : topUsers) writer.write(user);

                writer = new FileWriter(topFoodsPath);
                for (String food : topFoods) writer.write(food);
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
