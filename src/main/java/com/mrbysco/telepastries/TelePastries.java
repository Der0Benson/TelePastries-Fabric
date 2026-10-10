package com.mrbysco.telepastries;

import com.mojang.logging.LogUtils;
import com.mrbysco.telepastries.config.TeleConfig;
import com.mrbysco.telepastries.data.TelePastriesPlayerData;
import com.mrbysco.telepastries.init.TeleRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import org.slf4j.Logger;

public class TelePastries implements ModInitializer {
    public static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        TeleConfig.load();
        TeleRegistry.initialize();
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            TelePastriesPlayerData oldData = (TelePastriesPlayerData) oldPlayer;
            TelePastriesPlayerData newData = (TelePastriesPlayerData) newPlayer;
            newData.telepastries$setDimensionPositions(oldData.telepastries$getDimensionPositions());
        });
        LOGGER.info("Initialized the unofficial Fabric port of TelePastries");
    }
}
