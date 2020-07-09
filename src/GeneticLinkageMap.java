import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GeneticLinkageMap
{
    private HashMap<String, HashMap<String, Float>> analyzedMarkers;
    private ArrayList<Marker> markers;
    private Marker firstMarker;

    GeneticLinkageMap(HashMap<String, HashMap<String, Float>> analyzedMarkers)
    {
        this.analyzedMarkers = analyzedMarkers;
        this.markers = new ArrayList<>();
        this.firstMarker = null;

    }

    void findFurthestApart()
    {
        float biggestDistance = 0;
        String firstMarker = null;

        for (String referenceMarker : this.analyzedMarkers.keySet())
        {
            for (String currentMarker : this.analyzedMarkers.keySet())
            {
                if (!referenceMarker.equals(currentMarker))
                {
                    float distance = analyzedMarkers.get(referenceMarker).get(currentMarker);
                    if (distance > biggestDistance)
                    {
                        firstMarker = referenceMarker;
                        biggestDistance = distance;
                    }
                }
            }
        }
        this.firstMarker = new Marker(firstMarker, 0);
        this.markers.add(this.firstMarker);
    }

    void createOtherMarkers()
    {
        HashMap<String, Float> otherMarkers = this.analyzedMarkers.get(this.firstMarker.getMarkerName());
        for (Map.Entry<String, Float> otherMarker : otherMarkers.entrySet())
        {
            float distance = Float.parseFloat(String.format("%.2f", otherMarker.getValue()));
            Marker marker = new Marker(otherMarker.getKey(), distance);
            this.markers.add(marker);
        }
    }

    void writeSortedMarkersToFile(String fileName)
    {
        PrintWriter outputFile = null;
        try
        {
            Collections.sort(this.markers);
            outputFile = new PrintWriter(fileName);
            outputFile.println("group cm");
            for (Marker marker : this.markers)
            {
                outputFile.println(marker.toString());
            }

        }
        catch (IOException ioe)
        {
            System.out.println("An error occured");
        }
        finally
        {
            assert outputFile != null;
            outputFile.close();
        }

    }
}
