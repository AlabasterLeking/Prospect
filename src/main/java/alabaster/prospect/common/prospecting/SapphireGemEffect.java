package alabaster.prospect.common.prospecting;

public class SapphireGemEffect implements ProspectingGemEffect {
    @Override
    public int modifyRadius(int radius, ProspectingContext ctx) {
        return radius + 8;
    }
}