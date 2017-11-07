package mods.eln.transparentnode.windturbine;

import mods.eln.misc.Coordonate;
import mods.eln.misc.INBTTReady;
import mods.eln.misc.Utils;
import mods.eln.sim.IProcess;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

class WindTurbineSlowProcess implements IProcess, INBTTReady {
    //private static final double localWinDeriveMax = 0.1;
    private static final double environmentTimeCounterReset = 10.0;
    private static final double localWindTimeCounterReset = 1.0;
    private static final double localWindMax = 3.0;
    private static final double localWinDeriveLostFactor = 0.3;
    private static final double localWinDeriveDeriveMax = 0.1;

    private final WindTurbineElement turbine;
    private final String name;

    private double environmentWindFactor = 0.0;
    private double environmentTimeCounter = 0;
    private double localWind = 0;
    private double localWindDerive = 0;
    private double localWindTimeCounter = 0;
    private int counter = 0;

    WindTurbineSlowProcess(String name, WindTurbineElement turbine) {
        this.turbine = turbine;
        this.name = name;
    }

    double getWind() {
        return Math.abs(localWind + Utils.getWind(turbine.node.coordonate.dimention, turbine.node.coordonate.y +
            turbine.descriptor.offY)) * environmentWindFactor;
    }

    void setWind(double wind) {
        this.localWind = wind;
    }

    @Override
    public void process(double time) {
        WindTurbineDescriptor d = turbine.descriptor;
        environmentTimeCounter -= time;
        if (environmentTimeCounter < 0.0) {
            environmentTimeCounter += environmentTimeCounterReset * (0.75 + Math.random() * 0.5);

            int x1, x2, y1, y2, z1, z2;

            Coordonate coord = new Coordonate(turbine.node.coordonate);

            x1 = coord.x - d.rayX;
            x2 = coord.x + d.rayX;
            y1 = coord.y - d.rayY + d.offY;
            y2 = coord.y + d.rayY + d.offY;
            z1 = coord.z - d.rayZ;
            z2 = coord.z + d.rayZ;

            int blockBusyCount = -d.blockMalusSubCount;
            boolean notInCache = false;
            if (turbine.node.coordonate.getWorldExist()) {
                World world = turbine.node.coordonate.world();
                //IChunkProvider chunk = world.getChunkProvider();

                for (int x = x1; x <= x2; x++) {
                    for (int y = y1; y <= y2; y++) {
                        for (int z = z1; z <= z2; z++) {
                            if (!world.blockExists(x, y, z)) {
                                notInCache = true;
                                break;
                            }
                            if (world.getBlock(x, y, z) != Blocks.air) {
                                blockBusyCount++;
                            }
                        }
                        if (notInCache) break;
                    }
                    if (notInCache) break;
                }
            } else {
                notInCache = true;
            }
            if (!notInCache) {
                environmentWindFactor = Math.max(0.0, Math.min(1.0, 1.0 - blockBusyCount * d.blockMalus));

                Utils.println("EnvironementWindFactor : " + environmentWindFactor);
            }
        }

        localWindTimeCounter -= time;
        if (localWindTimeCounter < 0) {
            localWindTimeCounter += localWindTimeCounterReset;

            localWindDerive *= 1 - (localWinDeriveLostFactor * localWindTimeCounterReset);
            localWindDerive += (Math.random() * 2.0 - 1.0) * localWinDeriveDeriveMax * localWindTimeCounterReset;
        }

        localWind += localWindDerive * time;

        if (localWind > localWindMax) {
            localWind = localWindMax;
            localWindDerive = 0.0;
        }
        if (localWind < -localWindMax) {
            localWind = -localWindMax;
            localWindDerive = 0.0;
        }

        localWind = 0;
        double P;
        double wind = getWind();

        /*if (wind > d.maxWind) {
            if (Math.random() < (wind - d.maxWind) * 0.02) {
                turbine.selfDestroy();
            }
        }*/

        P = d.PfW.getValue(wind);

        turbine.powerSource.setP(P);
        turbine.powerSource.setUmax(d.maxVoltage);

        counter++;
        if (counter % 20 == 0) {
            Utils.println("Wind : " + getWind() + "  Derivate : " + localWindDerive + " EPmax : " + P);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt, String str) {
        localWind = nbt.getDouble(str + name + "localWind");
        environmentWindFactor = nbt.getDouble(str + name + "environementWindFactor");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt, String str) {
        nbt.setDouble(str + name + "localWind", localWind);
        nbt.setDouble(str + name + "environementWindFactor", environmentWindFactor);
    }
}
