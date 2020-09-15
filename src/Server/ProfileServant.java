package Server;

import ProfileService.FoodTypeCounter;
import ProfileService.ProfilerPOA;
import ProfileService.UserCounter;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;

public class ProfileServant extends ProfilerPOA {
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
                count += Integer.parseInt(line.split("( |\t)+")[3]);
            }
            // note that Scanner suppresses exceptions
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
