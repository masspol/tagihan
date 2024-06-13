package com.azhar.hitungpengeluaran.view.fragment.pengeluaran.add;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.azhar.hitungpengeluaran.database.DatabaseClient;
import com.azhar.hitungpengeluaran.database.dao.DatabaseDao;
import com.azhar.hitungpengeluaran.model.ModelDatabase;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class AddPengeluaranViewModel extends AndroidViewModel {

    private DatabaseDao databaseDao;

    public AddPengeluaranViewModel(@NonNull Application application) {
        super(application);

        databaseDao = DatabaseClient.getInstance(application).getAppDatabase().databaseDao();
    }

    public void addPengeluaran(final String type, final String plg, final String note, final String date, final int price, String photoUrl) {
        Completable.fromAction(new Action() {
                    @Override
                    public void run() throws Exception {
                        ModelDatabase pengeluaran = new ModelDatabase();
                        pengeluaran.tipe = type;
                        pengeluaran.pelanggan = plg;
                        pengeluaran.keterangan = note;
                        pengeluaran.tanggal = date;
                        pengeluaran.jmlUang = price;
                        databaseDao.insertPengeluaran(pengeluaran);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    public void updatePengeluaran(final int uid, final String plg, final String note, final String date, final int price, String photoUrl) {
        Completable.fromAction(new Action() {
                    @Override
                    public void run() throws Exception {
                        databaseDao.updateDataPengeluaran(plg, note, date, price, uid);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

}