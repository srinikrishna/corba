package Client;

import ProfileService.FoodTypeCounter;
import ProfileService.Profiler;
import ProfileService.UserCounter;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class FileHandler {

    ArrayList<String[]> commands;

    public FileHandler() {
    } //ctor

    public int runClientQueries(String inputs, Profiler clientRef) {
        ArrayList<String> results = issueCommandsToServer(readInput(inputs), clientRef);
        writeResultsToFile(results);
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

    private ArrayList<String> issueCommandsToServer(ArrayList<String[]> commands, Profiler clientRef) {

        ArrayList<String> results = new ArrayList<>();
        for (String[] strArray: commands) {
            System.out.println(Arrays.toString(strArray));

            switch (strArray[0]){
                case "getTimesOrdered": {
                    String restaurant_id = strArray[1];
                    long start = System.currentTimeMillis();
                    int orderCount = clientRef.getTimesOrdered(restaurant_id);
                    long lapsed = System.currentTimeMillis() - start;
                    results.add("Restaurant " + restaurant_id + " had " + orderCount + " orders. " + "(" +
                            lapsed + " ms)");
                    break;
                }
                case "getTimesOrderedByUser": {
                    String user_id = strArray[1];
                    String restaurant_id = strArray[2];
                    long start = System.currentTimeMillis();
                    int orderedByUserCount = clientRef.getTimesOrderedByUser(user_id, restaurant_id);
                    long lapsed = System.currentTimeMillis() - start;
                    results.add("Restaurant " + restaurant_id + " had " + orderedByUserCount + " from user " + user_id
                            + ". (" + lapsed + " ms)");
                    break;
                }
                case "getTopThreeUsersByRestaurant": {
                    String restaurant_id = strArray[1];
                    long start = System.currentTimeMillis();
                    UserCounter[] topThreeUsers = clientRef.getTopThreeUsersByRestaurant(restaurant_id);
                    long lapsed = System.currentTimeMillis() - start;
                    for (UserCounter us : topThreeUsers) {
                        results.add("User " + us.user_id + " ordered " + us.restaurant_timesOrdered + " times. (" +
                                lapsed + " ms)");
                    }
                    break;
                }
                case "getTopThreeFoodTypesByZone": {
                    String zone = strArray[1];
                    long start = System.currentTimeMillis();
                    FoodTypeCounter[] topThreeFoodsByZone = clientRef.getTopThreeFoodTypesByZone(zone);
                    long lapsed = System.currentTimeMillis() - start;
                    for (FoodTypeCounter ft : topThreeFoodsByZone) {
                        results.add("Food type " + ft.foodType_id + " was ordered " + ft.foodType_timesOrdered +
                                " times. (" + lapsed + " ms)");
                    }
                    break;
                }
                default:
                    break;
            }
        }
        return results;
    }

    private int writeResultsToFile(ArrayList<String> results) {
        String filename = "naive.txt";
        String path = "output_files/" + filename;
        try {
            FileWriter writer = new FileWriter(path);

            for(String r : results) {
                writer.write(r);
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0;
    }
}
