package faubiguy.xpupgrade;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.io.*;
import dan200.computer.api.IComputerAccess;
import dan200.turtle.api.ITurtleAccess;
import dan200.turtle.api.ITurtlePeripheral;
import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.Block;
import net.minecraft.src.EnchantmentData;
import net.minecraft.src.EnchantmentHelper;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityXPOrb;
import net.minecraft.src.Item;
import net.minecraft.src.ItemBlock;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Vec3;
import net.minecraft.src.World;

public class XPUpgradePeripheral implements ITurtlePeripheral {
	
	public int experience = 0;
	public int experienceRemainder = 0;
	public int experienceLevel = 0;
	public ITurtleAccess turtle;
	public IComputerAccess computer;
	public boolean autoCollect = false;
	public Random rand = new Random();
	public String errorMessage;
	
	public XPUpgradePeripheral(ITurtleAccess turt) {
		turtle = turt;
	}
	
	public static String[] methods = {
		"collect",
		"getXP",
		"getLevel",
		"enchant",
		"enchantUp",
		"enchantDown",
		"enchantWithItem",
		"anvilUp",
		"anvil",
		"anvilDown",
		"anvilWithItem",
		"calculateAnvilCostXP",
		"calculateAnvilCostLevels",
		"dropXP",
		"dropLevels",
		"setAuto",
		"setLevel",
		"resetXP"
	};

	@Override
	public String getType() {
		return "XPUpgrade";
	}

	@Override
	public String[] getMethodNames() {
		return methods;
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, int method,
			Object[] arguments) throws Exception {
		Object result = null;
		errorMessage = null;
			int offset = 0;
			boolean noSlot2 = false;
			switch (method) {
			case 0: //collect
				result = collectOrbs(true);
				break;
			case 1: //getXP
				result = (Double)(double) experience;
				break;
			case 2: //getLevel
				result = (Double)(double) experienceLevel;
				break;
			case 3: //enchant
			case 4: //enchantUp
			case 5: //enchantDown
			case 6: //enchantWithItem
				if ((!(arguments.length == 2 && !(method == 6 )) && !(arguments.length == 3 && (method == 6)))) {
					throw new Exception("Invalid number of arguments");
				}
				offset = method == 6 ? 1 : 0;
				result = enchant(((Double)arguments[0+offset]).intValue(), ((Double)arguments[1+offset]).intValue(), method, method == 6 ? ((Double) arguments[0]).intValue() : 0, false, null);
				break;
			case 7: //anvil
			case 8: //anvilUp
			case 9: //anvilDown
			case 10: //anvilWithItem
				if (!((arguments.length == 2 || arguments.length == 3) && !(method == 10)) && !(arguments.length == 3 || arguments.length == 4) && (method == 10)) {
					throw new Exception("Invalid number of arguments");
				}
				offset = method == 10 ? 1 : 0;
				noSlot2 = arguments.length == 0 && method == 10;
				result = enchant(((Double)arguments[1+offset]).intValue(), noSlot2 ? -1 : ((Double)arguments[2+offset]).intValue(), method-4, method == 10 ? ((Double) arguments[0]).intValue() : 0, true, (String)arguments[0+offset]);
				break;
			case 11: //calculateAnvilCostXP
			case 12: //calculateAnvilCostLevels
				if (arguments.length == 2) {
					noSlot2 = true;
				} else if (arguments.length != 3) {
					throw new Exception("Invalid number of arguments");
				}
				result = anvilCost((String)arguments[0], ((Double) arguments[1]).intValue(), noSlot2 ? -1 : ((Double) arguments[2]).intValue(), method == 12);
				break;
			case 13: //dropXP
			case 14: //dropLevels
				if (arguments.length != 1) {
					throw new Exception("Invalid number of arguments");
				}
				result = dropXP(((Double) arguments[0]).intValue(), (method == 14));
				break;
			case 15: //setAuto
				if (arguments.length != 1) {
					throw new Exception("Invalid number of arguments");
				}
				autoCollect = (Boolean)arguments[0];
				saveXP();
				break;
			case 16: //setLevel
				if (arguments.length != 1) {
					throw new Exception("Invalid number of arguments");
				}
				if (!XPUpgradeMain.setLevelEnabled) {
					throw new Exception("setLevel not enabled");
				}
				int level = ((Double)arguments[0]).intValue();
				addLevels(level < experienceLevel ? -level : level, true);
				result = true;
				saveXP();
				break;
			case 17: //resetXP
				experience = 0;
				experienceLevel = 0;
				experienceRemainder = 0;
				saveXP();
				result = true;
				break;
			}
			return new Object[] {result,errorMessage};
	}

	private Object anvilCost(String string, int intValue, int i, boolean b) {
		return null;
	}

	private boolean dropXP(int exp, boolean level) {
		int expToDrop = level ? calculateLevelXP(experienceLevel) - calculateLevelXP(exp) : exp;
		if (level ? experienceLevel < exp : experience < expToDrop) {
			errorMessage = "Not enough XP";
			return false;
		}
		Vec3 turtlePos = turtle.getPosition();
		int facing = turtle.getFacingDir();
		switch (facing) {
		case 3:
			turtlePos.zCoord++;
			break;
		case 4:
			turtlePos.xCoord--;
			break;
		case 2:
			turtlePos.zCoord--;
			break;
		case 5:
			turtlePos.xCoord++;
			break;
		}
		World world = turtle.getWorld();
		if (world.isAirBlock((int)turtlePos.xCoord, (int)turtlePos.yCoord, (int)turtlePos.zCoord)) {
			addExperience(-expToDrop);
			world.spawnEntityInWorld(new EntityXPOrb(turtle.getWorld(), turtlePos.xCoord, turtlePos.yCoord, turtlePos.zCoord, (int)expToDrop));
		} else {
			errorMessage = "No space";
			return false;
		}
		return true;
	}

	@Override
	public boolean canAttachToSide(int side) {
		return true;
	}

	@Override
	public void attach(IComputerAccess computer, String computerSide) {
		this.computer = computer;
		String saveDir = turtle.getWorld().getSaveHandler().getSaveDirectoryName();
		File XPPath = new File("saves" + File.separator + saveDir, "Turtle-XP");
		if (!XPPath.isDirectory()) {
			return;
		}
		XPPath = new File(XPPath, String.valueOf(computer.getID()));
		if (!XPPath.isFile()) {
			return;
		}
		try {
			DataInputStream XPFile = new DataInputStream(new BufferedInputStream(new FileInputStream(XPPath)));
			experience = XPFile.readInt();
			experienceLevel = XPFile.readInt();
			experienceRemainder = XPFile.readInt();
			autoCollect = XPFile.readBoolean();
			XPFile.close();
		} catch (IOException e) {
		}
	}

	public boolean saveXP() {
		String saveDir = turtle.getWorld().getSaveHandler().getSaveDirectoryName();
		File XPPath = new File("saves" + File.separator + saveDir, "Turtle-XP");
		XPPath.mkdirs();
		XPPath = new File(XPPath, String.valueOf(computer.getID()));
		try {
			XPPath.createNewFile();
			DataOutputStream XPFile = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(XPPath)));
			XPFile.writeInt(experience);
			XPFile.writeInt(experienceLevel);
			XPFile.writeInt(experienceRemainder);
			XPFile.writeBoolean(autoCollect);
			XPFile.close();
		} catch (IOException e) {
			return false;
		}
		return true;
	}
	
	@Override
	public void detach(IComputerAccess computer) {
	}

	@Override
	public void update() {
		if (autoCollect) {
			if (collectOrbs(false)) {
				computer.queueEvent("XP");
			}
		}
	}
	
	public boolean collectOrbs(boolean methodCalled) {
		Vec3 pos = turtle.getPosition();
		AxisAlignedBB xpBounds = AxisAlignedBB.getBoundingBox(pos.xCoord - 0.5F, pos.yCoord - 0.5F, pos.zCoord - 0.5F, pos.xCoord + 1.5F, pos.yCoord + 1.5F, pos.zCoord + 1.5F);
		List<EntityXPOrb> orbs = turtle.getWorld().getEntitiesWithinAABB(EntityXPOrb.class, xpBounds);
		if (orbs.isEmpty()) {
			if (methodCalled) {
				errorMessage = "No XP to collect";
			}
			return false;
		}
		for (EntityXPOrb orb : orbs) {
			addExperience(orb.getXpValue());
			orb.setDead();
		}
		saveXP();
		return true;
	}
	
	public boolean enchant(int slot, int level, int facing, int tableSlot, boolean anvil, String newname) throws Exception {
		Vec3 tablePos = turtle.getPosition();
		if (facing == 3) {
			facing = turtle.getFacingDir();
		} else if (facing == 4 || facing == 5) {
			facing = 5 - facing;
		}
		switch (facing) {
		case 3:
			tablePos.zCoord++;
			break;
		case 4:
			tablePos.xCoord--;
			break;
		case 2:
			tablePos.zCoord--;
			break;
		case 5:
			tablePos.xCoord++;
			break;
		case 1:
			tablePos.yCoord++;
			break;
		case 0:
			tablePos.yCoord--;
			break;
		case 6:
			tablePos = null;
			break;			
		}
		tableSlot--;
		if (!(tableSlot >= -1 && tableSlot < 16)) {
			throw new Exception("Invalid slot");
		}
		int idToMatch = anvil ? Block.field_82510_ck.blockID : Block.enchantmentTable.blockID;
		if (tablePos == null) {
			ItemStack itemToMatch = turtle.getSlotContents(tableSlot);
			if (itemToMatch == null || itemToMatch.itemID != idToMatch) {
				errorMessage = "No enchantment table in that slot";
				return false;
			}
			
		} else if(turtle.getWorld().getBlockId((int)tablePos.xCoord, (int)tablePos.yCoord, (int)tablePos.zCoord) != idToMatch) {
			errorMessage = "No enchantment table to use";
			return false;
		}
		if (!anvil) {
			return enchantItem(slot-1, level);
		} else {
			return anvil(newname, slot-1, level-1);
		}
		
	}
	
    public boolean anvil(String name, int slot1, int slot2) {
    	errorMessage = "Anvils not supported";
		return false;
	}
    
    public int calculateLevelXP(int level) {
    	int levelXP = 0;
    	for (int currentLevel = 1;currentLevel <= level;currentLevel++) {
    		levelXP += levelXP(currentLevel);
    	}
    	return levelXP;
    }

	public boolean enchantItem(int slot, int level)
    {
        ItemStack itemToEnchant = turtle.getSlotContents(slot);

        if (level > 0 && level <= 30 && experienceLevel >= level)
        {
        	if (itemToEnchant == null) {
        		errorMessage = "No item in that slot";
        		return false;
        	}
        	if (itemToEnchant.isItemEnchanted()) {
        		errorMessage = "Item already enchanted";
        		return false;
        	}
            List var4 = EnchantmentHelper.buildEnchantmentList(this.rand, itemToEnchant, level);

            if (var4 != null)
            {
                addLevels(-level, true);
                Iterator var5 = var4.iterator();
               
                while (var5.hasNext())
                {
                    EnchantmentData var6 = (EnchantmentData)var5.next();
                    itemToEnchant.addEnchantment(var6.enchantmentobj, var6.enchantmentLevel);
                }
                saveXP();
                return true;
            }
            errorMessage = "No valid enchantments";
            return false ;
        }
        else
        {
        	errorMessage = "Not enough XP!";
            return false;
        }
    }
	
    public void addExperience(int par1)
    {
        int var2 = Integer.MAX_VALUE - this.experience;

        if (par1 > var2)
        {
            par1 = var2;
        }

        this.experienceRemainder += par1;

        for (this.experience += par1; this.experienceRemainder < 0 || this.experienceRemainder >= thisLevelXP(); this.experienceRemainder -= thisLevelXP() * (this.experienceRemainder < 0 ? -1 : 1))
        {
            this.addLevels(this.experienceRemainder < 0 ? -1 : 1, false);
        }
    }

    public void addLevels(int par1, boolean updateXP)
    {
        this.experienceLevel += par1;

        if (this.experienceLevel < 0)
        {
            this.experienceLevel = 0;
        }
        if (updateXP) {experience = calculateLevelXP(experienceLevel) + experienceRemainder;}
    }

    public int thisLevelXP()
    {
        return levelXP(experienceLevel);
    }
    
    public int levelXP(int level) {
    	return level >= 30 ? 62 + (level - 30) * 7 : (level >= 15 ? 17 + (level - 15) * 3 : 17);
    }

}