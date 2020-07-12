import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class AnalyzeMarkers {

    private static final int LINES_TO_SKIP = 7;
    private static final String DATA_MAP = "data_files" + File.separator;
    private static final String MARKERS_INPUT_FILE = System.getProperty("user.dir") + File.separator + DATA_MAP + "CvixLer-MarkerSubset-LG1.txt";
    private static final String OUTPUT_MARKERS = System.getProperty("user.dir") + File.separator + DATA_MAP + "output_markers.txt";

    public static void main(String[] args) {
        HashMap<String, ArrayList<Character>> markers = readFile(MARKERS_INPUT_FILE);
        HashMap<String, HashMap<String, Float>> analyzedMarkers = analyze_markers(markers);
        GeneticLinkageMap geneticLinkageMap = new GeneticLinkageMap(analyzedMarkers);
        geneticLinkageMap.findFurthestApart();
        geneticLinkageMap.createOtherMarkers();
        geneticLinkageMap.writeSortedMarkersToFile(OUTPUT_MARKERS);
    }

    public static HashMap<String, ArrayList<Character>> readFile(String filePath) {
        HashMap<String, ArrayList<Character>> markers = new HashMap<>();


        try {
            BufferedReader buf = new BufferedReader(new FileReader(filePath));
            String line;

            // skip first 7 lines, because they are not of interest
            for (int i = 0; i < LINES_TO_SKIP; i++) {
                buf.readLine();
            }
            String markerName = "";
            while ((line = buf.readLine()) != null) {
                if (line.startsWith("\n")) break;

                if (line.contains(";")) {
                    markerName = line.split(" ")[0];
                    markers.put(markerName, new ArrayList<>());
                } else if (line.startsWith(" ")) {
                    ArrayList<Character> bands = new ArrayList<>();

                    line.chars()
                            .filter(band -> Arrays.asList('a', 'b', '-').contains((char) band))
                            .forEach(band -> bands.add((char) band));

                    markers.get(markerName).addAll(bands);
                }
            }
        } catch (IOException ioe) {
            System.out.println("Wrong filepath!");
        }

        return markers;
    }

    public static Map<String, ArrayList<Character>> getSuitableMarkers(HashMap<String, ArrayList<Character>> markers) {
        return markers.entrySet()
                .stream()
                .filter(entry -> calculateChiSquared(entry.getValue()) <= 3.84f)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    }

    public static float calculateChiSquared(ArrayList<Character> bands) {

        float countBandA = getAmountCharacters(bands, 'a');
        float countBandB = getAmountCharacters(bands, 'b');

        float expectedBandA = bands.size() / 2.0f;
        float expectedBandB = bands.size() / 2.0f;

        return (float) ((Math.pow(countBandA - expectedBandA, 2) / expectedBandA) + (Math.pow(countBandB - expectedBandB, 2) / expectedBandB));
    }

    public static float getAmountCharacters(ArrayList<Character> bands, char letter) {
        return bands
                .stream()
                .filter(band -> band == letter)
                .count();
    }

    public static HashMap<String, HashMap<String, Float>> analyze_markers(HashMap<String, ArrayList<Character>> markers) {
        Map<String, ArrayList<Character>> suitableMarkers = getSuitableMarkers(markers);
        HashMap<String, HashMap<String, Float>> comparedMarkers = new HashMap<>();
        suitableMarkers.forEach((refKey, refValue) -> {
            comparedMarkers.put(refKey, new HashMap<>());
            suitableMarkers.forEach((curKey, curValue) -> {
                if (!refKey.equals(curKey)) {
                    float distance = calculateRF(refValue, curValue);
                    comparedMarkers.get(refKey).put(curKey, distance);
                }
            });
        });

        return comparedMarkers;
    }

    public static float calculateRF(ArrayList<Character> referenceBands, ArrayList<Character> currentBands) {
        float recombinantProgeny = 0.0f;
        float amountHyphens = 0.0f;
        List<String> recombinantBands = Arrays.asList("ab", "ba");
        for (int i = 0; i < referenceBands.size(); i++) {
            String combination = String.format("%s%s", referenceBands.get(i), currentBands.get(i));
            if (combination.contains("-")) amountHyphens++;
            else if (recombinantBands.contains(combination)) recombinantProgeny++;
        }
        float amountProgeny = referenceBands.size() - amountHyphens;

        return (recombinantProgeny / amountProgeny) * 100.0f;
    }
}
