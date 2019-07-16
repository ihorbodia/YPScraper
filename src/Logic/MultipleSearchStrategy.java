package Logic;

import Models.Worker;
import Services.DIResolver;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class MultipleSearchStrategy {

    private ThreadPoolExecutor executor;
    private final DIResolver diResolver;

    public MultipleSearchStrategy(DIResolver diResolver) {
        this.diResolver = diResolver;
    }

    public void processData() {
        List<String> csvFileData = diResolver.getFilesService().getPostalCodes();
        String business = diResolver.getPropertiesService().restoreProperties().business;
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(50);

        for (String item : csvFileData) {
            Runnable worker = new Worker(diResolver, business, item);
            executor.execute(worker);
        }
    }
}
