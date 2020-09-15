package Client;

import ProfileService.Profiler;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class InputParser {

    ArrayList<String[]> commands;

    public InputParser() {
        this.commands = new ArrayList<>();
    } //ctor

    public int readInput(String path) {
        FileInputStream inputStream = null;
        Scanner sc = null;
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
        return 0;
    }

    public int issueCommandsToServer(Profiler clientRef) {

        //while(commands.isEmpty());
        for (String[] strArray: commands) {
            System.out.println(Arrays.toString(strArray));

            switch (strArray[0]){
                case "getTimesOrderedByUser":
                    break;
                case "getTimesOrdered":
                    break;
                case "getTopThreeUsersByRestaurant":
                    break;
                case "getTopThreeFoodTypesByZone":
                    break;
                default:
                    break;
            }
        }
        return 0;
    }
}
