package com.chinaex123.throwing_disc.data;

import com.chinaex123.throwing_disc.ThrowingDisc;
import com.chinaex123.throwing_disc.api.DiscMaterial;
import com.chinaex123.throwing_disc.api.DiscTier;
import com.chinaex123.throwing_disc.api.ThrowingDiscAPI;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        for (DiscTier tier : ThrowingDiscAPI.tiers()) {
            DiscMaterial ingredient = tier.recipeIngredient();
            String unlockedBy = "has_" + tier.getSerializedName();

            ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, tier.item().get())
                    .pattern(" A ")
                    .pattern("A A")
                    .pattern(" A ")
                    .define('A', ingredient.ingredient());

            if (ingredient.isTag()) {
                builder.unlockedBy(unlockedBy, has(ingredient.tag()));
            } else {
                builder.unlockedBy(unlockedBy, has(ingredient.item()));
            }

            builder.save(recipeOutput, ThrowingDisc.id(tier.itemName()));
        }
    }
}
