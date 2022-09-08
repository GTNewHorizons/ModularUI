package com.gtnewhorizons.modularui.test;

import com.gtnewhorizons.modularui.api.UIInfos;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class TestBlock extends Block implements ITileEntityProvider {

    public TestBlock(Material p_i45394_1_) {
        super(p_i45394_1_);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TestTile();
    }

    @Override
    public boolean onBlockActivated(
            World p_149727_1_,
            int p_149727_2_,
            int p_149727_3_,
            int p_149727_4_,
            EntityPlayer p_149727_5_,
            int p_149727_6_,
            float p_149727_7_,
            float p_149727_8_,
            float p_149727_9_) {
        if (!p_149727_1_.isRemote) {
            UIInfos.TILE_MODULAR_UI.open(
                    p_149727_5_, p_149727_1_, Vec3.createVectorHelper(p_149727_2_, p_149727_3_, p_149727_4_));
        }
        return true;
    }
}
