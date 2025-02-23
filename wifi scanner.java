import java.io.*;
import java.util.*;

public class WifiScanner {
    private static final String INTERFACE = "wlan0";
    private static final String MONITOR_INTERFACE = INTERFACE + "mon";
    private static final String OUTPUT_FILE = "wifi_scan_results.txt";
    
    public static void main(String[] args) {
        try {
            enableMonitorMode();
            scanNetworks();
            disableMonitorMode();
            System.out.println("Scan complete. Results saved to " + OUTPUT_FILE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void enableMonitorMode() throws IOException, InterruptedException {
        System.out.println("Enabling monitor mode...");
        executeCommand("sudo airmon-ng start " + INTERFACE);
    }

    private static void scanNetworks() throws IOException, InterruptedException {
        System.out.println("Scanning for Wi-Fi networks...");
        Process scan = executeCommand("sudo timeout 10 airodump-ng " + MONITOR_INTERFACE);
        BufferedReader reader = new BufferedReader(new InputStreamReader(scan.getInputStream()));
        List<String> networks = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains("WPA") || line.contains("WEP")) {
                networks.add(line);
            }
        }
        saveResults(networks);
    }

    private static void disableMonitorMode() throws IOException, InterruptedException {
        System.out.println("Disabling monitor mode...");
        executeCommand("sudo airmon-ng stop " + MONITOR_INTERFACE);
    }

    private static Process executeCommand(String command) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(command);
        process.waitFor();
        return process;
    }

    private static void saveResults(List<String> networks) throws IOException {
        try (PrintWriter writer = new PrintWriter(new File(OUTPUT_FILE))) {
            for (String network : networks) {
                writer.println(network);
            }
        }
    }
}
