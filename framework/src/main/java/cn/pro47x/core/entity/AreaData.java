package cn.pro47x.core.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class AreaData implements Comparable<AreaData>,Parcelable {

    private String name;

    private String pinyin;

    private String code;

    private boolean isMunicipality;

    private String firstLetter;

    private String parentCode;

    private boolean isAdministeredCounty;

    public AreaData(String name) {
        this.name = name;
    }

    public AreaData() {
    }

    public AreaData(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isMunicipality() {
        return isMunicipality;
    }

    public void setIsMunicipality(boolean isMunicipality) {
        this.isMunicipality = isMunicipality;
    }

    public String getFirstLetter() {
        return firstLetter;
    }

    public void setFirstLetter(String firstLetter) {
        this.firstLetter = firstLetter;
    }

    public String getParentCode() {
        return parentCode;
    }

    public void setParentCode(String parentCode) {
        this.parentCode = parentCode;
    }

    public boolean isAdministeredCounty() {
        return isAdministeredCounty;
    }

    public void setIsAdministeredCounty(boolean isAdministeredCounty) {
        this.isAdministeredCounty = isAdministeredCounty;
    }

    public static Creator<AreaData> getCREATOR() {
        return CREATOR;
    }

    public int compareTo(AreaData another) {
        return this.pinyin.compareTo(another.pinyin);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.pinyin);
        dest.writeString(this.code);
        dest.writeByte(isMunicipality ? (byte) 1 : (byte) 0);
        dest.writeString(this.firstLetter);
    }

    protected AreaData(Parcel in) {
        this.name = in.readString();
        this.pinyin = in.readString();
        this.code = in.readString();
        this.isMunicipality = in.readByte() != 0;
        this.firstLetter = in.readString();
    }

    public static final Creator<AreaData> CREATOR = new Creator<AreaData>() {
        public AreaData createFromParcel(Parcel source) {
            return new AreaData(source);
        }

        public AreaData[] newArray(int size) {
            return new AreaData[size];
        }
    };
}