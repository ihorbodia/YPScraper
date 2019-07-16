import Logic.YPScraperLogic;
import Services.DIResolver;

public class Main {
    static DIResolver diResolver;

    public static void main(String[] args) {

        diResolver = new DIResolver();
        YPScraperLogic ypScraperLogic = new YPScraperLogic();


        ypScraperLogic.Run(false);
    }
}
