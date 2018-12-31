package cn.pro47x.core.utils;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import cn.pro47x.core.config.AppConfig;
import cn.pro47x.core.entity.AreaData;
import cn.pro47x.core.utils.log.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class CityDbUtils {

    public static final int DB_VERSION = 1;

    private static final String DB_NAME = "city.db";
    private static SQLiteDatabase cityDb;

    /**
     * 获取省及直辖市列表
     */
    public static List<AreaData> getProvinceList() {
        List<AreaData> list = new ArrayList<AreaData>();
        Cursor cursor = null;
        try {
            cursor = getCityDb().rawQuery(
                    "select name,code,pinyin,firstletter,parent_code,shengzhixia,zhixiashi from t_area where "
                            + "parent_code is '000000'", null);
            while (cursor.moveToNext()) {
                String name = cursor.getString(0);
                String code = cursor.getString(1);
                AreaData city = new AreaData(name);
                city.setIsMunicipality((cursor.getInt(6) == 1) ? true : false);
                city.setCode(code);
                city.setPinyin(cursor.getString(2));
                city.setFirstLetter(cursor.getString(3));
                city.setParentCode(cursor.getString(4));
                city.setIsAdministeredCounty((cursor.getInt(5) == 1) ? true : false);
                list.add(city);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DataUtils.close(cursor);
            closeDatabase();
        }
        return list;
    }

    /**
     * 通过省代码获取城市列表
     */
    public static List<AreaData> getCityListByProvinceCode(String provinceCode) {
        List<AreaData> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = getCityDb().rawQuery(
                    "select name,code,pinyin,firstletter,parent_code,shengzhixia,zhixiashi from t_area where "
                            + "parent_code is '" + provinceCode + "'", null);
            while (cursor.moveToNext()) {
                String name = cursor.getString(0);
                if ("市辖区".equals(name)) {
                    continue;
                }
                AreaData city = new AreaData(name);
                city.setIsMunicipality((cursor.getInt(6) == 1) ? true : false);
                city.setCode(cursor.getString(1));
                city.setPinyin(cursor.getString(2));
                city.setFirstLetter(cursor.getString(3));
                city.setIsAdministeredCounty((cursor.getInt(5) == 1) ? true : false);
                city.setParentCode(cursor.getString(4));
                list.add(city);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DataUtils.close(cursor);
            closeDatabase();
        }
        return list;
    }

    /**
     * 通过市代码获取区列表
     */
    public static List<AreaData> getRegionListByCityCode(String cityCode) {
        List<AreaData> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = getCityDb().rawQuery(
                    "select name,code,pinyin,firstletter,parent_code,shengzhixia,zhixiashi from t_area where "
                            + "parent_code is '" + cityCode + "'", null);
            while (cursor.moveToNext()) {
                String name = cursor.getString(0);
                if ("市辖区".equals(name)) {
                    continue;
                }
                if ("市辖县".equals(name)) {
                    continue;
                }
                AreaData city = new AreaData(name);
                city.setIsMunicipality((cursor.getInt(6) == 1) ? true : false);
                city.setCode(cursor.getString(1));
                city.setPinyin(cursor.getString(2));
                city.setFirstLetter(cursor.getString(3));
                city.setIsAdministeredCounty((cursor.getInt(5) == 1) ? true : false);
                city.setParentCode(cursor.getString(4));
                list.add(city);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DataUtils.close(cursor);
            closeDatabase();
        }
        return list;
    }


    public static AreaData getAreaByCode(String code) {
        Cursor cursor = null;
        AreaData city = null;
        try {
            if (MiscUtils.isNotEmpty(code)) {
                cursor = getCityDb().rawQuery(
                        "select name,code,pinyin,firstletter,parent_code,shengzhixia,zhixiashi from t_area where code"
                                + " is '" + code + "'", null);
                if (cursor.moveToNext()) {
                    String name = cursor.getString(0);
                    city = new AreaData(name);
                    city.setIsMunicipality((cursor.getInt(6) == 1) ? true : false);
                    city.setCode(cursor.getString(1));
                    city.setPinyin(cursor.getString(2));
                    city.setFirstLetter(cursor.getString(3));
                    city.setIsAdministeredCounty((cursor.getInt(5) == 1) ? true : false);
                    city.setParentCode(cursor.getString(4));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DataUtils.close(cursor);
            closeDatabase();
        }
        return city;
    }

    public static AreaData getAreaByName(String name) {
        Cursor cursor = null;
        AreaData city = null;
        try {
            if (MiscUtils.isNotEmpty(name)) {
                cursor = getCityDb().rawQuery(
                        "select name,code,pinyin,firstletter,parent_code,shengzhixia,zhixiashi from t_area where name"
                                + " is '" + name + "'", null);
                if (cursor.moveToNext()) {
                    city = new AreaData(name);
                    city.setIsMunicipality(
                            name.startsWith("北京") || name.startsWith("上海") || name.startsWith("天津") || name
                                    .startsWith("重庆"));
                    city.setCode(cursor.getString(1));
                    city.setPinyin(cursor.getString(2));
                    city.setFirstLetter(cursor.getString(3));
                    city.setIsAdministeredCounty((cursor.getInt(5) == 1) ? true : false);
                    city.setParentCode(cursor.getString(4));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DataUtils.close(cursor);
            closeDatabase();
        }
        return city;
    }

    public static AreaData getParentAreaByCode(String code) {
        AreaData area = getAreaByCode(code);
        if (area != null) {
            AreaData parentArea = getAreaByCode(area.getParentCode());
            if (parentArea != null) {
                return parentArea;
            }
        }
        return new AreaData("全国", "000000");
    }

    public static AreaData getParentAreaByCodeForCity(String code) {
        AreaData area = getAreaByCode(code);
        if (area != null) {
            AreaData parentArea = getAreaByCode(area.getParentCode());
            if (parentArea != null) {
                if (!parentArea.isMunicipality() && parentArea.getCode().endsWith("0000")) {
                    return area;
                }
                return parentArea;
            }
        }
        return area;
    }

    private static boolean isMunicipalityCode(String provinceCode) {
        return provinceCode.startsWith("11") || provinceCode.startsWith("12") || provinceCode.startsWith("31")
                || provinceCode.startsWith("50");
    }

    private static boolean isAdministeredCountyCode(String code) {
        if (code.length() >= 4) {
            return code.substring(2, 4).equals("90");
        }
        return false;
    }

    public static void destory() {
        if (cityDb != null && cityDb.isOpen()) {
            DataUtils.close(cityDb);
            cityDb = null;
        }
    }

    /**
     * 把相应的数据库文件从程序打包的assets时里面拷贝到程序的数据库目录下
     *
     * @param dbFile 拷贝的目的地
     */
    private static void copyAssetDBToFile(File dbFile) {
        dbFile.getParentFile().mkdirs();
        FileOutputStream fout = null;
        InputStream is = null;
        try {
            is = DataUtils.readFileStream(null, "core/db/ala__city.aar");
            fout = new FileOutputStream(dbFile);
            DataUtils.copy(is, fout);
            Logger.i("info", "copyFile to DB:" + dbFile);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            DataUtils.close(is);
            DataUtils.close(fout);
        }
    }

    private static File getQuestionDBFile() {
        File file;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            file = DataUtils.createIfNotExists("databases/" + DB_NAME);
        } else {
            file = new File("/data/data/" + AppConfig.getPackageName() + "/databases/" + DB_NAME);
        }
        return file;
    }

    private synchronized static SQLiteDatabase getCityDb() {
        if (cityDb == null || !cityDb.isOpen()) {
            File dbFile = getQuestionDBFile();
            if (!dbFile.exists() || dbFile.length() <= 0) {
                copyAssetDBToFile(dbFile);
            }
            try {
                cityDb = SQLiteDatabase.openDatabase(dbFile.getPath(), null, SQLiteDatabase.OPEN_READWRITE);
                Cursor cursor = cityDb.rawQuery("select max(version) from t_version", null);
                boolean mustCopy = false;
                if (cursor.moveToNext()) {
                    int version = cursor.getInt(0);
                    if (version < DB_VERSION) {
                        Logger.w("ala", "getCityDb,find the newer db,must copy....");
                        mustCopy = true;
                    }
                } else {
                    mustCopy = true;
                }
                cursor.close();
                if (mustCopy) {
                    cityDb.close();
                    copyAssetDBToFile(dbFile);
                    cityDb = SQLiteDatabase.openDatabase(dbFile.getPath(), null, SQLiteDatabase.OPEN_READWRITE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                if (cityDb != null) {
                    cityDb.close();
                }
                copyAssetDBToFile(dbFile);
                cityDb = SQLiteDatabase.openDatabase(dbFile.getPath(), null, SQLiteDatabase.OPEN_READWRITE);
            }
        }
        return cityDb;
    }

    private static void closeDatabase() {
        if (cityDb != null) {
            try {
                DataUtils.close(cityDb);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
