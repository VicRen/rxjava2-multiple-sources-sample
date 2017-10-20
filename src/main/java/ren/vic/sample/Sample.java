package ren.vic.sample;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;

public class Sample {

    public static void main(String[] args) {
        Source sources = new Source();

        // Create our sequence for querying best available data
        Observable<Data> source = io.reactivex.Observable.concat(
                sources.memory(),
                sources.disk(),
                sources.network()
        ).filter(data -> data.isValid() && data.isUpToDate()).firstElement().toObservable();

        // "Request" latest data once a second
        Observable.interval(1, TimeUnit.SECONDS)
                .doOnNext(e -> System.out.println("...Start retrieve data..."))
                .flatMap(aLong -> source)
                .subscribe(data -> System.out.println("Received: " + data.value));

        // Occasionally clear memory (as if app restarted) so that we must go to disk
        Observable.interval(3, TimeUnit.SECONDS)
                .subscribe(aLong -> sources.clearMemory());

        // Java will quit unless we idle
        sleep(15 * 1000);
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
