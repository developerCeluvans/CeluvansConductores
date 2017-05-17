package com.imaginamos.taxisya.taxista.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by leo on 9/14/15.
 */
public class BDAdapter {
    public static final String MYDATABASE = "taxisya_taxista.db";
    public static final int MYDATABASE_VERSION = 2;

    // AccionComercial
    public static final String TABLE_SERVICES = "Services";

    public static final String SRV_ID = "_id";
    public static final String SRV_IDS = "serviceId";
    public static final String SRV_STA = "statusId";
    public static final String SRV_RNK = "score";
    public static final String SRV_USR = "userId";
    public static final String SRV_DRV = "driverID";
    public static final String SRV_TIM = "dateService";
    public static final String SRV_ADR = "address";
    public static final String SRV_KID = "kindId";
    public static final String SRV_SPK = "spoken";

    private static final String SCRIPT_CREA_TABLE_SERVICE = "CREATE TABLE "
            + TABLE_SERVICES + " (" + SRV_ID + " integer primary key autoincrement, "
            + SRV_IDS + " TEXT NOT NULL, "
            + SRV_STA + " TEXT NOT NULL, "
            + SRV_RNK + " TEXT NOT NULL, "
            + SRV_USR + " TEXT NOT NULL, "
            + SRV_DRV + " TEXT NOT NULL, "
            + SRV_TIM + " INTEGER NOT NULL, "
            + SRV_ADR + " TEXT NOT NULL, "
            + SRV_KID + " INTEGER NOT NULL, "
            + SRV_SPK + " INTEGER NOT NULL);";

    private SQLiteHelper sqLiteHelper;
    private SQLiteDatabase sqLiteDatabase;
    private Context context;
    private String SQL = "";
    private ArrayList<String> listSQL;

    public BDAdapter(Context c) {
        context = c;
    }

    public BDAdapter openToRead() throws android.database.SQLException {
        sqLiteHelper = new SQLiteHelper(context);
        sqLiteDatabase = sqLiteHelper.getReadableDatabase();
        return this;
    }

    public BDAdapter openToWrite() throws android.database.SQLException {
        sqLiteHelper = new SQLiteHelper(context);
        sqLiteDatabase = sqLiteHelper.getWritableDatabase();
        return this;
    }


    public void abreTransaccion() {

        // sqLiteDatabase.setLockingEnabled(false);
        // sqLiteDatabase.execSQL("PRAGMA read_uncommitted = true;");

        sqLiteDatabase.beginTransaction();
    }

    public void confirmaTransaccion() {
        sqLiteDatabase.setTransactionSuccessful();
        // sqLiteDatabase.endTransaction();
    }

    public void cierraTransaccion() {
        // sqLiteDatabase.setTransactionSuccessful();
        sqLiteDatabase.endTransaction();
    }

    public void close() {
        if (sqLiteHelper != null) {
            sqLiteHelper.close();
        }
    }


    public long insertService(String ids, String sta, String rnk, String usr, String drv, long tim, int kid) {
        ContentValues contentValues = new ContentValues();
        // contentValues.put(CAB_IDP, id);

        contentValues.put(SRV_IDS, ids);
        contentValues.put(SRV_STA, sta);
        contentValues.put(SRV_RNK, rnk);
        contentValues.put(SRV_USR, usr);
        contentValues.put(SRV_DRV, drv);
        contentValues.put(SRV_TIM, tim);
        contentValues.put(SRV_ADR, "");
        contentValues.put(SRV_SPK, 0);
        contentValues.put(SRV_KID, kid);

        return sqLiteDatabase.insert(TABLE_SERVICES, null, contentValues);

    }

    public int updateService(String ids, String sta, String rnk, String usr, String drv, long tim) {
        ContentValues registro = new ContentValues();
        registro.put(SRV_IDS, ids);
        registro.put(SRV_STA, sta);
        registro.put(SRV_RNK, rnk);
        registro.put(SRV_USR, usr);
        registro.put(SRV_DRV, drv);
        registro.put(SRV_TIM, tim);

        int cant = sqLiteDatabase.update(TABLE_SERVICES, registro, "serviceId=" + ids, null);
        return cant;
    }

    public int updateStatusService(String ids, String sta) {
        ContentValues registro = new ContentValues();
        registro.put(SRV_IDS, ids);
        registro.put(SRV_STA, sta);

        int cant = sqLiteDatabase.update(TABLE_SERVICES, registro, "serviceId=" + ids, null);
        return cant;
    }

    public int updateAddressService(String ids, String address) {
        ContentValues registro = new ContentValues();
        registro.put(SRV_IDS, ids);
        registro.put(SRV_ADR, address);

        int cant = sqLiteDatabase.update(TABLE_SERVICES, registro, "serviceId=" + ids, null);
        return cant;
    }

    public int updateSpokenService(String ids, String spk) {
        ContentValues registro = new ContentValues();
        registro.put(SRV_IDS, ids);
        registro.put(SRV_SPK, spk);

        int cant = sqLiteDatabase.update(TABLE_SERVICES, registro, "serviceId=" + ids, null);
        return cant;
    }

    public int updateScoreService(String ids, String rnk) {
        ContentValues registro = new ContentValues();
        registro.put(SRV_IDS, ids);
        registro.put(SRV_RNK, rnk);

        int cant = sqLiteDatabase.update(TABLE_SERVICES, registro, "serviceId=" + ids, null);
        return cant;
    }

    public Cursor filterService(String ids) {
        String sql = "SELECT Services.* FROM Services WHERE Services.serviceId = " + ids;
        Cursor cursor = sqLiteDatabase.rawQuery(sql, null);
        return cursor;
    }

    public Cursor filterServiceForSpeak(String spoken) {
        String sql = "SELECT Services.* FROM Services WHERE Services.statusId = 1 AND Services.spoken = " + spoken;
        Cursor cursor = sqLiteDatabase.rawQuery(sql, null);
        return cursor;
    }

    public int deleteAllServices() {
        return sqLiteDatabase.delete(TABLE_SERVICES, null, null);
    }

    public int deleteServices() {
        String where = "statusId=1";
        return sqLiteDatabase.delete(TABLE_SERVICES, where, null);
    }

    public int deleteServicesOperator() {
        String where = "kindId=3";
        return sqLiteDatabase.delete(TABLE_SERVICES, where, null);
    }

    private String ReplaceQuote(String t) {
        return t.replace("'", "''");
    }

    public class SQLiteHelper extends SQLiteOpenHelper {
        private Context myContext;

        public SQLiteHelper(Context context) {
            super(context, MYDATABASE, null, MYDATABASE_VERSION);
            this.myContext = context;
        }

        @Override
        public synchronized void close() {

            if (sqLiteDatabase != null)
                sqLiteDatabase.close();
            super.close();

        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.v("CNF_SRV1", "BDAdapter onCreate");
            db.execSQL(SCRIPT_CREA_TABLE_SERVICE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.v("onUpgrade", "Updating userstations database from " + oldVersion + " to " + newVersion + ".");
            if ((oldVersion == 1) && (newVersion == 2)) {
//                db.execSQL("CREATE VIEW VistaCabPedidos AS SELECT CabPedidos.*,1 As Existe FROM CabPedidos WHERE (CabPedidos.Numpedido IN (Select Documento FROM Consecutivo)) UNION ALL SELECT CabPedidos.*,0 As Existe FROM CabPedidos WHERE (CabPedidos.Numpedido NOT IN (Select Documento FROM Consecutivo))");
                db.execSQL("ALTER TABLE Services ADD COLUMN kindId INTEGER NOT NULL DEFAULT 1;");
            }
        }
    }


}
