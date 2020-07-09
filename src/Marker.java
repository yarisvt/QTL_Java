public class Marker implements Comparable<Marker>
{
    private String markerName;
    private float distance;

    Marker(String markerName, float distance)
    {
        this.markerName = markerName;
        this.distance = distance;
    }


    String getMarkerName()
    {
        return this.markerName;
    }

    float getDistance()
    {
        return this.distance;
    }

    @Override
    public String toString()
    {
        return String.format("%s %.2f", this.markerName, this.distance);
    }

    @Override
    public int compareTo(Marker marker)
    {
        return Float.compare(this.getDistance(), marker.getDistance());
    }
}
