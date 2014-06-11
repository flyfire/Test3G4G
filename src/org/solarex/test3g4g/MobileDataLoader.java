
package org.solarex.test3g4g;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.Bundle;
import android.net.NetworkTemplate;

import static android.net.NetworkStatsHistory.FIELD_RX_BYTES;
import static android.net.NetworkStatsHistory.FIELD_TX_BYTES;
import android.net.INetworkStatsSession;
import android.net.NetworkStatsHistory;
import android.net.NetworkTemplate;

import static org.solarex.test3g4g.Utils.log;
import static org.solarex.test3g4g.Utils.computeMonthBeginTime;

public class MobileDataLoader extends AsyncTaskLoader<NetworkStatsHistory> {
    private INetworkStatsSession mStatsSession = null;
    private Bundle mArgs = null;
    
    public MobileDataLoader(Context ctx, 
            INetworkStatsSession networkStatsSession, 
            Bundle args) {
        super(ctx);
        this.mStatsSession = networkStatsSession;
        this.mArgs = args;
    }

    public static Bundle buildArgs(NetworkTemplate networkTemplate) {
        final Bundle args = new Bundle();
        args.putParcelable("template", networkTemplate);
        log("buildArgs: args = " + args);
        return args;
    }

    @Override
    public NetworkStatsHistory loadInBackground() {
        NetworkTemplate mNetworkTemplate = this.mArgs.getParcelable("template");
        NetworkStatsHistory data = null;
        try{
            data = this.mStatsSession.getHistoryForNetwork(mNetworkTemplate, 
                    FIELD_RX_BYTES | FIELD_TX_BYTES );
        } catch(Exception ex){
            log("Exception happened, ex = " + ex.getMessage());
        }
        log("loadInBackground: data = " + data + " for NetworkTemplate = " + mNetworkTemplate);
        return data;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();

        log("onStartLoading");
    }

    @Override
    protected void onStopLoading() {
        super.onStopLoading();
        cancelLoad();

        log("onStopLoading ");
    }

    @Override
    protected void onReset() {
        super.onReset();
        cancelLoad();

        log("onReset ");
    }


}
