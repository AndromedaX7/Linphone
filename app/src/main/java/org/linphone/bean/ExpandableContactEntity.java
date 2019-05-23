package org.linphone.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by miao on 2018/10/25.
 */
public class ExpandableContactEntity {

    public ExpandableContactEntity() {
    }

    private ArrayList<ArrayList<ContactEntity>> dataSet;

    public ArrayList< ArrayList<ContactEntity>> getDataSet() {
        return dataSet;
    }

    public void setDataSet(ArrayList< ArrayList<ContactEntity>> dataSet) {
        this.dataSet = dataSet;
    }

    public ExpandableContactEntity(ArrayList< ArrayList<ContactEntity>> dataSet) {

        this.dataSet = dataSet;
    }

    public  static  class ContactEntity implements Parcelable {
        private String classification;
        private String name;
        private boolean check;

        public ContactEntity() {
        }

        public String getClassification() {

            return classification;
        }

        public void setClassification(String classification) {
            this.classification = classification;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public ContactEntity(String classification, String name, String id) {

            this.classification = classification;
            this.name = name;
            this.id = id;
        }

        private String id;

        public void setCheck(boolean check) {
            this.check = check;
        }

        public boolean getCheck() {
            return check;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.classification);
            dest.writeString(this.name);
            dest.writeByte(this.check ? (byte) 1 : (byte) 0);
            dest.writeString(this.id);
        }

        protected ContactEntity(Parcel in) {
            this.classification = in.readString();
            this.name = in.readString();
            this.check = in.readByte() != 0;
            this.id = in.readString();
        }

        public static final Creator<ContactEntity> CREATOR = new Creator<ContactEntity>() {
            @Override
            public ContactEntity createFromParcel(Parcel source) {
                return new ContactEntity(source);
            }

            @Override
            public ContactEntity[] newArray(int size) {
                return new ContactEntity[size];
            }
        };
    }
}
