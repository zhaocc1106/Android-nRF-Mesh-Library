package no.nordicsemi.android.meshprovisioner.models;

import android.os.Parcel;

public class SchedulerServer extends SigModel {

    public static final Creator<SchedulerServer> CREATOR = new Creator<SchedulerServer>() {
        @Override
        public SchedulerServer createFromParcel(final Parcel source) {
            return new SchedulerServer((short) source.readInt());
        }

        @Override
        public SchedulerServer[] newArray(final int size) {
            return new SchedulerServer[size];
        }
    };

    public SchedulerServer(final int modelId) {
        super(modelId);
    }

    @Override
    public String getModelName() {
        return "Scheduler Server";
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
