package scripts.InventoryListener;

public interface InventoryListener {
	public void inventoryItemGained(int id, int count);
	public void inventoryItemLost(int id, int count);

}
