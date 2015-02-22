package scripts.InventoryListener;

import org.tribot.api.General;
import org.tribot.api.types.generic.Condition;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.Login;
import org.tribot.api2007.types.RSItem;

import java.util.ArrayList;
import java.util.HashMap;

public class InventoryObserver extends Thread{

    private ArrayList<InventoryListener> listeners;
    private Condition condition;

    public InventoryObserver(Condition condition){
        this.listeners = new ArrayList<InventoryListener>();
        this.condition = condition;
    }

    @Override
    public void run(){

        while (Login.getLoginState() != Login.STATE.INGAME){
            General.sleep(500);
        }

        HashMap<Integer, Integer> map = inventoryHashMap();

        while (true){

            General.sleep(100);

            if (Login.getLoginState() != Login.STATE.INGAME)
                continue;


            if (!condition.active()){
                map = inventoryHashMap();
                continue;
            }

            HashMap<Integer, Integer> updatedMap = inventoryHashMap();

            for (Integer i : updatedMap.keySet()){
                int countInitial = map.containsKey(i) ? map.get(i) : 0, countFinal = updatedMap.get(i);
                if (countFinal > countInitial){
                    addTrigger(i, countFinal - countInitial);
                } else if (countFinal < countInitial){
                    subtractedTrigger(i, countInitial - countFinal);
                }
                map.remove(i);
            }

            for (Integer i : map.keySet())
                if (!updatedMap.containsKey(i))
                    subtractedTrigger(i, map.get(i));

            map = updatedMap;

        }
    }

    public HashMap<Integer, Integer> inventoryHashMap(){
        HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
        for (RSItem item : Inventory.getAll()){
            map.put(item.getID(), Inventory.getCount(item.getID()));
        }
        return map;
    }

    public void addListener(InventoryListener inventoryListener){
        listeners.add(inventoryListener);
    }

    public void addTrigger(int id, int count){
        for (InventoryListener l : listeners)
            l.inventoryItemGained(id, count);
    }

    public void subtractedTrigger(int id, int count){
        for (InventoryListener l : listeners)
            l.inventoryItemLost(id, count);
    }

}