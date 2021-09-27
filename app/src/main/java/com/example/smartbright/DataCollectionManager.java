package com.example.smartbright;

import static com.example.smartbright.Definitions.DBG;

import android.util.Log;

import com.example.smartbright.dataprovider.DataProvider;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataCollectionManager {
    private static final String TAG = DataCollectionManager.class.getSimpleName();

    public static final int MAX_LIST_SIZE = 1000;

    private final DataProvider[] providers;
    private final LinkedList<Map<String, Object>> pastReadings;

    public DataCollectionManager(DataProvider... providers) {
        this.providers = providers;
        pastReadings = new LinkedList<>();
    }

    public void recordData() {
        pastReadings.add(getCurrentData());
        if (pastReadings.size() > MAX_LIST_SIZE) {
            pastReadings.removeFirst();
        }
        if (DBG) Log.v(TAG, "Added " + pastReadings.getLast());
    }

    public Map<String, Object> getCurrentData() {
        Stream<Map<String, Object>> mapsStream = Arrays.stream(providers).map(DataProvider::getData);
        Stream<Map.Entry<String, Object>> flattened = mapsStream.flatMap(m -> m.entrySet().stream());

        Map<String, Object> mergedMap = flattened.collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue
                )
        );

        return mergedMap;
    }
}
