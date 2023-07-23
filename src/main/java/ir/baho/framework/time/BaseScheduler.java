package ir.baho.framework.time;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.PeriodicTrigger;

import java.time.Duration;
import java.time.ZoneId;
import java.util.concurrent.ScheduledFuture;

@RequiredArgsConstructor
public abstract class BaseScheduler {

    private static final String SEPARATOR = ":";

    @Autowired
    private TaskScheduler taskScheduler;
    private ScheduledFuture<?> scheduledFuture;

    public void schedule(String cron) {
        if (this.scheduledFuture != null) {
            this.scheduledFuture.cancel(true);
        }
        if (cron.contains(SEPARATOR)) {
            ZoneId zoneId = ZoneId.of(cron.substring(cron.indexOf(SEPARATOR) + 1));
            this.scheduledFuture = this.taskScheduler.schedule(this::start, new CronTrigger(cron.substring(0, cron.indexOf(SEPARATOR)), zoneId));
        } else {
            this.scheduledFuture = this.taskScheduler.schedule(this::start, new CronTrigger(cron));
        }
    }

    public void schedule(String cron, ZoneId zoneId) {
        if (this.scheduledFuture != null) {
            this.scheduledFuture.cancel(true);
        }
        this.scheduledFuture = this.taskScheduler.schedule(this::start, new CronTrigger(cron, zoneId));
    }

    public void schedule(Duration period) {
        if (this.scheduledFuture != null) {
            this.scheduledFuture.cancel(true);
        }
        this.scheduledFuture = this.taskScheduler.schedule(this::start, new PeriodicTrigger(period));
    }

    protected abstract void start();

}
