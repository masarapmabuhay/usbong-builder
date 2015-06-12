package usbong.android.builder.controllers;

import android.util.Log;

import com.activeandroid.query.Select;
import com.activeandroid.query.Update;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import usbong.android.builder.models.Screen;
import usbong.android.builder.models.ScreenRelation;

/**
 * Created by Rocky Camacho on 6/26/2014.
 */
public class ScreenController implements Controller {

    private static final String TAG = ScreenController.class.getSimpleName();

    public void fetchScreen(long id, Observer<Screen> observer) {
        getScreen(id).observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    private Observable<Screen> getScreen(final long id) {
        return Observable.create(new Observable.OnSubscribe<Screen>() {
            @Override
            public void call(Subscriber<? super Screen> subscriber) {
                Screen screen = loadScreen();
                subscriber.onNext(screen);
                subscriber.onCompleted();
            }

            private Screen loadScreen() {
                Screen screen = null;
                if (id == -1) {
                    screen = new Screen();
                } else {
                    screen = new Select().from(Screen.class)
                            .where(Screen._ID + " = ?", id)
                            .executeSingle();
                }
                return screen;
            }
        }).subscribeOn(Schedulers.io());
    }

//    public void save(final Screen screen, Observer<Screen> observer) {
    public void save(final Screen screen, final long parentId, final String condition, Observer<Screen> observer) {
        Observable.create(new Observable.OnSubscribe<Screen>() {
            @Override
            public void call(Subscriber<? super Screen> subscriber) {
                if (screen.isStart == 1) {
                    new Update(Screen.class).set("IsStart = ?", 0)
                            .where("Utree = ?", screen.utree.getId())
                            .execute();
                    screen.isStart = 1;
                    screen.save();
                } else if (Screen.getScreens(screen.utree.getId()).size() == 0) {
                    screen.isStart = 1;
                    screen.save();
                } else {
                    screen.save();
                }
                if(parentId != -1) {
                    Screen parentScreen = Screen.getScreenById(parentId);
                    saveScreenRelation(parentScreen, screen, condition);
                }

                subscriber.onNext(screen);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    private void saveScreenRelation(final Screen parentScreen, final Screen screen,  final String relationCondition){
        addOrUpdateRelation(parentScreen, screen, relationCondition, new Observer<ScreenRelation>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, e.getMessage(), e);
            }

            @Override
            public void onNext(ScreenRelation screenRelation) {

            }
        });
    }

    public void addRelation(final Screen parentScreen, final Screen childScreen, final String condition,Observer<ScreenRelation> observer) {
        Observable.create(new Observable.OnSubscribe<ScreenRelation>() {
            @Override
            public void call(Subscriber<? super ScreenRelation> subscriber) {
                ScreenRelation screenRelation = new ScreenRelation();
                screenRelation.parent = parentScreen;
                screenRelation.child = childScreen;
                screenRelation.condition = condition;
                screenRelation.save();
                subscriber.onNext(screenRelation);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    public void addOrUpdateRelation(final Screen parentScreen, final Screen childScreen, final String condition, Observer<ScreenRelation> observer) {
        Observable.create(new Observable.OnSubscribe<ScreenRelation>() {
            @Override
            public void call(Subscriber<? super ScreenRelation> subscriber) {
                ScreenRelation existingRelationWithSameCondition = new Select().from(ScreenRelation.class)
                        .where("parent = ? and condition = ?", parentScreen.getId(), condition)
                        .executeSingle();
                if(existingRelationWithSameCondition != null) {
                    existingRelationWithSameCondition.child = childScreen;
                    existingRelationWithSameCondition.save();
                    subscriber.onNext(existingRelationWithSameCondition);
                }
                else {
                    ScreenRelation screenRelation = new ScreenRelation();
                    screenRelation.parent = parentScreen;
                    screenRelation.child = childScreen;
                    screenRelation.condition = condition;
                    screenRelation.save();
                    subscriber.onNext(screenRelation);
                }
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }
}
