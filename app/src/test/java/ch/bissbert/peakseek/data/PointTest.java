package ch.bissbert.peakseek.data;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Logger;

@RunWith(Parameterized.class)
public class PointTest {

    private static final Logger LOGGER = Logger.getLogger(Point.class.getName());

    public PointTest(long east, long north, double expLong, double expLat) {
        this.east = east;
        this.north = north;
        this.expLong = expLong;
        this.expLat = expLat;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {2652111L, 1158700L, 8.118454255d, 46.577546066d},
                {2650642L, 1221866L, 8.106298461d, 47.145833522d},
                {2669827L, 1185759L, 8.353665937d, 46.819323523d},
                {2710121L, 1118860L, 8.865649307d, 46.212183650d},
                {2691241L, 1293512L, 8.656149835d, 47.785785917d}
        });
    }

    private final long east;
    private final long north;
    private final double expLong;
    private final double expLat;

    private static final PointType TYPE = new PointType("Berg");

    @Test
    public void testFromLV95() {
        Point point = Point.generatedFromLV95("BLANK", east, north, 100, Language.NONE, TYPE);

        assertEquals(expLat, point.getLatitude(), 0.001);
        System.out.println("lat:\t"+point.getLatitude());
        assertEquals(expLong, point.getLongitude(), 0.001);
        System.out.println("long:\t"+point.getLongitude());

        System.out.println("\n-------\n");
    }
}