package Logic.SearchStrategies;

import Models.AppPropertiesModel;
import Models.Worker;
import Services.DIResolver;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class SingleSearchStrategy extends BaseSearchStrategy {
    public SingleSearchStrategy(DIResolver diResolver) {
        super(diResolver);
    }

    private Worker worker = null;

    @Override
    public void processData() {
        createEmptyCsvDataFile();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        future = executorService.submit(() -> {
            String business = diResolver.getGuiService().getTextFieldBusiness();
            diResolver.getPropertiesService().saveBusiness(business);

            String location = diResolver.getGuiService().getTextFieldLocation();
            diResolver.getPropertiesService().saveLocation(location);

            executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
            worker = new Worker(diResolver, business, location);
            executor.execute(worker);
            waitForTheEnd(1);
        });
    }

    @Override
    public void createEmptyCsvDataFile() {
        AppPropertiesModel appPropertiesModel = diResolver.getPropertiesService().restoreProperties();
        String fileName = appPropertiesModel.province + "_" + appPropertiesModel.business;
        diResolver.getFilesService().createEmptyCSVFile(new String[] {"Link", "Name", "Address", "Location"}, fileName);
    }

    @Override
    public void updateStatusText(double onePercent) {
        if (worker != null) {
            diResolver.getGuiService().updateStatusTextBySingleSearch(worker.getCurrentPage());
        }
    }
}
