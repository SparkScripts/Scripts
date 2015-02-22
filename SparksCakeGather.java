package scripts.cakegather.nodes;

import org.tribot.api.DynamicClicking;
import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.types.generic.Condition;
import org.tribot.api2007.Banking;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.NPCs;
import org.tribot.api2007.Objects;
import org.tribot.api2007.Player;
import org.tribot.api2007.Walking;
import org.tribot.api2007.WebWalking;
import org.tribot.api2007.types.RSNPC;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.types.RSTile;
import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;

@ScriptManifest(authors = { "§park" }, category = "Tools", name = "SparksCakeWalk")
public class SparksCakeGather extends Script {

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
		}, General.random(1000, 1200));
		if (Timing.waitCondition(new Condition() {

			@Override
			public boolean active() {
				return isTakingCake();
			}
		}, General.random(8000, 9000))) {
			this.cake_tile = cake[0].getPosition().clone();
			return true;
		}
		return false;
	}

	private boolean bank(){ 
	General.useAntiBanCompliance(true);
		if(!Banking.isBankScreenOpen()){
			if(!Banking.openBank())
				return false;
		}
		if(Banking.depositAll() < 1){
			return false;
		}
		return Timing.waitCondition(new Condition() {
			
			@Override
			public boolean active() {
				return !Inventory.isFull();
			}
		}, General.random(3000, 4000));
	}
	
	@Override

	public void run() {
		while(true){
			sleep(125);
			if(isAtCake()){
				if(Inventory.isFull()){
					walkToBank();
				} else
					takeCake();
			} else if (isInBank()){
				if(Inventory.isFull())
					bank();
				else {
					walkToCake();
				}
			} else {
				if(Inventory.isFull())
					walkToBank();
				else
					walkToCake();
			}
		}

	}

}
