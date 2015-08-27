package usbong.android.builder.controllers;

import android.util.Log;

import com.activeandroid.query.Select;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import usbong.android.builder.exceptions.FormInputException;
import usbong.android.builder.exceptions.NoStartingScreenException;
import usbong.android.builder.models.Utree;

/**
 * Created by Rocky Camacho on 6/26/2014.
 * Edited by Michael Syson and Cere Blanco, 1 June 2015
 */
public class UtreeController implements Controller {

    public void fetchUtree(long id, Observer<Utree> observer) {
        getUtree(id).observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    private Observable<Utree> getUtree(final long id) {
        return Observable.create(new Observable.OnSubscribe<Utree>() {
            @Override
            public void call(Subscriber<? super Utree> subscriber) {
                Utree utree = loadUtree();
                subscriber.onNext(utree);
                subscriber.onCompleted();
            }

            private Utree loadUtree() {
                Utree utree = null;
                if (id == -1) {
                    utree = new Utree();
                } else {
                    utree = new Select().from(Utree.class)
                            .where(Utree._ID + " = ?", id)
                            .executeSingle();
                }
                return utree;
            }
        }).subscribeOn(Schedulers.io());
    }

    public void save(final Utree utree, Observer<Utree> observer) {
        Observable.create(new Observable.OnSubscribe<Utree>() {
            @Override
            public void call(Subscriber<? super Utree> subscriber) {
            	if(utree.name.equalsIgnoreCase("")){
            		subscriber.onError(new FormInputException("Please provide a valid .utree name."));
            	}else {
            		utree.save();
            	}
            	subscriber.onNext(utree);                
            	subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

}
