package no.nordicsemi.android.meshprovisioner.models;

import android.os.Parcel;

public class LightHslServer extends SigModel {

    public static final Creator<LightHslServer> CREATOR = new Creator<LightHslServer>() {
        @Override
        public LightHslServer createFromParcel(final Parcel source) {
            return new LightHslServer((short) source.readInt());
        }

        @Override
        public LightHslServer[] newArray(final int size) {
            return new LightHslServer[size];
        }
    };

    public LightHslServer(final int modelId) {
        super(modelId);
    }

    @Override
    public String getModelName() {
        return "Light HSL Server";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeInt(mModelId);
    }
}
