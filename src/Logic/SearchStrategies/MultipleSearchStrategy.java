package Logic.SearchStrategies;

import Models.AppPropertiesModel;
import Models.Worker;
import Services.DIResolver;
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
            for (String item : csvFileData) {
                Runnable task = new Worker(diResolver, business, item);
                executor.execute(task);
            }
            waitForTheEnd(csvFileData.size());
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
        diResolver.getGuiService().updateStatusTextByFileProcessing((int)(executor.getCompletedTaskCount() / onePercent));
    }
}
