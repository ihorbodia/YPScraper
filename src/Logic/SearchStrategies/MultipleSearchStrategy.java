package Logic.SearchStrategies;

import Models.AppPropertiesModel;
import Models.Worker;
import Services.DIResolver;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class MultipleSearchStrategy extends BaseSearchStrategy {

    public MultipleSearchStrategy(DIResolver diResolver) {
        super(diResolver);
    }

    @Override
    public void processData() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        future = executorService.submit(() -> {
            List<String> csvFileData = diResolver.getFilesService().getPostalCodes();
            String business = diResolver.getGuiService().getTextFieldBusiness();
            diResolver.getPropertiesService().saveBusiness(business);

            executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(50);
            int taskCount = 0;
            for (String item : csvFileData) {
                Runnable task = new Worker(diResolver, business, item);
                executor.execute(task);
                taskCount++;
            }
            waitForTheEnd(taskCount);
        });
    }

    @Override
    public void createEmptyCsvDataFile() {
        AppPropertiesModel appPropertiesModel = diResolver.getPropertiesService().restoreProperties();
        String fileName = appPropertiesModel.business;
        diResolver.getFilesService().createEmptyCSVFile(new String[] {"Link", "Name", "Address", "Location"}, fileName);
    }

    @Override
    public void updateStatusText(double onePercent) {
        double val = ((double)executor.getCompletedTaskCount() / onePercent);
        diResolver.getGuiService().updateStatusTextByFileProcessing(round(val, 2));
    }

    private double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
