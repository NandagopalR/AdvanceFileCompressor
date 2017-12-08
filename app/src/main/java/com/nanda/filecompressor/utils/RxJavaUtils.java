package com.nanda.filecompressor.utils;


import rx.Completable;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.exceptions.OnErrorNotImplementedException;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;


public class RxJavaUtils {
    public static <T> Observable.Transformer<T, T> applyObserverSchedulers() {
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> observable) {
                return observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

    public static Completable.Transformer applyCompletableSchedulers() {
        return new Completable.Transformer() {
            @Override
            public Completable call(Completable completable) {
                return completable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
            }
        };
    }


    public static <T> Observable.Transformer<T, T> applyErrorTransformer() {
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> observable) {
                return observable.onErrorResumeNext(new Func1<Throwable, Observable<? extends T>>() {
                    @Override
                    public Observable<? extends T> call(Throwable throwable) {
                        return Observable.error(throwable);
                    }
                });
            }
        };
    }

    public static <T> Observable.Transformer<T, T> applyOnErrorCrasher() {
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> observable) {
                return observable.doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        final Throwable checkpoint = new Throwable();
                        StackTraceElement[] stackTrace = checkpoint.getStackTrace();
                        StackTraceElement element = stackTrace[1]; // First element after `crashOnError()`
                        String msg = String.format("onError() crash from subscribe() in %s.%s(%s:%s)",
                                element.getClassName(),
                                element.getMethodName(),
                                element.getFileName(),
                                element.getLineNumber());

                        throw new OnErrorNotImplementedException(msg, throwable);
                    }
                });
            }
        };
    }
}
