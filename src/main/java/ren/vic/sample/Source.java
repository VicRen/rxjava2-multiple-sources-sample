package ren.vic.sample;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;

public class Source {

    // Memory cache of data
    private Data memory = new Data(null);

    // Data written on disk
    private Data disk = new Data(null);

    // Each network response is different
    private int requestNumber = 0;

    // In order to simulate memory being cleared, but data still on disk
    public void clearMemory() {
        System.out.println("Wiping memory...");
        memory = new Data(null);
        System.out.println("Memory wiped...");
    }

    public Observable<Data> memory() {
        Observable<Data> observable = Observable.create(e -> {
            e.onNext(memory);
            e.onComplete();
        });
        return observable.compose(logSource("MEMORY"));
    }

    public Observable<Data> disk() {
        Observable<Data> observable = Observable.create(e -> {
            e.onNext(disk);
            e.onComplete();
        });
        // Cache disk responses in memory
        return observable
                .doOnNext(data -> memory = data)
                .compose(logSource("DISK"));
    }

    public Observable<Data> network() {
        Observable<Data> observable = Observable.create(e -> {
            requestNumber++;
            e.onNext(new Data("Server Response #" + requestNumber));
            e.onComplete();
        });
        // Save network responses to disk and cache in memory
        return observable.doOnNext(data -> {
            disk = data;
            memory = data;
        }).compose(logSource("NETWORK"));
    }

    // Simple logging to let us know what each source is returning
    private ObservableTransformer<Data, Data> logSource(final String source) {
        return dataObservable -> dataObservable.doOnNext(data -> {
            if (!data.isValid()) {
                System.out.println(source + " does not have any data.");
            } else if (!data.isUpToDate()) {
                System.out.println(source + " has stale data.");
            } else {
                System.out.println(source + " has the data you are looking for!");
            }
        });
    }
}
