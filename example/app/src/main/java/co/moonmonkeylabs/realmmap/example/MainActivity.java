package co.moonmonkeylabs.realmmap.example;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import co.moonmonkeylabs.realmmap.example.models.Business;
import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MainActivity extends AppCompatActivity {

    private Realm realm;

    private BusinessRealmClusterMapFragment realmClusterMapFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        resetRealm();
        realm = Realm.getInstance(this);

        realm.beginTransaction();
        final List<Business> businesses = loadBusinessesData();
        realm.copyToRealm(businesses);
        realm.commitTransaction();

        if (savedInstanceState == null) {
            realmClusterMapFragment = new BusinessRealmClusterMapFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.container, realmClusterMapFragment, "realmMap")
                    .commit();
        }
    }

    public final List<Business> loadBusinessesData() {
        List<Business> businesses = new ArrayList<>();

        InputStream is = getResources().openRawResource(R.raw.businesses);

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        try {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null && lineNumber < 500) {
                if (lineNumber++ == 0) {
                    continue;
                }

                String[] rowData = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                if (rowData[6].isEmpty()) {
                    continue;
                }

                businesses.add(new Business(
                        Integer.parseInt(rowData[0]),
                        removeQuotes(rowData[1]),
                        Float.parseFloat(removeQuotes(rowData[6])),
                        Float.parseFloat(removeQuotes(rowData[7]))));
            }
        }
        catch (IOException ex) {}
        finally {
            try {
                is.close();
            }
            catch (IOException e) {}
        }
        return businesses;
    }

    private String removeQuotes(String original) {
        return original.subSequence(1, original.length() - 1).toString();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null) {
            realm.close();
            realm = null;
        }
    }

    private void resetRealm() {
        RealmConfiguration realmConfig = new RealmConfiguration
                .Builder(this)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.deleteRealm(realmConfig);
    }
}
