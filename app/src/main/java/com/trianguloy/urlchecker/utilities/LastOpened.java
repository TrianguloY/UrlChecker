package com.trianguloy.urlchecker.utilities;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class LastOpened {

    private static final int N = 5;

    private final List<GenericPref.Str> list = new ArrayList<>(N);

    public LastOpened(Context cntx) {
        for (int i = 0; i < N; i++) {
            GenericPref.Str gp = new GenericPref.Str("opened" + i, null);
            gp.init(cntx);
            list.add(gp);
        }

        // debug
//        System.out.println(list);
    }

    public void sort(List<String> packs){
        for (int i = 0; i < N; i++) {
            final String pack = list.get(i).get();
            if(packs.contains(pack)){
                packs.remove(pack);
                packs.add(0, pack);
            }
        }
    }

    public void usedPackage(String pack){

        // check if already the most used, and stop
        if(pack.equals(list.get(N-1).get())) return;

        // check intermediate ones, and swap with previous
        for (int i = N-2; i >= 0; i--) {
            if(pack.equals(list.get(i).get())){
                String prev = list.get(i).get();
                list.get(i).set(list.get(i+1).get());
                list.get(i+1).set(prev);
                return;
            }
        }

        // if not, set as last
        list.get(0).set(pack);
    }
}
