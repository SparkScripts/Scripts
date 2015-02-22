package scripts.cakewalk;

import java.awt.*; 

import org.tribot.api.Timing;
import org.tribot.script.interfaces.Painting;
import org.tribot.api.DynamicClicking;
import org.tribot.api.General;
import org.tribot.api.types.generic.Condition;
import org.tribot.api2007.Banking;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.NPCs;
import org.tribot.api2007.Objects;
import org.tribot.api2007.Player;
import org.tribot.api2007.Walking;
import org.tribot.api2007.WebWalking;
import org.tribot.api2007.types.RSItemDefinition;
import org.tribot.api2007.types.RSNPC;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.types.RSTile;
import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;

import scripts.InventoryListener.InventoryListener;
import scripts.InventoryListener.InventoryObserver;

@ScriptManifest(authors = { "§park" }, category = "Tools", name = "SparksCakeWalk", description = "Picks up sclieces of cake from birthday cake in Varrock Square.", version = 1.2)
public class SparksCakeWalk extends Script implements Painting,
		InventoryListener {

	RSTile cake_tile = null;

	private boolean isInBank() {
		final RSObject[] bank = Objects.findNearest(40, "Bank booth");
		if (bank.length > 1) {
			if (bank[0].isOnScreen())
				return true;
		}
		final RSNPC[] bankers = NPCs.findNearest("Banker");
		if (bankers.length < 1)
			return false;
		return bankers[0].isOnScreen();
	}

	private boolean isAtCake() {
		final RSObject[] cake = Objects.findNearest(40, "Birthday Cake");
		if (cake.length < 1)
			return false;
		return cake[0].isOnScreen();
	}

	private boolean walkToCake() {
		final RSObject[] cake = Objects.findNearest(50, "Birthday Cake");
		if (cake.length < 1)
			return false;
		if (!WebWalking.walkTo(cake[0]))
			return false;
		return Timing.waitCondition(new Condition() {

			@Override
			public boolean active() {
				General.random(200, 300);
				return isAtCake();
			}
		}, General.random(7000, 8000));
	}

	private boolean walkToBank() {
		if (!WebWalking.walkToBank()) {
			return false;
		}
		return Timing.waitCondition(new Condition() {

			@Override
			public boolean active() {
				General.random(200, 300);
				return isInBank();
			}
		}, General.random(8000, 9000));
	}

	private boolean isTakingCake() {
		return Player.getAnimation() > 0;
	}

	private boolean takeCake() {
		General.useAntiBanCompliance(true);
		if (isTakingCake()) {
			final long timeout = System.currentTimeMillis()
					+ General.random(60000, 80000);
			while (isTakingCake() && System.currentTimeMillis() < timeout) {
				sleep(100, 150);
				if (this.cake_tile != null) {
					if (!Objects.isAt(this.cake_tile, "Birthday Cake")) {
						break;
					}
				}
			}
		}
		final RSObject[] cake = Objects.findNearest(40, "Birthday Cake");
		if (cake.length < 1)
			return false;
		if (!cake[0].isOnScreen()) {
			if (!Walking.walkPath(Walking.generateStraightScreenPath(cake[0])))
				return false;
			if (!Timing.waitCondition(new Condition() {

				@Override
				public boolean active() {
					General.sleep(200);
					return cake[0].isOnScreen();
				}
			}, General.random(8000, 9000)))
				return false;
		}
		if (!DynamicClicking.clickRSObject(cake[0], "Take-slice"))
			return false;
		Timing.waitCondition(new Condition() {

			@Override
			public boolean active() {
				return !isTakingCake();
			}
		}, General.random(800, 1000));
		if (Timing.waitCondition(new Condition() {

			@Override
			public boolean active() {
				return isTakingCake();
			}
		}, General.random(5000, 6000))) {
			this.cake_tile = cake[0].getPosition().clone();
			return true;
		}
		return false;
	}

	private boolean bank() {
		General.useAntiBanCompliance(true);
		if (!Banking.isBankScreenOpen()) {
			if (!Banking.openBank())
				return false;
		}
		if (Banking.depositAll() < 1) {
			return false;
		}
		return Timing.waitCondition(new Condition() {

			@Override
			public boolean active() {
				return !Inventory.isFull();
			}
		}, General.random(3000, 4000));
	}

	// Provided by daxmagex

	public int onStart() {

		InventoryObserver inventoryObserver = new InventoryObserver(
				new Condition() {

					@Override
					public boolean active() {
						return !Banking.isBankScreenOpen();
					}
				});
		inventoryObserver.addListener(this);
		inventoryObserver.start();
		return cakes++;
	}

	@Override
	public void run() {
		while (true) {
			sleep(125);
			if (isAtCake()) {
				if (Inventory.isFull()) {
					walkToBank();
				} else
					takeCake();
			} else if (isInBank()) {
				if (Inventory.isFull())
					bank();
				else {
					walkToCake();
				}
			} else {
				if (Inventory.isFull())
					walkToBank();
				else
					walkToCake();
			}
		}

	}

	public int cakes = onStart();
	public long cakesGained = 0;

	// Provided by daxmagex
	@Override
	public void inventoryItemGained(int cakeID, int count) {
		General.println("Gained " + RSItemDefinition.get(cakeID).getName()
				+ " " + count);
		cakesGained++;
	}

	// Provided by daxmagex
	@Override
	public void inventoryItemLost(int cakeID, int count) {
		General.println("Lost " + RSItemDefinition.get(cakeID).getName() + " "
				+ count);
	}

	private final Color color1 = new Color(153, 153, 255);
	private final Color color2 = new Color(0, 0, 0);
	private final Color color3 = new Color(255, 255, 255);
	private final Color color4 = new Color(255, 0, 51);

	private final BasicStroke stroke1 = new BasicStroke(1);

	private final Font font1 = new Font("Arial", 1, 28);
	private final Font font2 = new Font("Arial", 1, 22);
	private final Font font3 = new Font("Arial", 1, 12);

	private static final long startTime = System.currentTimeMillis();

	public void onPaint(Graphics g1) {
		long timeRan = System.currentTimeMillis() - startTime;
		Graphics2D g = (Graphics2D) g1;

		g.setColor(color1);
		g.fillRoundRect(11, 348, 483, 108, 16, 16);
		g.setColor(color2);
		g.setStroke(stroke1);
		g.drawRoundRect(11, 348, 483, 108, 16, 16);
		g.setFont(font1);
		g.setColor(color3);
		g.drawString("§parksCakeGather", 16, 375);
		g.setColor(color4);
		g.fillOval(471, 357, 17, 17);
		g.setColor(color2);
		g.drawOval(471, 357, 17, 17);
		g.setFont(font2);
		g.setColor(color3);
		g.drawString("Cake Gathered:" + cakesGained, 21, 414);
		// g.drawString("Cake per/h:" , 267, 413);
		g.drawString("Run Time: " + Timing.msToString(timeRan), 20, 446);
		g.setFont(font3);
		g.drawString("Version: 1.2", 422, 451);

	}

}
