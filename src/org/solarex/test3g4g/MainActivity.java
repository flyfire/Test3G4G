package org.solarex.test3g4g;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import android.net.NetworkTemplate;
import android.net.NetworkStatsHistory;
import android.net.INetworkStatsService;
import android.net.INetworkStatsSession;
import android.os.ServiceManager;

import static org.solarex.test3g4g.Utils.log;
import static org.solarex.test3g4g.Utils.computeMonthBeginTime;
import static org.solarex.test3g4g.Utils.formatMB;

public class MainActivity extends Activity {
    public static TextView g3Data = null ;
    public static TextView g4Data = null;
    public static Button refresh = null;
    public static NetworkTemplate g3 = null;
    public static NetworkTemplate g4 = null;
    public static G3LoaderCallBack mG3LoaderCallBack = null;
    public static G4LoaderCallBack mG4LoaderCallBack = null;
    private INetworkStatsService mStatsService = null;
    public static INetworkStatsSession mStatsSession;
    public static LoaderManager mLoadManager = null;
    private int G3_LOAD = 1;
    private int G4_LOAD = 2;
    final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            refresh.setEnabled(true);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        g3Data = (TextView)this.findViewById(R.id.g3data);
        g4Data = (TextView)this.findViewById(R.id.g4data);
        refresh = (Button)this.findViewById(R.id.btn);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new StatsServiceForceUpdateTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mStatsService);  
                refresh.setEnabled(false);
                handler.sendEmptyMessageDelayed(0, 5000);
            }
        });
        
        g3 = NetworkTemplate.buildTemplateMobile3gLower(Utils.getActiveSubscriberId(this));
        g4 = NetworkTemplate.buildTemplateMobile4g(Utils.getActiveSubscriberId(this));
        mStatsService = INetworkStatsService.Stub.asInterface(ServiceManager.getService(Context.NETWORK_STATS_SERVICE));
        try{
            mStatsSession = mStatsService.openSession();
        }catch(Exception ex){
            log("Exception happened, ex = " + ex.getMessage());
        }
        mLoadManager = this.getLoaderManager();
        
        this.mG3LoaderCallBack = new G3LoaderCallBack();
        this.mG4LoaderCallBack = new G4LoaderCallBack();
        
        mLoadManager.initLoader(G3_LOAD, MobileDataLoader.buildArgs(g3), this.mG3LoaderCallBack);
        mLoadManager.initLoader(G4_LOAD, MobileDataLoader.buildArgs(g4), this.mG4LoaderCallBack);
        
        log("g3 = " + g3 + "\n" + "g4 = " + g4);
        log("mStatsService = " + mStatsService);
        log("mStatsSession = " + mStatsSession);
        log("mLoadManager = " + mLoadManager);
        log("mG3LoaderCallBack = " + mG3LoaderCallBack);
        log("mG4LoaderCallBack = " + mG4LoaderCallBack);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new StatsServiceForceUpdateTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, this.mStatsService);
        log("onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        log("onPause");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //unnecessary,because already set id for view component
    }
    
    class G3LoaderCallBack implements LoaderManager.LoaderCallbacks<NetworkStatsHistory>{

        @Override
        public Loader<NetworkStatsHistory> onCreateLoader(int id, Bundle args) {
            log("onCreateLoader: id = " + id + " args = " + args);
            return new MobileDataLoader(getApplicationContext(), MainActivity.mStatsSession, args);
        }

        @Override
        public void onLoadFinished(Loader<NetworkStatsHistory> loader, NetworkStatsHistory data) {
            if ( null == data ){
                MainActivity.g3Data.setText("No Data");
            } else {
                long begin = computeMonthBeginTime();
                long end = System.currentTimeMillis();

                NetworkStatsHistory.Entry g3Entry = data.getValues(begin, end, end, null);
                long monthBytes = g3Entry != null ? (g3Entry.rxBytes + g3Entry.txBytes) : 0;

                log("loader = " + loader + "\ndata = " + data);
                log("begin = " + begin + " end = " + end + " monthBytes = " + monthBytes);

                MainActivity.g3Data.setText(formatMB(monthBytes));

            }

        }

        @Override
        public void onLoaderReset(Loader<NetworkStatsHistory> loader) {
            MainActivity.g3Data.setText("Unknown");
        }
        
    }
    
    class G4LoaderCallBack implements LoaderManager.LoaderCallbacks<NetworkStatsHistory>{
        
        @Override
        public Loader<NetworkStatsHistory> onCreateLoader(int id, Bundle args) {
            log("onCreateLoader: id = " + id + " args = " + args);
            return new MobileDataLoader(getApplicationContext(), MainActivity.mStatsSession, args);
        }

        @Override
        public void onLoadFinished(Loader<NetworkStatsHistory> loader, NetworkStatsHistory data) {
            if ( null == data ){
                MainActivity.g4Data.setText("No Data");
            } else {
                long begin = computeMonthBeginTime();
                long end = System.currentTimeMillis();

                NetworkStatsHistory.Entry g4Entry = data.getValues(begin, end, end, null);
                long monthBytes = g4Entry != null ? (g4Entry.rxBytes + g4Entry.txBytes) : 0;

                log("loader = " + loader + "\ndata = " + data);
                log("begin = " + begin + " end = " + end + " monthBytes = " + monthBytes);

                MainActivity.g4Data.setText(formatMB(monthBytes));

            }

        }

        @Override
        public void onLoaderReset(Loader<NetworkStatsHistory> loader) {
            MainActivity.g4Data.setText("Unknown");
        }
        
    }
    
    class StatsServiceForceUpdateTask extends AsyncTask<INetworkStatsService, Void, Void>{

        @Override
        protected Void doInBackground(INetworkStatsService... params) {
            INetworkStatsService mStatsService = params[0];
            log("mStatsSession = " + mStatsSession);
            try{
                mStatsService.forceUpdate();
            } catch (Exception ex){
                log("Exception happened, ex = " + ex.getMessage());
            }
            return null;
        }
        
        @Override
        protected void onPostExecute(Void result) {
            log("onPostExecute");
            MainActivity.mLoadManager.restartLoader(G3_LOAD, MobileDataLoader.buildArgs(MainActivity.g3), MainActivity.mG3LoaderCallBack);
            MainActivity.mLoadManager.restartLoader(G4_LOAD, MobileDataLoader.buildArgs(MainActivity.g4), MainActivity.mG4LoaderCallBack);
        }
        
    }
    
}

