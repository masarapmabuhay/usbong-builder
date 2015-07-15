package usbong.android.builder.controllers;

import android.util.Log;

import com.activeandroid.Model;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import usbong.android.builder.converters.UtreeConverter;
import usbong.android.builder.exceptions.NoStartingScreenException;
import usbong.android.builder.models.Screen;
import usbong.android.builder.models.ScreenRelation;
import usbong.android.builder.models.Utree;
import usbong.android.builder.parsers.UtreeParser;
import usbong.android.builder.utils.FileUtils;

import java.io.File;
import java.util.List;

/**
 * Created by Rocky Camacho on 6/26/2014.
 */
public class UtreeListController implements Controller {

    public static final String TAG = UtreeListController.class.getSimpleName();
    public static final String UTREE_FILE_EXTENSION = ".utree";
    public static final String XML_FILE_EXTENSION = ".xml";

    public void fetchUtrees(Observer<List<Utree>> observer) {
        getUtrees().observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    public Observable<List<Utree>> getUtrees() {
        return Observable.create(new Observable.OnSubscribe<List<Utree>>() {
            @Override
            public void call(Subscriber<? super List<Utree>> subscriber) {
                List<Utree> utrees = new Select().from(Utree.class).execute();
                subscriber.onNext(utrees);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io());
    }

    /**
     * Added by Cere Blanco 5/2015
     * @param utree
     * @param observer
     */
    public void deleteUtree(final Utree utree, final String folderLocation, Observer<Object> observer) {

        Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                List<Screen> uTreeScreens = Screen.getScreens(utree.getId());

                for( Screen screen: uTreeScreens) {
                    new Delete().from(ScreenRelation.class)
                            .where("parent = ? OR child = ?", screen.getId())
                            .execute();
                    new Delete().from(Screen.class)
                            .where(Screen._ID + " = ?", screen.getId())
                            .execute();
                }

                String treeFolder = folderLocation + File.separator + utree.name;
                FileUtils.delete(treeFolder);

                new Delete().from(Utree.class)
                        .where(Utree._ID + " = ?", utree.getId())
                        .execute();

                subscriber.onNext(null);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    public void importTree(final String fileLocation, final String outputFolderLocation, Observer<String> observer) {
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                if (fileLocation.endsWith(XML_FILE_EXTENSION)) {
                    String treeName = fileLocation.substring(fileLocation.lastIndexOf("/") + 1, fileLocation.lastIndexOf(XML_FILE_EXTENSION));
                    parseTreeDetails(fileLocation, outputFolderLocation + File.separator + treeName);

                    subscriber.onNext(fileLocation);
                    subscriber.onCompleted();
                } else if (fileLocation.endsWith(UTREE_FILE_EXTENSION)) {
                    String treeName = fileLocation.substring(fileLocation.lastIndexOf("/") + 1, fileLocation.lastIndexOf(UTREE_FILE_EXTENSION));
                    FileUtils.unzip(fileLocation, outputFolderLocation);
                    String xmlFilePath = outputFolderLocation + File.separator + treeName + File.separator + treeName + XML_FILE_EXTENSION;
                    parseTreeDetails(xmlFilePath, outputFolderLocation + File.separator + treeName);
                    subscriber.onNext(xmlFilePath);
                    subscriber.onCompleted();
                } else {
                    throw new IllegalArgumentException("Unable to read file. Make sure to select a valid *.utree or *.xml file");
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    private void parseTreeDetails(String xmlPath, String utreeOutputFolder) {
        UtreeParser parser = UtreeParser.getInstance();
        parser.parseAndSave(xmlPath, utreeOutputFolder);
    }

    public void exportTree(final Utree utree, final String folderLocation, final String treeFolderLocation, final String tempFolderLocation, Observer<String> observer) {
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                Screen startScreen = new Select().from(Screen.class)
                        .where("Utree = ? AND IsStart = ?", utree.getId(), 1)
                        .executeSingle();
                if(startScreen == null) {
                    subscriber.onError(new NoStartingScreenException(".utree does not have a starting screen. Please mark one of the screens as the start screen"));
                }

                FileUtils.mkdir(treeFolderLocation);
                String xmlFileLocation = treeFolderLocation + utree.name + ".xml";
                String zipFilePath = folderLocation + File.separator + utree.name + ".utree";
                UtreeConverter converter = new UtreeConverter();
                converter.convert(utree, xmlFileLocation);
                FileUtils.delete(tempFolderLocation);
                FileUtils.copyAll(treeFolderLocation, tempFolderLocation + utree.name + ".utree" + File.separator);
                FileUtils.zip(zipFilePath, tempFolderLocation);
                FileUtils.delete(tempFolderLocation);
                subscriber.onNext(null);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);

    }

    public void exportTreeToUsbongApp(final Utree utree, final String treeFolderLocation, final String tempFolderLocation, Observer<String> observer) {
        System.out.println("exportAndUploadTree");
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                String folderLocation = "/storage/emulated/legacy/usbong/usbong_trees/";
                File file = new File(folderLocation);
                if (!file.exists())
                    file.mkdir();
                Screen startScreen = new Select().from(Screen.class)
                        .where("Utree = ? AND IsStart = ?", utree.getId(), 1)
                        .executeSingle();
                if (startScreen == null) {
                    subscriber.onError(new NoStartingScreenException(".utree does not have a starting screen. Please mark one of the screens as the start screen"));
                }

                FileUtils.mkdir(treeFolderLocation);
                String xmlFileLocation = treeFolderLocation + utree.name + ".xml";
                String zipFilePath = folderLocation + File.separator + utree.name + ".utree";
                UtreeConverter converter = new UtreeConverter();
                converter.convert(utree, xmlFileLocation);
                FileUtils.delete(tempFolderLocation);
                FileUtils.copyAll(treeFolderLocation, tempFolderLocation + utree.name + ".utree" + File.separator);
                FileUtils.zip(zipFilePath, tempFolderLocation);
                FileUtils.delete(tempFolderLocation);
                subscriber.onNext(null);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    public void exportTreeToTempLocation(final Utree utree, final String treeFolderLocation, final String tempFolderLocation, Observer<String> observer) {
        System.out.println("exportAndUploadTree");
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                String folderLocation = "/storage/emulated/legacy/usbong/usbong_trees/temp/";
                File file = new File(folderLocation);
                if (!file.exists())
                    file.mkdir();
                Screen startScreen = new Select().from(Screen.class)
                        .where("Utree = ? AND IsStart = ?", utree.getId(), 1)
                        .executeSingle();
                if (startScreen == null) {
                    subscriber.onError(new NoStartingScreenException(".utree does not have a starting screen. Please mark one of the screens as the start screen"));
                }

                FileUtils.mkdir(treeFolderLocation);
                String xmlFileLocation = treeFolderLocation + utree.name + ".xml";
                String zipFilePath = folderLocation + File.separator + utree.name + ".utree";
                UtreeConverter converter = new UtreeConverter();
                converter.convert(utree, xmlFileLocation);
                FileUtils.delete(tempFolderLocation);
                FileUtils.copyAll(treeFolderLocation, tempFolderLocation + utree.name + ".utree" + File.separator);
                FileUtils.zip(zipFilePath, tempFolderLocation);
                FileUtils.delete(tempFolderLocation);
                subscriber.onNext(null);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }
}