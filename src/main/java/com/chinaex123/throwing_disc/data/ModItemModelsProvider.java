package com.chinaex123.throwing_disc.data;

import com.chinaex123.throwing_disc.ThrowingDisc;
import com.chinaex123.throwing_disc.api.DiscTier;
import com.chinaex123.throwing_disc.api.ThrowingDiscAPI;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelsProvider extends ItemModelProvider {
    public ModItemModelsProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ThrowingDisc.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        for (DiscTier tier : ThrowingDiscAPI.tiers()) {
            String name = tier.itemName();
            withExistingParent(name, mcLoc("item/generated"))
                    .texture("layer0", modLoc("item/" + name));
        }
    }
}
