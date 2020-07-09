import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

public class AnalyzeMarkers
{

    private static final int LINES_TO_SKIP = 7;
    private static final String MARKERS_INPUT_FILE = System.getProperty("user.dir") + File.separator + "CvixLer-MarkerSubset-LG1.txt";
    private static final String OUTPUT_MARKERS = System.getProperty("user.dir") + File.separator + "output_markers.txt";

    public static void main(String[] args)
    {
        HashMap<String, ArrayList<Character>> markers = readFile(MARKERS_INPUT_FILE);
        HashMap<String, HashMap<String, Float>> analyzedMarkers = analyze_markers(markers);
        GeneticLinkageMap geneticLinkageMap = new GeneticLinkageMap(analyzedMarkers);
        geneticLinkageMap.findFurthestApart();
        geneticLinkageMap.createOtherMarkers();
        geneticLinkageMap.writeSortedMarkersToFile(OUTPUT_MARKERS);
    }

    public static HashMap<String, ArrayList<Character>> readFile(String filePath)
    {
        HashMap<String, ArrayList<Character>> markers = new HashMap<>();


        try
        {
            BufferedReader buf = new BufferedReader(new FileReader(filePath));
            String line;

            // skip first 7 lines, because they are not of interest
            for (int i = 0; i < LINES_TO_SKIP; i++)
            {
                buf.readLine();
            }
            String markerName = "";
            while ((line = buf.readLine()) != null)
            {
                if (line.startsWith("\n")) break;

                if (line.contains(";"))
                {
                    markerName = line.split(" ")[0];
                    markers.put(markerName, new ArrayList<>());
                }
                else if (line.startsWith(" "))
                {
                    ArrayList<Character> bands = new ArrayList<>();
                    char[] chars = line.toCharArray();
                    for (char band : chars)
                    {
                        if (Arrays.asList('a', 'b', '-').contains(band)) bands.add(band);
                    }
                    markers.get(markerName).addAll(bands);
                }
            }
        }
        catch (IOException ioe)
        {
            System.out.println("Wrong filepath!");
        }

        return markers;
    }

    public static HashMap<String, ArrayList<Character>> getSuitableMarkers(HashMap<String, ArrayList<Character>> markers)
    {
        HashMap<String, ArrayList<Character>> suitableMarkers = new HashMap<>();
        for (Map.Entry<String, ArrayList<Character>> entry : markers.entrySet())
        {
            float chiSquared = calculateChiSquared(entry.getValue());
            if (chiSquared <= 3.84) suitableMarkers.put(entry.getKey(), entry.getValue());
        }
        return suitableMarkers;
    }

    public static float calculateChiSquared(ArrayList<Character> bands)
    {
        float countBandA = getAmountCharacters(bands, 'a');
        float countBandB = getAmountCharacters(bands, 'b');

        float expectedBandA = bands.size() / 2.0f;
        float expectedBandB = bands.size() / 2.0f;

        return (float) ((Math.pow(Math.abs(countBandA - expectedBandA), 2) / expectedBandA) + (Math.pow(Math.abs(countBandB - expectedBandB), 2) / expectedBandB));
    }

    public static float getAmountCharacters(ArrayList<Character> bands, char letter)
    {
        float count = 0.0f;
        for (char band : bands)
        {
            if (band == letter) count++;
        }
        return count;
    }

    public static HashMap<String, HashMap<String, Float>> analyze_markers(HashMap<String, ArrayList<Character>> markers)
    {
        HashMap<String, ArrayList<Character>> suitableMarkers = getSuitableMarkers(markers);

        HashMap<String, HashMap<String, Float>> comparedMarkers = new HashMap<>();
        for (Map.Entry<String, ArrayList<Character>> referenceEntry : suitableMarkers.entrySet())
        {
            comparedMarkers.put(referenceEntry.getKey(), new HashMap<>());
            for (Map.Entry<String, ArrayList<Character>> currentEntry : suitableMarkers.entrySet())
            {
                if (!referenceEntry.getKey().equals(currentEntry.getKey()))
                {
                    float distance = calculateRF(referenceEntry.getValue(), currentEntry.getValue());

                    comparedMarkers.get(referenceEntry.getKey()).put(currentEntry.getKey(), distance);
                }
            }
        }

        return comparedMarkers;
    }

    public static float calculateRF(ArrayList<Character> referenceBands, ArrayList<Character> currentBands)
    {
        float recombinantProgeny = 0.0f;
        float amountHyphens = 0.0f;
        List<String> recombinantBands = Arrays.asList("ab", "ba");
        for (int i = 0; i < referenceBands.size(); i++)
        {
            String combination = String.format("%s%s", referenceBands.get(i), currentBands.get(i));
            if (combination.contains("-")) amountHyphens++;
            else if (recombinantBands.contains(combination)) recombinantProgeny++;
        }
        float amountProgeny = referenceBands.size() - amountHyphens;


        return (recombinantProgeny / amountProgeny) * 100.0f;
    }
}
