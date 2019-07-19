package Logic.SearchStrategies;
import Services.DIResolver;
import Services.LoggerService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

public abstract class BaseSearchStrategy {

    ThreadPoolExecutor executor;
    protected final DIResolver diResolver;
    Future<?> future;

    BaseSearchStrategy(DIResolver diResolver) {
        this.diResolver = diResolver;
    }

    public abstract void processData();
    public abstract void createEmptyCsvDataFile();
    public abstract void updateStatusText(double onePercent);
    public void stopProcessing() {
        executor.shutdownNow();
        future.cancel(true);
        diResolver.getPropertiesService().saveWorkState(false);
        System.out.println("Stop command");
        LoggerService.logMessage("Stop command");
    }

    void waitForTheEnd(int taskCount) {
        Thread thread = new Thread(() -> {
            double onePercent = taskCount / 100.0;
            diResolver.getGuiService().changeApplicationState(true);
            while (true) {
                if (taskCount == executor.getCompletedTaskCount()) {
                    executor.shutdownNow();
                }
                if (executor.isTerminated()) {
                    break;
                }
                updateStatusText(onePercent);
            }
            System.out.println("Program: Finished");
            LoggerService.logMessage("Program: Finished");
            diResolver.getGuiService().getTextFieldStatus().setText("Finished");
            diResolver.getGuiService().changeApplicationState(false);
        });
        thread.start();
    }
}
