package net.shadowmage.ancientwarfarerecompiled;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

/**
 * Point d'entrée de AncientWarfareRecompiled.
 *
 * Port NeoForge 1.21.1 d'Ancient Warfare 2 (à l'origine Copyright (C) 2013-2015 Shadowmage),
 * distribué sous GPL-3.0-or-later.
 */
@Mod(AncientWarfareRecompiled.MODID)
public class AncientWarfareRecompiled {
    public static final String MODID = "ancientwarfarerecompiled";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AncientWarfareRecompiled(IEventBus modEventBus) {
        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("AncientWarfareRecompiled : phase 0 (squelette) chargée");
    }
}
