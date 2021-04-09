package com.github.charlemaznable.logback.miner.level;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;
import lombok.AllArgsConstructor;
import lombok.val;
import org.slf4j.Marker;

@AllArgsConstructor
public class EffectorTurboFilter extends TurboFilter {

    private EffectorContext effectorContext;

    @Override
    public FilterReply decide(Marker marker, Logger logger, Level level,
                              String format, Object[] params, Throwable t) {
        val effector = effectorContext.getEffector(logger.getName());
        if (effector.getConsoleEffectiveLevelInt() > level.levelInt &&
                effector.getDqlEffectiveLevelInt() > level.levelInt &&
                effector.getVertxEffectiveLevelInt() > level.levelInt &&
                effector.getFileEffectiveLevelInt() > level.levelInt) {
            return FilterReply.DENY;
        }
        return FilterReply.ACCEPT;
    }
}
