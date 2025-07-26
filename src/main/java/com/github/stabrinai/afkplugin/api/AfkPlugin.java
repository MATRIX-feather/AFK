package com.github.stabrinai.afkplugin.api;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

public interface AfkPlugin {

    ScheduledTask getCheckScheduledTask();
}