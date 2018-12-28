package cn.pro47x.core.data;



import cn.pro47x.core.utils.DataUtils;
import cn.pro47x.core.utils.MiscUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * 城市编码到名字，或者名字到编码的映射
 *
 * @author lisiqi
 */
public class CityNameCodeMapping {

    private static final Map<String, String> code2Name = new LinkedHashMap<String, String>();
    private static final Map<String, String> name2Code = new LinkedHashMap<String, String>();

    static {
        init();
    }

    public static String getCityCode(String cityName) {
        if (MiscUtils.isEmpty(cityName)) {
            return "";
        }
        String value = name2Code.get(cityName);
        if (value == null) {
            for (String key : name2Code.keySet()) {
                if (cityName.startsWith(key)) {
                    value = name2Code.get(key);
                    break;
                }
            }
        }
        if (value == null && MiscUtils.isInteger(cityName)) {
            value = cityName;
        }
        return value;
    }

    public static String getCityName(String cityCode) {
        String value = code2Name.get(cityCode);
        if (value == null) {
            value = cityCode;
        }
        return value;
    }

    private static void init() {

        try {
            List<String> lines = DataUtils.readContentByLine(null, "core/data/city.name2code.txt");
            for (String s : lines) {
                if (MiscUtils.isEmpty(s) || s.startsWith("#")) {
                    continue;
                }
                String[] ss = s.split(":");
                name2Code.put(ss[0], ss[1]);
            }
        } catch (Exception ex) {
            // 什么都不做
        }
        try {
            List<String> lines = DataUtils.readContentByLine(null, "core/data/city.code2name.txt");
            for (String s : lines) {
                if (MiscUtils.isEmpty(s) || s.startsWith("#")) {
                    continue;
                }
                String[] ss = s.split(":");
                code2Name.put(ss[0], ss[1]);
            }
        } catch (Exception ex) {
            // 什么都不做
        }
    }
}
