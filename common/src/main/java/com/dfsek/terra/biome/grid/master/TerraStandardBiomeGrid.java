package com.dfsek.terra.biome.grid.master;

import com.dfsek.terra.api.math.vector.Location;
import com.dfsek.terra.api.math.vector.Vector2;
import com.dfsek.terra.api.world.biome.Biome;
import com.dfsek.terra.api.world.biome.BiomeGrid;
import com.dfsek.terra.api.world.generation.GenerationPhase;
import com.dfsek.terra.biome.BiomeZone;
import com.dfsek.terra.biome.UserDefinedBiome;
import com.dfsek.terra.biome.postprocessing.CoordinatePerturb;
import com.dfsek.terra.biome.postprocessing.ErosionNoise;
import com.dfsek.terra.config.base.ConfigPack;
import com.dfsek.terra.config.base.ConfigPackTemplate;

public class TerraStandardBiomeGrid extends TerraBiomeGrid {
    private CoordinatePerturb perturb;
    private ErosionNoise erode;

    public TerraStandardBiomeGrid(long seed, BiomeZone zone, ConfigPack c) {
        super(seed, 0, 0, zone);
        ConfigPackTemplate t = c.getTemplate();
        if(c.getTemplate().isBlend()) {
            perturb = new CoordinatePerturb(t.getBlendFreq(), t.getBlendAmp(), seed);
        }
        if(c.getTemplate().isErode()) {
            erode = new ErosionNoise(t.getErodeFreq(), t.getErodeThresh(), t.getErodeOctaves(), seed);
        }
    }

    @Override
    public BiomeGrid getGrid(int x, int z) {
        return zone.getGrid(x, z);
    }

    @Override
    public boolean isEroded(int x, int z) {
        return erode != null && erode.isEroded(x, z);
    }

    @Override
    public Biome getBiome(int x, int z, GenerationPhase phase) {
        int xp = x, zp = z;
        if(perturb != null && (phase.equals(GenerationPhase.PALETTE_APPLY) || phase.equals(GenerationPhase.POPULATE))) {
            Vector2 perturbCoords = perturb.getShiftedCoords(x, z);
            xp = (int) perturbCoords.getX();
            zp = (int) perturbCoords.getZ();
        }

        UserDefinedBiome b = (UserDefinedBiome) zone.getGrid(xp, zp).getBiome(xp, zp, phase);
        if(isEroded(xp, zp)) return b.getErode();
        return b;
    }


    @Override
    public Biome getBiome(Location l, GenerationPhase phase) {
        return getBiome(l.getBlockX(), l.getBlockZ(), phase);
    }
}
