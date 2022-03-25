import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

/**
 *
 * @author Stephen Adams
 */
public class playGame {
    public static void main( String[] args ) {
        double scores = 0;
        int over150=0, over200=0;
        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        final int N = 1000000;
        YahtzeeStrategy strat = null;
        for ( int x = 0; x < N; x ++ ) {
            int score = (strat = new YahtzeeStrategy()).play();
            scores += score;
            max = score > max ? score : max;
            min = score < min ? score : min;
            if (score>=200) over200++;
            else if (score>=150) over150++;
        }
        System.out.println("Total Stupid Scored: " + strat.totalStupidScored + ", " + ((((double)strat.totalStupidScored/13)/N) * 100.00) + "%");
        System.out.println("Total Stupid Scratched: " + strat.totalStupidScratched + ", " + ((((double)strat.totalStupidScratched/13)/N) * 100.00) + "%");
        System.out.println("Total Stupid Level: " + ((((double)(strat.totalStupidScored + strat.totalStupidScratched)/13)/N) * 100.00) + "%");
        System.out.printf( "Iterations: %d\t\t\tMin Score: %d\t\tMax Score: %d\t\tAverage Score: %.2f\nGames>=150: %.2f%%\tGames>=200: %.2f%%%n%n", N, min, max, ((double)scores/N), 100*((double)over150/N),100*((double)over200/N) );
    }
}
