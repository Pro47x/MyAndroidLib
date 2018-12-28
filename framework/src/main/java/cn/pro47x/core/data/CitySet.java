package cn.pro47x.core.data;

import android.os.Parcel;
import android.os.Parcelable;
import com.framework.core.utils.MiscUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * 城市集合对象，每一个对象代表一个城市列表的集合，此集合有两种组织形式，
 * 可能是以“省－市”的方式组织，也可能是以“拼音－城市”的方式组织，前者是自然的按省为单位组织，
 * 后者是以相同声母为单位进行组织。
 *
 * @author
 */
public class CitySet implements Serializable, Parcelable {

    private static final long serialVersionUID = 20120917L;
    private String label;
    private List<String> cityList;

    CitySet(Parcel in) {
        label = in.readString();
        int size = in.readInt();
        if (size > 0) {
            cityList = new ArrayList<String>();
            for (int i = 0; i < size; i++) {
                cityList.add(in.readString());
            }
        }
    }

    public CitySet() {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(label);
        if (MiscUtils.isEmpty(cityList)) {
            dest.writeInt(0);
        } else {
            dest.writeInt(cityList.size());
            for (String s : cityList) {
                dest.writeString(s);
            }
        }
    }

    public static final Creator<CitySet> CREATOR = new Creator<CitySet>() {

        @Override
        public CitySet createFromParcel(Parcel source) {
            return new CitySet(source);
        }

        @Override
        public CitySet[] newArray(int size) {
            return new CitySet[size];
        }
    };

    /**
     * 获取当前城市集合的名称，可能是省份名称，也可能是拼音首字母的名称
     *
     * @return 集合名称
     */
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * 获取当前城市集合对象下面的城市列表
     *
     * @return 城市列表
     */
    public List<String> getCityList() {
        return cityList;
    }

    public void setCityList(List<String> cityList) {
        this.cityList = cityList;
    }

    public String toString() {
        return label + ":" + cityList;
    }

}
